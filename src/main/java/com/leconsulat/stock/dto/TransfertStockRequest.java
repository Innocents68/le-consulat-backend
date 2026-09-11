package com.leconsulat.stock.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransfertStockRequest(
        @NotNull(message = "Le produit source est obligatoire") Long produitSourceId,
        @NotNull(message = "Le produit destination est obligatoire") Long produitDestinationId,
        @NotNull(message = "La quantité est obligatoire") @DecimalMin(value = "0.001", message = "La quantité doit être positive") BigDecimal quantite
) {
}
