package com.leconsulat.vente.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/** RG-047 : mode de paiement obligatoire. montantRecu n'est exigé (et vérifié, RG-048) que
 * pour un paiement en espèces. */
public record EncaisserRequest(
        @NotBlank(message = "Le mode de paiement est obligatoire") String modePaiement,
        BigDecimal montantRecu
) {
}
