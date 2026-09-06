package com.leconsulat.finance.controller;

import com.leconsulat.finance.dto.CategorieDepenseDto;
import com.leconsulat.finance.service.CategorieDepenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public List<CategorieDepenseDto> list() {
        return service.list();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public CategorieDepenseDto create(@Valid @RequestBody CategorieDepenseDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public CategorieDepenseDto update(@PathVariable Long id, @Valid @RequestBody CategorieDepenseDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
