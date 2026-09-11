package com.leconsulat.parametres.dto;

import com.leconsulat.parametres.entity.ParametresGeneraux;

/** Sous-ensemble exposé sans authentification (écran de connexion, en-tête de l'application,
 * page Aide EF-046) — jamais de paramètres de gestion (seuils, plafonds...), réservés au Super
 * Administrateur (RG-105). Le contact support (email/téléphone) est en revanche destiné à être
 * lu par tous les utilisateurs, pas une donnée de gestion interne. */
public record ParametresPublicsDto(
        String logoUrl,
        String nomMagasin,
        String devise,
        String email,
        String telephone
) {
    public static ParametresPublicsDto from(ParametresGeneraux p) {
        return new ParametresPublicsDto(p.getLogoUrl(), p.getNomMagasin(), p.getDevise(), p.getEmail(), p.getTelephone());
    }
}
