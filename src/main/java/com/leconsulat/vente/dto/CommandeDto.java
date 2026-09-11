package com.leconsulat.vente.dto;

import com.leconsulat.vente.entity.Commande;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CommandeDto(
        Long id,
        String numero,
        Long etablissementId,
        String etablissementNom,
        Long tableId,
        String tableNumero,
        String clientNom,
        List<LigneCommandeDto> lignes,
        BigDecimal montantBrut,
        BigDecimal remise,
        BigDecimal montantNet,
        String observations,
        String statut,
        String motifAnnulation,
        Long caissierId,
        String caissierNom,
        LocalDateTime dateCreation,
        LocalDateTime dateValidation
) {
    public static CommandeDto from(Commande c) {
        return new CommandeDto(
                c.getId(), c.getNumero(),
                c.getEtablissement().getId(), c.getEtablissement().getNom(),
                c.getTable() != null ? c.getTable().getId() : null,
                c.getTable() != null ? c.getTable().getNumero() : null,
                c.getClientNom(),
                c.getLignes().stream().map(LigneCommandeDto::from).toList(),
                c.getMontantBrut(), c.getRemise(), c.getMontantNet(),
                c.getObservations(), c.getStatut().name(), c.getMotifAnnulation(),
                c.getCaissier().getId(), c.getCaissier().getNom(),
                c.getDateCreation(), c.getDateValidation()
        );
    }
}
