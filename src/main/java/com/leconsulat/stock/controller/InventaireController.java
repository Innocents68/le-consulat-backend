package com.leconsulat.stock.controller;

import com.leconsulat.stock.dto.CreateInventaireRequest;
import com.leconsulat.stock.dto.InventaireDto;
import com.leconsulat.stock.dto.UpdateInventaireRequest;
import com.leconsulat.stock.service.InventaireService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventaires")
public class InventaireController {

    private final InventaireService service;

    public InventaireController(InventaireService service) {
        this.service = service;
    }

    @GetMapping
    public List<InventaireDto> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public InventaireDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public InventaireDto create(@Valid @RequestBody CreateInventaireRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public InventaireDto updateComptage(@PathVariable Long id, @RequestBody UpdateInventaireRequest req) {
        return service.updateComptage(id, req);
    }

    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public InventaireDto valider(@PathVariable Long id) {
        return service.valider(id);
    }
}
