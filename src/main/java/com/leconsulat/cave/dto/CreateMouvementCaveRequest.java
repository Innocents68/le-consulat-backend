package com.leconsulat.cave.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateMouvementCaveRequest(
        @NotNull(message = "La boisson est obligatoire") Long boissonId,
        @NotNull(message = "Le type de mouvement est obligatoire") String type,
        @NotNull(message = "Le motif est obligatoire") String motif,
        @NotNull(message = "La quantité est obligatoire") @DecimalMin(value = "0.01", message = "La quantité doit être positive") BigDecimal quantite,
        /** BOUTEILLE (default) or VERRE — only meaningful for a SORTIE/VENTE movement. */
        String uniteVente
) {
}
