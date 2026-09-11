package com.leconsulat.depense.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCategorieDepenseRequest(
        @NotBlank(message = "Le nom est obligatoire") String nom
) {
}
