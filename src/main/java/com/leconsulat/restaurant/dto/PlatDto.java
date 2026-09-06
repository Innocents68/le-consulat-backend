package com.leconsulat.restaurant.dto;

import com.leconsulat.restaurant.entity.Plat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record PlatDto(
        Long id,
        String nom,
        BigDecimal prix,
        Long categorieId,
        String categorieNom,
        String description,
        Integer tempsPreparation,
        boolean disponible,
        List<String> ingredients,
        boolean actif
) {
    public static PlatDto from(Plat p) {
        return new PlatDto(p.getId(), p.getNom(), p.getPrix(),
                p.getCategorie() != null ? p.getCategorie().getId() : null,
                p.getCategorie() != null ? p.getCategorie().getNom() : null,
                p.getDescription(), p.getTempsPreparation(), p.isDisponible(), new ArrayList<>(p.getIngredients()), p.isActif());
    }
}
