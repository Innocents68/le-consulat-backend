package com.leconsulat.stock.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.stock.dto.EntreeStockDto;
import com.leconsulat.stock.dto.EntreeStockRequest;
import com.leconsulat.stock.service.EntreeStockService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/entrees-stock")
public class EntreeStockController {

    private final EntreeStockService service;

    public EntreeStockController(EntreeStockService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<EntreeStockDto> list(@RequestParam(required = false) Long produitId,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer size,
                                              @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        Page<EntreeStockDto> result = service.search(produitId, dateDebut, dateFin, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public EntreeStockDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public EntreeStockDto create(@Valid @RequestBody EntreeStockRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public EntreeStockDto update(@PathVariable Long id, @Valid @RequestBody EntreeStockRequest req) {
        return service.update(id, req);
    }

    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public EntreeStockDto valider(@PathVariable Long id) {
        return service.valider(id);
    }
}
