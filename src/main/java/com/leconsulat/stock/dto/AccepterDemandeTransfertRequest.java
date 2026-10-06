package com.leconsulat.stock.dto;

import jakarta.validation.constraints.NotNull;

/** Le produit destination n'est choisi qu'à l'acceptation, par le responsable de l'établissement
 * destinataire — seul à avoir visibilité sur son propre catalogue (RG-002). */
public record AccepterDemandeTransfertRequest(
        @NotNull(message = "Le produit destination est obligatoire") Long produitDestinationId
) {
}
