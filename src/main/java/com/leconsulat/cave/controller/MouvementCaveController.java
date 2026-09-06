package com.leconsulat.cave.controller;

import com.leconsulat.cave.dto.CreateMouvementCaveRequest;
import com.leconsulat.cave.dto.MouvementCaveDto;
import com.leconsulat.cave.service.MouvementCaveService;
import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/mouvements-cave")
public class MouvementCaveController {

    private final MouvementCaveService service;

    public MouvementCaveController(MouvementCaveService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<MouvementCaveDto> list(@RequestParam(required = false) Long boissonId,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                                @RequestParam(required = false) Integer page,
                                                @RequestParam(required = false) Integer size,
                                                @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        LocalDateTime debutDt = dateDebut != null ? dateDebut.atStartOfDay() : null;
        LocalDateTime finDt = dateFin != null ? dateFin.atTime(LocalTime.MAX) : null;
        Page<MouvementCaveDto> result = service.search(boissonId, debutDt, finDt, pageable);
        return PageResponse.ofDto(result);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public MouvementCaveDto create(@Valid @RequestBody CreateMouvementCaveRequest req) {
        return service.create(req);
    }
}
