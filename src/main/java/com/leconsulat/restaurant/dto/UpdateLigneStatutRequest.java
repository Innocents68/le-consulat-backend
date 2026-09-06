package com.leconsulat.restaurant.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateLigneStatutRequest(@NotBlank(message = "Le statut est obligatoire") String statut) {
}
