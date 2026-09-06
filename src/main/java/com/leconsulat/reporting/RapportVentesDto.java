package com.leconsulat.reporting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RapportVentesDto(
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal chiffreAffaires,
        long quantiteVendue,
        BigDecimal panierMoyen,
        List<VentesParJour> ventesParJour,
        List<TopProduitVente> topProduits
) {
    public record VentesParJour(LocalDate date, BigDecimal montant, long nombreTickets) {
    }

    public record TopProduitVente(String nom, long quantite, BigDecimal montant) {
    }
}
