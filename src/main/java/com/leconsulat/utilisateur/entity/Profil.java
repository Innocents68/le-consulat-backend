package com.leconsulat.utilisateur.entity;

/**
 * Deux profils uniquement, fixes et indivisibles (cahier des charges RG-004b) — contrairement à
 * {@link com.leconsulat.etablissement.entity.Etablissement}, ce n'est pas destiné à être étendu
 * par paramétrage.
 */
public enum Profil {
    SUPER_ADMINISTRATEUR,
    GERANT_CAISSIER
}
