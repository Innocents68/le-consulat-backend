package com.leconsulat.etablissement.dto;

import com.leconsulat.etablissement.entity.Etablissement;

public record EtablissementDto(
        Long id,
        String code,
        String nom,
        boolean actif,
        boolean gereCuisine
) {
    public static EtablissementDto from(Etablissement e) {
        return new EtablissementDto(e.getId(), e.getCode(), e.getNom(), e.isActif(), e.isGereCuisine());
    }
}
