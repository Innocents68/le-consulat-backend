package com.leconsulat.stock.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.stock.dto.SortieStockDto;
import com.leconsulat.stock.dto.SortieStockRequest;
import com.leconsulat.stock.entity.MotifSortieStock;
import com.leconsulat.stock.entity.Produit;
import com.leconsulat.stock.entity.SortieStock;
import com.leconsulat.stock.entity.StatutMouvementStock;
import com.leconsulat.stock.repository.ProduitRepository;
import com.leconsulat.stock.repository.SortieStockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional
public class SortieStockService {

    private final SortieStockRepository repository;
    private final ProduitRepository produitRepository;
    private final JournalOperationService journal;

    public SortieStockService(SortieStockRepository repository, ProduitRepository produitRepository, JournalOperationService journal) {
        this.repository = repository;
        this.produitRepository = produitRepository;
        this.journal = journal;
    }

    public Page<SortieStockDto> search(Long produitId, LocalDate dateDebut, LocalDate dateFin, Pageable pageable) {
        return repository.search(produitId, dateDebut, dateFin, pageable).map(SortieStockDto::from);
    }

    public SortieStockDto get(Long id) {
        return SortieStockDto.from(findEntity(id));
    }

    @Transactional
    public SortieStockDto create(SortieStockRequest req) {
        Produit produit = produitRepository.findById(req.produitId())
                .orElseThrow(() -> ResourceNotFoundException.of("Produit", req.produitId()));
        MotifSortieStock motif = parseMotif(req.motif());
        if (motif != MotifSortieStock.VENTE && (req.motif() == null || req.motif().isBlank())) {
            throw new BusinessRuleException("Le motif est obligatoire pour une sortie de stock hors vente");
        }
        SortieStock s = new SortieStock();
        s.setProduit(produit);
        s.setMotif(motif);
        applyRequest(s, req);
        s.setStatut(StatutMouvementStock.BROUILLON);
        return SortieStockDto.from(repository.save(s));
    }

    @Transactional
    public SortieStockDto update(Long id, SortieStockRequest req) {
        SortieStock s = findEntity(id);
        if (s.getStatut() != StatutMouvementStock.BROUILLON) {
            throw new BusinessRuleException("Une sortie de stock validée n'est plus modifiable");
        }
        Produit produit = produitRepository.findById(req.produitId())
                .orElseThrow(() -> ResourceNotFoundException.of("Produit", req.produitId()));
        s.setProduit(produit);
        s.setMotif(parseMotif(req.motif()));
        applyRequest(s, req);
        return SortieStockDto.from(repository.save(s));
    }

    @Transactional
    public SortieStockDto valider(Long id) {
        SortieStock s = findEntity(id);
        if (s.getStatut() != StatutMouvementStock.BROUILLON) {
            throw new BusinessRuleException("Cette sortie de stock est déjà validée");
        }
        Produit produit = s.getProduit();
        if (produit.getQuantiteStock() < s.getQuantite()) {
            throw new BusinessRuleException("Stock insuffisant pour valider cette sortie (" + produit.getNom() + ")");
        }
        produit.setQuantiteStock(produit.getQuantiteStock() - s.getQuantite());
        produitRepository.save(produit);
        s.setStatut(StatutMouvementStock.VALIDE);
        SortieStock saved = repository.save(s);
        journal.enregistrer("STOCKS", "VALIDATION", "Validation sortie de stock #" + saved.getId()
                + " : -" + saved.getQuantite() + " " + produit.getNom() + " (" + saved.getMotif() + ")");
        return SortieStockDto.from(saved);
    }

    /** Used internally by the Vente module to record an automatic stock exit at checkout, pre-validated. */
    @Transactional
    public void enregistrerSortieVenteValidee(Produit produit, int quantite, BigDecimal prixUnitaire) {
        if (produit.getQuantiteStock() < quantite) {
            throw new BusinessRuleException("Stock insuffisant pour " + produit.getNom());
        }
        produit.setQuantiteStock(produit.getQuantiteStock() - quantite);
        produitRepository.save(produit);

        SortieStock s = new SortieStock();
        s.setProduit(produit);
        s.setMotif(MotifSortieStock.VENTE);
        s.setQuantite(quantite);
        s.setPrixUnitaire(prixUnitaire);
        s.setMontant(prixUnitaire.multiply(BigDecimal.valueOf(quantite)));
        s.setDateSortie(LocalDate.now());
        s.setStatut(StatutMouvementStock.VALIDE);
        repository.save(s);
    }

    private MotifSortieStock parseMotif(String motif) {
        try {
            return MotifSortieStock.valueOf(motif.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Motif de sortie inconnu : " + motif);
        }
    }

    private void applyRequest(SortieStock s, SortieStockRequest req) {
        s.setQuantite(req.quantite());
        s.setPrixUnitaire(req.prixUnitaire());
        s.setMontant(req.prixUnitaire().multiply(BigDecimal.valueOf(req.quantite())));
        s.setDateSortie(req.dateSortie() != null ? req.dateSortie() : LocalDate.now());
    }

    private SortieStock findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("SortieStock", id));
    }
}
