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
    AJUSTEMENT_INVENTAIRE
}
