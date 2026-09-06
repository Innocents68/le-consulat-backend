package com.leconsulat.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DepenseRequest(
        @NotNull(message = "La catégorie est obligatoire") Long categorieId,
        @NotNull(message = "Le montant est obligatoire") @DecimalMin(value = "0", message = "Le montant doit être positif") BigDecimal montant,
        String description,
        String fournisseur,
        LocalDate date
) {
}
