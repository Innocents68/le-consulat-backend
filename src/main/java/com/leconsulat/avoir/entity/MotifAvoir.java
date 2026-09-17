package com.leconsulat.avoir.entity;

/** §6.2.7 — liste fermée, complétée par {@code motifDetail} en texte libre. */
public enum MotifAvoir {
    ERREUR_SAISIE,
    PRODUIT_NON_SERVI,
    RETOUR,
    GESTE_COMMERCIAL,
    /** Recommandations et corrections.md §4 : monnaie que la caisse ne pouvait pas rendre au
     * moment de l'encaissement, convertie en avoir à la demande du caissier. Créé directement
     * par {@code AvoirService.creerPourMonnaieInsuffisante()}, sans lignes ni facture d'origine
     * à corriger — la règle RG-061 (cumul plafonné au montant net) ne s'y applique donc pas. */
    MONNAIE_INSUFFISANTE,
    AUTRE
}
