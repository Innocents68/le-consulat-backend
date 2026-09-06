package com.leconsulat.stock.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.stock.dto.DepotDto;
import com.leconsulat.stock.dto.TransfertDepotRequest;
import com.leconsulat.stock.entity.Depot;
import com.leconsulat.stock.entity.Produit;
import com.leconsulat.stock.repository.DepotRepository;
import com.leconsulat.stock.repository.ProduitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DepotService {

    private final DepotRepository repository;
    private final ProduitRepository produitRepository;
    private final JournalOperationService journal;

    public DepotService(DepotRepository repository, ProduitRepository produitRepository, JournalOperationService journal) {
        this.repository = repository;
        this.produitRepository = produitRepository;
        this.journal = journal;
    }

    public List<DepotDto> list() {
        return repository.findAll().stream().map(DepotDto::from).toList();
    }

    @Transactional
    public DepotDto create(DepotDto dto) {
        Depot d = new Depot();
        d.setNom(dto.nom());
        d.setAdresse(dto.adresse());
        d.setActif(true);
        return DepotDto.from(repository.save(d));
    }

    @Transactional
    public DepotDto update(Long id, DepotDto dto) {
        Depot d = findEntity(id);
        d.setNom(dto.nom());
        d.setAdresse(dto.adresse());
        return DepotDto.from(repository.save(d));
    }

    @Transactional
    public void archive(Long id) {
        Depot d = findEntity(id);
        d.setActif(false);
        repository.save(d);
    }

    /**
     * Transfers a quantity of a product from its current (source) depot record to the
     * equivalent product record (matched by code) at the destination depot, creating one
     * if it does not exist there yet. Recorded as a paired SortieStock(TRANSFERT)/EntreeStock
     * movement for traceability.
     */
    @Transactional
    public void transferer(TransfertDepotRequest req) {
        Produit source = produitRepository.findById(req.produitId())
                .orElseThrow(() -> ResourceNotFoundException.of("Produit", req.produitId()));
        Depot depotSource = findEntity(req.depotSourceId());
        Depot depotDestination = findEntity(req.depotDestinationId());

        if (source.getDepot() == null || !source.getDepot().getId().equals(depotSource.getId())) {
            throw new BusinessRuleException("Le produit n'appartient pas au dépôt source indiqué");
        }
        if (source.getQuantiteStock() < req.quantite()) {
            throw new BusinessRuleException("Stock insuffisant au dépôt source pour ce transfert");
        }

        source.setQuantiteStock(source.getQuantiteStock() - req.quantite());
        produitRepository.save(source);

        Optional<Produit> destinationExistant = produitRepository.findByCode(source.getCode())
                .filter(p -> p.getDepot() != null && p.getDepot().getId().equals(depotDestination.getId()));

        Produit destination = destinationExistant.orElseGet(() -> {
            Produit p = new Produit();
            p.setCode(source.getCode() + "-" + depotDestination.getId());
            p.setNom(source.getNom());
            p.setCategorie(source.getCategorie());
            p.setUnite(source.getUnite());
            p.setPrixAchat(source.getPrixAchat());
            p.setPrixVente(source.getPrixVente());
            p.setSeuilAlerte(source.getSeuilAlerte());
            p.setQuantiteStock(0);
            p.setDepot(depotDestination);
            p.setActif(true);
            return p;
        });
        destination.setQuantiteStock(destination.getQuantiteStock() + req.quantite());
        produitRepository.save(destination);

        journal.enregistrer("STOCKS", "TRANSFERT", "Transfert de " + req.quantite() + " x " + source.getNom()
                + " (" + depotSource.getNom() + " -> " + depotDestination.getNom() + ")");
    }

    private Depot findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Depot", id));
    }
}
