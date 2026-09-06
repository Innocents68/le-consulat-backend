package com.leconsulat.stock.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.stock.dto.EntreeStockDto;
import com.leconsulat.stock.dto.EntreeStockRequest;
import com.leconsulat.stock.entity.EntreeStock;
import com.leconsulat.stock.entity.Produit;
import com.leconsulat.stock.entity.StatutMouvementStock;
import com.leconsulat.stock.repository.EntreeStockRepository;
import com.leconsulat.stock.repository.ProduitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class EntreeStockService {

    private final EntreeStockRepository repository;
    private final ProduitRepository produitRepository;
    private final JournalOperationService journal;

    public EntreeStockService(EntreeStockRepository repository, ProduitRepository produitRepository, JournalOperationService journal) {
        this.repository = repository;
        this.produitRepository = produitRepository;
        this.journal = journal;
    }

    public Page<EntreeStockDto> search(Long produitId, LocalDate dateDebut, LocalDate dateFin, Pageable pageable) {
        return repository.search(produitId, dateDebut, dateFin, pageable).map(EntreeStockDto::from);
    }

    public EntreeStockDto get(Long id) {
        return EntreeStockDto.from(findEntity(id));
    }

    @Transactional
    public EntreeStockDto create(EntreeStockRequest req) {
        Produit produit = produitRepository.findById(req.produitId())
                .orElseThrow(() -> ResourceNotFoundException.of("Produit", req.produitId()));
        EntreeStock e = new EntreeStock();
        e.setProduit(produit);
        applyRequest(e, req);
        e.setStatut(StatutMouvementStock.BROUILLON);
        EntreeStock saved = repository.save(e);
        return EntreeStockDto.from(saved);
    }

    @Transactional
    public EntreeStockDto update(Long id, EntreeStockRequest req) {
        EntreeStock e = findEntity(id);
        if (e.getStatut() != StatutMouvementStock.BROUILLON) {
            throw new BusinessRuleException("Une entrée de stock validée n'est plus modifiable");
        }
        Produit produit = produitRepository.findById(req.produitId())
                .orElseThrow(() -> ResourceNotFoundException.of("Produit", req.produitId()));
        e.setProduit(produit);
        applyRequest(e, req);
        return EntreeStockDto.from(repository.save(e));
    }

    @Transactional
    public EntreeStockDto valider(Long id) {
        EntreeStock e = findEntity(id);
        if (e.getStatut() != StatutMouvementStock.BROUILLON) {
            throw new BusinessRuleException("Cette entrée de stock est déjà validée");
        }
        Produit produit = e.getProduit();
        produit.setQuantiteStock(produit.getQuantiteStock() + e.getQuantite());
        produitRepository.save(produit);
        e.setStatut(StatutMouvementStock.VALIDE);
        EntreeStock saved = repository.save(e);
        journal.enregistrer("STOCKS", "VALIDATION", "Validation entrée de stock #" + saved.getId()
                + " : +" + saved.getQuantite() + " " + produit.getNom());
        return EntreeStockDto.from(saved);
    }

    private void applyRequest(EntreeStock e, EntreeStockRequest req) {
        e.setFournisseur(req.fournisseur());
        e.setQuantite(req.quantite());
        e.setPrixUnitaire(req.prixUnitaire());
        e.setMontant(req.prixUnitaire().multiply(java.math.BigDecimal.valueOf(req.quantite())));
        e.setDateEntree(req.dateEntree() != null ? req.dateEntree() : LocalDate.now());
        e.setBonLivraison(req.bonLivraison());
    }

    private EntreeStock findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("EntreeStock", id));
    }
}
