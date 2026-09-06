package com.leconsulat.cave.dto;

import com.leconsulat.cave.entity.Boisson;

import java.math.BigDecimal;

public record BoissonDto(
        Long id,
        String nom,
        String type,
        String origine,
        Integer millesime,
        BigDecimal prixAchatBouteille,
        BigDecimal prixVenteBouteille,
        BigDecimal prixVenteVerre,
        Long fournisseurId,
        String fournisseurNom,
        BigDecimal quantiteStock,
        int seuilAlerte,
        boolean actif
) {
    public static BoissonDto from(Boisson b) {
        return new BoissonDto(b.getId(), b.getNom(), b.getType().name(), b.getOrigine(), b.getMillesime(),
                b.getPrixAchatBouteille(), b.getPrixVenteBouteille(), b.getPrixVenteVerre(),
                b.getFournisseur() != null ? b.getFournisseur().getId() : null,
                b.getFournisseur() != null ? b.getFournisseur().getNom() : null,
                b.getQuantiteStock(), b.getSeuilAlerte(), b.isActif());
    }
}
