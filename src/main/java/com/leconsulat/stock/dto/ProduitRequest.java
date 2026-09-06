package com.leconsulat.stock.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProduitRequest(
        @NotBlank(message = "Le code est obligatoire") String code,
        @NotBlank(message = "Le nom est obligatoire") String nom,
        Long categorieId,
        String unite,
        @NotNull(message = "Le prix d'achat est obligatoire") @DecimalMin(value = "0", message = "Le prix d'achat doit être positif") BigDecimal prixAchat,
        @NotNull(message = "Le prix de vente est obligatoire") @DecimalMin(value = "0", message = "Le prix de vente doit être positif") BigDecimal prixVente,
        @Min(value = 0, message = "Le seuil d'alerte doit être positif ou nul") int seuilAlerte,
        @Min(value = 0, message = "La quantité en stock ne peut être négative") Integer quantiteStock,
        Long depotId
) {
}
