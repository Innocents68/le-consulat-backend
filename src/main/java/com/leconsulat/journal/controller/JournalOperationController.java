package com.leconsulat.journal.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.journal.dto.JournalOperationDto;
import com.leconsulat.journal.repository.JournalOperationRepository;
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

/** Read-only journal des opérations : API_CONTRACT.md §2. */
@RestController
@RequestMapping("/api/v1/journal-operations")
public class JournalOperationController {

    private final JournalOperationRepository repository;

    public JournalOperationController(JournalOperationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public PageResponse<JournalOperationDto> list(
            @RequestParam(required = false) Long utilisateurId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        LocalDateTime debutDt = dateDebut != null ? dateDebut.atStartOfDay() : null;
        LocalDateTime finDt = dateFin != null ? dateFin.atTime(LocalTime.MAX) : null;
        Page<JournalOperationDto> result = repository.search(utilisateurId, module, debutDt, finDt, pageable)
                .map(JournalOperationDto::from);
        return PageResponse.ofDto(result);
    }
}
