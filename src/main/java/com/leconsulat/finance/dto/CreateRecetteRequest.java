package com.leconsulat.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateRecetteRequest(
        @NotNull(message = "Le montant est obligatoire") @DecimalMin(value = "0", message = "Le montant doit être positif") BigDecimal montant,
        String description
) {
}
