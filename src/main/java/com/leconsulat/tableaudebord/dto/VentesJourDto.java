package com.leconsulat.tableaudebord.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VentesJourDto(
        String etablissementNom,
        LocalDate date,
        BigDecimal montant
) {
}
