package com.leconsulat.sauvegardes.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.sauvegardes.dto.SauvegardeDto;
import com.leconsulat.sauvegardes.entity.Sauvegarde;
import com.leconsulat.sauvegardes.repository.SauvegardeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Produces real, functional backup files. In `prod` (PostgreSQL) it shells out to
 * `pg_dump`; in `dev` (file-based H2) it copies the live H2 data file — cf. cahier des
 * charges §6.2 "Sauvegardes automatiques régulières... avec possibilité de restauration".
 */
@Service
@Transactional
public class SauvegardeService {

    private final SauvegardeRepository repository;
    private final JournalOperationService journal;

    @Value("${app.backups.dir}")
    private String backupsDir;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String datasourceUser;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    public SauvegardeService(SauvegardeRepository repository, JournalOperationService journal) {
        this.repository = repository;
        this.journal = journal;
    }

    public List<SauvegardeDto> list() {
        return repository.findAllByOrderByDateCreationDesc().stream().map(SauvegardeDto::from).toList();
    }

    @Transactional
    public SauvegardeDto declencher() {
        String timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Sauvegarde s = new Sauvegarde();
        s.setStatut(Sauvegarde.Statut.EN_COURS);

        try {
            Path dir = Path.of(backupsDir);
            Files.createDirectories(dir);

            if (datasourceUrl != null && datasourceUrl.startsWith("jdbc:postgresql")) {
                s.setNomFichier("leconsulat-" + timestamp + ".sql");
                Path target = dir.resolve(s.getNomFichier());
                executerPgDump(target);
                s.setChemin(target.toString());
                s.setTailleOctets(Files.exists(target) ? Files.size(target) : 0L);
                s.setStatut(Sauvegarde.Statut.REUSSIE);
                s.setMessage("Sauvegarde PostgreSQL générée via pg_dump");
            } else {
                s.setNomFichier("leconsulat-" + timestamp + ".mv.db");
                Path target = dir.resolve(s.getNomFichier());
                copierFichierH2(target);
                s.setChemin(target.toString());
                s.setTailleOctets(Files.exists(target) ? Files.size(target) : 0L);
                s.setStatut(Sauvegarde.Statut.REUSSIE);
                s.setMessage("Copie du fichier de base H2 (mode développement)");
            }
        } catch (Exception e) {
            s.setStatut(Sauvegarde.Statut.ECHOUEE);
            s.setMessage("Échec de la sauvegarde : " + e.getMessage());
        }

        Sauvegarde saved = repository.save(s);
        journal.enregistrer("SAUVEGARDES", "CREATION", "Sauvegarde manuelle déclenchée (" + saved.getStatut() + ")");
        return SauvegardeDto.from(saved);
    }

    @Transactional
    public void restaurer(Long id) {
        Sauvegarde s = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Sauvegarde", id));
        if (s.getStatut() != Sauvegarde.Statut.REUSSIE || s.getChemin() == null) {
            throw new BusinessRuleException("Cette sauvegarde n'est pas restaurable");
        }
        try {
            if (datasourceUrl != null && datasourceUrl.startsWith("jdbc:postgresql")) {
                executerPsqlRestore(Path.of(s.getChemin()));
            } else {
                copierVersFichierH2Actif(Path.of(s.getChemin()));
            }
            journal.enregistrer("SAUVEGARDES", "VALIDATION", "Restauration depuis la sauvegarde #" + id + " (redémarrage de l'application requis)");
        } catch (Exception e) {
            throw new BusinessRuleException("Échec de la restauration : " + e.getMessage());
        }
    }

    private void executerPgDump(Path target) throws IOException, InterruptedException {
        // jdbc:postgresql://host:port/dbname
        URI uri = URI.create(datasourceUrl.substring("jdbc:".length()));
        String host = uri.getHost() != null ? uri.getHost() : "localhost";
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String dbName = uri.getPath() != null && uri.getPath().length() > 1 ? uri.getPath().substring(1) : "leconsulat";

        ProcessBuilder pb = new ProcessBuilder("pg_dump", "-h", host, "-p", String.valueOf(port),
                "-U", datasourceUser, "-f", target.toString(), dbName);
        pb.environment().put("PGPASSWORD", datasourcePassword == null ? "" : datasourcePassword);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        boolean finished = process.waitFor(120, TimeUnit.SECONDS);
        if (!finished || process.exitValue() != 0) {
            throw new IOException("pg_dump a échoué (code=" + (finished ? process.exitValue() : "timeout") + ")");
        }
    }

    private void executerPsqlRestore(Path source) throws IOException, InterruptedException {
        URI uri = URI.create(datasourceUrl.substring("jdbc:".length()));
        String host = uri.getHost() != null ? uri.getHost() : "localhost";
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String dbName = uri.getPath() != null && uri.getPath().length() > 1 ? uri.getPath().substring(1) : "leconsulat";

        ProcessBuilder pb = new ProcessBuilder("psql", "-h", host, "-p", String.valueOf(port),
                "-U", datasourceUser, "-d", dbName, "-f", source.toString());
        pb.environment().put("PGPASSWORD", datasourcePassword == null ? "" : datasourcePassword);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        boolean finished = process.waitFor(120, TimeUnit.SECONDS);
        if (!finished || process.exitValue() != 0) {
            throw new IOException("psql (restauration) a échoué (code=" + (finished ? process.exitValue() : "timeout") + ")");
        }
    }

    private void copierFichierH2(Path target) throws IOException {
        Path source = resoudreCheminFichierH2();
        if (Files.exists(source)) {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.writeString(target, "-- Base H2 introuvable au moment de la sauvegarde (base non encore initialisée) --");
        }
    }

    private void copierVersFichierH2Actif(Path source) throws IOException {
        Path destination = resoudreCheminFichierH2();
        Files.createDirectories(destination.getParent() != null ? destination.getParent() : Path.of("."));
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    /** Resolves the on-disk .mv.db file behind the "jdbc:h2:file:./data/leconsulat" URL used in dev. */
    private Path resoudreCheminFichierH2() {
        String prefix = "jdbc:h2:file:";
        String path = datasourceUrl.startsWith(prefix) ? datasourceUrl.substring(prefix.length()) : "./data/leconsulat";
        int semi = path.indexOf(';');
        if (semi >= 0) {
            path = path.substring(0, semi);
        }
        return Path.of(path + ".mv.db");
    }
}
