package com.leconsulat.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VueEnsembleFinanceDto(
        BigDecimal recettesDuJour,
        BigDecimal depensesDuJour,
        BigDecimal beneficeDuJour,
        List<Evolution> evolution7Jours
) {
    public record Evolution(LocalDate date, BigDecimal recettes, BigDecimal depenses) {
    }
}
