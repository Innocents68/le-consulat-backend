package com.leconsulat.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RapportRecettesDto(
        LocalDate dateDebut,
        LocalDate dateFin,
        List<RecettesParJourDto> parJour,
        List<RepartitionMontantDto> parMode,
        List<RepartitionMontantDto> parUtilisateur,
        BigDecimal avoirsTotal,
        BigDecimal totalNet,
        String genereParNom,
        LocalDateTime dateGeneration
) {
}
