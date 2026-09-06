package com.leconsulat.finance.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.finance.dto.DepenseDto;
import com.leconsulat.finance.dto.DepenseRequest;
import com.leconsulat.finance.service.DepenseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/depenses")
public class DepenseController {

    private final DepenseService service;

    public DepenseController(DepenseService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<DepenseDto> list(@RequestParam(required = false) Long categorieId,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                          @RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) Integer size,
                                          @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        Page<DepenseDto> result = service.search(categorieId, dateDebut, dateFin, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public DepenseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public DepenseDto create(@Valid @RequestBody DepenseRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public DepenseDto update(@PathVariable Long id, @Valid @RequestBody DepenseRequest req) {
        return service.update(id, req);
    }

    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public DepenseDto valider(@PathVariable Long id) {
        return service.valider(id);
    }

    @PostMapping(value = "/{id}/justificatif", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public DepenseDto uploadJustificatif(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return service.uploadJustificatif(id, file);
    }
}
