package com.leconsulat.stock.dto;

import com.leconsulat.stock.entity.Produit;

import java.math.BigDecimal;

public record ProduitDto(
        Long id,
        String code,
        String nom,
        Long categorieId,
        String categorieNom,
        String unite,
        BigDecimal prixAchat,
        BigDecimal prixVente,
        int seuilAlerte,
        int quantiteStock,
        Long depotId,
        String depotNom,
        boolean actif
) {
    public static ProduitDto from(Produit p) {
        return new ProduitDto(
                p.getId(), p.getCode(), p.getNom(),
                p.getCategorie() != null ? p.getCategorie().getId() : null,
                p.getCategorie() != null ? p.getCategorie().getNom() : null,
                p.getUnite(), p.getPrixAchat(), p.getPrixVente(), p.getSeuilAlerte(), p.getQuantiteStock(),
                p.getDepot() != null ? p.getDepot().getId() : null,
                p.getDepot() != null ? p.getDepot().getNom() : null,
                p.isActif());
    }
}
