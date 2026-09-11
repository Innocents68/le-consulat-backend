package com.leconsulat.utilisateur.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUtilisateurRequest(
        @NotBlank(message = "Le nom d'utilisateur est obligatoire") @Size(min = 3, max = 60, message = "Le nom d'utilisateur doit contenir entre 3 et 60 caractères") String username,
        @NotBlank(message = "Le nom complet est obligatoire") String nom,
        @Email(message = "Email invalide") String email,
        String telephone,
        @NotNull(message = "Le profil est obligatoire") String profil,
        /** Obligatoire pour tout profil autre que Super Administrateur — validé dans
         * UtilisateurService, pas ici, car la règle dépend de la valeur de {@code profil}. */
        Long etablissementId,
        @NotBlank(message = "Le mot de passe est obligatoire") @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères") String motDePasse
) {
}
