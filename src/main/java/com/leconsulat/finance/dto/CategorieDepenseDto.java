package com.leconsulat.finance.dto;

import com.leconsulat.finance.entity.CategorieDepense;
import jakarta.validation.constraints.NotBlank;

public record CategorieDepenseDto(Long id, @NotBlank(message = "Le nom est obligatoire") String nom) {
    public static CategorieDepenseDto from(CategorieDepense c) {
        return new CategorieDepenseDto(c.getId(), c.getNom());
    }
}
