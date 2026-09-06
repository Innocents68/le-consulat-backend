package com.leconsulat.cave.dto;

import com.leconsulat.cave.entity.Fournisseur;
import jakarta.validation.constraints.NotBlank;

public record FournisseurDto(
        Long id,
        @NotBlank(message = "Le nom est obligatoire") String nom,
        String contact,
        String telephone,
        String email,
        String conditions,
        boolean actif
) {
    public static FournisseurDto from(Fournisseur f) {
        return new FournisseurDto(f.getId(), f.getNom(), f.getContact(), f.getTelephone(), f.getEmail(), f.getConditions(), f.isActif());
    }
}
