package com.leconsulat.parametres.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record UpdateParametresRequest(
        @NotBlank(message = "Le nom du magasin est obligatoire") String nomMagasin,
        String adresse,
        String telephone,
        String email,
        String messageFin,
        String devise,
        @NotBlank(message = "Le format de ticket est obligatoire") String formatTicket,
        int nombreCopies,
        BigDecimal seuilAlerteDefaut,
        BigDecimal plafondRemisePourcentage,
        BigDecimal plafondRemiseMontant
) {
}
