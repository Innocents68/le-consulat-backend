package com.leconsulat.vente.dto;

import jakarta.validation.constraints.NotNull;

public record CreateVenteRequest(
        @NotNull(message = "La session de caisse est obligatoire") Long sessionCaisseId,
        String clientNom
) {
}
