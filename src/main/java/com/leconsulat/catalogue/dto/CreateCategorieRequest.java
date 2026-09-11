package com.leconsulat.catalogue.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategorieRequest(
        @NotBlank(message = "Le nom est obligatoire") String nom,
        /** Ignoré si l'utilisateur est un Gérant/Caissier (son établissement s'applique
         * automatiquement, RG-014/RG-015) — obligatoire seulement pour le Super Administrateur. */
        Long etablissementId
) {
}
