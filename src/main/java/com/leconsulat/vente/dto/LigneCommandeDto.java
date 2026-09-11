package com.leconsulat.vente.dto;

import com.leconsulat.vente.entity.LigneCommande;

import java.math.BigDecimal;

public record LigneCommandeDto(
        Long id,
        Long produitId,
        String articleNom,
        int quantite,
        BigDecimal prixUnitaire,
        BigDecimal montant
) {
    public static LigneCommandeDto from(LigneCommande l) {
        return new LigneCommandeDto(l.getId(), l.getProduit().getId(), l.getArticleNom(),
                l.getQuantite(), l.getPrixUnitaire(), l.getMontant());
    }
}
