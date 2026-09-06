package com.leconsulat.vente.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AddLigneVenteRequest(
        @NotNull(message = "Le produit est obligatoire") Long produitId,
        @Min(value = 1, message = "La quantité doit être positive") int quantite,
        @DecimalMin(value = "0", message = "La remise ne peut être négative") BigDecimal remise
) {
}
