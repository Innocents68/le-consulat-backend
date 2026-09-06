package com.leconsulat.restaurant.dto;

import jakarta.validation.constraints.NotBlank;

public record AnnulerCommandeRequest(@NotBlank(message = "Le motif d'annulation est obligatoire") String motif) {
}
