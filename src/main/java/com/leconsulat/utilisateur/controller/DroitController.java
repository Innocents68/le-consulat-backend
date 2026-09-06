package com.leconsulat.utilisateur.controller;

import com.leconsulat.utilisateur.dto.DroitFlagsDto;
import com.leconsulat.utilisateur.service.DroitService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/droits")
public class DroitController {

    private final DroitService service;

    public DroitController(DroitService service) {
        this.service = service;
    }

    @GetMapping("/matrice")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Map<String, DroitFlagsDto>> getMatrice() {
        return service.getMatrice();
    }

    @PutMapping("/matrice")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Map<String, DroitFlagsDto>> updateMatrice(@RequestBody Map<String, Map<String, DroitFlagsDto>> matrice) {
        return service.updateMatrice(matrice);
    }
}
