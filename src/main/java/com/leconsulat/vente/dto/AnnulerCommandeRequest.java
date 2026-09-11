package com.leconsulat.vente.dto;

import jakarta.validation.constraints.NotBlank;

/** RG-045 : motif obligatoire. */
public record AnnulerCommandeRequest(@NotBlank(message = "Le motif d'annulation est obligatoire") String motif) {
}
