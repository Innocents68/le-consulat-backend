package com.leconsulat.utilisateur;

import java.util.List;

/** The list of functional modules the droits (permissions) matrix applies to — cahier des charges §3. */
public final class ModulesCatalogue {

    private ModulesCatalogue() {
    }

    public static final List<String> MODULES = List.of(
            "dashboard",
            "ventes",
            "factures",
            "caisses",
            "remises",
            "avoirs",
            "tables",
            "commandes-restaurant",
            "cuisine",
            "plats",
            "cave",
            "maquis",
            "stocks",
            "entrees-stock",
            "sorties-stock",
            "finance",
            "depenses",
            "utilisateurs",
            "profils",
            "journal",
            "reporting",
            "parametres",
            "sauvegardes",
            "aide"
    );
}
