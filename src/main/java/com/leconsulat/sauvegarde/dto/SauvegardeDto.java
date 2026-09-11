package com.leconsulat.sauvegarde.dto;

import com.leconsulat.sauvegarde.entity.Sauvegarde;

import java.time.LocalDateTime;

public record SauvegardeDto(
        Long id,
        String nomFichier,
        Long tailleOctets,
        String type,
        String statut,
        Long auteurId,
        String auteurNom,
        String message,
        LocalDateTime dateCreation
) {
    public static SauvegardeDto from(Sauvegarde s) {
        return new SauvegardeDto(s.getId(), s.getNomFichier(), s.getTailleOctets(), s.getType().name(), s.getStatut().name(),
                s.getAuteur() != null ? s.getAuteur().getId() : null,
                s.getAuteur() != null ? s.getAuteur().getNom() : null,
                s.getMessage(), s.getDateCreation());
    }
}
