package com.leconsulat.restaurant.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record PlatRequest(
        @NotBlank(message = "Le nom est obligatoire") String nom,
        @NotNull(message = "Le prix est obligatoire") @DecimalMin(value = "0", message = "Le prix doit être positif") BigDecimal prix,
        Long categorieId,
        String description,
        Integer tempsPreparation,
        Boolean disponible,
        List<String> ingredients
) {
}
