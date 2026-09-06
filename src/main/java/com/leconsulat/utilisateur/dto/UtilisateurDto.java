package com.leconsulat.utilisateur.dto;

import com.leconsulat.utilisateur.entity.Utilisateur;

import java.time.LocalDateTime;

public record UtilisateurDto(
        Long id,
        String username,
        String nom,
        String email,
        String telephone,
        String role,
        boolean actif,
        LocalDateTime dateCreation
) {
    public static UtilisateurDto from(Utilisateur u) {
        return new UtilisateurDto(u.getId(), u.getUsername(), u.getNom(), u.getEmail(), u.getTelephone(),
                u.getRole().name(), u.isActif(), u.getDateCreation());
    }
}
