package com.leconsulat.parametres.dto;

import com.leconsulat.parametres.entity.ParametresGeneraux;

/** Sous-ensemble exposé sans authentification (écran de connexion, en-tête de l'application,
 * page Aide EF-046) — jamais de paramètres de gestion (seuils, plafonds...), réservés au Super
 * Administrateur (RG-105). Le contact support (email/téléphone) et le guide PDF (Cahier_de_
 * corrections_Le_Consulat.docx §4) sont en revanche destinés à être lus par tous les
 * utilisateurs, pas des données de gestion interne. */
public record ParametresPublicsDto(
        String logoUrl,
        String guidePdfUrl,
        String nomMagasin,
        String devise,
        String email,
        String telephone
) {
    public static ParametresPublicsDto from(ParametresGeneraux p) {
        return new ParametresPublicsDto(p.getLogoUrl(), p.getGuidePdfUrl(), p.getNomMagasin(), p.getDevise(), p.getEmail(), p.getTelephone());
    }
}
