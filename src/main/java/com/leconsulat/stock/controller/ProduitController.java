package com.leconsulat.stock.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.stock.dto.ProduitDto;
import com.leconsulat.stock.dto.ProduitRequest;
import com.leconsulat.stock.service.ProduitService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/produits")
public class ProduitController {

    private final ProduitService service;

    public ProduitController(ProduitService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ProduitDto> list(@RequestParam(required = false) String search,
                                          @RequestParam(required = false) Long categorieId,
                                          @RequestParam(required = false) Boolean actif,
                                          @RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) Integer size,
                                          @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        Page<ProduitDto> result = service.search(search, categorieId, actif, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public ProduitDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProduitDto create(@Valid @RequestBody ProduitRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ProduitDto update(@PathVariable Long id, @Valid @RequestBody ProduitRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public void archive(@PathVariable Long id) {
        service.archive(id);
    }
}
