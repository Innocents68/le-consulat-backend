package com.leconsulat.stock.controller;

import com.leconsulat.stock.dto.CreateFournisseurRequest;
import com.leconsulat.stock.dto.FournisseurDto;
import com.leconsulat.stock.service.FournisseurService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fournisseurs")
public class FournisseurController {

    private final FournisseurService service;

    public FournisseurController(FournisseurService service) {
        this.service = service;
    }

    @GetMapping
    public List<FournisseurDto> list(@RequestParam(required = false) Boolean actif) {
        return service.list(actif);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FournisseurDto create(@Valid @RequestBody CreateFournisseurRequest req) {
        return service.create(req);
    }

    @PatchMapping("/{id}/statut")
    public void toggleActif(@PathVariable Long id) {
        service.toggleActif(id);
    }
}
