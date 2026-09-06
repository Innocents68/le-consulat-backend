package com.leconsulat.finance.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BadRequestException;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.finance.dto.DepenseDto;
import com.leconsulat.finance.dto.DepenseRequest;
import com.leconsulat.finance.entity.CategorieDepense;
import com.leconsulat.finance.entity.Depense;
import com.leconsulat.finance.repository.CategorieDepenseRepository;
import com.leconsulat.finance.repository.DepenseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class DepenseService {

    private final DepenseRepository repository;
    private final CategorieDepenseRepository categorieRepository;
    private final JournalOperationService journal;

    @Value("${app.uploads.dir}")
    private String uploadsDir;

    public DepenseService(DepenseRepository repository, CategorieDepenseRepository categorieRepository, JournalOperationService journal) {
        this.repository = repository;
        this.categorieRepository = categorieRepository;
        this.journal = journal;
    }

    public Page<DepenseDto> search(Long categorieId, LocalDate dateDebut, LocalDate dateFin, Pageable pageable) {
        return repository.search(categorieId, dateDebut, dateFin, pageable).map(DepenseDto::from);
    }

    public DepenseDto get(Long id) {
        return DepenseDto.from(findEntity(id));
    }

    @Transactional
    public DepenseDto create(DepenseRequest req) {
        Depense d = new Depense();
        apply(d, req);
        d.setStatut(Depense.Statut.EN_ATTENTE);
        Depense saved = repository.save(d);
        return DepenseDto.from(saved);
    }

    @Transactional
    public DepenseDto update(Long id, DepenseRequest req) {
        Depense d = findEntity(id);
        if (d.getStatut() == Depense.Statut.VALIDEE) {
            throw new BusinessRuleException("Une dépense validée n'est plus modifiable");
        }
        apply(d, req);
        return DepenseDto.from(repository.save(d));
    }

    @Transactional
    public DepenseDto valider(Long id) {
        Depense d = findEntity(id);
        if (d.getStatut() == Depense.Statut.VALIDEE) {
            throw new BusinessRuleException("Cette dépense est déjà validée");
        }
        d.setStatut(Depense.Statut.VALIDEE);
        Depense saved = repository.save(d);
        journal.enregistrer("FINANCE", "VALIDATION", "Validation de la dépense #" + saved.getId() + " (" + saved.getMontant() + ")");
        return DepenseDto.from(saved);
    }

    @Transactional
    public DepenseDto uploadJustificatif(Long id, MultipartFile file) {
        Depense d = findEntity(id);
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Fichier justificatif manquant");
        }
        try {
            Path dir = Path.of(uploadsDir, "justificatifs");
            Files.createDirectories(dir);
            String extension = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                extension = original.substring(original.lastIndexOf('.'));
            }
            String filename = "depense-" + id + "-" + UUID.randomUUID() + extension;
            Path target = dir.resolve(filename);
            file.transferTo(target);
            d.setJustificatifUrl("/uploads/justificatifs/" + filename);
        } catch (IOException e) {
            throw new BadRequestException("Impossible d'enregistrer le justificatif : " + e.getMessage());
        }
        return DepenseDto.from(repository.save(d));
    }

    private void apply(Depense d, DepenseRequest req) {
        CategorieDepense categorie = categorieRepository.findById(req.categorieId())
                .orElseThrow(() -> ResourceNotFoundException.of("CategorieDepense", req.categorieId()));
        d.setCategorie(categorie);
        d.setMontant(req.montant());
        d.setDescription(req.description());
        d.setFournisseur(req.fournisseur());
        d.setDate(req.date() != null ? req.date() : LocalDate.now());
    }

    private Depense findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Depense", id));
    }
}
