package com.leconsulat.vente.controller;

import com.leconsulat.vente.dto.ClotureSessionRequest;
import com.leconsulat.vente.dto.OuvrirSessionRequest;
import com.leconsulat.vente.dto.SessionCaisseDto;
import com.leconsulat.vente.entity.SessionCaisse;
import com.leconsulat.vente.service.SessionCaisseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions-caisse")
public class SessionCaisseController {

    private final SessionCaisseService service;

    public SessionCaisseController(SessionCaisseService service) {
        this.service = service;
    }

    @GetMapping
    public List<SessionCaisseDto> list(@RequestParam(required = false) String statut) {
        SessionCaisse.Statut s = statut != null ? SessionCaisse.Statut.valueOf(statut.trim().toUpperCase()) : null;
        return service.list(s);
    }

    @GetMapping("/{id}")
    public SessionCaisseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionCaisseDto ouvrir(@Valid @RequestBody OuvrirSessionRequest req) {
        return service.ouvrir(req);
    }

    @PostMapping("/{id}/cloturer")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    public SessionCaisseDto cloturer(@PathVariable Long id, @Valid @RequestBody ClotureSessionRequest req) {
        return service.cloturer(id, req);
    }
}
