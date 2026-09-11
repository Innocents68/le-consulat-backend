package com.leconsulat.reporting.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.reporting.dto.*;
import com.leconsulat.reporting.service.ReportingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reporting")
public class ReportingController {

    private final ReportingService service;

    public ReportingController(ReportingService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public DashboardReportingDto dashboard(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                            @RequestParam(required = false) Long etablissementId) {
        return service.dashboard(dateDebut, dateFin, etablissementId);
    }

    @GetMapping("/dashboard/export")
    public ResponseEntity<byte[]> exporterDashboard(@RequestParam String format,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                                     @RequestParam(required = false) Long etablissementId) {
        return fichier(service.exporterDashboard(format, dateDebut, dateFin, etablissementId), format, "dashboard");
    }

    @GetMapping("/ventes")
    public PageResponse<VenteLigneDto> ventes(@RequestParam(required = false) Long etablissementId,
                                               @RequestParam(required = false) Long categorieId,
                                               @RequestParam(required = false) Long produitId,
                                               @RequestParam(required = false) Long utilisateurId,
                                               @RequestParam(required = false) String modePaiement,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                               @RequestParam(required = false) Integer page,
                                               @RequestParam(required = false) Integer size,
                                               @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        Page<VenteLigneDto> result = service.ventes(etablissementId, categorieId, produitId, utilisateurId, modePaiement, dateDebut, dateFin, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/ventes/resume")
    public RapportVentesResumeDto resumeVentes(@RequestParam(required = false) Long etablissementId,
                                                @RequestParam(required = false) Long categorieId,
                                                @RequestParam(required = false) Long produitId,
                                                @RequestParam(required = false) Long utilisateurId,
                                                @RequestParam(required = false) String modePaiement,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return service.resumeVentes(etablissementId, categorieId, produitId, utilisateurId, modePaiement, dateDebut, dateFin);
    }

    @GetMapping("/ventes/export")
    public ResponseEntity<byte[]> exporterVentes(@RequestParam String format,
                                                  @RequestParam(required = false) Long etablissementId,
                                                  @RequestParam(required = false) Long categorieId,
                                                  @RequestParam(required = false) Long produitId,
                                                  @RequestParam(required = false) Long utilisateurId,
                                                  @RequestParam(required = false) String modePaiement,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        byte[] fichier = service.exporterVentes(format, etablissementId, categorieId, produitId, utilisateurId, modePaiement, dateDebut, dateFin);
        return fichier(fichier, format, "rapport-ventes");
    }

    @GetMapping("/recettes")
    public RapportRecettesDto recettes(@RequestParam(required = false) Long etablissementId,
                                        @RequestParam(required = false) Long utilisateurId,
                                        @RequestParam(required = false) String modePaiement,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return service.recettes(etablissementId, utilisateurId, modePaiement, dateDebut, dateFin);
    }

    @GetMapping("/recettes/export")
    public ResponseEntity<byte[]> exporterRecettes(@RequestParam String format,
                                                    @RequestParam(required = false) Long etablissementId,
                                                    @RequestParam(required = false) Long utilisateurId,
                                                    @RequestParam(required = false) String modePaiement,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        byte[] fichier = service.exporterRecettes(format, etablissementId, utilisateurId, modePaiement, dateDebut, dateFin);
        return fichier(fichier, format, "rapport-recettes");
    }

    @GetMapping("/stocks")
    public RapportStockDto stocks(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                   @RequestParam(required = false) Long etablissementId,
                                   @RequestParam(required = false) Long categorieId) {
        return service.stocks(dateDebut, dateFin, etablissementId, categorieId);
    }

    @GetMapping("/stocks/export")
    public ResponseEntity<byte[]> exporterStocks(@RequestParam String format,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                                  @RequestParam(required = false) Long etablissementId,
                                                  @RequestParam(required = false) Long categorieId) {
        byte[] fichier = service.exporterStocks(format, dateDebut, dateFin, etablissementId, categorieId);
        return fichier(fichier, format, "rapport-stocks");
    }

    @GetMapping("/benefices")
    public RapportBeneficesDto benefices(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                          @RequestParam(required = false) Long etablissementId) {
        return service.benefices(dateDebut, dateFin, etablissementId);
    }

    @GetMapping("/benefices/export")
    public ResponseEntity<byte[]> exporterBenefices(@RequestParam String format,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                                     @RequestParam(required = false) Long etablissementId) {
        byte[] fichier = service.exporterBenefices(format, dateDebut, dateFin, etablissementId);
        return fichier(fichier, format, "rapport-benefices");
    }

    private ResponseEntity<byte[]> fichier(byte[] contenu, String format, String nomBase) {
        boolean excel = "excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format);
        MediaType type = excel ? MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") : MediaType.APPLICATION_PDF;
        String extension = excel ? "xlsx" : "pdf";
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomBase + "." + extension + "\"")
                .body(contenu);
    }
}
