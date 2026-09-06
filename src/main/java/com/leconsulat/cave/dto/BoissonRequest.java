package com.leconsulat.cave.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BoissonRequest(
        @NotBlank(message = "Le nom est obligatoire") String nom,
        @NotNull(message = "Le type est obligatoire") String type,
        String origine,
        Integer millesime,
        @NotNull(message = "Le prix d'achat est obligatoire") @DecimalMin(value = "0") BigDecimal prixAchatBouteille,
        @NotNull(message = "Le prix de vente à la bouteille est obligatoire") @DecimalMin(value = "0") BigDecimal prixVenteBouteille,
        @DecimalMin(value = "0") BigDecimal prixVenteVerre,
        Long fournisseurId,
        BigDecimal quantiteStock,
        Integer seuilAlerte
) {
}
