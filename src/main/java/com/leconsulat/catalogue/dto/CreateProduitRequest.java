package com.leconsulat.catalogue.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateProduitRequest(
        @NotBlank(message = "Le nom est obligatoire") String nom,
        @NotNull(message = "La catégorie est obligatoire") Long categorieId,
        String description,
        @NotBlank(message = "L'unité est obligatoire") String unite,
        @NotNull(message = "Le prix de vente est obligatoire") @DecimalMin(value = "0.01", message = "Le prix de vente doit être positif") BigDecimal prixVente,
        @NotNull(message = "Le prix d'achat est obligatoire") @DecimalMin(value = "0.01", message = "Le prix d'achat doit être positif") BigDecimal prixAchat,
        Boolean disponible,
        Boolean suiviStock,
        BigDecimal seuilAlerte,
        String emplacement,
        Long fournisseurId,
        /** Optionnel : laissé vide, un code est généré automatiquement après création (produit
         * sans code-barres d'origine, ex. plat maison) — sinon on enregistre le code déjà imprimé
         * sur le produit acheté (scanné ou saisi). */
        String codeBarre,
        /** Ignoré pour un Gérant/Caissier (RG-014/RG-015), obligatoire pour le Super Administrateur. */
        Long etablissementId
) {
}
