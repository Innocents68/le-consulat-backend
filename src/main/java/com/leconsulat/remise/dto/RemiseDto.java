package com.leconsulat.remise.dto;

import com.leconsulat.remise.entity.Remise;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record RemiseDto(
        Long id,
        String libelle,
        String type,
        BigDecimal valeur,
        LocalDate dateDebut,
        LocalDate dateFin,
        LocalTime heureDebut,
        LocalTime heureFin,
        Long etablissementId,
        String etablissementNom,
        boolean actif
) {
    public static RemiseDto from(Remise r) {
        return new RemiseDto(r.getId(), r.getLibelle(), r.getType().name(), r.getValeur(),
                r.getDateDebut(), r.getDateFin(), r.getHeureDebut(), r.getHeureFin(),
                r.getEtablissement().getId(), r.getEtablissement().getNom(), r.isActif());
    }
}
