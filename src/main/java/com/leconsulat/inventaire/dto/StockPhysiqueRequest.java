package com.leconsulat.inventaire.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

/** Cahier_des_charges_amelioration_inventaire_Le_Consulat.docx §2/§14 : {@code null} (vide) et
 * {@link BigDecimal#ZERO} sont deux valeurs distinctes — vide signifie "non compté, conserver le
 * théorique", 0 signifie "compté, réellement absent". D'où l'absence de {@code @NotNull} : cet
 * endpoint sert aussi à ramener une ligne à l'état non compté. */
public record StockPhysiqueRequest(
        @DecimalMin(value = "0", message = "Le stock physique ne peut pas être négatif") BigDecimal stockPhysique
) {
}
