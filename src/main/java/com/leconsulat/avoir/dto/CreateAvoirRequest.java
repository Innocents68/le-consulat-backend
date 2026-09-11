package com.leconsulat.avoir.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateAvoirRequest(
        @NotEmpty(message = "Sélectionnez au moins une ligne à annuler") @Valid List<LigneAvoirInput> lignes,
        @NotNull(message = "Le motif est obligatoire") String motif,
        String motifDetail,
        boolean remiseEnStock,
        @NotNull(message = "Le mode de remboursement est obligatoire") String modeRemboursement
) {
}
