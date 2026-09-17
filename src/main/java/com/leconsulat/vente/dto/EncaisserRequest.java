package com.leconsulat.vente.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/** RG-047 : mode de paiement obligatoire. montantRecu n'est exigé (et vérifié, RG-048) que
 * pour un paiement en espèces.
 * Recommandations et corrections.md §4 : {@code avoirNumero} déduit le solde d'un avoir existant
 * du montant net à payer ; {@code montantConvertiEnAvoir} (espèces uniquement) transforme tout ou
 * partie de la monnaie à rendre en un nouvel avoir quand la caisse ne peut pas la rendre. */
public record EncaisserRequest(
        @NotBlank(message = "Le mode de paiement est obligatoire") String modePaiement,
        BigDecimal montantRecu,
        String avoirNumero,
        BigDecimal montantConvertiEnAvoir
) {
}
