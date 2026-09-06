package com.leconsulat.vente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAvoirRequest(
        @NotNull(message = "La vente est obligatoire") Long venteId,
        @NotBlank(message = "Le motif est obligatoire") String motif
) {
}
