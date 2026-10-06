package com.leconsulat.avoir.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Consu_corrige.docx §1 : "Nouvel avoir" à formulaire libre — un montant saisi directement,
 * rattaché à une facture par son numéro, sans passer par la sélection ligne par ligne de
 * {@link CreateAvoirRequest}. Toujours plafonné par RG-061 (cumul ≤ montant net de la facture). */
public record CreerAvoirMontantLibreRequest(
        @NotBlank(message = "Le numéro de facture est obligatoire") String factureNumero,
        @NotNull(message = "Le montant est obligatoire") @DecimalMin(value = "0.01", message = "Le montant doit être positif") BigDecimal montant,
        @NotNull(message = "Le motif est obligatoire") String motif,
        String motifDetail,
        @NotNull(message = "Le mode de remboursement est obligatoire") String modeRemboursement
) {
}
