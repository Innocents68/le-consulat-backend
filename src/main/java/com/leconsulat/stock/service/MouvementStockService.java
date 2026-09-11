package com.leconsulat.stock.service;

import com.leconsulat.catalogue.entity.Produit;
import com.leconsulat.catalogue.repository.ProduitRepository;
import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.common.exception.UnauthorizedException;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.security.CustomUserDetails;
import com.leconsulat.security.PerimetreGuard;
import com.leconsulat.stock.dto.EntreeStockRequest;
import com.leconsulat.stock.dto.MouvementStockDto;
import com.leconsulat.stock.dto.SortieStockRequest;
import com.leconsulat.stock.dto.TransfertStockRequest;
import com.leconsulat.stock.entity.MouvementStock;
import com.leconsulat.stock.entity.TypeMouvementStock;
import com.leconsulat.stock.repository.MouvementStockRepository;
import com.leconsulat.utilisateur.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** §6.6.2 : toute variation de {@code Produit.quantiteStock} passe par ce service, qui en garde
 * la trace immuable dans {@code MouvementStock}. */
@Service
@Transactional
public class MouvementStockService {

    private final MouvementStockRepository repository;
    private final ProduitRepository produitRepository;
    private final EtablissementRepository etablissementRepository;
    private final JournalOperationService journal;
    private final PerimetreGuard perimetreGuard;

    public MouvementStockService(MouvementStockRepository repository, ProduitRepository produitRepository,
                                  EtablissementRepository etablissementRepository, JournalOperationService journal,
                                  PerimetreGuard perimetreGuard) {
        this.repository = repository;
        this.produitRepository = produitRepository;
        this.etablissementRepository = etablissementRepository;
        this.journal = journal;
        this.perimetreGuard = perimetreGuard;
    }

    public Page<MouvementStockDto> search(Long etablissementIdDemande, Long produitId, String type,
                                           LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable) {
        Etablissement cible = resoudreEtablissementCible(etablissementIdDemande);
        TypeMouvementStock t = type != null ? parseType(type) : null;
        return repository.search(cible, produitId, t, dateDebut, dateFin, pageable).map(MouvementStockDto::from);
    }

    @Transactional
    public MouvementStockDto entree(EntreeStockRequest req) {
        Produit produit = findProduitChecked(req.produitId());
        MouvementStock m = enregistrer(produit, TypeMouvementStock.ENTREE, req.quantite(), null, req.prixUnitaire());
        produit.setQuantiteStock(produit.getQuantiteStock().add(req.quantite()));
        produitRepository.save(produit);
        journal.enregistrer("STOCKS", "ENTREE", "Entrée de " + req.quantite() + " " + produit.getUnite() + " sur "
                + produit.getNom() + " (établissement " + produit.getEtablissement().getNom() + ")");
        return MouvementStockDto.from(m);
    }

    @Transactional
    public MouvementStockDto sortie(SortieStockRequest req) {
        Produit produit = findProduitChecked(req.produitId());
        if (req.quantite().compareTo(produit.getQuantiteStock()) > 0) {
            throw new BusinessRuleException("Stock insuffisant pour " + produit.getNom());
        }
        MouvementStock m = enregistrer(produit, TypeMouvementStock.SORTIE, req.quantite(), req.motif(), null);
        produit.setQuantiteStock(produit.getQuantiteStock().subtract(req.quantite()));
        produitRepository.save(produit);
        journal.enregistrer("STOCKS", "SORTIE", "Sortie de " + req.quantite() + " " + produit.getUnite() + " sur "
                + produit.getNom() + " (motif : " + req.motif() + ")");
        return MouvementStockDto.from(m);
    }

    /** RG-084 : réservé au Super Administrateur, seul à avoir la vision des deux périmètres. */
    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    @Transactional
    public void transfert(TransfertStockRequest req) {
        Produit source = produitRepository.findById(req.produitSourceId())
                .orElseThrow(() -> ResourceNotFoundException.of("Produit", req.produitSourceId()));
        Produit destination = produitRepository.findById(req.produitDestinationId())
                .orElseThrow(() -> ResourceNotFoundException.of("Produit", req.produitDestinationId()));
        if (source.getEtablissement().getId().equals(destination.getEtablissement().getId())) {
            throw new BusinessRuleException("Un transfert doit se faire entre deux établissements différents");
        }
        if (req.quantite().compareTo(source.getQuantiteStock()) > 0) {
            throw new BusinessRuleException("Stock insuffisant pour " + source.getNom());
        }
        String reference = UUID.randomUUID().toString();
        MouvementStock sortant = enregistrer(source, TypeMouvementStock.TRANSFERT_SORTANT, req.quantite(), null, null);
        sortant.setReferenceTransfert(reference);
        repository.save(sortant);
        MouvementStock entrant = enregistrer(destination, TypeMouvementStock.TRANSFERT_ENTRANT, req.quantite(), null, null);
        entrant.setReferenceTransfert(reference);
        repository.save(entrant);
        source.setQuantiteStock(source.getQuantiteStock().subtract(req.quantite()));
        destination.setQuantiteStock(destination.getQuantiteStock().add(req.quantite()));
        produitRepository.save(source);
        produitRepository.save(destination);
        journal.enregistrer("STOCKS", "TRANSFERT", "Transfert de " + req.quantite() + " " + source.getUnite() + " de "
                + source.getNom() + " (" + source.getEtablissement().getNom() + ") vers "
                + destination.getNom() + " (" + destination.getEtablissement().getNom() + ")");
    }

    /** RG-031 — appelé uniquement par {@code CommandeService.encaisser()}, dans la même
     * transaction : une rupture de stock ici annule tout l'encaissement. */
    public void enregistrerSortieVente(Produit produit, BigDecimal quantite, BigDecimal prixUnitaire) {
        if (quantite.compareTo(produit.getQuantiteStock()) > 0) {
            throw new BusinessRuleException("Stock insuffisant pour " + produit.getNom());
        }
        enregistrer(produit, TypeMouvementStock.SORTIE_VENTE, quantite, null, prixUnitaire);
        produit.setQuantiteStock(produit.getQuantiteStock().subtract(quantite));
        produitRepository.save(produit);
    }

    /** RG-062 — appelé uniquement par {@code AvoirService.create()} lorsque
     * {@code remiseEnStock == true}. */
    public void enregistrerRetourAvoir(Produit produit, BigDecimal quantite) {
        enregistrer(produit, TypeMouvementStock.RETOUR_AVOIR, quantite, null, null);
        produit.setQuantiteStock(produit.getQuantiteStock().add(quantite));
        produitRepository.save(produit);
    }

    /** RG-086 — appelé uniquement par {@code InventaireService.valider()}. Aligne directement
     * {@code Produit.quantiteStock} sur le stock physique compté (plus robuste qu'un simple
     * {@code += ecart} si un autre mouvement est survenu entre la clôture et la validation) et
     * trace l'écart signé dans le mouvement (voir Javadoc de {@code MouvementStock.quantite}). */
    public void enregistrerAjustementInventaire(Produit produit, BigDecimal ecart, BigDecimal nouveauStock, String motif) {
        MouvementStock m = new MouvementStock();
        m.setProduit(produit);
        m.setEtablissement(produit.getEtablissement());
        m.setType(TypeMouvementStock.AJUSTEMENT_INVENTAIRE);
        m.setQuantite(ecart);
        m.setMotif(motif);
        m.setAuteur(currentUtilisateur());
        repository.save(m);
        produit.setQuantiteStock(nouveauStock);
        produitRepository.save(produit);
    }

    private MouvementStock enregistrer(Produit produit, TypeMouvementStock type, BigDecimal quantite, String motif, BigDecimal prixUnitaire) {
        MouvementStock m = new MouvementStock();
        m.setProduit(produit);
        m.setEtablissement(produit.getEtablissement());
        m.setType(type);
        m.setQuantite(quantite);
        m.setMotif(motif);
        m.setPrixUnitaire(prixUnitaire);
        m.setMontant(prixUnitaire != null ? prixUnitaire.multiply(quantite) : null);
        m.setAuteur(currentUtilisateur());
        return repository.save(m);
    }

    private Produit findProduitChecked(Long id) {
        Produit p = produitRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Produit", id));
        if (!perimetreGuard.aAcces(p.getEtablissement())) {
            throw ResourceNotFoundException.of("Produit", id);
        }
        return p;
    }

    private TypeMouvementStock parseType(String type) {
        try {
            return TypeMouvementStock.valueOf(type.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Type de mouvement inconnu : " + type);
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

    private Utilisateur currentUtilisateur() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUtilisateur();
        }
        throw new UnauthorizedException("Utilisateur non authentifié");
    }
}
