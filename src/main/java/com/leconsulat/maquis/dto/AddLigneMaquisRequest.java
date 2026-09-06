package com.leconsulat.maquis.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AddLigneMaquisRequest(
        @NotBlank(message = "Le nom de l'article est obligatoire") String articleNom,
        @Min(value = 1, message = "La quantité doit être positive") int quantite,
        @NotNull(message = "Le prix unitaire est obligatoire") @DecimalMin(value = "0") BigDecimal prixUnitaire
) {
}
