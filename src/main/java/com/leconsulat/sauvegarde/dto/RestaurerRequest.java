package com.leconsulat.sauvegarde.dto;

import jakarta.validation.constraints.NotBlank;

/** RG-106 : deuxième palier de confirmation vérifié côté serveur — {@code confirmation} doit
 * correspondre exactement à la phrase attendue ({@code SauvegardeService.PHRASE_CONFIRMATION}),
 * en plus de la double confirmation déjà exigée côté écran. */
public record RestaurerRequest(
        @NotBlank(message = "La confirmation est obligatoire") String confirmation
) {
}
