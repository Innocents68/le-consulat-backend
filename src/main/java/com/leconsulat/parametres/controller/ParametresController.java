package com.leconsulat.parametres.controller;

import com.leconsulat.parametres.dto.ParametresDto;
import com.leconsulat.parametres.service.ParametresService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/parametres")
public class ParametresController {

    private final ParametresService service;

    public ParametresController(ParametresService service) {
        this.service = service;
    }

    @GetMapping
    public ParametresDto get() {
        return service.get();
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ParametresDto update(@Valid @RequestBody ParametresDto dto) {
        return service.update(dto);
    }

    @PostMapping(value = "/logo", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ParametresDto uploadLogo(@RequestParam("file") MultipartFile file) {
        return service.uploadLogo(file);
    }
}
