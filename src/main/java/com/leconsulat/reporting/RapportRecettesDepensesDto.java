package com.leconsulat.reporting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RapportRecettesDepensesDto(
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal totalRecettes,
        BigDecimal totalDepenses,
        BigDecimal marge,
        List<FluxParJour> fluxParJour
) {
    public record FluxParJour(LocalDate date, BigDecimal recettes, BigDecimal depenses) {
    }
}
