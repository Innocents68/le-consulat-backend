package com.leconsulat.vente.dto;

import com.leconsulat.vente.entity.LigneVente;

import java.math.BigDecimal;

public record LigneVenteDto(
        Long id,
        Long produitId,
        String produitNom,
        int quantite,
        BigDecimal prixUnitaire,
        BigDecimal remise,
        BigDecimal montant
) {
    public static LigneVenteDto from(LigneVente l) {
        return new LigneVenteDto(l.getId(), l.getProduit().getId(), l.getProduit().getNom(), l.getQuantite(),
                l.getPrixUnitaire(), l.getRemise(), l.getMontant());
    }
}
