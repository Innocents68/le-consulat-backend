package com.leconsulat.restaurant.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddLigneCommandeRequest(
        @NotNull(message = "Le plat est obligatoire") Long platId,
        @Min(value = 1, message = "La quantité doit être positive") int quantite,
        String options
) {
}
