package com.leconsulat.financier.dto;

import java.math.BigDecimal;

/** RG-088 : solde = recettes encaissées − avoirs − dépenses, sur la période retenue. */
public record SyntheseEtablissementDto(
        Long etablissementId,
        String etablissementNom,
        BigDecimal recettes,
        BigDecimal avoirs,
        BigDecimal depenses,
        BigDecimal solde
) {
    public static SyntheseEtablissementDto of(Long etablissementId, String etablissementNom,
                                               BigDecimal recettes, BigDecimal avoirs, BigDecimal depenses) {
        return new SyntheseEtablissementDto(etablissementId, etablissementNom, recettes, avoirs, depenses,
                recettes.subtract(avoirs).subtract(depenses));
    }
}
