package com.leconsulat.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TransfertDepotRequest(
        @NotNull(message = "Le dépôt source est obligatoire") Long depotSourceId,
        @NotNull(message = "Le dépôt destination est obligatoire") Long depotDestinationId,
        @NotNull(message = "Le produit est obligatoire") Long produitId,
        @Min(value = 1, message = "La quantité doit être positive") int quantite
) {
}
