package com.leconsulat.stock.dto;

import com.leconsulat.stock.entity.Inventaire;

import java.time.LocalDate;
import java.util.List;

public record InventaireDto(
        Long id,
        LocalDate dateInventaire,
        String type,
        String statut,
        List<LigneInventaireDto> lignes
) {
    public static InventaireDto from(Inventaire i) {
        List<LigneInventaireDto> lignes = i.getLignes().stream().map(LigneInventaireDto::from).toList();
        return new InventaireDto(i.getId(), i.getDateInventaire(), i.getType().name(), i.getStatut().name(), lignes);
    }
}
