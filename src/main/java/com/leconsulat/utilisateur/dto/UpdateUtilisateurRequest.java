package com.leconsulat.utilisateur.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUtilisateurRequest(
        @NotBlank(message = "Le nom complet est obligatoire") String nom,
        @Email(message = "Email invalide") String email,
        String telephone,
        @NotNull(message = "Le rôle est obligatoire") String role
) {
}
