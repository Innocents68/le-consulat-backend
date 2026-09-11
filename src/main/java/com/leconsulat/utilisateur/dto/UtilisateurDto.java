package com.leconsulat.utilisateur.dto;

import com.leconsulat.utilisateur.entity.Utilisateur;

import java.time.LocalDateTime;

public record UtilisateurDto(
        Long id,
        String username,
        String nom,
        String email,
        String telephone,
        String profil,
        Long etablissementId,
        String etablissementNom,
        boolean actif,
        LocalDateTime dateCreation
) {
    public static UtilisateurDto from(Utilisateur u) {
        return new UtilisateurDto(u.getId(), u.getUsername(), u.getNom(), u.getEmail(), u.getTelephone(),
                u.getProfil().name(),
                u.getEtablissement() != null ? u.getEtablissement().getId() : null,
                u.getEtablissement() != null ? u.getEtablissement().getNom() : null,
                u.isActif(), u.getDateCreation());
    }
}
