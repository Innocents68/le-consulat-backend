package com.leconsulat.vente.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.vente.dto.*;
import com.leconsulat.vente.service.CommandeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/commandes")
public class CommandeController {

    private final CommandeService service;

    public CommandeController(CommandeService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<CommandeDto> search(@RequestParam(required = false) Long etablissementId,
                                             @RequestParam(required = false) String statut,
                                             @RequestParam(required = false) Long tableId,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                             @RequestParam(required = false) Integer page,
                                             @RequestParam(required = false) Integer size,
                                             @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        LocalDateTime debutDt = dateDebut != null ? dateDebut.atStartOfDay() : null;
        LocalDateTime finDt = dateFin != null ? dateFin.atTime(LocalTime.MAX) : null;
        Page<CommandeDto> result = service.search(etablissementId, statut, tableId, debutDt, finDt, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public CommandeDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/suivi-cuisine")
    public java.util.List<CommandeDto> suiviCuisine(@RequestParam(required = false) Long etablissementId) {
        return service.suiviCuisine(etablissementId);
    }

    @PostMapping("/{id}/avancer-cuisine")
    public CommandeDto avancerCuisine(@PathVariable Long id) {
        return service.avancerCuisine(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommandeDto create(@Valid @RequestBody CreateCommandeRequest req) {
        return service.create(req);
    }

    @PostMapping("/{id}/lignes")
    public CommandeDto ajouterLigne(@PathVariable Long id, @Valid @RequestBody LigneCommandeInput req) {
        return service.ajouterLigne(id, req);
    }

    @DeleteMapping("/{id}/lignes/{ligneId}")
    public CommandeDto retirerLigne(@PathVariable Long id, @PathVariable Long ligneId) {
        return service.retirerLigne(id, ligneId);
    }

    @PostMapping("/{id}/remise/{remiseId}")
    public CommandeDto appliquerRemise(@PathVariable Long id, @PathVariable Long remiseId) {
        return service.appliquerRemise(id, remiseId);
    }

    @DeleteMapping("/{id}/remise")
    public CommandeDto retirerRemise(@PathVariable Long id) {
        return service.retirerRemise(id);
    }

    @PostMapping("/{id}/annuler")
    public CommandeDto annuler(@PathVariable Long id, @Valid @RequestBody AnnulerCommandeRequest req) {
        return service.annuler(id, req);
    }

    @PostMapping("/{id}/encaisser")
    public FactureDto encaisser(@PathVariable Long id, @Valid @RequestBody EncaisserRequest req) {
        return service.encaisser(id, req);
    }
}
