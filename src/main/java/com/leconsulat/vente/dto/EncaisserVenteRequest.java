package com.leconsulat.vente.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EncaisserVenteRequest(
        @NotNull(message = "Le mode de paiement est obligatoire") String modePaiement,
        @NotNull(message = "Le montant reçu est obligatoire") @DecimalMin(value = "0", message = "Le montant reçu doit être positif") BigDecimal montantRecu,
        Long remiseId
) {
}
