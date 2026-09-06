package com.leconsulat.finance.dto;

import com.leconsulat.finance.entity.Depense;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DepenseDto(
        Long id,
        Long categorieId,
        String categorieNom,
        BigDecimal montant,
        String description,
        String fournisseur,
        String justificatifUrl,
        String statut,
        LocalDate date
) {
    public static DepenseDto from(Depense d) {
        return new DepenseDto(d.getId(), d.getCategorie().getId(), d.getCategorie().getNom(), d.getMontant(),
                d.getDescription(), d.getFournisseur(), d.getJustificatifUrl(), d.getStatut().name(), d.getDate());
    }
}
