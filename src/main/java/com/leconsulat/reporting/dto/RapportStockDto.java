package com.leconsulat.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** §6.9.3 : stock actuel (indépendant de la période — photo de l'instant présent), mouvements et
 * écarts d'inventaire de la période choisie. */
public record RapportStockDto(
        LocalDate dateDebut,
        LocalDate dateFin,
        List<StockLigneDto> stockActuel,
        BigDecimal valorisationTotale,
        long nombreRuptures,
        long nombreSousSeuil,
        List<MouvementTypeDto> mouvementsParType,
        List<EcartInventaireDto> ecartsInventaire,
        String genereParNom,
        LocalDateTime dateGeneration
) {
}
