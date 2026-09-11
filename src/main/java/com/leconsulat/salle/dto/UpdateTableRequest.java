package com.leconsulat.salle.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateTableRequest(
        @NotBlank(message = "Le numéro est obligatoire") String numero,
        @Min(value = 1, message = "La capacité doit être d'au moins 1") int capacite,
        String zone
) {
}
