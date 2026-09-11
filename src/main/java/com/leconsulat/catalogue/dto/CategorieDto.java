package com.leconsulat.catalogue.dto;

import com.leconsulat.catalogue.entity.Categorie;

public record CategorieDto(
        Long id,
        String nom,
        Long etablissementId,
        String etablissementNom,
        boolean actif
) {
    public static CategorieDto from(Categorie c) {
        return new CategorieDto(c.getId(), c.getNom(),
                c.getEtablissement().getId(), c.getEtablissement().getNom(), c.isActif());
    }
}
