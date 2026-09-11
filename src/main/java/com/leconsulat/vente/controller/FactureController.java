package com.leconsulat.vente.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.vente.dto.FactureDto;
import com.leconsulat.vente.service.FactureService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/factures")
public class FactureController {

    private final FactureService service;

    public FactureController(FactureService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<FactureDto> search(@RequestParam(required = false) Long etablissementId,
                                            @RequestParam(required = false) String search,
                                            @RequestParam(required = false) Long commandeId,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer size,
                                            @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        LocalDateTime debutDt = dateDebut != null ? dateDebut.atStartOfDay() : null;
        LocalDateTime finDt = dateFin != null ? dateFin.atTime(LocalTime.MAX) : null;
        Page<FactureDto> result = service.search(etablissementId, search, commandeId, debutDt, finDt, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public FactureDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/{id}/reimprimer")
    public FactureDto reimprimer(@PathVariable Long id) {
        return service.reimprimer(id);
    }

    /** Ne journalise pas de réimpression ici : le premier appel (impression automatique après
     * encaissement, EF-013) n'en est pas une (RG-053) — seul l'appel explicite à
     * {@code /reimprimer} avant celui-ci compte comme telle. */
    @GetMapping("/{id}/ticket.pdf")
    public ResponseEntity<byte[]> ticket(@PathVariable Long id) {
        FactureDto dto = service.get(id); // vérifie le cloisonnement établissement, pour le nom de fichier
        byte[] pdf = service.genererTicketPdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + dto.numero() + ".pdf\"")
                .body(pdf);
    }
}
