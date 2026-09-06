package com.leconsulat.restaurant.dto;

import com.leconsulat.restaurant.entity.Commande;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CommandeDto(
        Long id,
        Long tableId,
        String tableNumero,
        String type,
        Long serveurId,
        String serveurNom,
        String statut,
        List<LigneCommandeDto> lignes,
        BigDecimal total,
        LocalDateTime dateCreation
) {
    public static CommandeDto from(Commande c) {
        List<LigneCommandeDto> lignes = c.getLignes().stream().map(LigneCommandeDto::from).toList();
        BigDecimal total = lignes.stream().map(LigneCommandeDto::montant).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CommandeDto(
                c.getId(),
                c.getTable() != null ? c.getTable().getId() : null,
                c.getTable() != null ? c.getTable().getNumero() : null,
                c.getType().name(),
                c.getServeur() != null ? c.getServeur().getId() : null,
                c.getServeur() != null ? c.getServeur().getNom() : null,
                c.getStatut().name(),
                lignes,
                total,
                c.getDateCreation()
        );
    }
}
