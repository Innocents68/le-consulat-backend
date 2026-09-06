package com.leconsulat.cave.service;

import com.leconsulat.cave.dto.BoissonDto;
import com.leconsulat.cave.dto.BoissonRequest;
import com.leconsulat.cave.entity.Boisson;
import com.leconsulat.cave.entity.Fournisseur;
import com.leconsulat.cave.entity.TypeBoisson;
import com.leconsulat.cave.repository.BoissonRepository;
import com.leconsulat.cave.repository.FournisseurRepository;
import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class BoissonService {

    private final BoissonRepository repository;
    private final FournisseurRepository fournisseurRepository;
    private final JournalOperationService journal;

    public BoissonService(BoissonRepository repository, FournisseurRepository fournisseurRepository, JournalOperationService journal) {
        this.repository = repository;
        this.fournisseurRepository = fournisseurRepository;
        this.journal = journal;
    }

    public Page<BoissonDto> search(String search, String type, Boolean actif, Pageable pageable) {
        TypeBoisson t = (type == null || type.isBlank()) ? null : parseType(type);
        return repository.search(search, t, actif, pageable).map(BoissonDto::from);
    }

    public BoissonDto get(Long id) {
        return BoissonDto.from(findEntity(id));
    }

    public Boisson findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Boisson", id));
    }

    @Transactional
    public BoissonDto create(BoissonRequest req) {
        Boisson b = new Boisson();
        apply(b, req);
        b.setActif(true);
        Boisson saved = repository.save(b);
        journal.enregistrer("CAVE", "CREATION", "Création de la référence " + saved.getNom());
        return BoissonDto.from(saved);
    }

    @Transactional
    public BoissonDto update(Long id, BoissonRequest req) {
        Boisson b = findEntity(id);
        apply(b, req);
        Boisson saved = repository.save(b);
        journal.enregistrer("CAVE", "MODIFICATION", "Modification de la référence " + saved.getNom());
        return BoissonDto.from(saved);
    }

    @Transactional
    public void archive(Long id) {
        Boisson b = findEntity(id);
        b.setActif(false);
        repository.save(b);
        journal.enregistrer("CAVE", "ARCHIVAGE", "Archivage de la référence " + b.getNom());
    }

    private void apply(Boisson b, BoissonRequest req) {
        b.setNom(req.nom());
        b.setType(parseType(req.type()));
        b.setOrigine(req.origine());
        b.setMillesime(req.millesime());
        b.setPrixAchatBouteille(req.prixAchatBouteille());
        b.setPrixVenteBouteille(req.prixVenteBouteille());
        b.setPrixVenteVerre(req.prixVenteVerre());
        if (req.quantiteStock() != null) {
            b.setQuantiteStock(req.quantiteStock());
        } else if (b.getQuantiteStock() == null) {
            b.setQuantiteStock(BigDecimal.ZERO);
        }
        if (req.seuilAlerte() != null) {
            b.setSeuilAlerte(req.seuilAlerte());
        }
        if (req.fournisseurId() != null) {
            Fournisseur f = fournisseurRepository.findById(req.fournisseurId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Fournisseur", req.fournisseurId()));
            b.setFournisseur(f);
        } else {
            b.setFournisseur(null);
        }
    }

    private TypeBoisson parseType(String type) {
        try {
            return TypeBoisson.valueOf(type.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Type de boisson inconnu : " + type);
        }
    }
}
