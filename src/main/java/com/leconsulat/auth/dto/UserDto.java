package com.leconsulat.auth.dto;

import com.leconsulat.utilisateur.entity.Utilisateur;

public record UserDto(
        Long id,
        String username,
        String nom,
        String role,
        String email,
        String avatarInitiales
) {
    public static UserDto from(Utilisateur u) {
        return new UserDto(u.getId(), u.getUsername(), u.getNom(), u.getRole().name(), u.getEmail(), u.getAvatarInitiales());
    }
}
