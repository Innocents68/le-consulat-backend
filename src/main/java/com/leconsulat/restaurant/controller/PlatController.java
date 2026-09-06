package com.leconsulat.restaurant.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.restaurant.dto.PlatDto;
import com.leconsulat.restaurant.dto.PlatRequest;
import com.leconsulat.restaurant.service.PlatService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/plats")
public class PlatController {

    private final PlatService service;

    public PlatController(PlatService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<PlatDto> list(@RequestParam(required = false) Long categorieId,
                                       @RequestParam(required = false) Boolean actif,
                                       @RequestParam(required = false) Boolean disponible,
                                       @RequestParam(required = false) String search,
                                       @RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer size,
                                       @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        Page<PlatDto> result = service.search(categorieId, actif, disponible, search, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public PlatDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public PlatDto create(@Valid @RequestBody PlatRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public PlatDto update(@PathVariable Long id, @Valid @RequestBody PlatRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public void archive(@PathVariable Long id) {
        service.archive(id);
    }
}
