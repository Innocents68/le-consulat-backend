package com.leconsulat.reporting;

import com.leconsulat.common.exception.BadRequestException;
import com.leconsulat.common.util.ExcelGenerator;
import com.leconsulat.common.util.PdfGenerator;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reporting")
public class ReportingController {

    private final ReportingService service;

    public ReportingController(ReportingService service) {
        this.service = service;
    }

    @GetMapping("/ventes")
    public RapportVentesDto ventes(@RequestParam(required = false) String periode,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return service.rapportVentes(periode, dateDebut, dateFin);
    }

    @GetMapping("/ventes/export")
    public ResponseEntity<byte[]> exportVentes(@RequestParam(defaultValue = "pdf") String format,
                                                @RequestParam(required = false) String periode,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        RapportVentesDto rapport = service.rapportVentes(periode, dateDebut, dateFin);
        String[] headers = {"Date", "Montant", "Nombre de tickets"};
        List<String[]> rows = new ArrayList<>();
        for (RapportVentesDto.VentesParJour v : rapport.ventesParJour()) {
            rows.add(new String[]{String.valueOf(v.date()), String.valueOf(v.montant()), String.valueOf(v.nombreTickets())});
        }
        return export(format, "rapport-ventes", "Rapport des ventes du " + rapport.dateDebut() + " au " + rapport.dateFin(),
                List.of(new String[]{"Chiffre d'affaires", String.valueOf(rapport.chiffreAffaires())},
                        new String[]{"Quantité vendue", String.valueOf(rapport.quantiteVendue())},
                        new String[]{"Panier moyen", String.valueOf(rapport.panierMoyen())}),
                headers, rows);
    }

    @GetMapping("/stocks")
    public RapportStocksDto stocks() {
        return service.rapportStocks();
    }

    @GetMapping("/stocks/export")
    public ResponseEntity<byte[]> exportStocks(@RequestParam(defaultValue = "pdf") String format) {
        RapportStocksDto rapport = service.rapportStocks();
        String[] headers = {"Catégorie", "Valorisation (FCFA)"};
        List<String[]> rows = new ArrayList<>();
        for (RapportStocksDto.RepartitionCategorie c : rapport.repartitionParCategorie()) {
            rows.add(new String[]{c.categorie(), String.valueOf(c.valorisation())});
        }
        return export(format, "rapport-stocks", "Rapport des stocks",
                List.of(new String[]{"Valorisation totale", String.valueOf(rapport.valorisationTotale())},
                        new String[]{"Nombre de ruptures", String.valueOf(rapport.nombreRuptures())}),
                headers, rows);
    }

    @GetMapping("/recettes-depenses")
    public RapportRecettesDepensesDto recettesDepenses(@RequestParam(required = false) String periode,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return service.rapportRecettesDepenses(periode, dateDebut, dateFin);
    }

    @GetMapping("/recettes-depenses/export")
    public ResponseEntity<byte[]> exportRecettesDepenses(@RequestParam(defaultValue = "pdf") String format,
                                                          @RequestParam(required = false) String periode,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        RapportRecettesDepensesDto rapport = service.rapportRecettesDepenses(periode, dateDebut, dateFin);
        String[] headers = {"Date", "Recettes", "Dépenses"};
        List<String[]> rows = new ArrayList<>();
        for (RapportRecettesDepensesDto.FluxParJour f : rapport.fluxParJour()) {
            rows.add(new String[]{String.valueOf(f.date()), String.valueOf(f.recettes()), String.valueOf(f.depenses())});
        }
        return export(format, "rapport-recettes-depenses", "Rapport recettes / dépenses du " + rapport.dateDebut() + " au " + rapport.dateFin(),
                List.of(new String[]{"Total recettes", String.valueOf(rapport.totalRecettes())},
                        new String[]{"Total dépenses", String.valueOf(rapport.totalDepenses())},
                        new String[]{"Marge", String.valueOf(rapport.marge())}),
                headers, rows);
    }

    @GetMapping("/benefices")
    public RapportBeneficesDto benefices(@RequestParam(required = false) String periode,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return service.rapportBenefices(periode, dateDebut, dateFin);
    }

    @GetMapping("/benefices/export")
    public ResponseEntity<byte[]> exportBenefices(@RequestParam(defaultValue = "pdf") String format,
                                                   @RequestParam(required = false) String periode,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        RapportBeneficesDto rapport = service.rapportBenefices(periode, dateDebut, dateFin);
        String[] headers = {"Date", "Recettes", "Dépenses", "Bénéfice"};
        List<String[]> rows = new ArrayList<>();
        for (RapportBeneficesDto.BeneficeParJour b : rapport.evolution()) {
            rows.add(new String[]{String.valueOf(b.date()), String.valueOf(b.recettes()), String.valueOf(b.depenses()), String.valueOf(b.benefice())});
        }
        return export(format, "rapport-benefices", "Rapport des bénéfices du " + rapport.dateDebut() + " au " + rapport.dateFin(),
                List.<String[]>of(new String[]{"Bénéfice net total", String.valueOf(rapport.beneficeNetTotal())}),
                headers, rows);
    }

    private ResponseEntity<byte[]> export(String format, String filenameBase, String title, List<String[]> infos, String[] headers, List<String[]> rows) {
        byte[] content;
        MediaType mediaType;
        String extension;
        if ("excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format)) {
            content = ExcelGenerator.simpleSheet(filenameBase, headers, rows);
            mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            extension = "xlsx";
        } else if ("pdf".equalsIgnoreCase(format)) {
            content = PdfGenerator.simpleDocument(title, infos, headers, rows);
            mediaType = MediaType.APPLICATION_PDF;
            extension = "pdf";
        } else {
            throw new BadRequestException("Format d'export inconnu (attendu : pdf ou excel) : " + format);
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filenameBase + "." + extension)
                .body(content);
    }
}
