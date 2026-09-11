package com.leconsulat.vente.entity;

/** RG-025 — les 7 statuts du cahier des charges. Les 3 derniers (production cuisine) ne
 * concernent que le Restaurant et ne sont pas encore atteignables (Lot 2c, Suivi cuisine). */
public enum StatutCommande {
    NON_VALIDEE,
    VALIDEE,
    ENVOYEE_CUISINE,
    EN_PREPARATION,
    PRETE,
    SERVIE,
    ANNULEE
}
