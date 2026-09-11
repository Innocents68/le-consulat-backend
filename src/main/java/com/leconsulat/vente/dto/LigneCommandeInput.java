package com.leconsulat.vente.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LigneCommandeInput(
        @NotNull(message = "Le produit est obligatoire") Long produitId,
        @Min(value = 1, message = "La quantité doit être supérieure à zéro") int quantite
) {
}
