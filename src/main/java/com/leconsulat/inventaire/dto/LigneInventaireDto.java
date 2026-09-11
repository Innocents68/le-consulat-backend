package com.leconsulat.inventaire.dto;

import com.leconsulat.inventaire.entity.LigneInventaire;

import java.math.BigDecimal;

public record LigneInventaireDto(
        Long id,
        Long produitId,
        String produitNom,
        BigDecimal stockTheorique,
        BigDecimal stockPhysique,
        BigDecimal ecartQuantite,
        BigDecimal ecartValeur
) {
    public static LigneInventaireDto from(LigneInventaire l) {
        return new LigneInventaireDto(l.getId(), l.getProduit().getId(), l.getProduitNom(),
                l.getStockTheorique(), l.getStockPhysique(), l.getEcartQuantite(), l.getEcartValeur());
    }
}
