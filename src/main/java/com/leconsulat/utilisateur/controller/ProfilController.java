package com.leconsulat.utilisateur.controller;

import com.leconsulat.utilisateur.dto.ProfilDto;
import com.leconsulat.utilisateur.entity.Role;
import com.leconsulat.utilisateur.repository.UtilisateurRepository;
import com.leconsulat.utilisateur.service.DroitService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profils")
public class ProfilController {

    private final DroitService droitService;
    private final UtilisateurRepository utilisateurRepository;

    public ProfilController(DroitService droitService, UtilisateurRepository utilisateurRepository) {
        this.droitService = droitService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProfilDto> list() {
        return java.util.Arrays.stream(Role.values())
                .map(role -> new ProfilDto(role.name(), utilisateurRepository.countByRole(role), droitService.getDroitsPourRole(role)))
                .toList();
    }
}
