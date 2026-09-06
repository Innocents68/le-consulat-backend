package com.leconsulat.vente.dto;

import com.leconsulat.vente.entity.Remise;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RemiseDto(
        Long id,
        @NotBlank(message = "Le nom est obligatoire") String nom,
        @NotNull(message = "Le type est obligatoire") String type,
        @NotNull(message = "La valeur est obligatoire") BigDecimal valeur,
        LocalDate dateDebut,
        LocalDate dateFin,
        boolean actif
) {
    public static RemiseDto from(Remise r) {
        return new RemiseDto(r.getId(), r.getNom(), r.getType().name(), r.getValeur(), r.getDateDebut(), r.getDateFin(), r.isActif());
    }
}
