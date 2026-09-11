package com.leconsulat.vente.dto;

import com.leconsulat.vente.entity.Facture;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FactureDto(
        Long id,
        String numero,
        Long etablissementId,
        String etablissementNom,
        Long commandeId,
        String commandeNumero,
        Long tableId,
        String tableNumero,
        Long caissierId,
        String caissierNom,
        List<LigneCommandeDto> lignes,
        BigDecimal montantBrut,
        BigDecimal remise,
        BigDecimal montantNet,
        String mode,
        BigDecimal montantRecu,
        BigDecimal monnaieRendue,
        LocalDateTime dateEmission,
        int nombreImpressions
) {
    public static FactureDto from(Facture f) {
        var c = f.getCommande();
        return new FactureDto(
                f.getId(), f.getNumero(),
                f.getEtablissement().getId(), f.getEtablissement().getNom(),
                c.getId(), c.getNumero(),
                c.getTable() != null ? c.getTable().getId() : null,
                c.getTable() != null ? c.getTable().getNumero() : null,
                c.getCaissier().getId(), c.getCaissier().getNom(),
                c.getLignes().stream().map(LigneCommandeDto::from).toList(),
                f.getMontantBrut(), f.getRemise(), f.getMontantNet(),
                f.getMode().name(), f.getMontantRecu(), f.getMonnaieRendue(),
                f.getDateEmission(), f.getNombreImpressions()
        );
    }
}
