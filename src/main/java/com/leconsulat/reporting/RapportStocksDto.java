package com.leconsulat.reporting;

import java.math.BigDecimal;
import java.util.List;

public record RapportStocksDto(
        BigDecimal valorisationTotale,
        long nombreRuptures,
        List<ProduitStockFaible> produitsStockFaible,
        List<RepartitionCategorie> repartitionParCategorie
) {
    public record ProduitStockFaible(String nom, int quantiteStock, int seuilAlerte) {
    }

    public record RepartitionCategorie(String categorie, BigDecimal valorisation) {
    }
}
