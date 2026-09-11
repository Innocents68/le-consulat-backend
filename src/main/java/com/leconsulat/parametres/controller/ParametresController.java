package com.leconsulat.parametres.controller;

import com.leconsulat.parametres.dto.ParametresDto;
import com.leconsulat.parametres.dto.ParametresPublicsDto;
import com.leconsulat.parametres.dto.UpdateParametresRequest;
import com.leconsulat.parametres.service.ParametresService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/parametres")
public class ParametresController {

    private final ParametresService service;

    public ParametresController(ParametresService service) {
        this.service = service;
    }

    /** Sans authentification (EF-043 : le logo doit s'afficher dès l'écran de connexion) —
     * enregistré dans {@code JwtAuthenticationFilter.publicPaths} et
     * {@code SecurityConfig.permitAll}, même mécanisme que {@code /auth/login}. */
    @GetMapping("/publics")
    public ParametresPublicsDto publics() {
        return service.getPublics();
    }

    @GetMapping
    public ParametresDto get() {
        return service.get();
    }

    @PutMapping
    public ParametresDto update(@Valid @RequestBody UpdateParametresRequest req) {
        return service.update(req);
    }

    @PostMapping(value = "/logo", consumes = "multipart/form-data")
    public ParametresDto uploadLogo(@RequestParam("fichier") MultipartFile fichier) {
        return service.uploadLogo(fichier);
    }
}
