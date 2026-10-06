package com.leconsulat.stock.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.stock.dto.AccepterDemandeTransfertRequest;
import com.leconsulat.stock.dto.CreerDemandeTransfertRequest;
import com.leconsulat.stock.dto.DemandeTransfertDto;
import com.leconsulat.stock.dto.RefuserDemandeTransfertRequest;
import com.leconsulat.stock.service.DemandeTransfertService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/demandes-transfert")
public class DemandeTransfertController {

    private final DemandeTransfertService service;

    public DemandeTransfertController(DemandeTransfertService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemandeTransfertDto creer(@Valid @RequestBody CreerDemandeTransfertRequest req) {
        return service.creer(req);
    }

    @GetMapping("/recues")
    public PageResponse<DemandeTransfertDto> recues(@RequestParam(required = false) Long etablissementId,
                                                      @RequestParam(required = false) String statut,
                                                      @RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer size,
                                                      @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        return PageResponse.ofDto(service.recues(etablissementId, statut, pageable));
    }

    @GetMapping("/emises")
    public PageResponse<DemandeTransfertDto> emises(@RequestParam(required = false) Long etablissementId,
                                                      @RequestParam(required = false) String statut,
                                                      @RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer size,
                                                      @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        return PageResponse.ofDto(service.emises(etablissementId, statut, pageable));
    }

    @GetMapping("/compte-en-attente")
    public Map<String, Long> compteEnAttente(@RequestParam(required = false) Long etablissementId) {
        return Map.of("count", service.compteEnAttente(etablissementId));
    }

    @PostMapping("/{id}/accepter")
    public DemandeTransfertDto accepter(@PathVariable Long id, @Valid @RequestBody AccepterDemandeTransfertRequest req) {
        return service.accepter(id, req);
    }

    @PostMapping("/{id}/refuser")
    public DemandeTransfertDto refuser(@PathVariable Long id, @RequestBody(required = false) RefuserDemandeTransfertRequest req) {
        return service.refuser(id, req != null ? req : new RefuserDemandeTransfertRequest(null));
    }
}
