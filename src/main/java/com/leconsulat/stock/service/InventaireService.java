package com.leconsulat.stock.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.stock.dto.CreateInventaireRequest;
import com.leconsulat.stock.dto.InventaireDto;
import com.leconsulat.stock.dto.UpdateInventaireRequest;
import com.leconsulat.stock.entity.Inventaire;
import com.leconsulat.stock.entity.LigneInventaire;
import com.leconsulat.stock.entity.Produit;
import com.leconsulat.stock.repository.InventaireRepository;
import com.leconsulat.stock.repository.ProduitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventaireService {

    private final InventaireRepository repository;
    private final ProduitRepository produitRepository;
    private final JournalOperationService journal;

    public InventaireService(InventaireRepository repository, ProduitRepository produitRepository, JournalOperationService journal) {
        this.repository = repository;
        this.produitRepository = produitRepository;
        this.journal = journal;
    }

    public List<InventaireDto> list() {
        return repository.findAll().stream().map(InventaireDto::from).toList();
    }

    public InventaireDto get(Long id) {
        return InventaireDto.from(findEntity(id));
    }

    @Transactional
    public InventaireDto create(CreateInventaireRequest req) {
        Inventaire.Type type;
        try {
            type = Inventaire.Type.valueOf(req.type().trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Type d'inventaire inconnu : " + req.type());
        }
        Inventaire inventaire = new Inventaire();
        inventaire.setType(type);
        inventaire.setStatut(Inventaire.Statut.EN_COURS);

        List<Produit> produits = (req.produitIds() == null || req.produitIds().isEmpty())
                ? produitRepository.findAll().stream().filter(Produit::isActif).toList()
                : produitRepository.findAllById(req.produitIds());

        for (Produit p : produits) {
            LigneInventaire ligne = new LigneInventaire();
            ligne.setInventaire(inventaire);
            ligne.setProduit(p);
            ligne.setQuantiteTheorique(p.getQuantiteStock());
            ligne.setQuantiteReelle(p.getQuantiteStock());
            inventaire.getLignes().add(ligne);
        }

        Inventaire saved = repository.save(inventaire);
        journal.enregistrer("STOCKS", "CREATION", "Ouverture d'un inventaire " + type + " (#" + saved.getId() + ", " + produits.size() + " produits)");
        return InventaireDto.from(saved);
    }

    @Transactional
    public InventaireDto updateComptage(Long id, UpdateInventaireRequest req) {
        Inventaire inventaire = findEntity(id);
        if (inventaire.getStatut() != Inventaire.Statut.EN_COURS) {
            throw new BusinessRuleException("Cet inventaire est déjà validé, les quantités comptées ne sont plus modifiables");
        }
        if (req.lignes() != null) {
            for (UpdateInventaireRequest.LigneComptageRequest lr : req.lignes()) {
                inventaire.getLignes().stream()
                        .filter(l -> l.getId().equals(lr.ligneId()))
                        .findFirst()
                        .ifPresent(l -> l.setQuantiteReelle(lr.quantiteReelle()));
            }
        }
        return InventaireDto.from(repository.save(inventaire));
    }

    @Transactional
    public InventaireDto valider(Long id) {
        Inventaire inventaire = findEntity(id);
        if (inventaire.getStatut() != Inventaire.Statut.EN_COURS) {
            throw new BusinessRuleException("Cet inventaire est déjà validé");
        }
        for (LigneInventaire ligne : inventaire.getLignes()) {
            Produit p = ligne.getProduit();
            p.setQuantiteStock(ligne.getQuantiteReelle());
            produitRepository.save(p);
        }
        inventaire.setStatut(Inventaire.Statut.VALIDE);
        Inventaire saved = repository.save(inventaire);
        journal.enregistrer("STOCKS", "VALIDATION", "Validation de l'inventaire #" + saved.getId() + " (rapport d'écart généré)");
        return InventaireDto.from(saved);
    }

    private Inventaire findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Inventaire", id));
    }
}
