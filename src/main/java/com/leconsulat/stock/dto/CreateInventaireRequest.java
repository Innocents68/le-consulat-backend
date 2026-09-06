package com.leconsulat.stock.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateInventaireRequest(
        @NotNull(message = "Le type d'inventaire est obligatoire") String type,
        /** Optional: restrict a PARTIEL inventory to these product ids. Null/empty = all active products. */
        List<Long> produitIds
) {
}
