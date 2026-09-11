package com.leconsulat.depense.controller;

import com.leconsulat.depense.dto.CategorieDepenseDto;
import com.leconsulat.depense.dto.CreateCategorieDepenseRequest;
import com.leconsulat.depense.dto.UpdateCategorieDepenseRequest;
import com.leconsulat.depense.service.CategorieDepenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories-depenses")
public class CategorieDepenseController {

    private final CategorieDepenseService service;

    public CategorieDepenseController(CategorieDepenseService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategorieDepenseDto> list(@RequestParam(required = false) Boolean actif) {
        return service.list(actif);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategorieDepenseDto create(@Valid @RequestBody CreateCategorieDepenseRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public CategorieDepenseDto update(@PathVariable Long id, @Valid @RequestBody UpdateCategorieDepenseRequest req) {
        return service.update(id, req);
    }

    @PatchMapping("/{id}/statut")
    public void toggleActif(@PathVariable Long id) {
        service.toggleActif(id);
    }
}
