package com.leconsulat.journal.dto;

import com.leconsulat.journal.entity.JournalOperation;

import java.time.LocalDateTime;

public record JournalOperationDto(
        Long id,
        Long utilisateurId,
        String utilisateurNom,
        String module,
        String action,
        String details,
        Long etablissementId,
        String etablissementNom,
        LocalDateTime dateOperation
) {
    public static JournalOperationDto from(JournalOperation j) {
        return new JournalOperationDto(j.getId(), j.getUtilisateurId(), j.getUtilisateurNom(),
                j.getModule(), j.getAction(), j.getDetails(),
                j.getEtablissement() != null ? j.getEtablissement().getId() : null,
                j.getEtablissement() != null ? j.getEtablissement().getNom() : null,
                j.getDateOperation());
    }
}
