package com.leconsulat.salle.dto;

import com.leconsulat.salle.entity.TableService;

public record TableServiceDto(
        Long id,
        String numero,
        int capacite,
        String zone,
        String statut,
        Long etablissementId,
        String etablissementNom,
        boolean actif
) {
    public static TableServiceDto from(TableService t) {
        return new TableServiceDto(t.getId(), t.getNumero(), t.getCapacite(), t.getZone(), t.getStatut().name(),
                t.getEtablissement().getId(), t.getEtablissement().getNom(), t.isActif());
    }
}
