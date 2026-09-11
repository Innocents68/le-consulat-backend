package com.leconsulat.reporting.dto;

import java.math.BigDecimal;

public record MouvementTypeDto(
        String type,
        BigDecimal quantiteTotale
) {
}
