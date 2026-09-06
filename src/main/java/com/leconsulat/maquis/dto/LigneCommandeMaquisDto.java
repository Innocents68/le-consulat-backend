package com.leconsulat.maquis.dto;

import com.leconsulat.maquis.entity.LigneCommandeMaquis;

import java.math.BigDecimal;

public record LigneCommandeMaquisDto(
        Long id,
        String articleNom,
        int quantite,
        BigDecimal prixUnitaire,
        BigDecimal montant
) {
    public static LigneCommandeMaquisDto from(LigneCommandeMaquis l) {
        return new LigneCommandeMaquisDto(l.getId(), l.getArticleNom(), l.getQuantite(), l.getPrixUnitaire(), l.getMontant());
    }
}
