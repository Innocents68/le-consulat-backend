package com.leconsulat.inventaire.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateInventaireRequest(
        Long etablissementId,
        @NotNull(message = "La date d'inventaire est obligatoire") LocalDate dateInventaire,
        String commentaire,
        /** "RAPIDE" (défaut, une ligne vide reste conforme au théorique à la clôture) ou
         * "COMPLET" (chaque produit doit être explicitement compté) — Cahier_des_charges_
         * amelioration_inventaire_Le_Consulat.docx §12. */
        String mode
) {
}
