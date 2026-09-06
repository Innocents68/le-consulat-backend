package com.leconsulat.vente.dto;

import com.leconsulat.vente.entity.SessionCaisse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessionCaisseDto(
        Long id,
        String caisseNom,
        String ouvertPar,
        BigDecimal fondInitial,
        String statut,
        LocalDateTime dateOuverture,
        LocalDateTime dateFermeture,
        BigDecimal totalVentes,
        BigDecimal totalEncaissements,
        BigDecimal ecart
) {
    public static SessionCaisseDto from(SessionCaisse s) {
        return new SessionCaisseDto(s.getId(), s.getCaisseNom(),
                s.getOuvertPar() != null ? s.getOuvertPar().getNom() : null,
                s.getFondInitial(), s.getStatut().name(), s.getDateOuverture(), s.getDateFermeture(),
                s.getTotalVentes(), s.getTotalEncaissements(), s.getEcart());
    }
}
