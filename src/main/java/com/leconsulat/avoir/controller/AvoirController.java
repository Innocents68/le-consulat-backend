package com.leconsulat.avoir.controller;

import com.leconsulat.avoir.dto.AvoirDto;
import com.leconsulat.avoir.dto.CreateAvoirRequest;
import com.leconsulat.avoir.service.AvoirService;
import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/avoirs")
public class AvoirController {

    private final AvoirService service;

    public AvoirController(AvoirService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<AvoirDto> search(@RequestParam(required = false) Long etablissementId,
                                          @RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) Integer size,
                                          @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        Page<AvoirDto> result = service.search(etablissementId, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public AvoirDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/par-facture/{factureId}")
    public List<AvoirDto> parFacture(@PathVariable Long factureId) {
        return service.parFacture(factureId);
    }

    /** Recommandations et corrections.md §4 : consultation du solde d'un avoir par son numéro,
     * avant de l'appliquer à un encaissement. */
    @GetMapping("/numero/{numero}")
    public AvoirDto parNumero(@PathVariable String numero, @RequestParam(required = false) Long etablissementId) {
        return service.rechercherParNumero(numero, etablissementId);
    }

    @PostMapping("/factures/{factureId}")
    @ResponseStatus(HttpStatus.CREATED)
    public AvoirDto create(@PathVariable Long factureId, @Valid @RequestBody CreateAvoirRequest req) {
        return service.create(factureId, req);
    }

    @GetMapping("/{id}/avoir.pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        AvoirDto dto = service.get(id);
        byte[] pdf = service.genererPdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + dto.numero() + ".pdf\"")
                .body(pdf);
    }
}
