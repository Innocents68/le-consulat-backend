package com.leconsulat.finance.controller;

import com.leconsulat.common.util.PdfGenerator;
import com.leconsulat.finance.dto.RapportCaisseDto;
import com.leconsulat.finance.entity.RapportCaisse;
import com.leconsulat.finance.service.RapportCaisseService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rapports-caisse")
public class RapportCaisseController {

    private final RapportCaisseService service;

    public RapportCaisseController(RapportCaisseService service) {
        this.service = service;
    }

    @GetMapping
    public List<RapportCaisseDto> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public RapportCaisseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        RapportCaisse r = service.findEntity(id);
        byte[] pdf = PdfGenerator.simpleDocument(
                "Rapport de caisse (Z) #" + r.getId(),
                List.of(
                        new String[]{"Session de caisse", String.valueOf(r.getSessionCaisseId())},
                        new String[]{"Date de génération", String.valueOf(r.getDateGeneration())}
                ),
                new String[]{"Libellé", "Montant (FCFA)"},
                List.of(
                        new String[]{"Encaissements", String.valueOf(r.getEncaissements())},
                        new String[]{"Décaissements", String.valueOf(r.getDecaissements())},
                        new String[]{"Solde", String.valueOf(r.getSolde())}
                )
        );
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rapport-caisse-" + id + ".pdf")
                .body(pdf);
    }
}
