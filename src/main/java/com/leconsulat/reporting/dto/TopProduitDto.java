package com.leconsulat.reporting.dto;

import java.math.BigDecimal;

public record TopProduitDto(
        Long produitId,
        String produitNom,
        long quantite,
        BigDecimal montant
) {
}
