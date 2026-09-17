package com.leconsulat.avoir.dto;

import com.leconsulat.avoir.entity.Avoir;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AvoirDto(
        Long id,
        String numero,
        Long etablissementId,
        String etablissementNom,
        Long factureId,
        String factureNumero,
        List<LigneAvoirDto> lignes,
        BigDecimal montant,
        String motif,
        String motifDetail,
        boolean remiseEnStock,
        String modeRemboursement,
        BigDecimal montantUtilise,
        BigDecimal soldeRestant,
        String statut,
        Long auteurId,
        String auteurNom,
        LocalDateTime dateCreation
) {
    public static AvoirDto from(Avoir a) {
        BigDecimal soldeRestant = a.getMontant().subtract(a.getMontantUtilise());
        return new AvoirDto(
                a.getId(), a.getNumero(),
                a.getEtablissement().getId(), a.getEtablissement().getNom(),
                a.getFacture().getId(), a.getFacture().getNumero(),
                a.getLignes().stream().map(LigneAvoirDto::from).toList(),
                a.getMontant(), a.getMotif().name(), a.getMotifDetail(), a.isRemiseEnStock(),
                a.getModeRemboursement().name(), a.getMontantUtilise(), soldeRestant,
                soldeRestant.signum() > 0 ? "DISPONIBLE" : "UTILISE",
                a.getAuteur().getId(), a.getAuteur().getNom(),
                a.getDateCreation()
        );
    }
}
