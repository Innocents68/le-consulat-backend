package com.leconsulat.financier.dto;

import java.time.LocalDate;
import java.util.List;

/** EF-034 : tableau comparatif consolidé — {@code total} n'est renseigné que lorsque plusieurs
 * établissements sont comparés (pas de filtre établissement précis). */
public record SyntheseFinanciereDto(
        LocalDate dateDebut,
        LocalDate dateFin,
        List<SyntheseEtablissementDto> lignes,
        SyntheseEtablissementDto total
) {
}
