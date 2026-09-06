package com.leconsulat.parametres.dto;

import com.leconsulat.parametres.entity.Parametres;
import jakarta.validation.constraints.NotBlank;

public record ParametresDto(
        @NotBlank(message = "Le nom de l'établissement est obligatoire") String nomEtablissement,
        String logoUrl,
        String devise,
        double tauxTva,
        int seuilAlerteGlobal,
        boolean modeSombreParDefaut
) {
    public static ParametresDto from(Parametres p) {
        return new ParametresDto(p.getNomEtablissement(), p.getLogoUrl(), p.getDevise(), p.getTauxTva(),
                p.getSeuilAlerteGlobal(), p.isModeSombreParDefaut());
    }
}
