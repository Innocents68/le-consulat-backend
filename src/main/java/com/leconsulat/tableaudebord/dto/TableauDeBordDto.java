package com.leconsulat.tableaudebord.dto;

import com.leconsulat.etablissement.dto.EtablissementDto;
import com.leconsulat.journal.dto.JournalOperationDto;
import com.leconsulat.reporting.dto.RepartitionMontantDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** §6.1 — page d'accueil, distincte du « Dashboard reporting » (Lot 5b, sous-écran du module
 * Reporting, plus analytique/filtrable). Vue Super Administrateur : comparaison entre les 3
 * établissements. Vue Gérant/Caissier : centrée sur son seul établissement (pas de carte
 * Utilisateurs, pas de répartition — n'a de sens qu'en comparant plusieurs établissements). */
public record TableauDeBordDto(
        BigDecimal ventesDuJour,
        BigDecimal ventesVariationPourcent,
        long stockNombreArticles,
        BigDecimal depensesDuJour,
        BigDecimal depensesVariationPourcent,
        Long utilisateursActifs,
        Long utilisateursTotal,
        List<EtablissementDto> etablissements,
        List<VentesJourDto> evolutionVentes,
        List<RepartitionMontantDto> repartitionVentes,
        List<JournalOperationDto> dernieresOperations,
        String genereParNom,
        LocalDateTime dateGeneration
) {
}
