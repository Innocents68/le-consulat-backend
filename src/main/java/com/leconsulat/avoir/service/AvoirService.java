package com.leconsulat.avoir.service;

import com.leconsulat.avoir.dto.AvoirDto;
import com.leconsulat.avoir.dto.CreateAvoirRequest;
import com.leconsulat.avoir.dto.LigneAvoirInput;
import com.leconsulat.avoir.entity.Avoir;
import com.leconsulat.avoir.entity.LigneAvoir;
import com.leconsulat.avoir.entity.ModeRemboursement;
import com.leconsulat.avoir.entity.MotifAvoir;
import com.leconsulat.avoir.repository.AvoirRepository;
import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.common.exception.UnauthorizedException;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.numerotation.service.NumerotationService;
import com.leconsulat.parametres.service.ParametresService;
import com.leconsulat.security.CustomUserDetails;
import com.leconsulat.security.PerimetreGuard;
import com.leconsulat.stock.service.MouvementStockService;
import com.leconsulat.utilisateur.entity.Utilisateur;
import com.leconsulat.vente.entity.Commande;
import com.leconsulat.vente.entity.Facture;
import com.leconsulat.vente.entity.LigneCommande;
import com.leconsulat.vente.repository.FactureRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/** Seul moyen de corriger une commande déjà validée (§6.2.7, RG-030). */
@Service
@Transactional
public class AvoirService {

    private final AvoirRepository repository;
    private final FactureRepository factureRepository;
    private final EtablissementRepository etablissementRepository;
    private final NumerotationService numerotationService;
    private final JournalOperationService journal;
    private final PerimetreGuard perimetreGuard;
    private final MouvementStockService mouvementStockService;
    private final ParametresService parametresService;

    public AvoirService(AvoirRepository repository, FactureRepository factureRepository,
                         EtablissementRepository etablissementRepository, NumerotationService numerotationService,
                         JournalOperationService journal, PerimetreGuard perimetreGuard,
                         MouvementStockService mouvementStockService, ParametresService parametresService) {
        this.repository = repository;
        this.factureRepository = factureRepository;
        this.etablissementRepository = etablissementRepository;
        this.numerotationService = numerotationService;
        this.journal = journal;
        this.perimetreGuard = perimetreGuard;
        this.mouvementStockService = mouvementStockService;
        this.parametresService = parametresService;
    }

    public Page<AvoirDto> search(Long etablissementIdDemande, Pageable pageable) {
        Etablissement demande = etablissementIdDemande != null
                ? etablissementRepository.findById(etablissementIdDemande).orElse(null)
                : null;
        Etablissement cible = perimetreGuard.scopeEtablissement(demande);
        if (cible == null) {
            throw new BusinessRuleException("Précisez un établissement");
        }
        return repository.findByEtablissementOrderByDateCreationDesc(cible, pageable).map(AvoirDto::from);
    }

    @Transactional
    public AvoirDto create(Long factureId, CreateAvoirRequest req) {
        Facture facture = findFactureChecked(factureId);
        Commande commande = facture.getCommande();

        MotifAvoir motif = parseMotif(req.motif());
        ModeRemboursement mode = parseModeRemboursement(req.modeRemboursement());

        Avoir avoir = new Avoir();
        avoir.setEtablissement(facture.getEtablissement());
        avoir.setFacture(facture);
        avoir.setMotif(motif);
        avoir.setMotifDetail(req.motifDetail());
        avoir.setRemiseEnStock(req.remiseEnStock());
        avoir.setModeRemboursement(mode);
        avoir.setAuteur(currentUtilisateur());
        avoir.setNumero(numerotationService.genererNumero(facture.getEtablissement(), "AVO"));

        BigDecimal montantTotal = BigDecimal.ZERO;
        for (LigneAvoirInput input : req.lignes()) {
            LigneCommande ligneOrigine = commande.getLignes().stream()
                    .filter(l -> l.getId().equals(input.ligneCommandeId()))
                    .findFirst()
                    .orElseThrow(() -> ResourceNotFoundException.of("LigneCommande", input.ligneCommandeId()));
            if (input.quantite() > ligneOrigine.getQuantite()) {
                throw new BusinessRuleException("La quantité à annuler dépasse la quantité commandée pour "
                        + ligneOrigine.getArticleNom());
            }
            LigneAvoir ligneAvoir = new LigneAvoir();
            ligneAvoir.setAvoir(avoir);
            ligneAvoir.setProduit(ligneOrigine.getProduit());
            ligneAvoir.setArticleNom(ligneOrigine.getArticleNom());
            ligneAvoir.setQuantite(input.quantite());
            BigDecimal montantLigne = ligneOrigine.getPrixUnitaire().multiply(BigDecimal.valueOf(input.quantite()));
            ligneAvoir.setMontant(montantLigne);
            avoir.getLignes().add(ligneAvoir);
            montantTotal = montantTotal.add(montantLigne);

            // RG-062 : réintégration en stock si demandée, uniquement pour un produit suivi.
            if (req.remiseEnStock() && ligneOrigine.getProduit().isSuiviStock()) {
                mouvementStockService.enregistrerRetourAvoir(ligneOrigine.getProduit(), BigDecimal.valueOf(input.quantite()));
            }
        }
        avoir.setMontant(montantTotal);

        // RG-061 : le cumul des avoirs d'une facture ne peut jamais dépasser son montant net.
        BigDecimal dejaEmis = repository.sommeAvoirsExistants(factureId);
        if (dejaEmis.add(montantTotal).compareTo(facture.getMontantNet()) > 0) {
            throw new BusinessRuleException("Le cumul des avoirs dépasserait le montant net de la facture");
        }

        Avoir saved = repository.save(avoir);
        journal.enregistrer("AVOIRS", "CREATION", "Avoir " + saved.getNumero() + " sur la facture "
                + facture.getNumero() + " (" + montantTotal + " FCFA, motif : " + motif + ")");
        return AvoirDto.from(saved);
    }

    public AvoirDto get(Long id) {
        return AvoirDto.from(findEntityChecked(id));
    }

    /** Comme pour le ticket de facture (Lot 2b) : la génération du PDF doit rester dans la
     * transaction, {@code Avoir.lignes} est chargée en LAZY. */
    @Transactional
    public byte[] genererPdf(Long id) {
        Avoir avoir = findEntityChecked(id);
        return com.leconsulat.avoir.util.AvoirGenerator.genererPdf(avoir, parametresService.getEntity());
    }

    public java.util.List<AvoirDto> parFacture(Long factureId) {
        findFactureChecked(factureId);
        return repository.findByFactureId(factureId).stream().map(AvoirDto::from).toList();
    }

    private MotifAvoir parseMotif(String motif) {
        try {
            return MotifAvoir.valueOf(motif.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Motif d'avoir inconnu : " + motif);
        }
    }

    private ModeRemboursement parseModeRemboursement(String mode) {
        try {
            return ModeRemboursement.valueOf(mode.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Mode de remboursement inconnu : " + mode);
        }
    }

    private Facture findFactureChecked(Long id) {
        Facture f = factureRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Facture", id));
        if (!perimetreGuard.aAcces(f.getEtablissement())) {
            throw ResourceNotFoundException.of("Facture", id);
        }
        return f;
    }

    private Avoir findEntityChecked(Long id) {
        Avoir a = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Avoir", id));
        if (!perimetreGuard.aAcces(a.getEtablissement())) {
            throw ResourceNotFoundException.of("Avoir", id);
        }
        return a;
    }

    private Utilisateur currentUtilisateur() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUtilisateur();
        }
        throw new UnauthorizedException("Utilisateur non authentifié");
    }
}
