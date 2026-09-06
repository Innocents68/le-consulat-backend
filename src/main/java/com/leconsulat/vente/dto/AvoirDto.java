package com.leconsulat.vente.dto;

import com.leconsulat.vente.entity.Avoir;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AvoirDto(
        Long id,
        Long venteId,
        String venteNumero,
        String motif,
        BigDecimal montant,
        String statut,
        LocalDateTime dateEmission,
        String validePar
) {
    public static AvoirDto from(Avoir a) {
        return new AvoirDto(a.getId(), a.getVente().getId(), a.getVente().getNumero(), a.getMotif(),
                a.getMontant(), a.getStatut().name(), a.getDateEmission(),
                a.getValidePar() != null ? a.getValidePar().getNom() : null);
    }
}
