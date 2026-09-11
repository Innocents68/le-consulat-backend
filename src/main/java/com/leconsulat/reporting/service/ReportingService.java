package com.leconsulat.reporting.service;

import com.leconsulat.avoir.repository.AvoirRepository;
import com.leconsulat.catalogue.entity.Produit;
import com.leconsulat.catalogue.repository.ProduitRepository;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.common.exception.UnauthorizedException;
import com.leconsulat.common.util.ExcelGenerator;
import com.leconsulat.common.util.PdfGenerator;
import com.leconsulat.depense.repository.DepenseRepository;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.inventaire.entity.LigneInventaire;
import com.leconsulat.inventaire.repository.LigneInventaireRepository;
import com.leconsulat.parametres.service.ParametresService;
import com.leconsulat.reporting.dto.*;
import com.leconsulat.reporting.repository.LigneCommandeRepository;
import com.leconsulat.security.CustomUserDetails;
import com.leconsulat.security.PerimetreGuard;
import com.leconsulat.stock.entity.MouvementStock;
import com.leconsulat.stock.repository.MouvementStockRepository;
import com.leconsulat.vente.entity.Facture;
import com.leconsulat.vente.entity.LigneCommande;
import com.leconsulat.vente.entity.ModePaiement;
import com.leconsulat.vente.repository.FactureRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** §6.9 — Dashboard, Rapport des ventes, Rapport des recettes (Lot 5b). Cloisonné comme
 * Dépenses (RG-101), pas réservé au Super Administrateur comme Gestion financière. Réutilise
 * {@code FactureRepository.sommeRecettes}/{@code AvoirRepository.sommeAvoirsPeriode} (Lot 5a)
 * pour rester strictement cohérent avec la Gestion financière (RG-102).
 *
 * {@code @Transactional} de classe : {@code VenteLigneDto.from()} navigue
 * {@code LigneCommande.commande} (LAZY) au-delà de la simple lecture de l'id, qui redevient
 * inaccessible dès que la session Hibernate ouverte par le repository se referme — même piège que
 * la génération du ticket PDF (Lot 2b) et de l'avoir PDF (Lot 3). */
@Service
@Transactional(readOnly = true)
public class ReportingService {

    private final EtablissementRepository etablissementRepository;
    private final FactureRepository factureRepository;
    private final AvoirRepository avoirRepository;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final ProduitRepository produitRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final LigneInventaireRepository ligneInventaireRepository;
    private final DepenseRepository depenseRepository;
    private final PerimetreGuard perimetreGuard;
    private final ParametresService parametresService;

    public ReportingService(EtablissementRepository etablissementRepository, FactureRepository factureRepository,
                             AvoirRepository avoirRepository, LigneCommandeRepository ligneCommandeRepository,
                             ProduitRepository produitRepository, MouvementStockRepository mouvementStockRepository,
                             LigneInventaireRepository ligneInventaireRepository, DepenseRepository depenseRepository,
                             PerimetreGuard perimetreGuard, ParametresService parametresService) {
        this.etablissementRepository = etablissementRepository;
        this.factureRepository = factureRepository;
        this.avoirRepository = avoirRepository;
        this.ligneCommandeRepository = ligneCommandeRepository;
        this.produitRepository = produitRepository;
        this.mouvementStockRepository = mouvementStockRepository;
        this.ligneInventaireRepository = ligneInventaireRepository;
        this.depenseRepository = depenseRepository;
        this.perimetreGuard = perimetreGuard;
        this.parametresService = parametresService;
    }

    public DashboardReportingDto dashboard(LocalDate dateDebut, LocalDate dateFin, Long etablissementIdDemande) {
        Long etablissementId = resoudreEtablissementIdCible(etablissementIdDemande);
        LocalDateTime debut = debutJournee(dateDebut);
        LocalDateTime fin = finJournee(dateFin);

        List<RepartitionMontantDto> parEtablissement = null;
        BigDecimal caTotal;
        if (etablissementId == null) {
            parEtablissement = etablissementRepository.findByActifTrue().stream()
                    .map(e -> new RepartitionMontantDto(e.getNom(), factureRepository.sommeRecettes(e, debut, fin, null, null)))
                    .toList();
            caTotal = parEtablissement.stream().map(RepartitionMontantDto::montant).reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            caTotal = factureRepository.sommeRecettes(etablissementRepository.findById(etablissementId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Etablissement", etablissementId)), debut, fin, null, null);
        }

        List<Facture> factures = facturesPourPerimetre(etablissementId, debut, fin, null, null);
        List<EvolutionCaDto> evolutionCA = factures.stream()
                .collect(Collectors.groupingBy(f -> f.getDateEmission().toLocalDate(), TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Facture::getMontantNet, BigDecimal::add)))
                .entrySet().stream().map(en -> new EvolutionCaDto(en.getKey(), en.getValue())).toList();
        List<RepartitionMontantDto> parModePaiement = factures.stream()
                .collect(Collectors.groupingBy(Facture::getMode,
                        Collectors.reducing(BigDecimal.ZERO, Facture::getMontantNet, BigDecimal::add)))
                .entrySet().stream().map(en -> new RepartitionMontantDto(en.getKey().name(), en.getValue())).toList();

        List<TopProduitDto> topProduits = ligneCommandeRepository.topProduits(etablissementId, debut, fin, PageRequest.of(0, 10))
                .stream().map(row -> new TopProduitDto((Long) row[0], (String) row[1], (Long) row[2], (BigDecimal) row[3]))
                .toList();

        return new DashboardReportingDto(dateDebut, dateFin, caTotal, evolutionCA, parEtablissement, parModePaiement,
                topProduits, currentUtilisateurNom(), LocalDateTime.now());
    }

    public Page<VenteLigneDto> ventes(Long etablissementIdDemande, Long categorieId, Long produitId, Long utilisateurId,
                                       String modePaiement, LocalDate dateDebut, LocalDate dateFin, Pageable pageable) {
        Long etablissementId = resoudreEtablissementIdCible(etablissementIdDemande);
        ModePaiement mode = modePaiement != null ? parseMode(modePaiement) : null;
        return ligneCommandeRepository.searchVentes(etablissementId, debutJournee(dateDebut), finJournee(dateFin),
                categorieId, produitId, utilisateurId, mode, pageable).map(VenteLigneDto::from);
    }

    public RapportVentesResumeDto resumeVentes(Long etablissementIdDemande, Long categorieId, Long produitId, Long utilisateurId,
                                                String modePaiement, LocalDate dateDebut, LocalDate dateFin) {
        List<LigneCommande> lignes = toutesLesVentes(etablissementIdDemande, categorieId, produitId, utilisateurId, modePaiement, dateDebut, dateFin);
        long totalQuantite = lignes.stream().mapToLong(LigneCommande::getQuantite).sum();
        BigDecimal totalMontant = lignes.stream().map(LigneCommande::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
        long nbCommandes = lignes.stream().map(l -> l.getCommande().getId()).distinct().count();
        BigDecimal panierMoyen = nbCommandes > 0
                ? totalMontant.divide(BigDecimal.valueOf(nbCommandes), 0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new RapportVentesResumeDto(dateDebut, dateFin, totalQuantite, totalMontant, panierMoyen,
                currentUtilisateurNom(), LocalDateTime.now());
    }

    public RapportRecettesDto recettes(Long etablissementIdDemande, Long utilisateurId, String modePaiement,
                                        LocalDate dateDebut, LocalDate dateFin) {
        Long etablissementId = resoudreEtablissementIdCible(etablissementIdDemande);
        ModePaiement mode = modePaiement != null ? parseMode(modePaiement) : null;
        LocalDateTime debut = debutJournee(dateDebut);
        LocalDateTime fin = finJournee(dateFin);

        List<Facture> factures = facturesPourPerimetre(etablissementId, debut, fin, utilisateurId, mode);

        List<RecettesParJourDto> parJour = factures.stream()
                .collect(Collectors.groupingBy(f -> f.getDateEmission().toLocalDate(), TreeMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(en -> new RecettesParJourDto(en.getKey(),
                        somme(en.getValue(), Facture::getMontantBrut),
                        somme(en.getValue(), Facture::getRemise),
                        somme(en.getValue(), Facture::getMontantNet)))
                .toList();
        List<RepartitionMontantDto> parMode = factures.stream()
                .collect(Collectors.groupingBy(Facture::getMode,
                        Collectors.reducing(BigDecimal.ZERO, Facture::getMontantNet, BigDecimal::add)))
                .entrySet().stream().map(en -> new RepartitionMontantDto(en.getKey().name(), en.getValue())).toList();
        List<RepartitionMontantDto> parUtilisateur = factures.stream()
                .collect(Collectors.groupingBy(f -> f.getCommande().getCaissier().getNom(),
                        Collectors.reducing(BigDecimal.ZERO, Facture::getMontantNet, BigDecimal::add)))
                .entrySet().stream().map(en -> new RepartitionMontantDto(en.getKey(), en.getValue())).toList();

        BigDecimal avoirsTotal = avoirsPourPerimetre(etablissementId, debut, fin, utilisateurId);
        BigDecimal totalNet = factures.stream().map(Facture::getMontantNet).reduce(BigDecimal.ZERO, BigDecimal::add).subtract(avoirsTotal);

        return new RapportRecettesDto(dateDebut, dateFin, parJour, parMode, parUtilisateur, avoirsTotal, totalNet,
                currentUtilisateurNom(), LocalDateTime.now());
    }

    public byte[] exporterDashboard(String format, LocalDate dateDebut, LocalDate dateFin, Long etablissementIdDemande) {
        DashboardReportingDto dto = dashboard(dateDebut, dateFin, etablissementIdDemande);
        String[] headers = {"Produit", "Quantité vendue", "Montant (FCFA)"};
        List<String[]> rows = dto.topProduits().stream()
                .map(t -> new String[]{t.produitNom(), String.valueOf(t.quantite()), t.montant().toPlainString()})
                .toList();
        if (isExcel(format)) {
            return ExcelGenerator.simpleSheet("Dashboard", headers, rows);
        }
        List<String[]> lines = new ArrayList<>();
        lines.add(new String[]{"Période", periodeTexte(dateDebut, dateFin)});
        lines.add(new String[]{"Chiffre d'affaires total", dto.caTotal().toPlainString() + " FCFA"});
        lines.add(new String[]{"Généré par", dto.genereParNom()});
        lines.add(new String[]{"Date de génération", dto.dateGeneration().toString()});
        return PdfGenerator.simpleDocument(parametresService.getEntity().getNomMagasin(), "Dashboard Reporting — Top produits", lines, headers, rows);
    }

    public byte[] exporterVentes(String format, Long etablissementIdDemande, Long categorieId, Long produitId, Long utilisateurId,
                                  String modePaiement, LocalDate dateDebut, LocalDate dateFin) {
        List<LigneCommande> lignes = toutesLesVentes(etablissementIdDemande, categorieId, produitId, utilisateurId, modePaiement, dateDebut, dateFin);
        RapportVentesResumeDto resume = resumeVentes(etablissementIdDemande, categorieId, produitId, utilisateurId, modePaiement, dateDebut, dateFin);
        String[] headers = {"Date", "Heure", "Produit", "Catégorie", "Table", "Utilisateur", "Quantité", "Montant (FCFA)"};
        List<String[]> rows = lignes.stream().map(VenteLigneDto::from).map(v -> new String[]{
                v.date().toString(), v.heure().toString(), v.produitNom(), v.categorieNom(),
                v.tableNumero() != null ? v.tableNumero() : "—", v.utilisateurNom(),
                String.valueOf(v.quantite()), v.montant().toPlainString(),
        }).toList();
        if (isExcel(format)) {
            return ExcelGenerator.simpleSheet("Rapport des ventes", headers, rows);
        }
        List<String[]> lines = List.of(
                new String[]{"Période", periodeTexte(dateDebut, dateFin)},
                new String[]{"Quantité totale", String.valueOf(resume.totalQuantite())},
                new String[]{"Montant total", resume.totalMontant().toPlainString() + " FCFA"},
                new String[]{"Panier moyen", resume.panierMoyen().toPlainString() + " FCFA"},
                new String[]{"Généré par", resume.genereParNom()},
                new String[]{"Date de génération", resume.dateGeneration().toString()});
        return PdfGenerator.simpleDocument(parametresService.getEntity().getNomMagasin(), "Rapport des ventes", lines, headers, rows);
    }

    public byte[] exporterRecettes(String format, Long etablissementIdDemande, Long utilisateurId, String modePaiement,
                                    LocalDate dateDebut, LocalDate dateFin) {
        RapportRecettesDto dto = recettes(etablissementIdDemande, utilisateurId, modePaiement, dateDebut, dateFin);
        String[] headers = {"Jour", "Brut (FCFA)", "Remises (FCFA)", "Net (FCFA)"};
        List<String[]> rows = dto.parJour().stream()
                .map(j -> new String[]{j.date().toString(), j.brut().toPlainString(), j.remises().toPlainString(), j.net().toPlainString()})
                .toList();
        if (isExcel(format)) {
            return ExcelGenerator.simpleSheet("Rapport des recettes", headers, rows);
        }
        List<String[]> lines = new ArrayList<>();
        lines.add(new String[]{"Période", periodeTexte(dateDebut, dateFin)});
        lines.add(new String[]{"Avoirs déduits", dto.avoirsTotal().toPlainString() + " FCFA"});
        lines.add(new String[]{"Total net", dto.totalNet().toPlainString() + " FCFA"});
        lines.add(new String[]{"Généré par", dto.genereParNom()});
        lines.add(new String[]{"Date de génération", dto.dateGeneration().toString()});
        return PdfGenerator.simpleDocument(parametresService.getEntity().getNomMagasin(), "Rapport des recettes", lines, headers, rows);
    }

    /** §6.9.3 : stock actuel (indépendant de la période), mouvements et écarts d'inventaire de
     * la période choisie. */
    public RapportStockDto stocks(LocalDate dateDebut, LocalDate dateFin, Long etablissementIdDemande, Long categorieId) {
        Long etablissementId = resoudreEtablissementIdCible(etablissementIdDemande);
        LocalDateTime debut = debutJournee(dateDebut);
        LocalDateTime fin = finJournee(dateFin);

        List<Etablissement> etablissements = etablissementId != null
                ? List.of(etablissementRepository.findById(etablissementId).orElseThrow(() -> ResourceNotFoundException.of("Etablissement", etablissementId)))
                : etablissementRepository.findByActifTrue();

        List<StockLigneDto> stockActuel = etablissements.stream()
                .flatMap(e -> produitRepository.findByEtablissementAndActifTrueAndSuiviStockTrue(e).stream())
                .filter(p -> categorieId == null || categorieId.equals(p.getCategorie().getId()))
                .map(this::versStockLigne)
                .toList();
        BigDecimal valorisationTotale = stockActuel.stream().map(StockLigneDto::valorisation)
                .filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        long nombreRuptures = stockActuel.stream().filter(StockLigneDto::rupture).count();
        long nombreSousSeuil = stockActuel.stream().filter(StockLigneDto::sousSeuil).count();

        List<MouvementStock> mouvements = etablissements.stream()
                .flatMap(e -> mouvementStockRepository.findForReporting(e, debut, fin).stream())
                .toList();
        List<MouvementTypeDto> mouvementsParType = mouvements.stream()
                .collect(Collectors.groupingBy(m -> m.getType().name(),
                        Collectors.reducing(BigDecimal.ZERO, MouvementStock::getQuantite, BigDecimal::add)))
                .entrySet().stream().map(en -> new MouvementTypeDto(en.getKey(), en.getValue())).toList();

        List<EcartInventaireDto> ecartsInventaire = etablissements.stream()
                .flatMap(e -> ligneInventaireRepository.findEcarts(e.getId(), debut, fin).stream())
                .map(this::versEcartInventaire)
                .toList();

        return new RapportStockDto(dateDebut, dateFin, stockActuel, valorisationTotale, nombreRuptures, nombreSousSeuil,
                mouvementsParType, ecartsInventaire, currentUtilisateurNom(), LocalDateTime.now());
    }

    /** §6.9.3 (Rapport des bénéfices) : CA, coût des marchandises vendues, marge brute, dépenses,
     * résultat — par établissement, réutilisant les mêmes sommes que Gestion financière (RG-102). */
    public RapportBeneficesDto benefices(LocalDate dateDebut, LocalDate dateFin, Long etablissementIdDemande) {
        Long etablissementId = resoudreEtablissementIdCible(etablissementIdDemande);
        LocalDateTime debut = debutJournee(dateDebut);
        LocalDateTime fin = finJournee(dateFin);

        List<Etablissement> etablissements = etablissementId != null
                ? List.of(etablissementRepository.findById(etablissementId).orElseThrow(() -> ResourceNotFoundException.of("Etablissement", etablissementId)))
                : etablissementRepository.findByActifTrue();

        List<BeneficeEtablissementDto> lignes = etablissements.stream().map(e -> {
            BigDecimal ca = factureRepository.sommeRecettes(e, debut, fin, null, null);
            BigDecimal cmv = ligneCommandeRepository.sommeCoutMarchandisesVendues(e.getId(), debut, fin);
            BigDecimal depenses = depenseRepository.sommeDepenses(e, dateDebut, dateFin, null, null);
            return BeneficeEtablissementDto.of(e.getId(), e.getNom(), ca, cmv, depenses);
        }).toList();

        BeneficeEtablissementDto total = null;
        if (etablissementId == null) {
            BigDecimal ca = lignes.stream().map(BeneficeEtablissementDto::chiffreAffaires).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal cmv = lignes.stream().map(BeneficeEtablissementDto::cmv).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal depenses = lignes.stream().map(BeneficeEtablissementDto::depenses).reduce(BigDecimal.ZERO, BigDecimal::add);
            total = BeneficeEtablissementDto.of(null, "TOTAL", ca, cmv, depenses);
        }

        return new RapportBeneficesDto(dateDebut, dateFin, lignes, total, currentUtilisateurNom(), LocalDateTime.now());
    }

    public byte[] exporterStocks(String format, LocalDate dateDebut, LocalDate dateFin, Long etablissementIdDemande, Long categorieId) {
        RapportStockDto dto = stocks(dateDebut, dateFin, etablissementIdDemande, categorieId);
        String[] headers = {"Produit", "Catégorie", "Stock", "Seuil", "Valorisation (FCFA)", "Statut"};
        List<String[]> rows = dto.stockActuel().stream().map(s -> new String[]{
                s.produitNom(), s.categorieNom(), s.quantiteStock().toPlainString(),
                s.seuilAlerte() != null ? s.seuilAlerte().toPlainString() : "—",
                s.valorisation() != null ? s.valorisation().toPlainString() : "—",
                s.rupture() ? "Rupture" : s.sousSeuil() ? "Sous seuil" : "OK",
        }).toList();
        if (isExcel(format)) {
            return ExcelGenerator.simpleSheet("Rapport des stocks", headers, rows);
        }
        List<String[]> lines = new ArrayList<>();
        lines.add(new String[]{"Période (mouvements/écarts)", periodeTexte(dateDebut, dateFin)});
        lines.add(new String[]{"Valorisation totale", dto.valorisationTotale().toPlainString() + " FCFA"});
        lines.add(new String[]{"Ruptures", String.valueOf(dto.nombreRuptures())});
        lines.add(new String[]{"Sous seuil", String.valueOf(dto.nombreSousSeuil())});
        lines.add(new String[]{"Généré par", dto.genereParNom()});
        lines.add(new String[]{"Date de génération", dto.dateGeneration().toString()});
        return PdfGenerator.simpleDocument(parametresService.getEntity().getNomMagasin(), "Rapport des stocks", lines, headers, rows);
    }

    public byte[] exporterBenefices(String format, LocalDate dateDebut, LocalDate dateFin, Long etablissementIdDemande) {
        RapportBeneficesDto dto = benefices(dateDebut, dateFin, etablissementIdDemande);
        String[] headers = {"Établissement", "CA", "CMV", "Marge brute", "Dépenses", "Résultat"};
        List<String[]> rows = new ArrayList<>(dto.lignes().stream().map(l -> new String[]{
                l.etablissementNom(), l.chiffreAffaires().toPlainString(), l.cmv().toPlainString(),
                l.margeBrute().toPlainString(), l.depenses().toPlainString(), l.resultat().toPlainString(),
        }).toList());
        if (dto.total() != null) {
            var t = dto.total();
            rows.add(new String[]{t.etablissementNom(), t.chiffreAffaires().toPlainString(), t.cmv().toPlainString(),
                    t.margeBrute().toPlainString(), t.depenses().toPlainString(), t.resultat().toPlainString()});
        }
        if (isExcel(format)) {
            return ExcelGenerator.simpleSheet("Rapport des bénéfices", headers, rows);
        }
        List<String[]> lines = List.of(
                new String[]{"Période", periodeTexte(dateDebut, dateFin)},
                new String[]{"Généré par", dto.genereParNom()},
                new String[]{"Date de génération", dto.dateGeneration().toString()});
        return PdfGenerator.simpleDocument(parametresService.getEntity().getNomMagasin(), "Rapport des bénéfices", lines, headers, rows);
    }

    private StockLigneDto versStockLigne(Produit p) {
        BigDecimal valorisation = p.getPrixAchat() != null ? p.getQuantiteStock().multiply(p.getPrixAchat()) : null;
        boolean rupture = p.getQuantiteStock().compareTo(BigDecimal.ZERO) <= 0;
        boolean sousSeuil = !rupture && p.getSeuilAlerte() != null && p.getQuantiteStock().compareTo(p.getSeuilAlerte()) <= 0;
        return new StockLigneDto(p.getId(), p.getNom(), p.getCategorie().getNom(), p.getQuantiteStock(),
                p.getSeuilAlerte(), valorisation, rupture, sousSeuil);
    }

    private EcartInventaireDto versEcartInventaire(LigneInventaire l) {
        return new EcartInventaireDto(l.getInventaire().getNumero(), l.getInventaire().getDateValidation(),
                l.getProduitNom(), l.getEcartQuantite(), l.getEcartValeur());
    }

    private List<LigneCommande> toutesLesVentes(Long etablissementIdDemande, Long categorieId, Long produitId, Long utilisateurId,
                                                 String modePaiement, LocalDate dateDebut, LocalDate dateFin) {
        Long etablissementId = resoudreEtablissementIdCible(etablissementIdDemande);
        ModePaiement mode = modePaiement != null ? parseMode(modePaiement) : null;
        return ligneCommandeRepository.findAllVentes(etablissementId, debutJournee(dateDebut), finJournee(dateFin),
                categorieId, produitId, utilisateurId, mode);
    }

    private List<Facture> facturesPourPerimetre(Long etablissementId, LocalDateTime debut, LocalDateTime fin, Long utilisateurId, ModePaiement mode) {
        if (etablissementId != null) {
            Etablissement e = etablissementRepository.findById(etablissementId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Etablissement", etablissementId));
            return factureRepository.findForReporting(e, debut, fin, utilisateurId, mode);
        }
        return etablissementRepository.findByActifTrue().stream()
                .flatMap(e -> factureRepository.findForReporting(e, debut, fin, utilisateurId, mode).stream())
                .toList();
    }

    private BigDecimal avoirsPourPerimetre(Long etablissementId, LocalDateTime debut, LocalDateTime fin, Long utilisateurId) {
        if (etablissementId != null) {
            Etablissement e = etablissementRepository.findById(etablissementId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Etablissement", etablissementId));
            return avoirRepository.sommeAvoirsPeriode(e, debut, fin, utilisateurId);
        }
        return etablissementRepository.findByActifTrue().stream()
                .map(e -> avoirRepository.sommeAvoirsPeriode(e, debut, fin, utilisateurId))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal somme(List<Facture> factures, java.util.function.Function<Facture, BigDecimal> extracteur) {
        return factures.stream().map(extracteur).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isExcel(String format) {
        return "excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format);
    }

    private String periodeTexte(LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut == null && dateFin == null) {
            return "Toutes périodes";
        }
        return (dateDebut != null ? dateDebut : "…") + " au " + (dateFin != null ? dateFin : "…");
    }

    private LocalDateTime debutJournee(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    private LocalDateTime finJournee(LocalDate date) {
        return date != null ? date.atTime(23, 59, 59) : null;
    }

    private ModePaiement parseMode(String mode) {
        try {
            return ModePaiement.valueOf(mode.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Mode de paiement inconnu : " + mode);
        }
    }

    /** RG-101 : un Gérant/Caissier est toujours forcé sur son établissement (jamais nul) ; le
     * Super Administrateur obtient {@code null} pour la vue globale comparée s'il ne précise
     * rien. */
    private Long resoudreEtablissementIdCible(Long etablissementIdDemande) {
        Etablissement demande = etablissementIdDemande != null
                ? etablissementRepository.findById(etablissementIdDemande)
                        .orElseThrow(() -> ResourceNotFoundException.of("Etablissement", etablissementIdDemande))
                : null;
        Etablissement cible = perimetreGuard.scopeEtablissement(demande);
        return cible != null ? cible.getId() : null;
    }

    private String currentUtilisateurNom() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUtilisateur().getNom();
        }
        throw new UnauthorizedException("Utilisateur non authentifié");
    }
}
