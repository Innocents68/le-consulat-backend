package com.leconsulat.cave.controller;

import com.leconsulat.cave.dto.BoissonDto;
import com.leconsulat.cave.dto.BoissonRequest;
import com.leconsulat.cave.service.BoissonService;
import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/boissons")
public class BoissonController {

    private final BoissonService service;

    public BoissonController(BoissonService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<BoissonDto> list(@RequestParam(required = false) String search,
                                          @RequestParam(required = false) String type,
                                          @RequestParam(required = false) Boolean actif,
                                          @RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) Integer size,
                                          @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        Page<BoissonDto> result = service.search(search, type, actif, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public BoissonDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public BoissonDto create(@Valid @RequestBody BoissonRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public BoissonDto update(@PathVariable Long id, @Valid @RequestBody BoissonRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public void archive(@PathVariable Long id) {
        service.archive(id);
    }
}
