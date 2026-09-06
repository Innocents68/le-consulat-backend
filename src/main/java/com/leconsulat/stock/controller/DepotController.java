package com.leconsulat.stock.controller;

import com.leconsulat.stock.dto.DepotDto;
import com.leconsulat.stock.dto.TransfertDepotRequest;
import com.leconsulat.stock.service.DepotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/depots")
public class DepotController {

    private final DepotService service;

    public DepotController(DepotService service) {
        this.service = service;
    }

    @GetMapping
    public List<DepotDto> list() {
        return service.list();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public DepotDto create(@Valid @RequestBody DepotDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public DepotDto update(@PathVariable Long id, @Valid @RequestBody DepotDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public void archive(@PathVariable Long id) {
        service.archive(id);
    }

    @PostMapping("/transferts")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public void transferer(@Valid @RequestBody TransfertDepotRequest req) {
        service.transferer(req);
    }
}
