package com.leconsulat.reporting.dto;

import com.leconsulat.vente.entity.LigneCommande;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record VenteLigneDto(
        LocalDate date,
        LocalTime heure,
        String produitNom,
        String categorieNom,
        String tableNumero,
        String utilisateurNom,
        int quantite,
        BigDecimal montant
) {
    public static VenteLigneDto from(LigneCommande l) {
        var commande = l.getCommande();
        return new VenteLigneDto(
                commande.getDateValidation().toLocalDate(),
                commande.getDateValidation().toLocalTime(),
                l.getArticleNom(),
                l.getProduit().getCategorie().getNom(),
                commande.getTable() != null ? commande.getTable().getNumero() : null,
                commande.getCaissier().getNom(),
                l.getQuantite(), l.getMontant());
    }
}
