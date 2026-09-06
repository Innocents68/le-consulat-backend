package com.leconsulat.aide;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aide")
public class AideController {

    private static final List<FaqDto> FAQ = List.of(
            new FaqDto("Comment ouvrir une session de caisse ?",
                    "Rendez-vous dans le module Gestion des caisses, cliquez sur \"Ouvrir une caisse\", indiquez le fond de caisse initial puis validez.",
                    "Caisse"),
            new FaqDto("Comment encaisser un ticket ?",
                    "Dans le module Ventes / Caisse, ajoutez les articles au panier puis cliquez sur \"Encaisser\", choisissez le mode de paiement et saisissez le montant reçu.",
                    "Caisse"),
            new FaqDto("Comment émettre un avoir ?",
                    "Depuis le détail d'une vente payée, cliquez sur \"Émettre un avoir\", indiquez le motif : l'avoir est immédiatement validé et tracé.",
                    "Caisse"),
            new FaqDto("Comment fonctionne la vente de vin au verre ?",
                    "La cave à vin décompte automatiquement une fraction de bouteille (1/6) à chaque vente au verre, proportionnellement au stock en bouteilles.",
                    "Cave à vin"),
            new FaqDto("Comment envoyer une commande en cuisine ?",
                    "Depuis le module Commandes restaurant, composez la commande puis cliquez sur \"Envoyer en cuisine\" : elle apparaît immédiatement sur l'écran cuisine.",
                    "Restaurant"),
            new FaqDto("Comment valider une entrée de stock ?",
                    "Dans Entrées en stock, créez l'entrée en brouillon puis cliquez sur \"Valider\" : le stock du produit est alors incrémenté définitivement.",
                    "Stocks"),
            new FaqDto("Comment clôturer une caisse (rapport Z) ?",
                    "Dans Gestion des caisses, cliquez sur \"Clôturer\", saisissez le montant réel compté : l'écart théorique/réel est calculé et le rapport de caisse est généré automatiquement.",
                    "Finance"),
            new FaqDto("Qui peut modifier la matrice des droits ?",
                    "Seul un compte avec le rôle Administrateur peut modifier la matrice des droits, dans le module Profils et droits.",
                    "Utilisateurs"),
            new FaqDto("Comment restaurer une sauvegarde ?",
                    "Dans Sauvegarde / Restauration, sélectionnez une sauvegarde réussie et cliquez sur \"Restaurer\". Un redémarrage de l'application peut être nécessaire.",
                    "Système"),
            new FaqDto("J'ai perdu la connexion Internet, que se passe-t-il ?",
                    "L'application tente une reconnexion automatique. Le suivi cuisine et le plan de salle basculent en rafraîchissement automatique toutes les 5 secondes en attendant.",
                    "Système")
    );

    @GetMapping("/faq")
    public List<FaqDto> faq() {
        return FAQ;
    }
}
