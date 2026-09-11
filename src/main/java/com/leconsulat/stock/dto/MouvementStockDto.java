package com.leconsulat.stock.dto;

import com.leconsulat.stock.entity.MouvementStock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MouvementStockDto(
        Long id,
        Long produitId,
        String produitNom,
        Long etablissementId,
        String etablissementNom,
        String type,
        BigDecimal quantite,
        String motif,
        BigDecimal prixUnitaire,
        BigDecimal montant,
        String referenceTransfert,
        Long auteurId,
        String auteurNom,
        LocalDateTime dateMouvement
) {
    public static MouvementStockDto from(MouvementStock m) {
        return new MouvementStockDto(m.getId(),
                m.getProduit().getId(), m.getProduit().getNom(),
                m.getEtablissement().getId(), m.getEtablissement().getNom(),
                m.getType().name(), m.getQuantite(), m.getMotif(),
                m.getPrixUnitaire(), m.getMontant(), m.getReferenceTransfert(),
                m.getAuteur().getId(), m.getAuteur().getNom(), m.getDateMouvement());
    }
}
