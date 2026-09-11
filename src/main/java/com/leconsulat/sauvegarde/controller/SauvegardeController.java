package com.leconsulat.sauvegarde.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.sauvegarde.dto.RestaurerRequest;
import com.leconsulat.sauvegarde.dto.SauvegardeDto;
import com.leconsulat.sauvegarde.service.SauvegardeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** §6.10.2 — réservé au Super Administrateur (module de gestion système, comme Utilisateurs
 * ou Paramètres). */
@RestController
@RequestMapping("/api/v1/sauvegardes")
@PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
public class SauvegardeController {

    private final SauvegardeService service;

    public SauvegardeController(SauvegardeService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<SauvegardeDto> lister(@RequestParam(required = false) Integer page,
                                               @RequestParam(required = false) Integer size) {
        Pageable pageable = PageableUtil.build(page, size, null);
        Page<SauvegardeDto> result = service.lister(pageable);
        return PageResponse.ofDto(result);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SauvegardeDto declencher() {
        return service.declencherManuelle();
    }

    @GetMapping("/{id}/fichier")
    public ResponseEntity<byte[]> telecharger(@PathVariable Long id) {
        byte[] contenu = service.telecharger(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + service.nomFichier(id) + "\"")
                .body(contenu);
    }

    @PostMapping("/{id}/restaurer")
    public ResponseEntity<Void> restaurer(@PathVariable Long id, @Valid @RequestBody RestaurerRequest req) {
        service.restaurerDepuisHistorique(id, req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/restaurer-fichier", consumes = "multipart/form-data")
    public ResponseEntity<Void> restaurerDepuisFichier(@RequestParam("fichier") MultipartFile fichier,
                                                        @RequestParam("confirmation") String confirmation) {
        service.restaurerDepuisFichier(fichier, new RestaurerRequest(confirmation));
        return ResponseEntity.noContent().build();
    }
}
