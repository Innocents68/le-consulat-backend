package com.leconsulat.salle.controller;

import com.leconsulat.salle.dto.CreateTableRequest;
import com.leconsulat.salle.dto.TableServiceDto;
import com.leconsulat.salle.dto.UpdateTableRequest;
import com.leconsulat.salle.service.TableServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tables")
public class TableServiceController {

    private final TableServiceService service;

    public TableServiceController(TableServiceService service) {
        this.service = service;
    }

    @GetMapping
    public List<TableServiceDto> list(@RequestParam(required = false) Long etablissementId) {
        return service.list(etablissementId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TableServiceDto create(@Valid @RequestBody CreateTableRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public TableServiceDto update(@PathVariable Long id, @Valid @RequestBody UpdateTableRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PatchMapping("/{id}/statut")
    public void toggleActif(@PathVariable Long id) {
        service.toggleActif(id);
    }

    @PostMapping("/{id}/reserver")
    public TableServiceDto reserver(@PathVariable Long id) {
        return service.reserver(id);
    }

    @PostMapping("/{id}/annuler-reservation")
    public TableServiceDto annulerReservation(@PathVariable Long id) {
        return service.annulerReservation(id);
    }
}
