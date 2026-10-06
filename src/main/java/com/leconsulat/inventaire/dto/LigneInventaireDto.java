package com.leconsulat.inventaire.dto;

import com.leconsulat.inventaire.entity.LigneInventaire;

import java.math.BigDecimal;

public record LigneInventaireDto(
        Long id,
        Long produitId,
        String produitNom,
        Long produitCategorieId,
        String produitCategorieNom,
        BigDecimal stockTheorique,
        BigDecimal stockPhysique,
        BigDecimal ecartQuantite,
        BigDecimal ecartValeur
) {
    /** Filtre par catégorie (Cahier_des_charges_amelioration_inventaire_Le_Consulat.docx §4/§5) :
     * la catégorie vient du produit, pas de l'inventaire lui-même. */
    public static LigneInventaireDto from(LigneInventaire l) {
        return new LigneInventaireDto(l.getId(), l.getProduit().getId(), l.getProduitNom(),
                l.getProduit().getCategorie().getId(), l.getProduit().getCategorie().getNom(),
                l.getStockTheorique(), l.getStockPhysique(), l.getEcartQuantite(), l.getEcartValeur());
    }
}
