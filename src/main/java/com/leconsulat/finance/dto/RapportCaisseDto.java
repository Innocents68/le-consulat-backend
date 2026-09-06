package com.leconsulat.finance.dto;

import com.leconsulat.finance.entity.RapportCaisse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RapportCaisseDto(
        Long id,
        Long sessionCaisseId,
        LocalDateTime dateGeneration,
        BigDecimal encaissements,
        BigDecimal decaissements,
        BigDecimal solde
) {
    public static RapportCaisseDto from(RapportCaisse r) {
        return new RapportCaisseDto(r.getId(), r.getSessionCaisseId(), r.getDateGeneration(), r.getEncaissements(), r.getDecaissements(), r.getSolde());
    }
}
