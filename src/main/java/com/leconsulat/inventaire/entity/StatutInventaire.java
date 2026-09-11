package com.leconsulat.inventaire.entity;

/** §6.6.5 : Brouillon → En comptage → Clôturé (écarts calculés) → Validé (ajustements générés). */
public enum StatutInventaire {
    BROUILLON,
    EN_COMPTAGE,
    CLOTURE,
    VALIDE
}
