package com.leconsulat.stock.dto;

import com.leconsulat.stock.entity.SortieStock;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SortieStockDto(
        Long id,
        Long produitId,
        String produitNom,
        String motif,
        int quantite,
        BigDecimal prixUnitaire,
        BigDecimal montant,
        LocalDate dateSortie,
        String statut
) {
    public static SortieStockDto from(SortieStock s) {
        return new SortieStockDto(s.getId(), s.getProduit().getId(), s.getProduit().getNom(), s.getMotif().name(),
                s.getQuantite(), s.getPrixUnitaire(), s.getMontant(), s.getDateSortie(), s.getStatut().name());
    }
}
