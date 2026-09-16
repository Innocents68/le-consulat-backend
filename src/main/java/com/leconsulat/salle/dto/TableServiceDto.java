package com.leconsulat.salle.dto;

import com.leconsulat.salle.entity.TableService;

import java.time.LocalDateTime;

public record TableServiceDto(
        Long id,
        String numero,
        int capacite,
        String zone,
        String statut,
        Long etablissementId,
        String etablissementNom,
        boolean actif,
        LocalDateTime dateDebutOccupation
) {
    public static TableServiceDto from(TableService t) {
        return new TableServiceDto(t.getId(), t.getNumero(), t.getCapacite(), t.getZone(), t.getStatut().name(),
                t.getEtablissement().getId(), t.getEtablissement().getNom(), t.isActif(), t.getDateDebutOccupation());
    }
}
