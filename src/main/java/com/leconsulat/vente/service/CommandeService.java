package com.leconsulat.vente.service;

import com.leconsulat.catalogue.entity.Produit;
import com.leconsulat.catalogue.repository.ProduitRepository;
import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.common.exception.UnauthorizedException;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.numerotation.service.NumerotationService;
import com.leconsulat.parametres.service.ParametresService;
import com.leconsulat.remise.entity.Remise;
import com.leconsulat.remise.entity.TypeRemise;
import com.leconsulat.remise.service.RemiseService;
import com.leconsulat.salle.entity.StatutTable;
import com.leconsulat.salle.entity.TableService;
import com.leconsulat.salle.repository.TableServiceRepository;
import com.leconsulat.security.CustomUserDetails;
import com.leconsulat.security.PerimetreGuard;
import com.leconsulat.stock.service.MouvementStockService;
import com.leconsulat.utilisateur.entity.Utilisateur;
import com.leconsulat.vente.dto.*;
import com.leconsulat.vente.entity.*;
import com.leconsulat.vente.repository.CommandeRepository;
import com.leconsulat.vente.repository.FactureRepository;
import com.leconsulat.vente.repository.PaiementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Cœur de la chaîne de vente (§6.2 du CDC) — un seul service pour les 3 établissements
 * (EF-006), le cloisonnement se fait uniquement par le filtre établissement. */
@Service
@Transactional
public class CommandeService {

    private final CommandeRepository repository;
    private final TableServiceRepository tableRepository;
    private final ProduitRepository produitRepository;
    private final EtablissementRepository etablissementRepository;
    private final PaiementRepository paiementRepository;
    private final FactureRepository factureRepository;
    private final NumerotationService numerotationService;
    private final JournalOperationService journal;
    private final PerimetreGuard perimetreGuard;
    private final RemiseService remiseService;
    private final MouvementStockService mouvementStockService;
    private final ParametresService parametresService;

    public CommandeService(CommandeRepository repository, TableServiceRepository tableRepository,
                            ProduitRepository produitRepository, EtablissementRepository etablissementRepository,
                            PaiementRepository paiementRepository, FactureRepository factureRepository,
                            NumerotationService numerotationService, JournalOperationService journal,
                            PerimetreGuard perimetreGuard, RemiseService remiseService,
                            MouvementStockService mouvementStockService, ParametresService parametresService) {
        this.repository = repository;
        this.tableRepository = tableRepository;
        this.produitRepository = produitRepository;
        this.etablissementRepository = etablissementRepository;
        this.paiementRepository = paiementRepository;
        this.factureRepository = factureRepository;
        this.numerotationService = numerotationService;
        this.journal = journal;
        this.perimetreGuard = perimetreGuard;
        this.remiseService = remiseService;
        this.mouvementStockService = mouvementStockService;
        this.parametresService = parametresService;
    }

    public Page<CommandeDto> search(Long etablissementIdDemande, String statut, Long tableId,
                                     LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable) {
        Etablissement cible = resoudreEtablissementCible(etablissementIdDemande);
        StatutCommande s = statut != null ? parseStatut(statut) : null;
        return repository.search(cible, s, tableId, dateDebut, dateFin, pageable).map(CommandeDto::from);
    }

    public CommandeDto get(Long id) {
        return CommandeDto.from(findEntityChecked(id));
    }

    /** Écran Suivi cuisine (§6.3.2, EF-018) — réservé aux établissements avec cuisine
     * (EF-022/EF-024 : Maquis/Cave n'en ont pas). */
    private static final List<StatutCommande> STATUTS_CUISINE =
            List.of(StatutCommande.ENVOYEE_CUISINE, StatutCommande.EN_PREPARATION, StatutCommande.PRETE);

    public List<CommandeDto> suiviCuisine(Long etablissementIdDemande) {
        Etablissement cible = resoudreEtablissementCible(etablissementIdDemande);
        if (!cible.isGereCuisine()) {
            throw new BusinessRuleException("Cet établissement n'a pas de suivi cuisine");
        }
        return repository.findByEtablissementAndStatutInOrderByDateValidationAsc(cible, STATUTS_CUISINE)
                .stream().map(CommandeDto::from).toList();
    }

    /** Avance d'une seule étape (RG-107 : toute transition non décrite est interdite) —
     * ENVOYEE_CUISINE→EN_PREPARATION→PRETE→SERVIE, jamais un saut arbitraire choisi par le
     * client. Libère la table à l'arrivée en SERVIE (RG-072). */
    @Transactional
    public CommandeDto avancerCuisine(Long commandeId) {
        Commande commande = findEntityChecked(commandeId);
        if (!commande.getEtablissement().isGereCuisine()) {
            throw new BusinessRuleException("Cet établissement n'a pas de suivi cuisine");
        }
        StatutCommande suivant = switch (commande.getStatut()) {
            case ENVOYEE_CUISINE -> StatutCommande.EN_PREPARATION;
            case EN_PREPARATION -> StatutCommande.PRETE;
            case PRETE -> StatutCommande.SERVIE;
            default -> throw new BusinessRuleException("Cette commande n'est pas en cours de préparation");
        };
        commande.setStatut(suivant);
        if (suivant == StatutCommande.SERVIE) {
            libererTableSiOccupeeParCetteCommande(commande);
        }
        Commande saved = repository.save(commande);
        journal.enregistrer("CUISINE", "PROGRESSION", "Commande " + saved.getNumero() + " -> " + suivant);
        return CommandeDto.from(saved);
    }

    @Transactional
    public CommandeDto create(CreateCommandeRequest req) {
        Etablissement cible = resoudreEtablissementCible(req.etablissementId());

        Commande commande = new Commande();
        commande.setEtablissement(cible);
        commande.setCaissier(currentUtilisateur());
        commande.setClientNom(req.clientNom());
        commande.setObservations(req.observations());
        commande.setNumero(numerotationService.genererNumero(cible, "CMD"));

        if (req.tableId() != null) {
            TableService table = tableRepository.findById(req.tableId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Table", req.tableId()));
            if (!table.getEtablissement().getId().equals(cible.getId())) {
                throw new BusinessRuleException("Cette table n'appartient pas à cet établissement");
            }
            if (table.getStatut() != StatutTable.LIBRE) {
                throw new BusinessRuleException("Cette table n'est pas libre");
            }
            commande.setTable(table);
            table.setStatut(StatutTable.OCCUPEE);
            tableRepository.save(table);
        }

        for (LigneCommandeInput input : req.lignes()) {
            commande.getLignes().add(construireLigne(commande, cible, input));
        }
        recalculerTotaux(commande);

        Commande saved = repository.save(commande);
        journal.enregistrer("COMMANDES", "CREATION", "Création de la commande " + saved.getNumero());
        return CommandeDto.from(saved);
    }

    @Transactional
    public CommandeDto ajouterLigne(Long commandeId, LigneCommandeInput input) {
        Commande commande = findEntityChecked(commandeId);
        assertModifiable(commande);
        commande.getLignes().add(construireLigne(commande, commande.getEtablissement(), input));
        recalculerTotaux(commande);
        Commande saved = repository.save(commande);
        return CommandeDto.from(saved);
    }

    @Transactional
    public CommandeDto retirerLigne(Long commandeId, Long ligneId) {
        Commande commande = findEntityChecked(commandeId);
        assertModifiable(commande);
        boolean removed = commande.getLignes().removeIf(l -> l.getId().equals(ligneId));
        if (!removed) {
            throw ResourceNotFoundException.of("LigneCommande", ligneId);
        }
        recalculerTotaux(commande);
        Commande saved = repository.save(commande);
        return CommandeDto.from(saved);
    }

    /** RG-059 : une remise ne peut être appliquée qu'à une commande non validée. Portée
     * "commande entière" uniquement (§6.2.6) — {@code Commande.remise} est un champ unique. */
    @Transactional
    public CommandeDto appliquerRemise(Long commandeId, Long remiseId) {
        Commande commande = findEntityChecked(commandeId);
        assertModifiable(commande);
        Remise remise = remiseService.findEntity(remiseId);
        if (!remise.isActif()) {
            throw new BusinessRuleException("Cette remise n'est plus active");
        }
        if (!remise.getEtablissement().getId().equals(commande.getEtablissement().getId())) {
            throw new BusinessRuleException("Cette remise n'appartient pas à cet établissement");
        }
        LocalDate aujourdHui = LocalDate.now();
        if (remise.getDateDebut() != null && aujourdHui.isBefore(remise.getDateDebut())
                || remise.getDateFin() != null && aujourdHui.isAfter(remise.getDateFin())) {
            throw new BusinessRuleException("Cette remise n'est pas valide à cette date");
        }
        LocalTime maintenant = LocalTime.now();
        if (remise.getHeureDebut() != null && maintenant.isBefore(remise.getHeureDebut())
                || remise.getHeureFin() != null && maintenant.isAfter(remise.getHeureFin())) {
            throw new BusinessRuleException("Cette remise n'est valide que sur une plage horaire précise");
        }

        // §6.10.1 : plafond de remise paramétrable (pourcentage ou montant selon le type de la
        // remise) — pas de plafond tant qu'aucune valeur n'est configurée.
        var parametres = parametresService.getEntity();
        if (remise.getType() == TypeRemise.POURCENTAGE && parametres.getPlafondRemisePourcentage() != null
                && remise.getValeur().compareTo(parametres.getPlafondRemisePourcentage()) > 0) {
            throw new BusinessRuleException("Cette remise dépasse le plafond autorisé de " + parametres.getPlafondRemisePourcentage() + " %");
        }
        if (remise.getType() == TypeRemise.MONTANT_FIXE && parametres.getPlafondRemiseMontant() != null
                && remise.getValeur().compareTo(parametres.getPlafondRemiseMontant()) > 0) {
            throw new BusinessRuleException("Cette remise dépasse le plafond autorisé de " + parametres.getPlafondRemiseMontant() + " FCFA");
        }

        BigDecimal montantRemise = remise.getType() == TypeRemise.POURCENTAGE
                ? commande.getMontantBrut().multiply(remise.getValeur()).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                : remise.getValeur();
        // Jamais un total net négatif, même si la remise fixe dépasse le total brut.
        montantRemise = montantRemise.min(commande.getMontantBrut());

        commande.setRemise(montantRemise);
        recalculerTotaux(commande);
        Commande saved = repository.save(commande);
        journal.enregistrer("COMMANDES", "REMISE", "Remise « " + remise.getLibelle() + " » appliquée à la commande " + saved.getNumero());
        return CommandeDto.from(saved);
    }

    @Transactional
    public CommandeDto retirerRemise(Long commandeId) {
        Commande commande = findEntityChecked(commandeId);
        assertModifiable(commande);
        commande.setRemise(BigDecimal.ZERO);
        recalculerTotaux(commande);
        Commande saved = repository.save(commande);
        return CommandeDto.from(saved);
    }

    @Transactional
    public CommandeDto annuler(Long commandeId, AnnulerCommandeRequest req) {
        Commande commande = findEntityChecked(commandeId);
        assertModifiable(commande);
        commande.setStatut(StatutCommande.ANNULEE);
        commande.setMotifAnnulation(req.motif());
        libererTableSiOccupeeParCetteCommande(commande);
        Commande saved = repository.save(commande);
        journal.enregistrer("COMMANDES", "ANNULATION", "Commande " + saved.getNumero() + " annulée : " + req.motif());
        return CommandeDto.from(saved);
    }

    /** RG-032 : validation + paiement + facture + libération de table (si applicable) dans une
     * seule transaction — tout échec annule l'ensemble. */
    @Transactional
    public FactureDto encaisser(Long commandeId, EncaisserRequest req) {
        Commande commande = findEntityChecked(commandeId);
        if (commande.getStatut() != StatutCommande.NON_VALIDEE) {
            throw new BusinessRuleException("Cette commande a déjà été validée ou annulée");
        }
        if (commande.getLignes().isEmpty()) {
            throw new BusinessRuleException("Impossible d'encaisser une commande vide");
        }

        ModePaiement mode = parseMode(req.modePaiement());
        BigDecimal montantNet = commande.getMontantNet();
        BigDecimal monnaieRendue = BigDecimal.ZERO;
        if (mode == ModePaiement.ESPECES) {
            if (req.montantRecu() == null || req.montantRecu().compareTo(montantNet) < 0) {
                throw new BusinessRuleException("Le montant reçu est inférieur au total net à payer");
            }
            monnaieRendue = req.montantRecu().subtract(montantNet);
        }

        Paiement paiement = new Paiement();
        paiement.setCommande(commande);
        paiement.setMode(mode);
        paiement.setMontant(montantNet);
        paiement.setMontantRecu(req.montantRecu());
        paiement.setMonnaieRendue(monnaieRendue);
        paiementRepository.save(paiement);

        // RG-031 : décrémentation automatique du stock des produits suivis — dans la même
        // transaction que l'encaissement (RG-032) : une rupture de stock annule tout, y compris
        // le paiement déjà enregistré ci-dessus.
        for (LigneCommande ligne : commande.getLignes()) {
            if (ligne.getProduit().isSuiviStock()) {
                mouvementStockService.enregistrerSortieVente(ligne.getProduit(),
                        BigDecimal.valueOf(ligne.getQuantite()), ligne.getPrixUnitaire());
            }
        }

        boolean gereCuisine = commande.getEtablissement().isGereCuisine();
        commande.setStatut(gereCuisine ? StatutCommande.ENVOYEE_CUISINE : StatutCommande.VALIDEE);
        commande.setDateValidation(LocalDateTime.now());
        repository.save(commande);

        // Sans cuisine, la commande est immédiatement terminale (EF-022/EF-024) : la table est
        // libérée maintenant. Au Restaurant, la libération n'arrive qu'au Suivi cuisine
        // (RG-072, Lot 2c) — la table reste Occupée jusque-là.
        if (!gereCuisine) {
            libererTableSiOccupeeParCetteCommande(commande);
        }

        Facture facture = new Facture();
        facture.setNumero(numerotationService.genererNumero(commande.getEtablissement(), "FAC"));
        facture.setEtablissement(commande.getEtablissement());
        facture.setCommande(commande);
        facture.setMontantBrut(commande.getMontantBrut());
        facture.setRemise(commande.getRemise());
        facture.setMontantNet(montantNet);
        facture.setMode(mode);
        facture.setMontantRecu(req.montantRecu());
        facture.setMonnaieRendue(monnaieRendue);
        Facture savedFacture = factureRepository.save(facture);

        journal.enregistrer("VENTES", "ENCAISSEMENT",
                "Commande " + commande.getNumero() + " encaissée (" + mode + ", " + montantNet + " FCFA) -> facture " + savedFacture.getNumero());

        return FactureDto.from(savedFacture);
    }

    private LigneCommande construireLigne(Commande commande, Etablissement etablissement, LigneCommandeInput input) {
        Produit produit = produitRepository.findById(input.produitId())
                .orElseThrow(() -> ResourceNotFoundException.of("Produit", input.produitId()));
        if (!produit.getEtablissement().getId().equals(etablissement.getId())) {
            throw new BusinessRuleException("Ce produit n'appartient pas à cet établissement");
        }
        if (!produit.isActif() || !produit.isDisponible()) {
            throw new BusinessRuleException("Le produit " + produit.getNom() + " n'est pas disponible");
        }
        LigneCommande ligne = new LigneCommande();
        ligne.setCommande(commande);
        ligne.setProduit(produit);
        ligne.setArticleNom(produit.getNom());
        ligne.setQuantite(input.quantite());
        ligne.setPrixUnitaire(produit.getPrixVente());
        ligne.setMontant(produit.getPrixVente().multiply(BigDecimal.valueOf(input.quantite())));
        return ligne;
    }

    private void recalculerTotaux(Commande commande) {
        BigDecimal brut = commande.getLignes().stream().map(LigneCommande::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
        commande.setMontantBrut(brut);
        commande.setMontantNet(brut.subtract(commande.getRemise()));
    }

    /** RG-026/RG-044 : une commande validée, envoyée en cuisine ou annulée ne peut plus être
     * touchée — refusé ici même si la requête est forgée directement contre l'API. */
    private void assertModifiable(Commande commande) {
        if (commande.getStatut() != StatutCommande.NON_VALIDEE) {
            throw new BusinessRuleException("Cette commande ne peut plus être modifiée");
        }
    }

    private void libererTableSiOccupeeParCetteCommande(Commande commande) {
        if (commande.getTable() == null) {
            return;
        }
        TableService table = commande.getTable();
        List<Commande> autresActives = repository.findByTableIdAndStatutNotIn(table.getId(),
                List.of(StatutCommande.VALIDEE, StatutCommande.SERVIE, StatutCommande.ANNULEE));
        boolean aucuneAutreActive = autresActives.stream().allMatch(c -> c.getId().equals(commande.getId()));
        if (aucuneAutreActive) {
            table.setStatut(StatutTable.LIBRE);
            tableRepository.save(table);
        }
    }

    private StatutCommande parseStatut(String statut) {
        try {
            return StatutCommande.valueOf(statut.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Statut de commande inconnu : " + statut);
        }
    }

    private ModePaiement parseMode(String mode) {
        try {
            return ModePaiement.valueOf(mode.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Mode de paiement inconnu : " + mode);
        }
    }

    /** Le Super Administrateur doit explicitement choisir un établissement pour agir dessus
     * (RG-016) — pas de vue "toutes commandes mélangées" dans ce lot. */
    private Etablissement resoudreEtablissementCible(Long etablissementIdDemande) {
        Etablissement demande = etablissementIdDemande != null
                ? etablissementRepository.findById(etablissementIdDemande)
                        .orElseThrow(() -> ResourceNotFoundException.of("Etablissement", etablissementIdDemande))
                : null;
        Etablissement cible = perimetreGuard.scopeEtablissement(demande);
        if (cible == null) {
            throw new BusinessRuleException("Précisez un établissement");
        }
        return cible;
    }

    private Commande findEntityChecked(Long id) {
        Commande c = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Commande", id));
        if (!perimetreGuard.aAcces(c.getEtablissement())) {
            throw ResourceNotFoundException.of("Commande", id);
        }
        return c;
    }

    private Utilisateur currentUtilisateur() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUtilisateur();
        }
        throw new UnauthorizedException("Utilisateur non authentifié");
    }
}
