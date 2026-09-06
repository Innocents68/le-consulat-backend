package com.leconsulat.restaurant.controller;

import com.leconsulat.restaurant.dto.TableRestaurantDto;
import com.leconsulat.restaurant.service.TableRestaurantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tables")
public class TableController {

    private final TableRestaurantService service;

    public TableController(TableRestaurantService service) {
        this.service = service;
    }

    @GetMapping
    public List<TableRestaurantDto> list() {
        return service.list();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    @ResponseStatus(HttpStatus.CREATED)
    public TableRestaurantDto create(@Valid @RequestBody TableRestaurantDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CAISSIER_SERVEUR')")
    public TableRestaurantDto update(@PathVariable Long id, @Valid @RequestBody TableRestaurantDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
