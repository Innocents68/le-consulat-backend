package com.leconsulat.stock.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EntreeStockRequest(
        @NotNull(message = "Le produit est obligatoire") Long produitId,
        @NotNull(message = "La quantité est obligatoire") @DecimalMin(value = "0.001", message = "La quantité doit être positive") BigDecimal quantite,
        BigDecimal prixUnitaire
) {
}
