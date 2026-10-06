package com.leconsulat.catalogue.service;

import com.leconsulat.catalogue.dto.*;
import com.leconsulat.catalogue.entity.Categorie;
import com.leconsulat.catalogue.entity.HistoriquePrix;
import com.leconsulat.catalogue.entity.Produit;
import com.leconsulat.catalogue.entity.UniteProduit;
import com.leconsulat.catalogue.repository.CategorieRepository;
import com.leconsulat.catalogue.repository.HistoriquePrixRepository;
import com.leconsulat.catalogue.repository.ProduitRepository;
import com.leconsulat.common.exception.BadRequestException;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.avoir.repository.AvoirRepository;
import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.parametres.service.ParametresService;
import com.leconsulat.reporting.repository.LigneCommandeRepository;
import com.leconsulat.security.CustomUserDetails;
import com.leconsulat.security.PerimetreGuard;
import com.leconsulat.stock.entity.Fournisseur;
import com.leconsulat.stock.repository.FournisseurRepository;
import com.leconsulat.stock.repository.MouvementStockRepository;
import com.leconsulat.utilisateur.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProduitService {

    private final ProduitRepository repository;
    private final CategorieRepository categorieRepository;
    private final EtablissementRepository etablissementRepository;
    private final HistoriquePrixRepository historiquePrixRepository;
    private final FournisseurRepository fournisseurRepository;
    private final PerimetreGuard perimetreGuard;
    private final ParametresService parametresService;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final AvoirRepository avoirRepository;
    private final JournalOperationService journal;

    public ProduitService(ProduitRepository repository, CategorieRepository categorieRepository,
                           EtablissementRepository etablissementRepository, HistoriquePrixRepository historiquePrixRepository,
                           FournisseurRepository fournisseurRepository, PerimetreGuard perimetreGuard,
                           ParametresService parametresService, LigneCommandeRepository ligneCommandeRepository,
                           MouvementStockRepository mouvementStockRepository, AvoirRepository avoirRepository,
                           JournalOperationService journal) {
        this.repository = repository;
        this.categorieRepository = categorieRepository;
        this.etablissementRepository = etablissementRepository;
        this.historiquePrixRepository = historiquePrixRepository;
        this.fournisseurRepository = fournisseurRepository;
        this.perimetreGuard = perimetreGuard;
        this.parametresService = parametresService;
        this.ligneCommandeRepository = ligneCommandeRepository;
        this.mouvementStockRepository = mouvementStockRepository;
        this.avoirRepository = avoirRepository;
        this.journal = journal;
    }

    public Page<ProduitDto> search(Long etablissementIdDemande, Long categorieId, Boolean actif, Boolean suiviStock, String search, Pageable pageable) {
        Etablissement demande = etablissementIdDemande != null
                ? etablissementRepository.findById(etablissementIdDemande).orElse(null)
                : null;
        Etablissement cible = perimetreGuard.scopeEtablissement(demande);
        if (cible == null) {
            throw new BusinessRuleException("Précisez un établissement");
        }
        return repository.search(cible, categorieId, actif, suiviStock, search, pageable).map(ProduitDto::from);
    }

    public ProduitDto get(Long id) {
        return ProduitDto.from(findEntityChecked(id));
    }

    /** Consu_corrige.docx §7 : alerte visible (Tableau de bord) pour tout produit suivi passé
     * sous son seuil — vue consolidée des 3 établissements pour le Super Administrateur qui ne
     * filtre pas, comme le reste du tableau de bord. */
    public java.util.List<ProduitDto> alertesStock(Long etablissementIdDemande) {
        if (etablissementIdDemande == null && perimetreGuard.isSuperAdmin()) {
            return etablissementRepository.findAll().stream()
                    .flatMap(e -> repository.findEnAlerteStock(e).stream())
                    .map(ProduitDto::from)
                    .toList();
        }
        Etablissement demande = etablissementIdDemande != null
                ? etablissementRepository.findById(etablissementIdDemande).orElse(null)
                : null;
        Etablissement cible = perimetreGuard.scopeEtablissement(demande);
        if (cible == null) {
            throw new BusinessRuleException("Précisez un établissement");
        }
        return repository.findEnAlerteStock(cible).stream().map(ProduitDto::from).toList();
    }

    /** Point d'entrée unique pour "Scanner" (Nouvelle commande, Mouvements de stock) : essaie
     * d'abord le code-barres (scopé à l'établissement actif — le même code réel peut exister dans
     * deux établissements, RG-002), puis retombe sur un QR interne (id encodé, périmètre
     * indépendant puisque c'est déjà une clé primaire). */
    public ProduitDto resoudreParCodeScanne(String codeScanne, Long etablissementIdDemande) {
        String texte = codeScanne == null ? "" : codeScanne.trim();
        if (etablissementIdDemande != null) {
            var parCodeBarre = repository.findByCodeBarreAndEtablissementId(texte, etablissementIdDemande);
            if (parCodeBarre.isPresent()) {
                return get(parCodeBarre.get().getId());
            }
        }
        Long id = com.leconsulat.catalogue.util.QrCodeGenerator.extraireIdProduit(texte);
        if (id == null) {
            throw new BusinessRuleException("Code non reconnu");
        }
        return get(id);
    }

    /** Toujours en Code128 : couvre à la fois un code interne généré (alphanumérique, "P123") et
     * un EAN-13 fabricant recopié (Code128 encode aussi bien des chiffres). Rattrapage pour tout
     * produit créé avant l'ajout de cette fonctionnalité : {@code create()} ne génère le code que
     * pour les nouveaux produits, donc le catalogue existant avait {@code codeBarre == null}
     * jusqu'ici — on le complète ici à la demande plutôt que de planter. */
    @Transactional
    public byte[] genererCodeBarrePng(Long id) {
        Produit p = findEntityChecked(id);
        if (p.getCodeBarre() == null || p.getCodeBarre().isBlank()) {
            p.setCodeBarre("P" + p.getId());
            p = repository.save(p);
        }
        return com.leconsulat.catalogue.util.BarcodeGenerator.genererPng(p.getCodeBarre(), 300, 120);
    }

    public java.util.List<HistoriquePrixDto> historiquePrix(Long id) {
        findEntityChecked(id);
        return historiquePrixRepository.findByProduitIdOrderByDateEffetDesc(id).stream().map(HistoriquePrixDto::from).toList();
    }

    @Transactional
    public ProduitDto create(CreateProduitRequest req) {
        Etablissement cible = resoudreEtablissementCible(req.etablissementId());
        if (repository.existsByEtablissementAndNomIgnoreCase(cible, req.nom())) {
            throw new BadRequestException("Ce produit existe déjà pour cet établissement");
        }
        Produit p = new Produit();
        p.setEtablissement(cible);
        // §6.10.1 : seuil d'alerte par défaut appliqué aux nouveaux articles suivis en stock
        // quand aucun seuil n'est précisé explicitement à la création.
        java.math.BigDecimal seuilAlerte = req.seuilAlerte();
        if (seuilAlerte == null && Boolean.TRUE.equals(req.suiviStock())) {
            seuilAlerte = parametresService.getEntity().getSeuilAlerteDefaut();
        }
        apply(p, req.nom(), req.categorieId(), req.description(), req.unite(), req.prixVente(), req.prixAchat(),
                req.disponible(), req.suiviStock(), seuilAlerte, req.emplacement(), req.fournisseurId(), req.codeBarre());
        p.setActif(true);
        Produit saved = repository.save(p);
        // Un produit acheté a déjà son code (recopié ci-dessus) ; un produit sans code d'origine
        // (ex. plat maison) en reçoit un interne, dérivé de l'id — donc seulement connu après
        // l'enregistrement initial, d'où ce second save.
        if (saved.getCodeBarre() == null || saved.getCodeBarre().isBlank()) {
            saved.setCodeBarre("P" + saved.getId());
            saved = repository.save(saved);
        }
        return ProduitDto.from(saved);
    }

    /** Demandes_amelioration_logiciel_Le_Consulat_Professionnel.docx §3 : la modification d'un
     * produit (dont son prix) est réservée au Super Administrateur — un Gérant/Caissier ne peut
     * qu'augmenter le stock d'un produit existant, via {@code MouvementStockService} (RG-062),
     * jamais via cet endpoint. Une tentative bloquée est tracée automatiquement par
     * {@code GlobalExceptionHandler} (RG-012). */
    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    @Transactional
    public ProduitDto update(Long id, UpdateProduitRequest req) {
        Produit p = findEntityChecked(id);
        var ancienPrix = p.getPrixVente();
        apply(p, req.nom(), req.categorieId(), req.description(), req.unite(), req.prixVente(), req.prixAchat(),
                req.disponible(), req.suiviStock(), req.seuilAlerte(), req.emplacement(), req.fournisseurId(), req.codeBarre());
        Produit saved = repository.save(p);
        if (ancienPrix.compareTo(req.prixVente()) != 0) {
            historiquePrixRepository.save(new HistoriquePrix(saved, ancienPrix, req.prixVente(), currentUtilisateurOuNull()));
        }
        return ProduitDto.from(saved);
    }

    /** Demandes_amelioration_logiciel_Le_Consulat_Professionnel.docx §3 : retirer/réactiver un
     * produit du catalogue est une information sensible au même titre que le prix. */
    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    @Transactional
    public void toggleActif(Long id) {
        Produit p = findEntityChecked(id);
        p.setActif(!p.isActif());
        repository.save(p);
    }

    /** Consu_corrige.docx §3/§6 : suppression réservée au Super Administrateur, et uniquement si
     * le produit n'a jamais été vendu, mouvementé, crédité ou reprix — sinon une FK (lignes de
     * commande, mouvements de stock, avoirs, historique des prix) empêcherait la suppression et
     * effacerait un historique qu'on ne veut jamais perdre. Dans ce cas, désactiver reste le seul
     * moyen de le retirer du catalogue actif. */
    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    @Transactional
    public void supprimer(Long id) {
        Produit p = findEntityChecked(id);
        if (ligneCommandeRepository.existsByProduitId(id) || mouvementStockRepository.existsByProduitId(id)
                || historiquePrixRepository.existsByProduitId(id) || avoirRepository.existsByLigneProduitId(id)) {
            throw new BusinessRuleException("Ce produit a déjà été utilisé (vente, mouvement de stock, avoir ou changement de prix) "
                    + "et ne peut pas être supprimé définitivement — désactivez-le à la place");
        }
        String nom = p.getNom();
        repository.delete(p);
        journal.enregistrer("PRODUITS", "SUPPRESSION", "Produit " + nom + " supprimé");
    }

    private void apply(Produit p, String nom, Long categorieId, String description, String unite,
                        java.math.BigDecimal prixVente, java.math.BigDecimal prixAchat, Boolean disponible, Boolean suiviStock,
                        java.math.BigDecimal seuilAlerte, String emplacement, Long fournisseurId, String codeBarre) {
        Categorie categorie = categorieRepository.findById(categorieId)
                .orElseThrow(() -> ResourceNotFoundException.of("Categorie", categorieId));
        if (!categorie.getEtablissement().getId().equals(p.getEtablissement().getId())) {
            throw new BusinessRuleException("Cette catégorie n'appartient pas à cet établissement");
        }
        p.setNom(nom);
        p.setCategorie(categorie);
        p.setDescription(description);
        p.setUnite(parseUnite(unite));
        p.setPrixVente(prixVente);
        p.setPrixAchat(prixAchat);
        // Un code vide reste vide pour l'instant (rempli automatiquement après la création,
        // voir create()) ; un code fourni (scanné ou tapé) doit être unique dans l'établissement.
        if (codeBarre != null && !codeBarre.isBlank()) {
            String normalise = codeBarre.trim();
            if (!normalise.equalsIgnoreCase(p.getCodeBarre()) && repository.existsByEtablissementAndCodeBarre(p.getEtablissement(), normalise)) {
                throw new BusinessRuleException("Ce code-barres est déjà utilisé par un autre produit de cet établissement");
            }
            p.setCodeBarre(normalise);
        }
        if (disponible != null) {
            p.setDisponible(disponible);
        }
        if (suiviStock != null) {
            p.setSuiviStock(suiviStock);
        }
        p.setSeuilAlerte(seuilAlerte);
        p.setEmplacement(emplacement);
        if (fournisseurId != null) {
            Fournisseur fournisseur = fournisseurRepository.findById(fournisseurId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Fournisseur", fournisseurId));
            p.setFournisseur(fournisseur);
        } else {
            p.setFournisseur(null);
        }
    }

    private UniteProduit parseUnite(String unite) {
        try {
            return UniteProduit.valueOf(unite.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Unité inconnue : " + unite);
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

    private Produit findEntityChecked(Long id) {
        Produit p = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Produit", id));
        if (!perimetreGuard.aAcces(p.getEtablissement())) {
            throw ResourceNotFoundException.of("Produit", id);
        }
        return p;
    }

    private Utilisateur currentUtilisateurOuNull() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUtilisateur();
        }
        return null;
    }
}
