package com.leconsulat.etablissement.controller;

import com.leconsulat.etablissement.dto.CreateEtablissementRequest;
import com.leconsulat.etablissement.dto.EtablissementDto;
import com.leconsulat.etablissement.service.EtablissementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Liste ouverte à tout utilisateur connecté (nécessaire au sélecteur d'affectation), écriture
 * réservée au Super Administrateur (lui seul administre la plateforme, cf. §3.2 du CDC). */
@RestController
@RequestMapping("/api/v1/etablissements")
public class EtablissementController {

    private final EtablissementService service;

    public EtablissementController(EtablissementService service) {
        this.service = service;
    }

    @GetMapping
    public List<EtablissementDto> list(@RequestParam(required = false, defaultValue = "true") boolean actifsSeulement) {
        return service.list(actifsSeulement);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    @ResponseStatus(HttpStatus.CREATED)
    public EtablissementDto create(@Valid @RequestBody CreateEtablissementRequest req) {
        return service.create(req);
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    public EtablissementDto toggleActif(@PathVariable Long id) {
        return service.toggleActif(id);
    }
}
