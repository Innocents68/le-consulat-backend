package com.leconsulat.parametres.service;

import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.parametres.dto.ParametresDto;
import com.leconsulat.parametres.dto.ParametresPublicsDto;
import com.leconsulat.parametres.dto.UpdateParametresRequest;
import com.leconsulat.parametres.entity.FormatTicket;
import com.leconsulat.parametres.entity.ParametresGeneraux;
import com.leconsulat.parametres.repository.ParametresGeneralRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/** §6.10.1 — ligne singleton créée paresseusement au premier accès (pas de seeder séparé,
 * les valeurs par défaut de {@link ParametresGeneraux} suffisent). RG-105 : la modification est
 * réservée au Super Administrateur, la lecture complète aussi (seul {@code /publics} est ouvert). */
@Service
@Transactional
public class ParametresService {

    private final ParametresGeneralRepository repository;
    private final String uploadsDir;

    public ParametresService(ParametresGeneralRepository repository, @Value("${app.uploads.dir}") String uploadsDir) {
        this.repository = repository;
        this.uploadsDir = uploadsDir;
    }

    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    public ParametresDto get() {
        return ParametresDto.from(charger());
    }

    public ParametresPublicsDto getPublics() {
        return ParametresPublicsDto.from(charger());
    }

    /** Accès interne, sans restriction de profil — utilisé par les autres services (génération
     * de ticket/avoir, seuil de stock par défaut, plafond de remise) qui doivent lire les
     * paramètres quel que soit le profil de l'utilisateur en cours, contrairement à {@link #get()}
     * qui sert l'écran de gestion réservé au Super Administrateur (RG-105). */
    public ParametresGeneraux getEntity() {
        return charger();
    }

    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    @Transactional
    public ParametresDto update(UpdateParametresRequest req) {
        ParametresGeneraux p = charger();
        p.setNomMagasin(req.nomMagasin());
        p.setAdresse(req.adresse());
        p.setTelephone(req.telephone());
        p.setEmail(req.email());
        p.setMessageFin(req.messageFin());
        if (req.devise() != null && !req.devise().isBlank()) {
            p.setDevise(req.devise());
        }
        p.setFormatTicket(parseFormat(req.formatTicket()));
        p.setNombreCopies(Math.max(1, req.nombreCopies()));
        p.setSeuilAlerteDefaut(req.seuilAlerteDefaut());
        p.setPlafondRemisePourcentage(req.plafondRemisePourcentage());
        p.setPlafondRemiseMontant(req.plafondRemiseMontant());
        return ParametresDto.from(repository.save(p));
    }

    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    @Transactional
    public ParametresDto uploadLogo(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new BusinessRuleException("Aucun fichier fourni");
        }
        String extension = extensionDe(fichier.getOriginalFilename());
        try {
            Path dossier = Path.of(uploadsDir, "logo");
            Files.createDirectories(dossier);
            // Un seul logo à la fois : on retire l'ancien avant d'écrire le nouveau (évite
            // l'accumulation de fichiers orphelins d'un précédent format d'image).
            try (var flux = Files.list(dossier)) {
                flux.sorted(Comparator.naturalOrder()).forEach(f -> {
                    try {
                        Files.deleteIfExists(f);
                    } catch (IOException ignored) {
                        // best-effort — un fichier verrouillé ne doit pas bloquer le nouvel upload.
                    }
                });
            }
            Path cible = dossier.resolve("logo" + extension);
            fichier.transferTo(cible);
        } catch (IOException e) {
            throw new UncheckedIOException("Erreur lors de l'enregistrement du logo", e);
        }
        ParametresGeneraux p = charger();
        p.setLogoUrl("/uploads/logo/logo" + extension);
        return ParametresDto.from(repository.save(p));
    }

    private ParametresGeneraux charger() {
        return repository.findById(1L).orElseGet(() -> repository.save(new ParametresGeneraux()));
    }

    private FormatTicket parseFormat(String format) {
        try {
            return FormatTicket.valueOf(format.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Format de ticket inconnu : " + format);
        }
    }

    private String extensionDe(String nomFichier) {
        if (nomFichier == null) {
            return "";
        }
        int i = nomFichier.lastIndexOf('.');
        return i >= 0 ? nomFichier.substring(i).toLowerCase() : "";
    }
}
