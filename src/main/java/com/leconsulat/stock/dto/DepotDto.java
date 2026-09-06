package com.leconsulat.stock.dto;

import com.leconsulat.stock.entity.Depot;
import jakarta.validation.constraints.NotBlank;

public record DepotDto(Long id, @NotBlank(message = "Le nom est obligatoire") String nom, String adresse, boolean actif) {
    public static DepotDto from(Depot d) {
        return new DepotDto(d.getId(), d.getNom(), d.getAdresse(), d.isActif());
    }
}
