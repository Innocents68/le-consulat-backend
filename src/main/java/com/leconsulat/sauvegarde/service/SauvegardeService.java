package com.leconsulat.sauvegarde.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.common.exception.UnauthorizedException;
import com.leconsulat.sauvegarde.dto.RestaurerRequest;
import com.leconsulat.sauvegarde.dto.SauvegardeDto;
import com.leconsulat.sauvegarde.entity.Sauvegarde;
import com.leconsulat.sauvegarde.entity.StatutSauvegarde;
import com.leconsulat.sauvegarde.entity.TypeSauvegarde;
import com.leconsulat.sauvegarde.repository.SauvegardeRepository;
import com.leconsulat.security.CustomUserDetails;
import com.leconsulat.security.TokenInvalidationRegistry;
import com.leconsulat.utilisateur.entity.Utilisateur;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** §6.10.2 — EF-044/045, RG-106. Exécute {@code pg_dump}/{@code pg_restore} en sous-processus
 * (déjà présents sur la machine, mêmes identifiants que {@code spring.datasource.*}). */
@Service
public class SauvegardeService {

    /** RG-106 : phrase exacte exigée en plus de la double confirmation côté écran. */
    public static final String PHRASE_CONFIRMATION = "RESTAURER";

    private static final DateTimeFormatter HORODATAGE = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Pattern JDBC_URL = Pattern.compile("jdbc:postgresql://([^:/]+):(\\d+)/([^?]+)");

    private final SauvegardeRepository repository;
    private final JournalOperationService journal;
    private final TokenInvalidationRegistry tokenInvalidationRegistry;
    private final String backupsDir;
    private final String binDir;
    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;

    public SauvegardeService(SauvegardeRepository repository, JournalOperationService journal,
                              TokenInvalidationRegistry tokenInvalidationRegistry,
                              @Value("${app.backups.dir}") String backupsDir,
                              @Value("${app.postgres.bin-dir:}") String binDir,
                              @Value("${spring.datasource.url}") String jdbcUrl,
                              @Value("${spring.datasource.username}") String username,
                              @Value("${spring.datasource.password}") String password) {
        this.repository = repository;
        this.journal = journal;
        this.tokenInvalidationRegistry = tokenInvalidationRegistry;
        this.backupsDir = backupsDir;
        this.binDir = binDir;
        this.username = username;
        this.password = password;

        Matcher m = JDBC_URL.matcher(jdbcUrl);
        if (!m.find()) {
            throw new IllegalStateException("URL JDBC PostgreSQL inattendue, impossible d'en extraire hôte/port/base : " + jdbcUrl);
        }
        this.host = m.group(1);
        this.port = m.group(2);
        this.database = m.group(3);
    }

    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    public Page<SauvegardeDto> lister(Pageable pageable) {
        return repository.findAllByOrderByDateCreationDesc(pageable).map(SauvegardeDto::from);
    }

    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    @Transactional
    public SauvegardeDto declencherManuelle() {
        return declencher(TypeSauvegarde.MANUELLE, currentUtilisateur());
    }

    /** Appelée aussi par le planificateur quotidien (EF-045), sans utilisateur (nul). */
    @Transactional
    public SauvegardeDto declencher(TypeSauvegarde type, Utilisateur auteur) {
        try {
            Files.createDirectories(Path.of(backupsDir));
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de créer le répertoire de sauvegardes", e);
        }
        String nomFichier = "leconsulat_" + LocalDateTime.now().format(HORODATAGE) + ".dump";
        Path cible = Path.of(backupsDir, nomFichier);

        Sauvegarde s = new Sauvegarde();
        s.setNomFichier(nomFichier);
        s.setType(type);
        s.setAuteur(auteur);

        ProcessBuilder pb = new ProcessBuilder(executable("pg_dump"),
                "-h", host, "-p", port, "-U", username, "-Fc", "-f", cible.toString(), database);
        pb.environment().put("PGPASSWORD", password);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            String sortie = new String(process.getInputStream().readAllBytes());
            boolean termine = process.waitFor(5, TimeUnit.MINUTES);
            if (termine && process.exitValue() == 0) {
                s.setStatut(StatutSauvegarde.REUSSIE);
                s.setTailleOctets(Files.exists(cible) ? Files.size(cible) : null);
            } else {
                s.setStatut(StatutSauvegarde.ECHOUEE);
                s.setMessage(termine ? "pg_dump a échoué (code " + process.exitValue() + ") : " + tronquer(sortie)
                        : "pg_dump n'a pas terminé dans le délai imparti");
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            s.setStatut(StatutSauvegarde.ECHOUEE);
            s.setMessage("Erreur lors de l'exécution de pg_dump : " + e.getMessage());
        }

        Sauvegarde saved = repository.save(s);
        journal.enregistrer("SYSTEME", "SAUVEGARDE", "Sauvegarde " + type.name().toLowerCase()
                + " " + (saved.getStatut() == StatutSauvegarde.REUSSIE ? "réussie" : "échouée") + " : " + nomFichier);
        return SauvegardeDto.from(saved);
    }

    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    public byte[] telecharger(Long id) {
        Sauvegarde s = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Sauvegarde", id));
        Path fichier = Path.of(backupsDir, s.getNomFichier());
        if (!Files.exists(fichier)) {
            throw new BusinessRuleException("Le fichier de sauvegarde n'existe plus sur le serveur");
        }
        try {
            return Files.readAllBytes(fichier);
        } catch (IOException e) {
            throw new UncheckedIOException("Erreur lors de la lecture du fichier de sauvegarde", e);
        }
    }

    public String nomFichier(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Sauvegarde", id)).getNomFichier();
    }

    /** RG-106 : opération critique — double confirmation (écran + phrase exacte revérifiée ici),
     * journalisée, déconnecte tous les utilisateurs. */
    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    @Transactional
    public void restaurerDepuisHistorique(Long id, RestaurerRequest req) {
        verifierConfirmation(req);
        Sauvegarde s = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Sauvegarde", id));
        Path fichier = Path.of(backupsDir, s.getNomFichier());
        if (!Files.exists(fichier)) {
            throw new BusinessRuleException("Le fichier de sauvegarde n'existe plus sur le serveur");
        }
        executerRestauration(fichier, s.getNomFichier());
    }

    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    @Transactional
    public void restaurerDepuisFichier(MultipartFile fichierUploade, RestaurerRequest req) {
        verifierConfirmation(req);
        if (fichierUploade == null || fichierUploade.isEmpty()) {
            throw new BusinessRuleException("Aucun fichier fourni");
        }
        try {
            Path temp = Files.createTempFile("restauration_", ".dump");
            fichierUploade.transferTo(temp);
            try {
                executerRestauration(temp, fichierUploade.getOriginalFilename());
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Erreur lors de l'enregistrement du fichier à restaurer", e);
        }
    }

    private void verifierConfirmation(RestaurerRequest req) {
        if (!PHRASE_CONFIRMATION.equals(req.confirmation())) {
            throw new BusinessRuleException("Confirmation invalide — tapez exactement « " + PHRASE_CONFIRMATION + " » pour restaurer");
        }
    }

    /** Journalise avant l'exécution : cette ligne sera elle-même écrasée par le contenu restauré
     * (comportement inhérent à toute restauration complète — le journal reflète après coup l'état
     * de la sauvegarde restaurée, pas les événements survenus entre-temps). */
    private void executerRestauration(Path fichier, String nomAffiche) {
        journal.enregistrer("SYSTEME", "RESTAURATION_DEMARREE", "Restauration à partir de " + nomAffiche
                + " par " + currentUtilisateur().getNom() + " — toutes les sessions vont être déconnectées");

        ProcessBuilder pb = new ProcessBuilder(executable("pg_restore"),
                "-h", host, "-p", port, "-U", username, "-d", database, "--clean", "--if-exists", fichier.toString());
        pb.environment().put("PGPASSWORD", password);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            String sortie = new String(process.getInputStream().readAllBytes());
            boolean termine = process.waitFor(10, TimeUnit.MINUTES);
            if (!termine) {
                throw new BusinessRuleException("La restauration n'a pas terminé dans le délai imparti");
            }
            // pg_restore peut renvoyer un code non nul pour de simples avertissements (objets déjà
            // absents avec --if-exists) — on ne bloque que si la sortie ne contient aucune trace
            // d'avoir restauré quoi que ce soit ET que le code est non nul.
            if (process.exitValue() != 0) {
                throw new BusinessRuleException("pg_restore a signalé des erreurs (code " + process.exitValue() + ") : " + tronquer(sortie));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Erreur lors de l'exécution de pg_restore", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessRuleException("Restauration interrompue");
        }

        tokenInvalidationRegistry.invaliderToutesLesSessions();
    }

    private String executable(String nom) {
        return binDir == null || binDir.isBlank() ? nom : Path.of(binDir, nom).toString();
    }

    private String tronquer(String texte) {
        return texte.length() > 400 ? texte.substring(0, 400) + "..." : texte;
    }

    private Utilisateur currentUtilisateur() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUtilisateur();
        }
        throw new UnauthorizedException("Utilisateur non authentifié");
    }
}
