package com.leconsulat.remise.controller;

import com.leconsulat.remise.dto.CreateRemiseRequest;
import com.leconsulat.remise.dto.RemiseDto;
import com.leconsulat.remise.service.RemiseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Cloisonnement établissement géré par le service via PerimetreGuard — les deux profils
 * peuvent gérer les remises de leur périmètre (matrice §3.3). */
@RestController
@RequestMapping("/api/v1/remises")
public class RemiseController {

    private final RemiseService service;

    public RemiseController(RemiseService service) {
        this.service = service;
    }

    @GetMapping
    public List<RemiseDto> list(@RequestParam(required = false) Long etablissementId) {
        return service.list(etablissementId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RemiseDto create(@Valid @RequestBody CreateRemiseRequest req) {
        return service.create(req);
    }

    @PatchMapping("/{id}/statut")
    public void toggleActif(@PathVariable Long id) {
        service.toggleActif(id);
    }
}
