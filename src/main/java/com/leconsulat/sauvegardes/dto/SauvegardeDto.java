package com.leconsulat.sauvegardes.dto;

import com.leconsulat.sauvegardes.entity.Sauvegarde;

import java.time.LocalDateTime;

public record SauvegardeDto(
        Long id,
        String nomFichier,
        Long tailleOctets,
        String statut,
        String message,
        LocalDateTime dateCreation
) {
    public static SauvegardeDto from(Sauvegarde s) {
        return new SauvegardeDto(s.getId(), s.getNomFichier(), s.getTailleOctets(), s.getStatut().name(), s.getMessage(), s.getDateCreation());
    }
}
