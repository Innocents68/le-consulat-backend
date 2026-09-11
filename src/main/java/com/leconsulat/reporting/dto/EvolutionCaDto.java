package com.leconsulat.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EvolutionCaDto(
        LocalDate date,
        BigDecimal montant
) {
}
