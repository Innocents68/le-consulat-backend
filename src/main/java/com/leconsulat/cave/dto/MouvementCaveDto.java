package com.leconsulat.cave.dto;

import com.leconsulat.cave.entity.MouvementCave;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MouvementCaveDto(
        Long id,
        Long boissonId,
        String boissonNom,
        String type,
        String motif,
        BigDecimal quantite,
        LocalDateTime date
) {
    public static MouvementCaveDto from(MouvementCave m) {
        return new MouvementCaveDto(m.getId(), m.getBoisson().getId(), m.getBoisson().getNom(),
                m.getType().name(), m.getMotif().name(), m.getQuantite(), m.getDate());
    }
}
