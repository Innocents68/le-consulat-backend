package com.leconsulat.vente.controller;

import com.leconsulat.vente.dto.RemiseDto;
import com.leconsulat.vente.service.RemiseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/remises")
public class RemiseController {

    private final RemiseService service;

    public RemiseController(RemiseService service) {
        this.service = service;
    }

    @GetMapping
    public List<RemiseDto> list() {
        return service.list();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public RemiseDto create(@Valid @RequestBody RemiseDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public RemiseDto update(@PathVariable Long id, @Valid @RequestBody RemiseDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public void desactiver(@PathVariable Long id) {
        service.desactiver(id);
    }
}
