package com.leconsulat.catalogue.controller;

import com.leconsulat.catalogue.dto.CategorieDto;
import com.leconsulat.catalogue.dto.CreateCategorieRequest;
import com.leconsulat.catalogue.service.CategorieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Cloisonnement établissement géré entièrement par le service via PerimetreGuard — pas de
 * @PreAuthorize ici, les deux profils peuvent gérer les catégories de leur périmètre (matrice §3.3). */
@RestController
@RequestMapping("/api/v1/categories")
public class CategorieController {

    private final CategorieService service;

    public CategorieController(CategorieService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategorieDto> list(@RequestParam(required = false) Long etablissementId) {
        return service.list(etablissementId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategorieDto create(@Valid @RequestBody CreateCategorieRequest req) {
        return service.create(req);
    }

    @PatchMapping("/{id}/statut")
    public void toggleActif(@PathVariable Long id) {
        service.toggleActif(id);
    }
}
