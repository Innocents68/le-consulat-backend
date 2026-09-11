package com.leconsulat.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EcartInventaireDto(
        String inventaireNumero,
        LocalDateTime dateValidation,
        String produitNom,
        BigDecimal ecartQuantite,
        BigDecimal ecartValeur
) {
}
