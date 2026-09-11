package com.leconsulat.catalogue.dto;

import com.leconsulat.catalogue.entity.Produit;

import java.math.BigDecimal;

public record ProduitDto(
        Long id,
        String nom,
        Long categorieId,
        String categorieNom,
        String description,
        String unite,
        BigDecimal prixVente,
        BigDecimal prixAchat,
        boolean disponible,
        boolean actif,
        boolean suiviStock,
        BigDecimal quantiteStock,
        BigDecimal seuilAlerte,
        String emplacement,
        Long fournisseurId,
        String fournisseurNom,
        Long etablissementId,
        String etablissementNom
) {
    public static ProduitDto from(Produit p) {
        return new ProduitDto(p.getId(), p.getNom(),
                p.getCategorie().getId(), p.getCategorie().getNom(),
                p.getDescription(), p.getUnite().name(),
                p.getPrixVente(), p.getPrixAchat(),
                p.isDisponible(), p.isActif(), p.isSuiviStock(),
                p.getQuantiteStock(), p.getSeuilAlerte(), p.getEmplacement(),
                p.getFournisseur() != null ? p.getFournisseur().getId() : null,
                p.getFournisseur() != null ? p.getFournisseur().getNom() : null,
                p.getEtablissement().getId(), p.getEtablissement().getNom());
    }
}
