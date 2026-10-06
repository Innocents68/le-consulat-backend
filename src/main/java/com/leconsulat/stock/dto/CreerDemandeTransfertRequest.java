package com.leconsulat.stock.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreerDemandeTransfertRequest(
        @NotNull(message = "Le produit source est obligatoire") Long produitSourceId,
        @NotNull(message = "La quantité est obligatoire") @DecimalMin(value = "0.001", message = "La quantité doit être positive") BigDecimal quantite,
        @NotNull(message = "L'établissement destinataire est obligatoire") Long etablissementDestinationId,
        String commentaire
) {
}
