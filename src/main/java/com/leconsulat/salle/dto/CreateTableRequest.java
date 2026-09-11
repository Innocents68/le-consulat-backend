package com.leconsulat.salle.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateTableRequest(
        @NotBlank(message = "Le numéro est obligatoire") String numero,
        @Min(value = 1, message = "La capacité doit être d'au moins 1") int capacite,
        String zone,
        /** Ignoré pour un Gérant/Caissier (RG-014/RG-015), obligatoire pour le Super Administrateur. */
        Long etablissementId
) {
}
