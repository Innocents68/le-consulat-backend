package com.leconsulat.financier.controller;

import com.leconsulat.financier.dto.SyntheseFinanciereDto;
import com.leconsulat.financier.service.FinancierService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/** EF-031 : accessible exclusivement au Super Administrateur — n'apparaît dans le menu d'aucun
 * autre profil et est refusé côté backend, contrairement au module Dépenses. */
@RestController
@RequestMapping("/api/v1/financier")
@PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
public class FinancierController {

    private final FinancierService service;

    public FinancierController(FinancierService service) {
        this.service = service;
    }

    @GetMapping("/synthese")
    public SyntheseFinanciereDto synthese(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                                           @RequestParam(required = false) Long etablissementId,
                                           @RequestParam(required = false) Long utilisateurId,
                                           @RequestParam(required = false) String modePaiement) {
        return service.synthese(dateDebut, dateFin, etablissementId, utilisateurId, modePaiement);
    }
}
