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
        LocalDateTime dateOperation
) {
    public static JournalOperationDto from(JournalOperation j) {
        return new JournalOperationDto(j.getId(), j.getUtilisateurId(), j.getUtilisateurNom(),
                j.getModule(), j.getAction(), j.getDetails(), j.getDateOperation());
    }
}
