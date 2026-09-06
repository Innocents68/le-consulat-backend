package com.leconsulat.stock.dto;

import com.leconsulat.stock.entity.CategorieProduit;
import jakarta.validation.constraints.NotBlank;

public record CategorieProduitDto(Long id, @NotBlank(message = "Le nom est obligatoire") String nom) {
    public static CategorieProduitDto from(CategorieProduit c) {
        return new CategorieProduitDto(c.getId(), c.getNom());
    }
}
