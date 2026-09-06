package com.leconsulat.stock.controller;

import com.leconsulat.stock.dto.CategorieProduitDto;
import com.leconsulat.stock.service.CategorieProduitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories-produits")
public class CategorieProduitController {

    private final CategorieProduitService service;

    public CategorieProduitController(CategorieProduitService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategorieProduitDto> list() {
        return service.list();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public CategorieProduitDto create(@Valid @RequestBody CategorieProduitDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public CategorieProduitDto update(@PathVariable Long id, @Valid @RequestBody CategorieProduitDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
