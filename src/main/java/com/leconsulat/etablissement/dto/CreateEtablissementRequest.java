package com.leconsulat.etablissement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEtablissementRequest(
        @NotBlank(message = "Le code est obligatoire") @Size(max = 10, message = "Le code doit faire au plus 10 caractères") String code,
        @NotBlank(message = "Le nom est obligatoire") String nom
) {
}
