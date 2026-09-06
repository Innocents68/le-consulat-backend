package com.leconsulat.vente.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ClotureSessionRequest(
        @NotNull(message = "Le montant réel compté en caisse est obligatoire")
        @DecimalMin(value = "0", message = "Le montant réel doit être positif")
        BigDecimal montantReel
) {
}
