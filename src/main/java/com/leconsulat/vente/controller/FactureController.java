package com.leconsulat.vente.controller;

import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.util.PdfGenerator;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.vente.dto.LigneVenteDto;
import com.leconsulat.vente.dto.VenteDto;
import com.leconsulat.vente.entity.StatutVente;
import com.leconsulat.vente.service.VenteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Read-only view of paid Ventes, presented as invoices — API_CONTRACT.md §3. */
@RestController
@RequestMapping("/api/v1/factures")
public class FactureController {

    private final VenteService venteService;

    public FactureController(VenteService venteService) {
        this.venteService = venteService;
    }

    @GetMapping
    public PageResponse<VenteDto> list(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size,
                                        @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        LocalDateTime debutDt = dateDebut != null ? dateDebut.atStartOfDay() : null;
        LocalDateTime finDt = dateFin != null ? dateFin.atTime(LocalTime.MAX) : null;
        Page<VenteDto> result = venteService.search(StatutVente.PAYEE, null, null, debutDt, finDt, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public VenteDto get(@PathVariable Long id) {
        VenteDto dto = venteService.get(id);
        if (!"PAYEE".equals(dto.statut())) {
            throw new BusinessRuleException("Cette vente n'est pas encore payée, elle n'a pas de facture");
        }
        return dto;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        VenteDto v = get(id);

        List<String[]> lignesInfo = new ArrayList<>();
        lignesInfo.add(new String[]{"Numéro de facture", v.numero()});
        lignesInfo.add(new String[]{"Date", String.valueOf(v.dateVente())});
        lignesInfo.add(new String[]{"Client", v.clientNom() != null ? v.clientNom() : "Client de passage"});
        lignesInfo.add(new String[]{"Caissier", v.caissierNom() != null ? v.caissierNom() : "-"});
        lignesInfo.add(new String[]{"Mode de paiement", v.modePaiement()});

        List<String[]> rows = new ArrayList<>();
        for (LigneVenteDto ligne : v.lignes()) {
            rows.add(new String[]{ligne.produitNom(), String.valueOf(ligne.quantite()),
                    String.valueOf(ligne.prixUnitaire()), String.valueOf(ligne.montant())});
        }
        rows.add(new String[]{"", "", "Sous-total", String.valueOf(v.sousTotal())});
        rows.add(new String[]{"", "", "Remise", String.valueOf(v.remiseMontant())});
        rows.add(new String[]{"", "", "TOTAL", String.valueOf(v.total())});

        byte[] pdf = PdfGenerator.simpleDocument("Facture " + v.numero(), lignesInfo,
                new String[]{"Article", "Qté", "P.U.", "Montant"}, rows);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=facture-" + v.numero() + ".pdf")
                .body(pdf);
    }
}
