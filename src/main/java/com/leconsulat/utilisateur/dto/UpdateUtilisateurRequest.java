package com.leconsulat.utilisateur.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUtilisateurRequest(
        @NotBlank(message = "Le nom complet est obligatoire") String nom,
        @Email(message = "Email invalide") String email,
        String telephone,
        @NotNull(message = "Le profil est obligatoire") String profil,
        /** Obligatoire pour tout profil autre que Super Administrateur — validé dans UtilisateurService. */
        Long etablissementId
) {
}
