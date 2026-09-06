package com.leconsulat.sauvegardes.controller;

import com.leconsulat.sauvegardes.dto.SauvegardeDto;
import com.leconsulat.sauvegardes.service.SauvegardeService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sauvegardes")
public class SauvegardeController {

    private final SauvegardeService service;

    public SauvegardeController(SauvegardeService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public List<SauvegardeDto> list() {
        return service.list();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public SauvegardeDto declencher() {
        return service.declencher();
    }

    @PostMapping("/{id}/restaurer")
    @PreAuthorize("hasRole('ADMIN')")
    public void restaurer(@PathVariable Long id) {
        service.restaurer(id);
    }
}
