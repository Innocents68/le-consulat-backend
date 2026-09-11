package com.leconsulat.catalogue.controller;

import com.leconsulat.catalogue.dto.*;
import com.leconsulat.catalogue.service.ProduitService;
import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/produits")
public class ProduitController {

    private final ProduitService service;

    public ProduitController(ProduitService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ProduitDto> search(@RequestParam(required = false) Long etablissementId,
                                            @RequestParam(required = false) Long categorieId,
                                            @RequestParam(required = false) Boolean actif,
                                            @RequestParam(required = false) String search,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer size,
                                            @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        Page<ProduitDto> result = service.search(etablissementId, categorieId, actif, search, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public ProduitDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/{id}/historique-prix")
    public List<HistoriquePrixDto> historiquePrix(@PathVariable Long id) {
        return service.historiquePrix(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProduitDto create(@Valid @RequestBody CreateProduitRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public ProduitDto update(@PathVariable Long id, @Valid @RequestBody UpdateProduitRequest req) {
        return service.update(id, req);
    }

    @PatchMapping("/{id}/statut")
    public void toggleActif(@PathVariable Long id) {
        service.toggleActif(id);
    }
}
