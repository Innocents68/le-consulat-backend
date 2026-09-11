package com.leconsulat.reporting.dto;

import java.math.BigDecimal;

public record StockLigneDto(
        Long produitId,
        String produitNom,
        String categorieNom,
        BigDecimal quantiteStock,
        BigDecimal seuilAlerte,
        BigDecimal valorisation,
        boolean rupture,
        boolean sousSeuil
) {
}
