package com.leconsulat.stock.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateFournisseurRequest(
        @NotBlank(message = "Le nom est obligatoire") String nom,
        String telephone
) {
}
