package com.leconsulat.vente.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.vente.dto.*;
import com.leconsulat.vente.entity.StatutVente;
import com.leconsulat.vente.service.VenteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/ventes")
public class VenteController {

    private final VenteService service;

    public VenteController(VenteService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<VenteDto> list(@RequestParam(required = false) String statut,
                                        @RequestParam(required = false) Long sessionCaisseId,
                                        @RequestParam(required = false) Long caissierId,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size,
                                        @RequestParam(required = false) String sort) {
        StatutVente s = statut != null ? StatutVente.valueOf(statut.trim().toUpperCase()) : null;
        Pageable pageable = PageableUtil.build(page, size, sort);
        LocalDateTime debutDt = dateDebut != null ? dateDebut.atStartOfDay() : null;
        LocalDateTime finDt = dateFin != null ? dateFin.atTime(LocalTime.MAX) : null;
        Page<VenteDto> result = service.search(s, sessionCaisseId, caissierId, debutDt, finDt, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public VenteDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    @ResponseStatus(HttpStatus.CREATED)
    public VenteDto create(@Valid @RequestBody CreateVenteRequest req) {
        return service.create(req);
    }

    @PostMapping("/{id}/lignes")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    public VenteDto ajouterLigne(@PathVariable Long id, @Valid @RequestBody AddLigneVenteRequest req) {
        return service.ajouterLigne(id, req);
    }

    @DeleteMapping("/{id}/lignes/{ligneId}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    public VenteDto retirerLigne(@PathVariable Long id, @PathVariable Long ligneId) {
        return service.retirerLigne(id, ligneId);
    }

    @PostMapping("/{id}/encaisser")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    public VenteDto encaisser(@PathVariable Long id, @Valid @RequestBody EncaisserVenteRequest req) {
        return service.encaisser(id, req);
    }
}
