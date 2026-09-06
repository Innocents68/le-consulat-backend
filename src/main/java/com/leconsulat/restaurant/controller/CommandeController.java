package com.leconsulat.restaurant.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.restaurant.dto.*;
import com.leconsulat.restaurant.entity.StatutCommande;
import com.leconsulat.restaurant.service.CommandeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/commandes-restaurant")
public class CommandeController {

    private final CommandeService service;

    public CommandeController(CommandeService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<CommandeDto> list(@RequestParam(required = false) String statut,
                                           @RequestParam(required = false) Long tableId,
                                           @RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer size,
                                           @RequestParam(required = false) String sort) {
        StatutCommande s = statut != null ? StatutCommande.valueOf(statut.trim().toUpperCase()) : null;
        Pageable pageable = PageableUtil.build(page, size, sort);
        Page<CommandeDto> result = service.search(s, tableId, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public CommandeDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    @ResponseStatus(HttpStatus.CREATED)
    public CommandeDto create(@Valid @RequestBody CreateCommandeRequest req) {
        return service.create(req);
    }

    @PostMapping("/{id}/lignes")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    public CommandeDto ajouterLigne(@PathVariable Long id, @Valid @RequestBody AddLigneCommandeRequest req) {
        return service.ajouterLigne(id, req);
    }

    @DeleteMapping("/{id}/lignes/{ligneId}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    public CommandeDto retirerLigne(@PathVariable Long id, @PathVariable Long ligneId) {
        return service.retirerLigne(id, ligneId);
    }

    @PostMapping("/{id}/envoyer-cuisine")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    public CommandeDto envoyerCuisine(@PathVariable Long id) {
        return service.envoyerCuisine(id);
    }

    @PatchMapping("/{id}/lignes/{ligneId}/statut")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR','CUISINIER')")
    public CommandeDto updateStatutLigne(@PathVariable Long id, @PathVariable Long ligneId, @Valid @RequestBody UpdateLigneStatutRequest req) {
        return service.updateStatutLigne(id, ligneId, req);
    }

    @PostMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    public CommandeDto annuler(@PathVariable Long id, @Valid @RequestBody AnnulerCommandeRequest req) {
        return service.annuler(id, req);
    }
}
