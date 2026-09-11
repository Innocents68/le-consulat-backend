package com.leconsulat.auth.dto;

import com.leconsulat.utilisateur.entity.Utilisateur;

public record UserDto(
        Long id,
        String username,
        String nom,
        String email,
        String profil,
        Long etablissementId,
        String etablissementNom,
        boolean etablissementGereCuisine,
        String avatarInitiales
) {
    public static UserDto from(Utilisateur u) {
        return new UserDto(u.getId(), u.getUsername(), u.getNom(), u.getEmail(),
                u.getProfil().name(),
                u.getEtablissement() != null ? u.getEtablissement().getId() : null,
                u.getEtablissement() != null ? u.getEtablissement().getNom() : null,
                u.getEtablissement() != null && u.getEtablissement().isGereCuisine(),
                u.getAvatarInitiales());
    }
}
