package com.leconsulat.restaurant.dto;

import com.leconsulat.restaurant.entity.LigneCommande;

import java.math.BigDecimal;

public record LigneCommandeDto(
        Long id,
        Long platId,
        String platNom,
        int quantite,
        String options,
        BigDecimal prixUnitaire,
        BigDecimal montant,
        String statut
) {
    public static LigneCommandeDto from(LigneCommande l) {
        return new LigneCommandeDto(l.getId(), l.getPlat().getId(), l.getPlat().getNom(), l.getQuantite(),
                l.getOptions(), l.getPrixUnitaire(), l.getMontant(), l.getStatut().name());
    }
}
