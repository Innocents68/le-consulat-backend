package com.leconsulat.depense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDepenseRequest(
        @NotNull(message = "La date est obligatoire") LocalDate date,
        @NotNull(message = "La catégorie est obligatoire") Long categorieId,
        @NotBlank(message = "Le libellé est obligatoire") String libelle,
        @NotNull(message = "Le montant est obligatoire") @DecimalMin(value = "0.01", message = "Le montant doit être positif") BigDecimal montant,
        @NotBlank(message = "Le mode de paiement est obligatoire") String modePaiement,
        @NotBlank(message = "Le bénéficiaire est obligatoire") String beneficiaire,
        String commentaire,
        /** Ignoré pour un Gérant/Caissier (RG-090), obligatoire pour le Super Administrateur. */
        Long etablissementId
) {
}
