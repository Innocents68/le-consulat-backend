package com.leconsulat.maquis.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.maquis.dto.*;
import com.leconsulat.maquis.entity.StatutCommandeMaquis;
import com.leconsulat.maquis.service.CommandeMaquisService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/commandes-maquis")
public class CommandeMaquisController {

    private final CommandeMaquisService service;

    public CommandeMaquisController(CommandeMaquisService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<CommandeMaquisDto> list(@RequestParam(required = false) String statut,
                                                 @RequestParam(required = false) Integer page,
                                                 @RequestParam(required = false) Integer size,
                                                 @RequestParam(required = false) String sort) {
        StatutCommandeMaquis s = statut != null ? StatutCommandeMaquis.valueOf(statut.trim().toUpperCase()) : null;
        Pageable pageable = PageableUtil.build(page, size, sort);
        Page<CommandeMaquisDto> result = service.search(s, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public CommandeMaquisDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    @ResponseStatus(HttpStatus.CREATED)
    public CommandeMaquisDto create(@RequestBody CreateCommandeMaquisRequest req) {
        return service.create(req);
    }

    @PostMapping("/{id}/lignes")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    public CommandeMaquisDto ajouterLigne(@PathVariable Long id, @Valid @RequestBody AddLigneMaquisRequest req) {
        return service.ajouterLigne(id, req);
    }

    @DeleteMapping("/{id}/lignes/{ligneId}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    public CommandeMaquisDto retirerLigne(@PathVariable Long id, @PathVariable Long ligneId) {
        return service.retirerLigne(id, ligneId);
    }

    @PostMapping("/{id}/cloturer")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    public CommandeMaquisDto cloturer(@PathVariable Long id) {
        return service.cloturer(id);
    }

    @PostMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    public CommandeMaquisDto annuler(@PathVariable Long id) {
        return service.annuler(id);
    }
}
