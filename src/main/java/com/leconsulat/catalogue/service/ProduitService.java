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
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.parametres.service.ParametresService;
import com.leconsulat.security.CustomUserDetails;
import com.leconsulat.security.PerimetreGuard;
import com.leconsulat.stock.entity.Fournisseur;
import com.leconsulat.stock.repository.FournisseurRepository;
import com.leconsulat.utilisateur.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public ProduitService(ProduitRepository repository, CategorieRepository categorieRepository,
                           EtablissementRepository etablissementRepository, HistoriquePrixRepository historiquePrixRepository,
                           FournisseurRepository fournisseurRepository, PerimetreGuard perimetreGuard,
                           ParametresService parametresService) {
        this.repository = repository;
        this.categorieRepository = categorieRepository;
        this.etablissementRepository = etablissementRepository;
        this.historiquePrixRepository = historiquePrixRepository;
        this.fournisseurRepository = fournisseurRepository;
        this.perimetreGuard = perimetreGuard;
        this.parametresService = parametresService;
    }

    public Page<ProduitDto> search(Long etablissementIdDemande, Long categorieId, Boolean actif, String search, Pageable pageable) {
        Etablissement demande = etablissementIdDemande != null
                ? etablissementRepository.findById(etablissementIdDemande).orElse(null)
                : null;
        Etablissement cible = perimetreGuard.scopeEtablissement(demande);
        if (cible == null) {
            throw new BusinessRuleException("Précisez un établissement");
        }
        return repository.search(cible, categorieId, actif, search, pageable).map(ProduitDto::from);
    }

    public ProduitDto get(Long id) {
        return ProduitDto.from(findEntityChecked(id));
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
                req.disponible(), req.suiviStock(), seuilAlerte, req.emplacement(), req.fournisseurId());
        p.setActif(true);
        return ProduitDto.from(repository.save(p));
    }

    @Transactional
    public ProduitDto update(Long id, UpdateProduitRequest req) {
        Produit p = findEntityChecked(id);
        var ancienPrix = p.getPrixVente();
        apply(p, req.nom(), req.categorieId(), req.description(), req.unite(), req.prixVente(), req.prixAchat(),
                req.disponible(), req.suiviStock(), req.seuilAlerte(), req.emplacement(), req.fournisseurId());
        Produit saved = repository.save(p);
        if (ancienPrix.compareTo(req.prixVente()) != 0) {
            historiquePrixRepository.save(new HistoriquePrix(saved, ancienPrix, req.prixVente(), currentUtilisateurOuNull()));
        }
        return ProduitDto.from(saved);
    }

    @Transactional
    public void toggleActif(Long id) {
        Produit p = findEntityChecked(id);
        p.setActif(!p.isActif());
        repository.save(p);
    }

    private void apply(Produit p, String nom, Long categorieId, String description, String unite,
                        java.math.BigDecimal prixVente, java.math.BigDecimal prixAchat, Boolean disponible, Boolean suiviStock,
                        java.math.BigDecimal seuilAlerte, String emplacement, Long fournisseurId) {
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
