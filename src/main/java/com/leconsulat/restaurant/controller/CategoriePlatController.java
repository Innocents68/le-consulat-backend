package com.leconsulat.restaurant.controller;

import com.leconsulat.restaurant.dto.CategoriePlatDto;
import com.leconsulat.restaurant.service.CategoriePlatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories-plats")
public class CategoriePlatController {

    private final CategoriePlatService service;

    public CategoriePlatController(CategoriePlatService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoriePlatDto> list() {
        return service.list();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriePlatDto create(@Valid @RequestBody CategoriePlatDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public CategoriePlatDto update(@PathVariable Long id, @Valid @RequestBody CategoriePlatDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
