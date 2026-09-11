package com.leconsulat.depense.dto;

import com.leconsulat.depense.entity.CategorieDepense;

public record CategorieDepenseDto(
        Long id,
        String nom,
        boolean actif
) {
    public static CategorieDepenseDto from(CategorieDepense c) {
        return new CategorieDepenseDto(c.getId(), c.getNom(), c.isActif());
    }
}
