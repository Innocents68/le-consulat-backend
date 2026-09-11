package com.leconsulat.catalogue.dto;

import com.leconsulat.catalogue.entity.HistoriquePrix;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoriquePrixDto(
        Long id,
        BigDecimal ancienPrix,
        BigDecimal nouveauPrix,
        LocalDateTime dateEffet,
        String auteurNom
) {
    public static HistoriquePrixDto from(HistoriquePrix h) {
        return new HistoriquePrixDto(h.getId(), h.getAncienPrix(), h.getNouveauPrix(), h.getDateEffet(),
                h.getAuteur() != null ? h.getAuteur().getNom() : null);
    }
}
