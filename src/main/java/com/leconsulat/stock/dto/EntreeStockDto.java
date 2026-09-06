package com.leconsulat.stock.dto;

import com.leconsulat.stock.entity.EntreeStock;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EntreeStockDto(
        Long id,
        Long produitId,
        String produitNom,
        String fournisseur,
        int quantite,
        BigDecimal prixUnitaire,
        BigDecimal montant,
        LocalDate dateEntree,
        String bonLivraison,
        String statut
) {
    public static EntreeStockDto from(EntreeStock e) {
        return new EntreeStockDto(e.getId(), e.getProduit().getId(), e.getProduit().getNom(), e.getFournisseur(),
                e.getQuantite(), e.getPrixUnitaire(), e.getMontant(), e.getDateEntree(), e.getBonLivraison(), e.getStatut().name());
    }
}
