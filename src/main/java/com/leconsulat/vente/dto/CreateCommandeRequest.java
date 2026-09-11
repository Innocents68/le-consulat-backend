package com.leconsulat.vente.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateCommandeRequest(
        /** Nullable — vente à emporter/comptoir (PC-07). Doit être une table Libre du même
         * établissement si fournie. */
        Long tableId,
        String clientNom,
        String observations,
        @NotEmpty(message = "Une commande doit contenir au moins une ligne") @Valid List<LigneCommandeInput> lignes,
        /** Ignoré pour un Gérant/Caissier (RG-014/RG-015), obligatoire pour le Super Administrateur. */
        Long etablissementId
) {
}
