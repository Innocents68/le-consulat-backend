package com.leconsulat.tableaudebord.service;

import com.leconsulat.depense.repository.DepenseRepository;
import com.leconsulat.catalogue.repository.ProduitRepository;
import com.leconsulat.common.exception.UnauthorizedException;
import com.leconsulat.etablissement.dto.EtablissementDto;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.journal.dto.JournalOperationDto;
import com.leconsulat.journal.repository.JournalOperationRepository;
import com.leconsulat.reporting.dto.RepartitionMontantDto;
import com.leconsulat.security.CustomUserDetails;
import com.leconsulat.security.PerimetreGuard;
import com.leconsulat.tableaudebord.dto.TableauDeBordDto;
import com.leconsulat.tableaudebord.dto.VentesJourDto;
import com.leconsulat.utilisateur.repository.UtilisateurRepository;
import com.leconsulat.vente.entity.Facture;
import com.leconsulat.vente.repository.FactureRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** §6.1 — page d'accueil, distincte du « Dashboard reporting » (Lot 5b). Pure agrégation en
 * lecture, aucune donnée persistée propre à ce module. */
@Service
public class TableauDeBordService {

    private static final int JOURS_EVOLUTION = 7;

    private final EtablissementRepository etablissementRepository;
    private final FactureRepository factureRepository;
    private final DepenseRepository depenseRepository;
    private final ProduitRepository produitRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final JournalOperationRepository journalOperationRepository;
    private final PerimetreGuard perimetreGuard;

    public TableauDeBordService(EtablissementRepository etablissementRepository, FactureRepository factureRepository,
                                 DepenseRepository depenseRepository, ProduitRepository produitRepository,
                                 UtilisateurRepository utilisateurRepository, JournalOperationRepository journalOperationRepository,
                                 PerimetreGuard perimetreGuard) {
        this.etablissementRepository = etablissementRepository;
        this.factureRepository = factureRepository;
        this.depenseRepository = depenseRepository;
        this.produitRepository = produitRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.journalOperationRepository = journalOperationRepository;
        this.perimetreGuard = perimetreGuard;
    }

    public TableauDeBordDto get() {
        boolean superAdmin = perimetreGuard.isSuperAdmin();
        List<Etablissement> etablissements = superAdmin
                ? etablissementRepository.findByActifTrue()
                : List.of(perimetreGuard.scopeEtablissement(null));

        LocalDate aujourdhui = LocalDate.now();
        LocalDate hier = aujourdhui.minusDays(1);

        BigDecimal ventesAuj = BigDecimal.ZERO;
        BigDecimal ventesHier = BigDecimal.ZERO;
        BigDecimal depensesAuj = BigDecimal.ZERO;
        BigDecimal depensesHier = BigDecimal.ZERO;
        long stockNombreArticles = 0;
        for (Etablissement e : etablissements) {
            ventesAuj = ventesAuj.add(factureRepository.sommeRecettes(e, aujourdhui.atStartOfDay(), aujourdhui.atTime(23, 59, 59), null, null));
            ventesHier = ventesHier.add(factureRepository.sommeRecettes(e, hier.atStartOfDay(), hier.atTime(23, 59, 59), null, null));
            depensesAuj = depensesAuj.add(depenseRepository.sommeDepenses(e, aujourdhui, aujourdhui, null, null));
            depensesHier = depensesHier.add(depenseRepository.sommeDepenses(e, hier, hier, null, null));
            stockNombreArticles += produitRepository.findByEtablissementAndActifTrueAndSuiviStockTrue(e).size();
        }

        Long utilisateursActifs = null;
        Long utilisateursTotal = null;
        if (superAdmin) {
            utilisateursActifs = utilisateurRepository.countByActifTrue();
            utilisateursTotal = utilisateurRepository.count();
        }

        LocalDate debutPeriode = aujourdhui.minusDays(JOURS_EVOLUTION - 1L);
        List<VentesJourDto> evolution = new ArrayList<>();
        Map<String, BigDecimal> totauxParEtablissement = new LinkedHashMap<>();
        for (Etablissement e : etablissements) {
            List<Facture> factures = factureRepository.findForReporting(e, debutPeriode.atStartOfDay(), aujourdhui.atTime(23, 59, 59), null, null);
            Map<LocalDate, BigDecimal> parJour = factures.stream()
                    .collect(Collectors.groupingBy(f -> f.getDateEmission().toLocalDate(),
                            Collectors.reducing(BigDecimal.ZERO, Facture::getMontantNet, BigDecimal::add)));
            for (LocalDate d = debutPeriode; !d.isAfter(aujourdhui); d = d.plusDays(1)) {
                evolution.add(new VentesJourDto(e.getNom(), d, parJour.getOrDefault(d, BigDecimal.ZERO)));
            }
            totauxParEtablissement.put(e.getNom(), parJour.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        }

        // La répartition n'a de sens qu'en comparant plusieurs établissements.
        List<RepartitionMontantDto> repartition = superAdmin
                ? totauxParEtablissement.entrySet().stream().map(en -> new RepartitionMontantDto(en.getKey(), en.getValue())).toList()
                : null;

        Long etablissementIdJournal = superAdmin ? null : etablissements.get(0).getId();
        List<JournalOperationDto> dernieresOperations = journalOperationRepository
                .search(null, null, etablissementIdJournal, null, null, PageRequest.of(0, 5))
                .map(JournalOperationDto::from)
                .getContent();

        return new TableauDeBordDto(ventesAuj, variationPourcent(ventesAuj, ventesHier), stockNombreArticles,
                depensesAuj, variationPourcent(depensesAuj, depensesHier),
                utilisateursActifs, utilisateursTotal,
                etablissements.stream().map(EtablissementDto::from).toList(),
                evolution, repartition, dernieresOperations,
                currentUtilisateurNom(), LocalDateTime.now());
    }

    /** Nul si la valeur d'hier est nulle (évite une division par zéro et un pourcentage fabriqué
     * — le front affiche « — » dans ce cas plutôt qu'une variation arbitraire). */
    private BigDecimal variationPourcent(BigDecimal aujourdhui, BigDecimal hier) {
        if (hier == null || hier.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return aujourdhui.subtract(hier).divide(hier, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);
    }

    private String currentUtilisateurNom() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUtilisateur().getNom();
        }
        throw new UnauthorizedException("Utilisateur non authentifié");
    }
}
