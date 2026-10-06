package com.leconsulat.vente.dto;

import jakarta.validation.constraints.NotBlank;

/** Consu_corrige.docx §4 : suppression exceptionnelle d'une facture (Super Administrateur
 * uniquement) — motif obligatoire, même exigence que pour une sortie de stock manuelle (RG-081),
 * puisque ça revient aussi à corriger/annuler un mouvement déjà enregistré. */
public record DeleteFactureRequest(
        @NotBlank(message = "Le motif est obligatoire") String motif
) {
}
