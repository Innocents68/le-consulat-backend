package com.leconsulat.maquis.dto;

import com.leconsulat.maquis.entity.CommandeMaquis;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CommandeMaquisDto(
        Long id,
        Long tableId,
        String clientNom,
        String statut,
        List<LigneCommandeMaquisDto> lignes,
        BigDecimal total,
        LocalDateTime dateCreation
) {
    public static CommandeMaquisDto from(CommandeMaquis c) {
        List<LigneCommandeMaquisDto> lignes = c.getLignes().stream().map(LigneCommandeMaquisDto::from).toList();
        BigDecimal total = lignes.stream().map(LigneCommandeMaquisDto::montant).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CommandeMaquisDto(c.getId(), c.getTable() != null ? c.getTable().getId() : null,
                c.getClientNom(), c.getStatut().name(), lignes, total, c.getDateCreation());
    }
}
