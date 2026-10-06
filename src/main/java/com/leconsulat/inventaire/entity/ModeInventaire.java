package com.leconsulat.inventaire.entity;

/** Cahier_des_charges_amelioration_inventaire_Le_Consulat.docx §12 : en mode {@code RAPIDE}
 * (par défaut), une ligne laissée vide à la clôture est considérée conforme au stock théorique
 * (§2/§14) ; en mode {@code COMPLET}, chaque produit doit être explicitement compté avant de
 * pouvoir clôturer. */
public enum ModeInventaire {
    RAPIDE,
    COMPLET
}
