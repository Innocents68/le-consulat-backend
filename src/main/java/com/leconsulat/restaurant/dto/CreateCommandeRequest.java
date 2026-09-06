package com.leconsulat.restaurant.dto;

import jakarta.validation.constraints.NotNull;

public record CreateCommandeRequest(
        Long tableId,
        @NotNull(message = "Le type de commande est obligatoire") String type
) {
}
