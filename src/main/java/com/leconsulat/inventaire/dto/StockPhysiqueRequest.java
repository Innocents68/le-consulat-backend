package com.leconsulat.inventaire.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockPhysiqueRequest(
        @NotNull(message = "Le stock physique est obligatoire") @DecimalMin(value = "0", message = "Le stock physique ne peut pas être négatif") BigDecimal stockPhysique
) {
}
