package com.leconsulat.parametres.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BadRequestException;
import com.leconsulat.parametres.dto.ParametresDto;
import com.leconsulat.parametres.entity.Parametres;
import com.leconsulat.parametres.repository.ParametresRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@Transactional
public class ParametresService {

    private static final java.util.Set<String> TYPES_AUTORISES = java.util.Set.of(
            "image/png", "image/jpeg", "image/webp", "image/svg+xml");

    private final ParametresRepository repository;
    private final JournalOperationService journal;

    @Value("${app.uploads.dir}")
    private String uploadsDir;

    public ParametresService(ParametresRepository repository, JournalOperationService journal) {
        this.repository = repository;
        this.journal = journal;
    }

    public ParametresDto get() {
        return ParametresDto.from(getOrCreateSingleton());
    }

    @Transactional
    public ParametresDto update(ParametresDto dto) {
        Parametres p = getOrCreateSingleton();
        p.setNomEtablissement(dto.nomEtablissement());
        p.setLogoUrl(dto.logoUrl());
        p.setDevise(dto.devise());
        p.setTauxTva(dto.tauxTva());
        p.setSeuilAlerteGlobal(dto.seuilAlerteGlobal());
        p.setModeSombreParDefaut(dto.modeSombreParDefaut());
        Parametres saved = repository.save(p);
        journal.enregistrer("PARAMETRES", "MODIFICATION", "Mise à jour des paramètres généraux");
        return ParametresDto.from(saved);
    }

    @Transactional
    public Parametres getOrCreateSingleton() {
        return repository.findById(Parametres.SINGLETON_ID).orElseGet(() -> repository.save(new Parametres()));
    }

    /** Reçoit le fichier logo envoyé depuis l'écran Paramètres généraux, le stocke sur disque et
     * met à jour {@code logoUrl} en conséquence (l'ancien fichier, s'il en existait un, est supprimé). */
    @Transactional
    public ParametresDto uploadLogo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Aucun fichier reçu");
        }
        String contentType = file.getContentType();
        if (contentType == null || !TYPES_AUTORISES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Format d'image non supporté (PNG, JPEG, WebP ou SVG uniquement)");
        }

        Parametres p = getOrCreateSingleton();
        try {
            Path dir = Path.of(uploadsDir, "logos");
            Files.createDirectories(dir);

            String extension = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                extension = original.substring(original.lastIndexOf('.'));
            }
            String filename = "logo-" + UUID.randomUUID() + extension;
            Path target = dir.resolve(filename);
            file.transferTo(target);

            supprimerAncienLogo(p.getLogoUrl(), dir);
            p.setLogoUrl("/uploads/logos/" + filename);
        } catch (IOException e) {
            throw new BadRequestException("Impossible d'enregistrer le logo : " + e.getMessage());
        }

        Parametres saved = repository.save(p);
        journal.enregistrer("PARAMETRES", "MODIFICATION", "Mise à jour du logo de l'établissement");
        return ParametresDto.from(saved);
    }

    private void supprimerAncienLogo(String ancienLogoUrl, Path logosDir) {
        if (ancienLogoUrl == null || !ancienLogoUrl.startsWith("/uploads/logos/")) {
            return; // rien à nettoyer, ou l'ancienne valeur était une URL externe
        }
        try {
            String ancienFichier = ancienLogoUrl.substring("/uploads/logos/".length());
            Files.deleteIfExists(logosDir.resolve(ancienFichier));
        } catch (IOException ignored) {
            // le nettoyage de l'ancien fichier n'est pas critique
        }
    }
}
