package com.leconsulat.vente.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OuvrirSessionRequest(
        @NotBlank(message = "Le nom de la caisse est obligatoire") String caisseNom,
        @NotNull(message = "Le fond de caisse initial est obligatoire") @DecimalMin(value = "0", message = "Le fond initial doit être positif") BigDecimal fondInitial
) {
}
