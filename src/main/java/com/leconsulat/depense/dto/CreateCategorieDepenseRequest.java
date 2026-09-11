package com.leconsulat.depense.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategorieDepenseRequest(
        @NotBlank(message = "Le nom est obligatoire") String nom
) {
}
