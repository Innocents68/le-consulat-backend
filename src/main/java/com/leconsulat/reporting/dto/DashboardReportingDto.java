package com.leconsulat.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** EF-042 : chaque rapport porte sa période, son périmètre (implicite via les filtres passés en
 * paramètre par l'appelant), l'auteur et la date de génération. */
public record DashboardReportingDto(
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal caTotal,
        List<EvolutionCaDto> evolutionCA,
        /** Non nul seulement quand aucun établissement précis n'est choisi (Super Admin, RG-101). */
        List<RepartitionMontantDto> parEtablissement,
        List<RepartitionMontantDto> parModePaiement,
        List<TopProduitDto> topProduits,
        String genereParNom,
        LocalDateTime dateGeneration
) {
}
