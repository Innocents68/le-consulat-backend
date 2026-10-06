package com.leconsulat.journal.service;

import com.leconsulat.common.util.ExcelGenerator;
import com.leconsulat.common.util.PdfGenerator;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.journal.dto.JournalOperationDto;
import com.leconsulat.journal.repository.JournalOperationRepository;
import com.leconsulat.security.PerimetreGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/** Lecture seule (RG-019). Consu_corrige.docx §8 ajoute l'export — le reste (recherche paginée)
 * vivait jusqu'ici directement dans le contrôleur ; regroupé ici pour rester cohérent avec le
 * pattern service/contrôleur du reste de l'application, et pour partager le filtrage entre
 * {@code list()} et {@code exporter()}.
 * Nommé "Query" (pas simplement JournalOperationService) pour ne pas entrer en collision de nom
 * de bean avec {@link com.leconsulat.common.audit.JournalOperationService}, le service d'audit
 * (celui qui écrit dans le journal) déjà utilisé partout ailleurs — Spring nomme ses beans d'après
 * le nom de classe simple, indépendamment du package. */
@Service
public class JournalOperationQueryService {

    private final JournalOperationRepository repository;
    private final EtablissementRepository etablissementRepository;
    private final PerimetreGuard perimetreGuard;

    public JournalOperationQueryService(JournalOperationRepository repository, EtablissementRepository etablissementRepository,
                                    PerimetreGuard perimetreGuard) {
        this.repository = repository;
        this.etablissementRepository = etablissementRepository;
        this.perimetreGuard = perimetreGuard;
    }

    public Page<JournalOperationDto> search(Long utilisateurId, String module, Long etablissementIdDemande,
                                             LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable) {
        Long scopeId = resoudreEtablissementIdCible(etablissementIdDemande);
        return repository.search(utilisateurId, module, scopeId, dateDebut, dateFin, pageable).map(JournalOperationDto::from);
    }

    public byte[] exporter(String format, Long utilisateurId, String module, Long etablissementIdDemande,
                            LocalDateTime dateDebut, LocalDateTime dateFin) {
        Long scopeId = resoudreEtablissementIdCible(etablissementIdDemande);
        List<JournalOperationDto> lignes = repository
                .search(utilisateurId, module, scopeId, dateDebut, dateFin, Pageable.unpaged())
                .map(JournalOperationDto::from)
                .getContent();
        String[] headers = {"Date", "Utilisateur", "Établissement", "Module", "Action", "Détails"};
        List<String[]> rows = lignes.stream().map(j -> new String[]{
                j.dateOperation().toString(), j.utilisateurNom(), j.etablissementNom() != null ? j.etablissementNom() : "—",
                j.module(), j.action(), j.details() != null ? j.details() : "",
        }).toList();
        if ("excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format)) {
            return ExcelGenerator.simpleSheet("Journal des opérations", headers, rows);
        }
        return PdfGenerator.simpleDocument("Journal des opérations", List.of(), headers, rows);
    }

    private Long resoudreEtablissementIdCible(Long etablissementIdDemande) {
        Etablissement demande = etablissementIdDemande != null
                ? etablissementRepository.findById(etablissementIdDemande).orElse(null)
                : null;
        Etablissement scope = perimetreGuard.scopeEtablissement(demande);
        return scope != null ? scope.getId() : null;
    }
}
