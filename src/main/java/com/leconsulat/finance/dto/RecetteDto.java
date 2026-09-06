package com.leconsulat.finance.dto;

import com.leconsulat.finance.entity.Recette;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecetteDto(
        Long id,
        String source,
        BigDecimal montant,
        String description,
        LocalDateTime date
) {
    public static RecetteDto from(Recette r) {
        return new RecetteDto(r.getId(), r.getSource().name(), r.getMontant(), r.getDescription(), r.getDate());
    }
}
