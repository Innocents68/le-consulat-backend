package com.leconsulat.inventaire.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateInventaireRequest(
        Long etablissementId,
        @NotNull(message = "La date d'inventaire est obligatoire") LocalDate dateInventaire,
        String commentaire
) {
}
