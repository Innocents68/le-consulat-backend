package com.leconsulat.reporting.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RapportBeneficesDto(
        LocalDate dateDebut,
        LocalDate dateFin,
        List<BeneficeEtablissementDto> lignes,
        /** Non nul seulement quand plusieurs établissements sont comparés (RG-101). */
        BeneficeEtablissementDto total,
        String genereParNom,
        LocalDateTime dateGeneration
) {
}
