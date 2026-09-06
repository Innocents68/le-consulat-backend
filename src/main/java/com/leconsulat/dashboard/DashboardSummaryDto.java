package com.leconsulat.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Exact shape mandated by API_CONTRACT.md §7. */
public record DashboardSummaryDto(
        BigDecimal ventesDuJour,
        BigDecimal recettesDuJour,
        long commandesRestaurant,
        long consommationsMaquis,
        BigDecimal beneficeDuJour,
        Variations variations,
        List<PointEvolution> evolutionVentes,
        List<RepartitionActivite> repartitionActivites,
        List<Alerte> alertes,
        List<TopProduit> topProduits,
        List<ActiviteRecente> activiteRecente
) {
    public record Variations(double ventes, double recettes, double commandes, double consommations, double benefice) {
    }

    public record PointEvolution(LocalDate date, BigDecimal montant) {
    }

    public record RepartitionActivite(String label, double pourcentage) {
    }

    public record Alerte(String type, String niveau, String message) {
    }

    public record TopProduit(String nom, long ventes, BigDecimal montant) {
    }

    public record ActiviteRecente(String type, String libelle, BigDecimal montant, String ilYA) {
    }
}
