package com.leconsulat.utilisateur.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.utilisateur.dto.*;
import com.leconsulat.utilisateur.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Réservé au Super Administrateur (cf. matrice §3.3 du CDC : aucun accès, même en lecture,
 * pour le Gérant/Caissier). */
@RestController
@RequestMapping("/api/v1/utilisateurs")
@PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
public class UtilisateurController {

    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<UtilisateurDto> list(@RequestParam(required = false) String search,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer size,
                                              @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        Page<UtilisateurDto> result = service.list(search, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public UtilisateurDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UtilisateurDto create(@Valid @RequestBody CreateUtilisateurRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public UtilisateurDto update(@PathVariable Long id, @Valid @RequestBody UpdateUtilisateurRequest req) {
        return service.update(id, req);
    }

    @PatchMapping("/{id}/statut")
    public UtilisateurDto toggleStatut(@PathVariable Long id) {
        return service.toggleStatut(id);
    }

    @PutMapping("/{id}/mot-de-passe")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordRequest req) {
        service.resetPassword(id, req);
        return ResponseEntity.noContent().build();
    }
}
