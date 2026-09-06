package com.leconsulat.stock.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EntreeStockRequest(
        @NotNull(message = "Le produit est obligatoire") Long produitId,
        String fournisseur,
        @Min(value = 1, message = "La quantité doit être positive") int quantite,
        @NotNull(message = "Le prix unitaire est obligatoire") @DecimalMin(value = "0", message = "Le prix unitaire doit être positif") BigDecimal prixUnitaire,
        LocalDate dateEntree,
        String bonLivraison
) {
}
