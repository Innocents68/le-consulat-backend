package com.leconsulat.sauvegarde.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.sauvegarde.entity.Sauvegarde;
import com.leconsulat.sauvegarde.entity.TypeSauvegarde;
import com.leconsulat.sauvegarde.repository.SauvegardeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/** EF-045 : sauvegarde automatique quotidienne, historique conservé au moins 30 jours. */
@Component
public class SauvegardeSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SauvegardeSchedulerService.class);
    private static final int RETENTION_JOURS = 30;

    private final SauvegardeService sauvegardeService;
    private final SauvegardeRepository repository;
    private final JournalOperationService journal;
    private final String backupsDir;

    public SauvegardeSchedulerService(SauvegardeService sauvegardeService, SauvegardeRepository repository,
                                       JournalOperationService journal, @Value("${app.backups.dir}") String backupsDir) {
        this.sauvegardeService = sauvegardeService;
        this.repository = repository;
        this.journal = journal;
        this.backupsDir = backupsDir;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void sauvegardeQuotidienne() {
        log.info("[SAUVEGARDE] Déclenchement de la sauvegarde automatique quotidienne");
        sauvegardeService.declencher(TypeSauvegarde.AUTOMATIQUE, null);
        purgerAnciennesSauvegardes();
    }

    /** Public pour rester vérifiable directement (via un appel de service) sans attendre un vrai
     * cycle {@code @Scheduled}. */
    public void purgerAnciennesSauvegardes() {
        LocalDateTime seuil = LocalDateTime.now().minusDays(RETENTION_JOURS);
        List<Sauvegarde> anciennes = repository.findByDateCreationBefore(seuil);
        for (Sauvegarde s : anciennes) {
            try {
                Files.deleteIfExists(Path.of(backupsDir, s.getNomFichier()));
            } catch (IOException e) {
                throw new UncheckedIOException("Erreur lors de la suppression du fichier " + s.getNomFichier(), e);
            }
        }
        if (!anciennes.isEmpty()) {
            repository.deleteAll(anciennes);
            journal.enregistrer("SYSTEME", "SAUVEGARDE_PURGE", anciennes.size() + " sauvegarde(s) de plus de "
                    + RETENTION_JOURS + " jours supprimée(s)");
        }
    }
}
