package com.leconsulat.restaurant.dto;

import com.leconsulat.restaurant.entity.CategoriePlat;
import jakarta.validation.constraints.NotBlank;

public record CategoriePlatDto(Long id, @NotBlank(message = "Le nom est obligatoire") String nom, int ordre) {
    public static CategoriePlatDto from(CategoriePlat c) {
        return new CategoriePlatDto(c.getId(), c.getNom(), c.getOrdre());
    }
}
