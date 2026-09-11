package com.leconsulat.stock.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.stock.dto.EntreeStockRequest;
import com.leconsulat.stock.dto.MouvementStockDto;
import com.leconsulat.stock.dto.SortieStockRequest;
import com.leconsulat.stock.dto.TransfertStockRequest;
import com.leconsulat.stock.service.MouvementStockService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/mouvements-stock")
public class MouvementStockController {

    private final MouvementStockService service;

    public MouvementStockController(MouvementStockService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<MouvementStockDto> search(@RequestParam(required = false) Long etablissementId,
                                                   @RequestParam(required = false) Long produitId,
                                                   @RequestParam(required = false) String type,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin,
                                                   @RequestParam(required = false) Integer page,
                                                   @RequestParam(required = false) Integer size,
                                                   @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        Page<MouvementStockDto> result = service.search(etablissementId, produitId, type, dateDebut, dateFin, pageable);
        return PageResponse.ofDto(result);
    }

    @PostMapping("/entrees")
    @ResponseStatus(HttpStatus.CREATED)
    public MouvementStockDto entree(@Valid @RequestBody EntreeStockRequest req) {
        return service.entree(req);
    }

    @PostMapping("/sorties")
    @ResponseStatus(HttpStatus.CREATED)
    public MouvementStockDto sortie(@Valid @RequestBody SortieStockRequest req) {
        return service.sortie(req);
    }

    @PostMapping("/transferts")
    @ResponseStatus(HttpStatus.CREATED)
    public void transfert(@Valid @RequestBody TransfertStockRequest req) {
        service.transfert(req);
    }
}
