package com.leconsulat.stock.service;

import com.leconsulat.catalogue.repository.ProduitRepository;
import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BadRequestException;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.stock.dto.CreateFournisseurRequest;
import com.leconsulat.stock.dto.FournisseurDto;
import com.leconsulat.stock.entity.Fournisseur;
import com.leconsulat.stock.repository.FournisseurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Référentiel partagé (§6.6.4) — pas de cloisonnement établissement, ouvert aux deux profils. */
@Service
@Transactional
public class FournisseurService {

    private final FournisseurRepository repository;
    private final ProduitRepository produitRepository;
    private final JournalOperationService journal;

    public FournisseurService(FournisseurRepository repository, ProduitRepository produitRepository, JournalOperationService journal) {
        this.repository = repository;
        this.produitRepository = produitRepository;
        this.journal = journal;
    }

    public List<FournisseurDto> list(Boolean actif) {
        List<Fournisseur> fournisseurs = actif == null || actif ? repository.findByActifTrue() : repository.findAll();
        return fournisseurs.stream().map(FournisseurDto::from).toList();
    }

    @Transactional
    public FournisseurDto create(CreateFournisseurRequest req) {
        if (repository.existsByNomIgnoreCase(req.nom())) {
            throw new BadRequestException("Ce fournisseur existe déjà");
        }
        Fournisseur f = new Fournisseur();
        f.setNom(req.nom());
        f.setTelephone(req.telephone());
        f.setActif(true);
        return FournisseurDto.from(repository.save(f));
    }

    @Transactional
    public void toggleActif(Long id) {
        Fournisseur f = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Fournisseur", id));
        f.setActif(!f.isActif());
        repository.save(f);
    }

    /** Demandes_amelioration_logiciel_Le_Consulat_Professionnel.docx §1. */
    @Transactional
    public FournisseurDto update(Long id, CreateFournisseurRequest req) {
        Fournisseur f = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Fournisseur", id));
        if (!req.nom().equalsIgnoreCase(f.getNom()) && repository.existsByNomIgnoreCase(req.nom())) {
            throw new BadRequestException("Ce fournisseur existe déjà");
        }
        f.setNom(req.nom());
        f.setTelephone(req.telephone());
        return FournisseurDto.from(repository.save(f));
    }

    /** Référentiel partagé entre établissements (RG-002) : un fournisseur déjà lié à un produit ne
     * peut pas être supprimé sans casser cette référence — on refuse plutôt que de désactiver le
     * produit à sa place, contrairement à {@code ProduitService.supprimer}, moins intrusif. */
    @Transactional
    public void supprimer(Long id) {
        Fournisseur f = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Fournisseur", id));
        if (produitRepository.existsByFournisseurId(id)) {
            throw new BusinessRuleException("Ce fournisseur est associé à au moins un produit et ne peut pas être supprimé — désactivez-le à la place");
        }
        String nom = f.getNom();
        repository.delete(f);
        journal.enregistrer("FOURNISSEURS", "SUPPRESSION", "Fournisseur " + nom + " supprimé");
    }
}
