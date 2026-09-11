package com.leconsulat.reporting.dto;

import java.math.BigDecimal;

/** Paire générique {@code label -> montant} — réutilisée pour la répartition par établissement,
 * par mode de paiement et par utilisateur. */
public record RepartitionMontantDto(
        String label,
        BigDecimal montant
) {
}
