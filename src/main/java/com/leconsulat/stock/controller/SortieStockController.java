package com.leconsulat.stock.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.stock.dto.SortieStockDto;
import com.leconsulat.stock.dto.SortieStockRequest;
import com.leconsulat.stock.service.SortieStockService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/sorties-stock")
public class SortieStockController {

    private final SortieStockService service;

    public SortieStockController(SortieStockService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<SortieStockDto> list(@RequestParam(required = false) Long produitId,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer size,
                                              @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        Page<SortieStockDto> result = service.search(produitId, dateDebut, dateFin, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public SortieStockDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT','CUISINIER')")
    @ResponseStatus(HttpStatus.CREATED)
    public SortieStockDto create(@Valid @RequestBody SortieStockRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public SortieStockDto update(@PathVariable Long id, @Valid @RequestBody SortieStockRequest req) {
        return service.update(id, req);
    }

    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public SortieStockDto valider(@PathVariable Long id) {
        return service.valider(id);
    }
}
