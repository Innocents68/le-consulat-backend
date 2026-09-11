package com.leconsulat.depense.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.depense.dto.AnnulerDepenseRequest;
import com.leconsulat.depense.dto.CreateDepenseRequest;
import com.leconsulat.depense.dto.DepenseDto;
import com.leconsulat.depense.dto.UpdateDepenseRequest;
import com.leconsulat.depense.service.DepenseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/depenses")
public class DepenseController {

    private final DepenseService service;

    public DepenseController(DepenseService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<DepenseDto> search(@RequestParam(required = false) Long etablissementId,
                                            @RequestParam(required = false) Long categorieId,
                                            @RequestParam(required = false) String modePaiement,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer size,
                                            @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        Page<DepenseDto> result = service.search(etablissementId, categorieId, modePaiement, dateDebut, dateFin, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public DepenseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepenseDto create(@Valid @RequestBody CreateDepenseRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public DepenseDto update(@PathVariable Long id, @Valid @RequestBody UpdateDepenseRequest req) {
        return service.update(id, req);
    }

    @PostMapping("/{id}/annuler")
    public DepenseDto annuler(@PathVariable Long id, @Valid @RequestBody AnnulerDepenseRequest req) {
        return service.annuler(id, req);
    }
}
