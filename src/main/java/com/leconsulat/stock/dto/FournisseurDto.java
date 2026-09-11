package com.leconsulat.stock.dto;

import com.leconsulat.stock.entity.Fournisseur;

public record FournisseurDto(
        Long id,
        String nom,
        String telephone,
        boolean actif
) {
    public static FournisseurDto from(Fournisseur f) {
        return new FournisseurDto(f.getId(), f.getNom(), f.getTelephone(), f.isActif());
    }
}
