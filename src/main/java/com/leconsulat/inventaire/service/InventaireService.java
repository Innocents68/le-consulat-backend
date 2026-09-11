package com.leconsulat.inventaire.service;

import com.leconsulat.catalogue.entity.Produit;
import com.leconsulat.catalogue.repository.ProduitRepository;
import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.common.exception.UnauthorizedException;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.inventaire.dto.CloturerInventaireRequest;
import com.leconsulat.inventaire.dto.CreateInventaireRequest;
import com.leconsulat.inventaire.dto.InventaireDto;
import com.leconsulat.inventaire.dto.StockPhysiqueRequest;
import com.leconsulat.inventaire.entity.Inventaire;
import com.leconsulat.inventaire.entity.LigneInventaire;
import com.leconsulat.inventaire.entity.StatutInventaire;
import com.leconsulat.inventaire.repository.InventaireRepository;
import com.leconsulat.numerotation.service.NumerotationService;
import com.leconsulat.security.CustomUserDetails;
import com.leconsulat.security.PerimetreGuard;
import com.leconsulat.stock.service.MouvementStockService;
import com.leconsulat.utilisateur.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** §6.6.5 : compare le stock théorique (issu des mouvements) au stock physique compté, calcule
 * les écarts puis génère les ajustements. */
@Service
@Transactional
public class InventaireService {

    private final InventaireRepository repository;
    private final ProduitRepository produitRepository;
    private final EtablissementRepository etablissementRepository;
    private final NumerotationService numerotationService;
    private final JournalOperationService journal;
    private final PerimetreGuard perimetreGuard;
    private final MouvementStockService mouvementStockService;

    public InventaireService(InventaireRepository repository, ProduitRepository produitRepository,
                              EtablissementRepository etablissementRepository, NumerotationService numerotationService,
                              JournalOperationService journal, PerimetreGuard perimetreGuard,
                              MouvementStockService mouvementStockService) {
        this.repository = repository;
        this.produitRepository = produitRepository;
        this.etablissementRepository = etablissementRepository;
        this.numerotationService = numerotationService;
        this.journal = journal;
        this.perimetreGuard = perimetreGuard;
        this.mouvementStockService = mouvementStockService;
    }

    public Page<InventaireDto> search(Long etablissementIdDemande, String statut, LocalDate dateDebut, LocalDate dateFin, Pageable pageable) {
        Etablissement cible = resoudreEtablissementCible(etablissementIdDemande);
        StatutInventaire s = statut != null ? parseStatut(statut) : null;
        return repository.search(cible, s, dateDebut, dateFin, pageable).map(InventaireDto::summary);
    }

    public InventaireDto get(Long id) {
        return InventaireDto.from(findEntityChecked(id));
    }

    @Transactional
    public InventaireDto create(CreateInventaireRequest req) {
        Etablissement cible = resoudreEtablissementCible(req.etablissementId());
        var produits = produitRepository.findByEtablissementAndActifTrueAndSuiviStockTrue(cible);
        if (produits.isEmpty()) {
            throw new BusinessRuleException("Aucun produit suivi en stock pour cet établissement");
        }

        Inventaire inventaire = new Inventaire();
        inventaire.setNumero(numerotationService.genererNumero(cible, "INV"));
        inventaire.setEtablissement(cible);
        inventaire.setDateInventaire(req.dateInventaire());
        inventaire.setCommentaire(req.commentaire());
        inventaire.setAuteur(currentUtilisateur());
        inventaire.setStatut(StatutInventaire.BROUILLON);

        for (Produit p : produits) {
            LigneInventaire ligne = new LigneInventaire();
            ligne.setInventaire(inventaire);
            ligne.setProduit(p);
            ligne.setProduitNom(p.getNom());
            ligne.setStockTheorique(p.getQuantiteStock());
            inventaire.getLignes().add(ligne);
        }

        Inventaire saved = repository.save(inventaire);
        journal.enregistrer("STOCKS", "INVENTAIRE_CREATION", "Inventaire " + saved.getNumero() + " créé pour "
                + cible.getNom() + " (" + produits.size() + " produits)");
        return InventaireDto.from(saved);
    }

    @Transactional
    public InventaireDto demarrerComptage(Long id) {
        Inventaire inventaire = findEntityChecked(id);
        if (inventaire.getStatut() != StatutInventaire.BROUILLON) {
            throw new BusinessRuleException("Cet inventaire n'est pas en brouillon");
        }
        inventaire.setStatut(StatutInventaire.EN_COMPTAGE);
        return InventaireDto.from(repository.save(inventaire));
    }

    @Transactional
    public InventaireDto saisirComptage(Long id, Long ligneId, StockPhysiqueRequest req) {
        Inventaire inventaire = findEntityChecked(id);
        if (inventaire.getStatut() != StatutInventaire.EN_COMPTAGE) {
            throw new BusinessRuleException("Le comptage n'est pas en cours sur cet inventaire");
        }
        LigneInventaire ligne = inventaire.getLignes().stream()
                .filter(l -> l.getId().equals(ligneId))
                .findFirst()
                .orElseThrow(() -> ResourceNotFoundException.of("LigneInventaire", ligneId));
        ligne.setStockPhysique(req.stockPhysique());
        return InventaireDto.from(repository.save(inventaire));
    }

    @Transactional
    public InventaireDto cloturer(Long id, CloturerInventaireRequest req) {
        Inventaire inventaire = findEntityChecked(id);
        if (inventaire.getStatut() != StatutInventaire.EN_COMPTAGE) {
            throw new BusinessRuleException("Le comptage n'est pas en cours sur cet inventaire");
        }
        boolean incomplet = inventaire.getLignes().stream().anyMatch(l -> l.getStockPhysique() == null);
        if (incomplet) {
            throw new BusinessRuleException("Toutes les lignes doivent être comptées avant de clôturer l'inventaire");
        }
        for (LigneInventaire ligne : inventaire.getLignes()) {
            BigDecimal ecart = ligne.getStockPhysique().subtract(ligne.getStockTheorique());
            ligne.setEcartQuantite(ecart);
            BigDecimal prixAchat = ligne.getProduit().getPrixAchat();
            ligne.setEcartValeur(prixAchat != null ? ecart.multiply(prixAchat) : null);
        }
        inventaire.setStatut(StatutInventaire.CLOTURE);
        inventaire.setDateCloture(LocalDateTime.now());
        if (req.commentaire() != null) {
            inventaire.setCommentaire(req.commentaire());
        }
        Inventaire saved = repository.save(inventaire);
        journal.enregistrer("STOCKS", "INVENTAIRE_CLOTURE", "Inventaire " + saved.getNumero() + " clôturé, écarts calculés");
        return InventaireDto.from(saved);
    }

    /** RG-086 : génère automatiquement les mouvements d'ajustement et fige les lignes. */
    @Transactional
    public InventaireDto valider(Long id) {
        Inventaire inventaire = findEntityChecked(id);
        if (inventaire.getStatut() != StatutInventaire.CLOTURE) {
            throw new BusinessRuleException("Cet inventaire n'est pas clôturé");
        }
        for (LigneInventaire ligne : inventaire.getLignes()) {
            if (ligne.getEcartQuantite().compareTo(BigDecimal.ZERO) != 0) {
                mouvementStockService.enregistrerAjustementInventaire(ligne.getProduit(), ligne.getEcartQuantite(),
                        ligne.getStockPhysique(), "Ajustement inventaire " + inventaire.getNumero());
            }
        }
        inventaire.setStatut(StatutInventaire.VALIDE);
        inventaire.setValidateur(currentUtilisateur());
        inventaire.setDateValidation(LocalDateTime.now());
        Inventaire saved = repository.save(inventaire);
        journal.enregistrer("STOCKS", "INVENTAIRE_VALIDATION", "Inventaire " + saved.getNumero() + " validé, ajustements générés");
        return InventaireDto.from(saved);
    }

    private StatutInventaire parseStatut(String statut) {
        try {
            return StatutInventaire.valueOf(statut.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Statut d'inventaire inconnu : " + statut);
        }
    }

    private Etablissement resoudreEtablissementCible(Long etablissementIdDemande) {
        Etablissement demande = etablissementIdDemande != null
                ? etablissementRepository.findById(etablissementIdDemande)
                        .orElseThrow(() -> ResourceNotFoundException.of("Etablissement", etablissementIdDemande))
                : null;
        Etablissement cible = perimetreGuard.scopeEtablissement(demande);
        if (cible == null) {
            throw new BusinessRuleException("L'établissement est obligatoire");
        }
        return cible;
    }

    private Inventaire findEntityChecked(Long id) {
        Inventaire i = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Inventaire", id));
        if (!perimetreGuard.aAcces(i.getEtablissement())) {
            throw ResourceNotFoundException.of("Inventaire", id);
        }
        return i;
    }

    private Utilisateur currentUtilisateur() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUtilisateur();
        }
        throw new UnauthorizedException("Utilisateur non authentifié");
    }
}
