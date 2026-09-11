package com.leconsulat.avoir.dto;

import com.leconsulat.avoir.entity.LigneAvoir;

import java.math.BigDecimal;

public record LigneAvoirDto(
        Long id,
        Long produitId,
        String articleNom,
        int quantite,
        BigDecimal montant
) {
    public static LigneAvoirDto from(LigneAvoir l) {
        return new LigneAvoirDto(l.getId(), l.getProduit().getId(), l.getArticleNom(), l.getQuantite(), l.getMontant());
    }
}
