package com.leconsulat.reporting.dto;

import java.math.BigDecimal;

/** §6.9.3 (Rapport des bénéfices) : CA, coût des marchandises vendues, marge brute, dépenses,
 * résultat — repris tel quel du tableau du CDC. */
public record BeneficeEtablissementDto(
        Long etablissementId,
        String etablissementNom,
        BigDecimal chiffreAffaires,
        BigDecimal cmv,
        BigDecimal margeBrute,
        BigDecimal depenses,
        BigDecimal resultat
) {
    public static BeneficeEtablissementDto of(Long etablissementId, String etablissementNom,
                                               BigDecimal chiffreAffaires, BigDecimal cmv, BigDecimal depenses) {
        BigDecimal margeBrute = chiffreAffaires.subtract(cmv);
        return new BeneficeEtablissementDto(etablissementId, etablissementNom, chiffreAffaires, cmv, margeBrute,
                depenses, margeBrute.subtract(depenses));
    }
}
