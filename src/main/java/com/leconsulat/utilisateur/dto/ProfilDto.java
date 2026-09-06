package com.leconsulat.utilisateur.dto;

import java.util.Map;

public record ProfilDto(
        String role,
        long nombreUtilisateurs,
        Map<String, DroitFlagsDto> droits
) {
}
