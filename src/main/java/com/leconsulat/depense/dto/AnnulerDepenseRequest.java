package com.leconsulat.depense.dto;

import jakarta.validation.constraints.NotBlank;

public record AnnulerDepenseRequest(
        @NotBlank(message = "Le motif est obligatoire") String motif
) {
}
