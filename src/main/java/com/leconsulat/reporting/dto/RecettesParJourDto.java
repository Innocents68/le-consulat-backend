package com.leconsulat.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecettesParJourDto(
        LocalDate date,
        BigDecimal brut,
        BigDecimal remises,
        BigDecimal net
) {
}
