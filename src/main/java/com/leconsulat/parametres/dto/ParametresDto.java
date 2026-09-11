package com.leconsulat.parametres.dto;

import com.leconsulat.parametres.entity.ParametresGeneraux;

import java.math.BigDecimal;

public record ParametresDto(
        String logoUrl,
        String nomMagasin,
        String adresse,
        String telephone,
        String email,
        String messageFin,
        String devise,
        String formatTicket,
        int nombreCopies,
        BigDecimal seuilAlerteDefaut,
        BigDecimal plafondRemisePourcentage,
        BigDecimal plafondRemiseMontant
) {
    public static ParametresDto from(ParametresGeneraux p) {
        return new ParametresDto(p.getLogoUrl(), p.getNomMagasin(), p.getAdresse(), p.getTelephone(), p.getEmail(),
                p.getMessageFin(), p.getDevise(), p.getFormatTicket().name(), p.getNombreCopies(),
                p.getSeuilAlerteDefaut(), p.getPlafondRemisePourcentage(), p.getPlafondRemiseMontant());
    }
}
