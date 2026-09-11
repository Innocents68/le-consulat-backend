package com.leconsulat.remise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateRemiseRequest(
        @NotBlank(message = "Le libellé est obligatoire") String libelle,
        @NotNull(message = "Le type est obligatoire") String type,
        @NotNull(message = "La valeur est obligatoire") @Positive(message = "La valeur doit être supérieure à zéro") BigDecimal valeur,
        LocalDate dateDebut,
        LocalDate dateFin,
        LocalTime heureDebut,
        LocalTime heureFin,
        /** Ignoré pour un Gérant/Caissier (RG-014/RG-015), obligatoire pour le Super Administrateur. */
        Long etablissementId
) {
}
