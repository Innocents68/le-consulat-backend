package com.leconsulat.vente.dto;

import com.leconsulat.vente.entity.Vente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VenteDto(
        Long id,
        String numero,
        Long sessionCaisseId,
        Long caissierId,
        String caissierNom,
        String clientNom,
        String statut,
        String modePaiement,
        List<LigneVenteDto> lignes,
        BigDecimal sousTotal,
        BigDecimal remiseMontant,
        BigDecimal total,
        LocalDateTime dateVente
) {
    public static VenteDto from(Vente v) {
        List<LigneVenteDto> lignes = v.getLignes().stream().map(LigneVenteDto::from).toList();
        return new VenteDto(
                v.getId(), v.getNumero(), v.getSessionCaisse().getId(),
                v.getCaissier() != null ? v.getCaissier().getId() : null,
                v.getCaissier() != null ? v.getCaissier().getNom() : null,
                v.getClientNom(), v.getStatut().name(),
                v.getModePaiement() != null ? v.getModePaiement().name() : null,
                lignes, v.getSousTotal(), v.getRemiseMontant(), v.getTotal(), v.getDateVente()
        );
    }
}
