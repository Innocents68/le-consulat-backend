package com.leconsulat.reporting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RapportBeneficesDto(
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal beneficeNetTotal,
        List<BeneficeParJour> evolution
) {
    public record BeneficeParJour(LocalDate date, BigDecimal recettes, BigDecimal depenses, BigDecimal benefice) {
    }
}
