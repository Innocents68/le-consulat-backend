package com.leconsulat.depense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** RG-094 : réservé au Super Administrateur, motif obligatoire. L'établissement n'est pas
 * modifiable — une dépense reste rattachée à celui pour lequel elle a été engagée. */
public record UpdateDepenseRequest(
        @NotNull(message = "La date est obligatoire") LocalDate date,
        @NotNull(message = "La catégorie est obligatoire") Long categorieId,
        @NotBlank(message = "Le libellé est obligatoire") String libelle,
        @NotNull(message = "Le montant est obligatoire") @DecimalMin(value = "0.01", message = "Le montant doit être positif") BigDecimal montant,
        @NotBlank(message = "Le mode de paiement est obligatoire") String modePaiement,
        @NotBlank(message = "Le bénéficiaire est obligatoire") String beneficiaire,
        String commentaire,
        @NotBlank(message = "Le motif de la modification est obligatoire") String motif
) {
}
