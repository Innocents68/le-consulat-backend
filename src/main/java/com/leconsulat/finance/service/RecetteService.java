package com.leconsulat.finance.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.finance.dto.CreateRecetteRequest;
import com.leconsulat.finance.dto.RecetteDto;
import com.leconsulat.finance.entity.Recette;
import com.leconsulat.finance.entity.SourceRecette;
import com.leconsulat.finance.repository.RecetteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RecetteService {

    private final RecetteRepository repository;
    private final JournalOperationService journal;

    public RecetteService(RecetteRepository repository, JournalOperationService journal) {
        this.repository = repository;
        this.journal = journal;
    }

    public Page<RecetteDto> search(LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable) {
        return repository.search(dateDebut, dateFin, pageable).map(RecetteDto::from);
    }

    /** Called automatically by the Vente module when a ticket is paid — cahier des charges §2.6. */
    @Transactional
    public void enregistrerDepuisVente(BigDecimal montant, String description) {
        Recette r = new Recette();
        r.setSource(SourceRecette.VENTE);
        r.setMontant(montant);
        r.setDescription(description);
        r.setDate(LocalDateTime.now());
        repository.save(r);
    }

    @Transactional
    public RecetteDto enregistrerManuelle(CreateRecetteRequest req) {
        Recette r = new Recette();
        r.setSource(SourceRecette.AUTRE);
        r.setMontant(req.montant());
        r.setDescription(req.description());
        r.setDate(LocalDateTime.now());
        Recette saved = repository.save(r);
        journal.enregistrer("FINANCE", "CREATION", "Saisie manuelle d'une recette de " + req.montant());
        return RecetteDto.from(saved);
    }

    public List<Recette> findByPeriode(LocalDateTime debut, LocalDateTime fin) {
        return repository.findByDateBetween(debut, fin);
    }

    public BigDecimal totalPeriode(LocalDateTime debut, LocalDateTime fin) {
        return findByPeriode(debut, fin).stream().map(Recette::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
