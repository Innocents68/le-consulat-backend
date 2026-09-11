package com.leconsulat.journal.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.journal.dto.JournalOperationDto;
import com.leconsulat.journal.repository.JournalOperationRepository;
import com.leconsulat.security.PerimetreGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Lecture seule (RG-019). Le Super Administrateur voit tout et peut filtrer par établissement ;
 * un Gérant/Caissier est forcé sur le sien, quel que soit le filtre demandé (RG-099). */
@RestController
@RequestMapping("/api/v1/journal-operations")
public class JournalOperationController {

    private final JournalOperationRepository repository;
    private final EtablissementRepository etablissementRepository;
    private final PerimetreGuard perimetreGuard;

    public JournalOperationController(JournalOperationRepository repository,
                                       EtablissementRepository etablissementRepository,
                                       PerimetreGuard perimetreGuard) {
        this.repository = repository;
        this.etablissementRepository = etablissementRepository;
        this.perimetreGuard = perimetreGuard;
    }

    @GetMapping
    public PageResponse<JournalOperationDto> list(
            @RequestParam(required = false) Long utilisateurId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Long etablissementId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        LocalDateTime debutDt = dateDebut != null ? dateDebut.atStartOfDay() : null;
        LocalDateTime finDt = dateFin != null ? dateFin.atTime(LocalTime.MAX) : null;

        Etablissement demande = etablissementId != null
                ? etablissementRepository.findById(etablissementId).orElse(null)
                : null;
        Etablissement scope = perimetreGuard.scopeEtablissement(demande);
        Long scopeId = scope != null ? scope.getId() : null;

        Page<JournalOperationDto> result = repository.search(utilisateurId, module, scopeId, debutDt, finDt, pageable)
                .map(JournalOperationDto::from);
        return PageResponse.ofDto(result);
    }
}
