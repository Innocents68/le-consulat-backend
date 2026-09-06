package com.leconsulat.stock.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BadRequestException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.stock.dto.ProduitDto;
import com.leconsulat.stock.dto.ProduitRequest;
import com.leconsulat.stock.entity.CategorieProduit;
import com.leconsulat.stock.entity.Depot;
import com.leconsulat.stock.entity.Produit;
import com.leconsulat.stock.repository.CategorieProduitRepository;
import com.leconsulat.stock.repository.DepotRepository;
import com.leconsulat.stock.repository.ProduitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProduitService {

    private final ProduitRepository repository;
    private final CategorieProduitRepository categorieRepository;
    private final DepotRepository depotRepository;
    private final JournalOperationService journal;

    public ProduitService(ProduitRepository repository, CategorieProduitRepository categorieRepository,
                           DepotRepository depotRepository, JournalOperationService journal) {
        this.repository = repository;
        this.categorieRepository = categorieRepository;
        this.depotRepository = depotRepository;
        this.journal = journal;
    }

    public Page<ProduitDto> search(String search, Long categorieId, Boolean actif, Pageable pageable) {
        return repository.search(search, categorieId, actif, pageable).map(ProduitDto::from);
    }

    public ProduitDto get(Long id) {
        return ProduitDto.from(findEntity(id));
    }

    public Produit findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Produit", id));
    }

    @Transactional
    public ProduitDto create(ProduitRequest req) {
        if (repository.existsByCode(req.code())) {
            throw new BadRequestException("Le code produit '" + req.code() + "' existe déjà");
        }
        Produit p = new Produit();
        applyRequest(p, req);
        p.setActif(true);
        Produit saved = repository.save(p);
        journal.enregistrer("STOCKS", "CREATION", "Création du produit " + saved.getNom() + " (" + saved.getCode() + ")");
        return ProduitDto.from(saved);
    }

    @Transactional
    public ProduitDto update(Long id, ProduitRequest req) {
        Produit p = findEntity(id);
        if (!p.getCode().equals(req.code()) && repository.existsByCode(req.code())) {
            throw new BadRequestException("Le code produit '" + req.code() + "' existe déjà");
        }
        applyRequest(p, req);
        Produit saved = repository.save(p);
        journal.enregistrer("STOCKS", "MODIFICATION", "Modification du produit " + saved.getNom());
        return ProduitDto.from(saved);
    }

    @Transactional
    public void archive(Long id) {
        Produit p = findEntity(id);
        p.setActif(false);
        repository.save(p);
        journal.enregistrer("STOCKS", "ARCHIVAGE", "Archivage du produit " + p.getNom());
    }

    private void applyRequest(Produit p, ProduitRequest req) {
        p.setCode(req.code());
        p.setNom(req.nom());
        p.setUnite(req.unite());
        p.setPrixAchat(req.prixAchat());
        p.setPrixVente(req.prixVente());
        p.setSeuilAlerte(req.seuilAlerte());
        if (req.quantiteStock() != null) {
            p.setQuantiteStock(req.quantiteStock());
        }
        if (req.categorieId() != null) {
            CategorieProduit cat = categorieRepository.findById(req.categorieId())
                    .orElseThrow(() -> ResourceNotFoundException.of("CategorieProduit", req.categorieId()));
            p.setCategorie(cat);
        } else {
            p.setCategorie(null);
        }
        if (req.depotId() != null) {
            Depot depot = depotRepository.findById(req.depotId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Depot", req.depotId()));
            p.setDepot(depot);
        } else {
            p.setDepot(null);
        }
    }
}
