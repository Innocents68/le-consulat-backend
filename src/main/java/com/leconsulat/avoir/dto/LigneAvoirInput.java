package com.leconsulat.avoir.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LigneAvoirInput(
        @NotNull(message = "La ligne de commande d'origine est obligatoire") Long ligneCommandeId,
        @Min(value = 1, message = "La quantité doit être supérieure à zéro") int quantite
) {
}
