package com.leconsulat.journal.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.journal.dto.JournalOperationDto;
import com.leconsulat.journal.service.JournalOperationQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    private final JournalOperationQueryService service;

    public JournalOperationController(JournalOperationQueryService service) {
        this.service = service;
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
        Page<JournalOperationDto> result = service.search(utilisateurId, module, etablissementId, debutDt, finDt, pageable);
        return PageResponse.ofDto(result);
    }

    /** Consu_corrige.docx §8. */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exporter(
            @RequestParam String format,
            @RequestParam(required = false) Long utilisateurId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Long etablissementId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        LocalDateTime debutDt = dateDebut != null ? dateDebut.atStartOfDay() : null;
        LocalDateTime finDt = dateFin != null ? dateFin.atTime(LocalTime.MAX) : null;
        byte[] fichier = service.exporter(format, utilisateurId, module, etablissementId, debutDt, finDt);
        boolean excel = "excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format);
        MediaType type = excel ? MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") : MediaType.APPLICATION_PDF;
        String extension = excel ? "xlsx" : "pdf";
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"journal-operations." + extension + "\"")
                .body(fichier);
    }
}
