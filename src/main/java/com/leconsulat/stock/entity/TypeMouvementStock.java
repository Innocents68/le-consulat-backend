package com.leconsulat.stock.entity;

/** §6.6.2. {@code AJUSTEMENT_INVENTAIRE} n'est produit qu'au Lot 4b (Inventaire) — présent dès
 * maintenant pour ne pas avoir à migrer la colonne plus tard. */
public enum TypeMouvementStock {
    ENTREE,
    SORTIE,
    SORTIE_VENTE,
    RETOUR_AVOIR,
    TRANSFERT_SORTANT,
    TRANSFERT_ENTRANT,
    AJUSTEMENT_INVENTAIRE,
    /** Consu_corrige.docx §4 : stock réintégré suite à la suppression d'une facture par un
     * Super Administrateur (cas exceptionnel — distinct de RETOUR_AVOIR qui ne concerne qu'une
     * correction partielle via avoir). */
    ANNULATION_FACTURE
}
