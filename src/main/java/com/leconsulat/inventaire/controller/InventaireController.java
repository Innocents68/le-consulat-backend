package com.leconsulat.inventaire.controller;

import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import com.leconsulat.inventaire.dto.CloturerInventaireRequest;
import com.leconsulat.inventaire.dto.CreateInventaireRequest;
import com.leconsulat.inventaire.dto.InventaireDto;
import com.leconsulat.inventaire.dto.StockPhysiqueRequest;
import com.leconsulat.inventaire.service.InventaireService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/inventaires")
public class InventaireController {

    private final InventaireService service;

    public InventaireController(InventaireService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<InventaireDto> search(@RequestParam(required = false) Long etablissementId,
                                               @RequestParam(required = false) String statut,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                               @RequestParam(required = false) Integer page,
                                               @RequestParam(required = false) Integer size,
                                               @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        Page<InventaireDto> result = service.search(etablissementId, statut, dateDebut, dateFin, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public InventaireDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventaireDto create(@Valid @RequestBody CreateInventaireRequest req) {
        return service.create(req);
    }

    @PostMapping("/{id}/demarrer-comptage")
    public InventaireDto demarrerComptage(@PathVariable Long id) {
        return service.demarrerComptage(id);
    }

    @PatchMapping("/{id}/lignes/{ligneId}")
    public InventaireDto saisirComptage(@PathVariable Long id, @PathVariable Long ligneId, @Valid @RequestBody StockPhysiqueRequest req) {
        return service.saisirComptage(id, ligneId, req);
    }

    @PostMapping("/{id}/valider-lignes-vides")
    public InventaireDto validerLignesVides(@PathVariable Long id) {
        return service.validerLignesVides(id);
    }

    @PostMapping("/{id}/cloturer")
    public InventaireDto cloturer(@PathVariable Long id, @RequestBody(required = false) CloturerInventaireRequest req) {
        return service.cloturer(id, req != null ? req : new CloturerInventaireRequest(null));
    }

    @PostMapping("/{id}/valider")
    public InventaireDto valider(@PathVariable Long id) {
        return service.valider(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }

    /** Cahier_des_charges_amelioration_inventaire_Le_Consulat.docx — export du détail (théorique,
     * physique, écart, état) en PDF ou Excel. */
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exporter(@PathVariable Long id, @RequestParam String format) {
        byte[] fichier = service.exporter(id, format);
        boolean excel = "excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format);
        MediaType type = excel ? MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") : MediaType.APPLICATION_PDF;
        String extension = excel ? "xlsx" : "pdf";
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inventaire." + extension + "\"")
                .body(fichier);
    }
}
