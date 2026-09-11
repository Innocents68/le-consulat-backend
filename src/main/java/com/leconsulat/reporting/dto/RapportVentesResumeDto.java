package com.leconsulat.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Porte sur l'ensemble filtré, pas seulement la page affichée par {@code GET /reporting/ventes}. */
public record RapportVentesResumeDto(
        LocalDate dateDebut,
        LocalDate dateFin,
        long totalQuantite,
        BigDecimal totalMontant,
        BigDecimal panierMoyen,
        String genereParNom,
        LocalDateTime dateGeneration
) {
}
