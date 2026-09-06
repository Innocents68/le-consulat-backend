package com.leconsulat.seed;

import com.leconsulat.cave.entity.Boisson;
import com.leconsulat.cave.entity.Fournisseur;
import com.leconsulat.cave.entity.MouvementCave;
import com.leconsulat.cave.entity.TypeBoisson;
import com.leconsulat.cave.repository.BoissonRepository;
import com.leconsulat.cave.repository.FournisseurRepository;
import com.leconsulat.cave.repository.MouvementCaveRepository;
import com.leconsulat.finance.entity.CategorieDepense;
import com.leconsulat.finance.entity.Depense;
import com.leconsulat.finance.entity.Recette;
import com.leconsulat.finance.entity.SourceRecette;
import com.leconsulat.finance.repository.CategorieDepenseRepository;
import com.leconsulat.finance.repository.DepenseRepository;
import com.leconsulat.finance.repository.RecetteRepository;
import com.leconsulat.maquis.entity.CommandeMaquis;
import com.leconsulat.maquis.entity.LigneCommandeMaquis;
import com.leconsulat.maquis.entity.StatutCommandeMaquis;
import com.leconsulat.maquis.repository.CommandeMaquisRepository;
import com.leconsulat.restaurant.entity.*;
import com.leconsulat.restaurant.repository.CategoriePlatRepository;
import com.leconsulat.restaurant.repository.CommandeRepository;
import com.leconsulat.restaurant.repository.PlatRepository;
import com.leconsulat.restaurant.repository.TableRestaurantRepository;
import com.leconsulat.stock.entity.*;
import com.leconsulat.stock.repository.CategorieProduitRepository;
import com.leconsulat.stock.repository.DepotRepository;
import com.leconsulat.stock.repository.EntreeStockRepository;
import com.leconsulat.stock.repository.ProduitRepository;
import com.leconsulat.utilisateur.entity.Droit;
import com.leconsulat.utilisateur.entity.Role;
import com.leconsulat.utilisateur.entity.Utilisateur;
import com.leconsulat.utilisateur.repository.DroitRepository;
import com.leconsulat.utilisateur.repository.UtilisateurRepository;
import com.leconsulat.vente.entity.*;
import com.leconsulat.vente.repository.RemiseRepository;
import com.leconsulat.vente.repository.SessionCaisseRepository;
import com.leconsulat.vente.repository.VenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;

/**
 * Seeds a realistic demo dataset the first time the application starts against an empty
 * database, matching API_CONTRACT.md §9 (seed accounts) and the sample values from the
 * cahier des charges / UI mockups (Château Margaux, Heineken, Côte de Bœuf, tables T1-T12...).
 * Never runs again once at least one Utilisateur exists.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final DroitRepository droitRepository;

    private final DepotRepository depotRepository;
    private final CategorieProduitRepository categorieProduitRepository;
    private final ProduitRepository produitRepository;
    private final EntreeStockRepository entreeStockRepository;

    private final FournisseurRepository fournisseurRepository;
    private final BoissonRepository boissonRepository;
    private final MouvementCaveRepository mouvementCaveRepository;

    private final CategoriePlatRepository categoriePlatRepository;
    private final PlatRepository platRepository;
    private final TableRestaurantRepository tableRepository;
    private final CommandeRepository commandeRepository;

    private final CommandeMaquisRepository commandeMaquisRepository;

    private final SessionCaisseRepository sessionCaisseRepository;
    private final VenteRepository venteRepository;
    private final RemiseRepository remiseRepository;

    private final CategorieDepenseRepository categorieDepenseRepository;
    private final DepenseRepository depenseRepository;
    private final RecetteRepository recetteRepository;

    public DataSeeder(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder,
                       DroitRepository droitRepository, DepotRepository depotRepository,
                       CategorieProduitRepository categorieProduitRepository, ProduitRepository produitRepository,
                       EntreeStockRepository entreeStockRepository, FournisseurRepository fournisseurRepository,
                       BoissonRepository boissonRepository, MouvementCaveRepository mouvementCaveRepository,
                       CategoriePlatRepository categoriePlatRepository, PlatRepository platRepository,
                       TableRestaurantRepository tableRepository, CommandeRepository commandeRepository,
                       CommandeMaquisRepository commandeMaquisRepository, SessionCaisseRepository sessionCaisseRepository,
                       VenteRepository venteRepository, RemiseRepository remiseRepository,
                       CategorieDepenseRepository categorieDepenseRepository, DepenseRepository depenseRepository,
                       RecetteRepository recetteRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.droitRepository = droitRepository;
        this.depotRepository = depotRepository;
        this.categorieProduitRepository = categorieProduitRepository;
        this.produitRepository = produitRepository;
        this.entreeStockRepository = entreeStockRepository;
        this.fournisseurRepository = fournisseurRepository;
        this.boissonRepository = boissonRepository;
        this.mouvementCaveRepository = mouvementCaveRepository;
        this.categoriePlatRepository = categoriePlatRepository;
        this.platRepository = platRepository;
        this.tableRepository = tableRepository;
        this.commandeRepository = commandeRepository;
        this.commandeMaquisRepository = commandeMaquisRepository;
        this.sessionCaisseRepository = sessionCaisseRepository;
        this.venteRepository = venteRepository;
        this.remiseRepository = remiseRepository;
        this.categorieDepenseRepository = categorieDepenseRepository;
        this.depenseRepository = depenseRepository;
        this.recetteRepository = recetteRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long existing = utilisateurRepository.count();
        if (existing > 0) {
            log.info("[SEED] Base déjà initialisée ({} utilisateur(s) trouvé(s)) — seed ignoré. " +
                    "Pour reseeder, arrêtez l'appli et supprimez le dossier ./data (H2).", existing);
            return;
        }

        log.info("[SEED] Base vide détectée — génération des données de démonstration...");
        try {
            Map<String, Utilisateur> users = seedUtilisateurs();
            log.info("[SEED] {} utilisateur(s) créé(s)", users.size());
            seedDroits();
            log.info("[SEED] Droits/profils créés");
            Depot depot = seedDepot();
            Map<String, Produit> produits = seedProduits(depot);
            log.info("[SEED] {} produit(s) créé(s)", produits.size());
            Map<String, Boisson> boissons = seedCave();
            log.info("[SEED] {} boisson(s) créée(s)", boissons.size());
            Map<String, Plat> plats = seedRestaurantMenu();
            log.info("[SEED] {} plat(s) créé(s)", plats.size());
            List<TableRestaurant> tables = seedTables();
            log.info("[SEED] {} table(s) créée(s)", tables.size());
            seedRemises();
            Map<String, CategorieDepense> categoriesDepense = seedCategoriesDepense();
            seedDepenses(categoriesDepense);
            seedCommandesRestaurant(tables, plats, users);
            seedCommandesMaquis(tables);
            seedVentesEtCaisse(produits, users, boissons);
            log.info("[SEED] Génération des données de démonstration terminée avec succès. " +
                    "Comptes disponibles (mot de passe 'password123') : admin, jean, mariam, paul, sophie, chef.");
        } catch (Exception e) {
            log.error("[SEED] ÉCHEC de la génération des données de démonstration — la transaction va être annulée " +
                    "(la base restera vide). Cause : {}", e.toString(), e);
            throw e;
        }
    }

    // ---------------------------------------------------------------- Utilisateurs

    private Map<String, Utilisateur> seedUtilisateurs() {
        String pwd = passwordEncoder.encode("password123");
        Map<String, Utilisateur> users = new LinkedHashMap<>();
        users.put("admin", creerUtilisateur("admin", "Administrateur", "admin@leconsulat.bf", "+226 70 00 00 01", Role.ADMIN, pwd));
        users.put("jean", creerUtilisateur("jean", "Jean Ouedraogo", "jean@leconsulat.bf", "+226 70 00 00 02", Role.CAISSIER_SERVEUR, pwd));
        users.put("mariam", creerUtilisateur("mariam", "Mariam Kaboré", "mariam@leconsulat.bf", "+226 70 00 00 03", Role.CAISSIER_SERVEUR, pwd));
        users.put("paul", creerUtilisateur("paul", "Paul Sawadogo", "paul@leconsulat.bf", "+226 70 00 00 04", Role.GERANT, pwd));
        users.put("sophie", creerUtilisateur("sophie", "Sophie Compaoré", "sophie@leconsulat.bf", "+226 70 00 00 05", Role.GERANT, pwd));
        users.put("chef", creerUtilisateur("chef", "Issa Traoré", "chef@leconsulat.bf", "+226 70 00 00 06", Role.CUISINIER, pwd));
        return users;
    }

    private Utilisateur creerUtilisateur(String username, String nom, String email, String tel, Role role, String pwd) {
        Utilisateur u = new Utilisateur();
        u.setUsername(username);
        u.setNom(nom);
        u.setEmail(email);
        u.setTelephone(tel);
        u.setRole(role);
        u.setMotDePasse(pwd);
        u.setActif(true);
        u.setDateCreation(LocalDateTime.now().minusMonths(3));
        return utilisateurRepository.save(u);
    }

    // ---------------------------------------------------------------- Droits

    /**
     * Matrice des droits — reproduit exactement le document "4 profils" fourni par le client
     * (légende V = Voir, A = Ajouter, M = Modifier, S = Supprimer). Un module absent d'un
     * appel grant() pour un rôle donné reste sans aucun droit (voir/ajouter/modifier/supprimer
     * tous à false) pour ce rôle.
     */
    private void seedDroits() {
        // ---- ADMINISTRATEUR : tous les droits, y compris sur les modules en lecture seule
        // (dashboard/journal/reporting/aide n'ont de toute façon aucune action d'écriture).
        grant(Role.ADMIN, true, false, false, false, Set.of("dashboard", "journal", "reporting", "aide"));
        grant(Role.ADMIN, true, true, true, true, minus(allModules(), Set.of("dashboard", "journal", "reporting", "aide")));

        // ---- GÉRANT
        grant(Role.GERANT, true, false, false, false, Set.of("dashboard", "journal", "reporting", "finance", "sauvegardes", "aide"));
        grant(Role.GERANT, true, false, true, false, Set.of("cuisine", "parametres"));
        grant(Role.GERANT, true, true, true, false, Set.of("ventes", "factures", "caisses", "remises", "avoirs", "tables",
                "commandes-restaurant", "plats", "cave", "maquis", "stocks", "entrees-stock", "sorties-stock", "depenses"));
        grant(Role.GERANT, true, true, false, false, Set.of("utilisateurs"));
        // "profils" : aucun droit pour le Gérant (réservé à l'Administrateur).

        // ---- CAISSIER / SERVEUR (rôle fusionné)
        grant(Role.CAISSIER_SERVEUR, true, false, false, false, Set.of("remises", "cuisine", "plats", "cave", "maquis", "aide"));
        grant(Role.CAISSIER_SERVEUR, true, true, true, false, Set.of("ventes", "tables", "commandes-restaurant"));
        grant(Role.CAISSIER_SERVEUR, true, true, false, false, Set.of("factures", "avoirs"));
        grant(Role.CAISSIER_SERVEUR, true, false, true, false, Set.of("caisses"));

        // ---- CUISINIER
        grant(Role.CUISINIER, true, false, false, false, Set.of("commandes-restaurant", "plats", "maquis", "stocks", "aide"));
        grant(Role.CUISINIER, true, false, true, false, Set.of("cuisine"));
        grant(Role.CUISINIER, true, true, false, false, Set.of("sorties-stock"));
    }

    private void grant(Role role, boolean voir, boolean ajouter, boolean modifier, boolean supprimer, Set<String> modules) {
        for (String module : modules) {
            if (droitRepository.findByRoleAndModule(role, module).isEmpty()) {
                droitRepository.save(new Droit(role, module, voir, ajouter, modifier, supprimer));
            }
        }
    }

    private Set<String> allModules() {
        return new LinkedHashSet<>(com.leconsulat.utilisateur.ModulesCatalogue.MODULES);
    }

    private Set<String> minus(Set<String> base, Set<String> excluded) {
        Set<String> result = new LinkedHashSet<>(base);
        result.removeAll(excluded);
        return result;
    }

    // ---------------------------------------------------------------- Stocks

    private Depot seedDepot() {
        Depot depot = new Depot();
        depot.setNom("Dépôt principal");
        depot.setAdresse("Ouagadougou, Secteur 15");
        depot.setActif(true);
        return depotRepository.save(depot);
    }

    private Map<String, Produit> seedProduits(Depot depot) {
        CategorieProduit boissonsCat = categorieProduitRepository.save(new CategorieProduit("Boissons non-alcoolisées"));
        CategorieProduit epicerieCat = categorieProduitRepository.save(new CategorieProduit("Épicerie & snacks"));
        CategorieProduit consommablesCat = categorieProduitRepository.save(new CategorieProduit("Consommables"));

        Map<String, Produit> produits = new LinkedHashMap<>();
        produits.put("EAU50", creerProduit("EAU-50CL", "Eau minérale 50cl", boissonsCat, "bouteille",
                new BigDecimal("200"), new BigDecimal("500"), 20, 150, depot));
        produits.put("COCA33", creerProduit("COCA-33CL", "Coca-Cola 33cl", boissonsCat, "canette",
                new BigDecimal("350"), new BigDecimal("750"), 24, 200, depot));
        produits.put("JUS100", creerProduit("JUS-100", "Jus de fruit local 1L", boissonsCat, "bouteille",
                new BigDecimal("800"), new BigDecimal("1500"), 10, 60, depot));
        produits.put("CACAHUETE", creerProduit("SNACK-CACA", "Cacahuètes grillées (sachet)", epicerieCat, "sachet",
                new BigDecimal("150"), new BigDecimal("500"), 30, 8, depot));
        produits.put("CIGARETTE", creerProduit("TAB-CIG", "Paquet de cigarettes", epicerieCat, "paquet",
                new BigDecimal("1000"), new BigDecimal("1500"), 15, 40, depot));
        produits.put("SERVIETTE", creerProduit("CONS-SERV", "Serviettes en papier (paquet)", consommablesCat, "paquet",
                new BigDecimal("500"), new BigDecimal("500"), 10, 3, depot)); // volontairement sous le seuil -> alerte stock faible
        produits.put("CHARBON", creerProduit("CONS-CHAR", "Sac de charbon de bois", consommablesCat, "sac",
                new BigDecimal("3000"), new BigDecimal("3000"), 5, 0, depot)); // rupture volontaire

        // Historique réel : quelques entrées de stock validées pour la traçabilité comptable.
        for (Produit p : produits.values()) {
            if (p.getQuantiteStock() > 0) {
                enregistrerEntreeStockHistorique(p, p.getQuantiteStock(), p.getPrixAchat());
            }
        }

        return produits;
    }

    private Produit creerProduit(String code, String nom, CategorieProduit categorie, String unite,
                                  BigDecimal prixAchat, BigDecimal prixVente, int seuilAlerte, int quantiteStock, Depot depot) {
        Produit p = new Produit();
        p.setCode(code);
        p.setNom(nom);
        p.setCategorie(categorie);
        p.setUnite(unite);
        p.setPrixAchat(prixAchat);
        p.setPrixVente(prixVente);
        p.setSeuilAlerte(seuilAlerte);
        p.setQuantiteStock(quantiteStock);
        p.setDepot(depot);
        p.setActif(true);
        return produitRepository.save(p);
    }

    private void enregistrerEntreeStockHistorique(Produit produit, int quantite, BigDecimal prixUnitaire) {
        EntreeStock e = new EntreeStock();
        e.setProduit(produit);
        e.setFournisseur("SODIBO Distribution");
        e.setQuantite(quantite);
        e.setPrixUnitaire(prixUnitaire);
        e.setMontant(prixUnitaire.multiply(BigDecimal.valueOf(quantite)));
        e.setDateEntree(LocalDate.now().minusDays(10));
        e.setBonLivraison("BL-" + (1000 + produit.getId()));
        e.setStatut(StatutMouvementStock.VALIDE);
        entreeStockRepository.save(e);
    }

    // ---------------------------------------------------------------- Cave à vin

    private Map<String, Boisson> seedCave() {
        Fournisseur fournisseurVins = seedFournisseur("Import Vins Premium", "M. Kader Zongo", "+226 25 30 10 20", "contact@importvins.bf", "Paiement 30 jours");
        Fournisseur fournisseurBieres = seedFournisseur("Brasseries du Faso", "Mme Awa Sanou", "+226 25 30 40 50", "commercial@brasseriesfaso.bf", "Paiement comptant");

        Map<String, Boisson> boissons = new LinkedHashMap<>();
        boissons.put("MARGAUX", creerBoisson("Château Margaux", TypeBoisson.VIN_ROUGE, "Bordeaux, France", 2015,
                new BigDecimal("45000"), new BigDecimal("75000"), new BigDecimal("15000"), fournisseurVins, new BigDecimal("18"), 5));
        boissons.put("VINROUGE", creerBoisson("Vin Rouge Maison", TypeBoisson.VIN_ROUGE, "Vallée du Rhône, France", 2019,
                new BigDecimal("6000"), new BigDecimal("12000"), new BigDecimal("2500"), fournisseurVins, new BigDecimal("30"), 8));
        boissons.put("MOET", creerBoisson("Champagne Moët & Chandon", TypeBoisson.CHAMPAGNE, "Champagne, France", 2018,
                new BigDecimal("35000"), new BigDecimal("60000"), new BigDecimal("12000"), fournisseurVins, new BigDecimal("10"), 4));
        boissons.put("JACKDANIELS", creerBoisson("Jack Daniel's", TypeBoisson.WHISKY, "Tennessee, USA", null,
                new BigDecimal("18000"), new BigDecimal("35000"), new BigDecimal("5000"), fournisseurVins, new BigDecimal("12"), 3));
        boissons.put("DIPLOMATICO", creerBoisson("Rhum Diplomático", TypeBoisson.SPIRITUEUX, "Venezuela", null,
                new BigDecimal("20000"), new BigDecimal("38000"), new BigDecimal("6000"), fournisseurVins, new BigDecimal("9"), 3));
        boissons.put("HEINEKEN", creerBoisson("Heineken", TypeBoisson.BIERE, "Pays-Bas", null,
                new BigDecimal("4500"), new BigDecimal("8500"), null, fournisseurBieres, new BigDecimal("60"), 15));

        for (Boisson b : boissons.values()) {
            enregistrerMouvementCaveHistorique(b, b.getQuantiteStock());
        }
        return boissons;
    }

    private Fournisseur seedFournisseur(String nom, String contact, String tel, String email, String conditions) {
        Fournisseur f = new Fournisseur();
        f.setNom(nom);
        f.setContact(contact);
        f.setTelephone(tel);
        f.setEmail(email);
        f.setConditions(conditions);
        f.setActif(true);
        return fournisseurRepository.save(f);
    }

    private Boisson creerBoisson(String nom, TypeBoisson type, String origine, Integer millesime,
                                  BigDecimal prixAchat, BigDecimal prixVenteBouteille, BigDecimal prixVenteVerre,
                                  Fournisseur fournisseur, BigDecimal quantiteStock, int seuilAlerte) {
        Boisson b = new Boisson();
        b.setNom(nom);
        b.setType(type);
        b.setOrigine(origine);
        b.setMillesime(millesime);
        b.setPrixAchatBouteille(prixAchat);
        b.setPrixVenteBouteille(prixVenteBouteille);
        b.setPrixVenteVerre(prixVenteVerre);
        b.setFournisseur(fournisseur);
        b.setQuantiteStock(quantiteStock);
        b.setSeuilAlerte(seuilAlerte);
        b.setActif(true);
        return boissonRepository.save(b);
    }

    private void enregistrerMouvementCaveHistorique(Boisson boisson, BigDecimal quantite) {
        MouvementCave m = new MouvementCave();
        m.setBoisson(boisson);
        m.setType(MouvementCave.Type.ENTREE);
        m.setMotif(MouvementCave.Motif.ACHAT);
        m.setQuantite(quantite);
        m.setUniteVente(MouvementCave.UniteVente.BOUTEILLE);
        m.setDate(LocalDateTime.now().minusDays(15));
        mouvementCaveRepository.save(m);
    }

    // ---------------------------------------------------------------- Restaurant

    private Map<String, Plat> seedRestaurantMenu() {
        CategoriePlat grillades = seedCategoriePlat("Grillades & viandes", 1);
        CategoriePlat accompagnements = seedCategoriePlat("Accompagnements", 2);
        CategoriePlat entrees = seedCategoriePlat("Entrées", 3);
        CategoriePlat desserts = seedCategoriePlat("Desserts", 4);

        Map<String, Plat> plats = new LinkedHashMap<>();
        plats.put("COTEBOEUF", creerPlat("Côte de Bœuf", new BigDecimal("12000"), grillades,
                "Côte de bœuf grillée, sauce au poivre", 25, List.of("Bœuf", "Poivre", "Beurre")));
        plats.put("POULETBRAISE", creerPlat("Poulet Braisé", new BigDecimal("6500"), grillades,
                "Demi-poulet braisé, marinade maison", 30, List.of("Poulet", "Épices", "Ail")));
        plats.put("BROCHETTES", creerPlat("Brochettes de Bœuf", new BigDecimal("4000"), grillades,
                "Brochettes de bœuf marinées grillées au charbon", 15, List.of("Bœuf", "Oignon", "Poivron")));
        plats.put("FRITES", creerPlat("Frites Maison", new BigDecimal("1500"), accompagnements,
                "Pommes de terre fraîches coupées et frites", 10, List.of("Pomme de terre", "Huile")));
        plats.put("RIZGRAS", creerPlat("Riz Gras", new BigDecimal("2000"), accompagnements,
                "Riz cuit dans un bouillon de viande et légumes", 20, List.of("Riz", "Tomate", "Légumes")));
        plats.put("SALADE", creerPlat("Salade César", new BigDecimal("2500"), entrees,
                "Salade, poulet grillé, parmesan, croûtons", 10, List.of("Laitue", "Poulet", "Parmesan")));
        plats.put("CREME", creerPlat("Crème Caramel", new BigDecimal("1500"), desserts,
                "Dessert maison au caramel", 5, List.of("Lait", "Œufs", "Sucre")));

        return plats;
    }

    private CategoriePlat seedCategoriePlat(String nom, int ordre) {
        CategoriePlat c = new CategoriePlat();
        c.setNom(nom);
        c.setOrdre(ordre);
        return categoriePlatRepository.save(c);
    }

    private Plat creerPlat(String nom, BigDecimal prix, CategoriePlat categorie, String description, int tempsPrep, List<String> ingredients) {
        Plat p = new Plat();
        p.setNom(nom);
        p.setPrix(prix);
        p.setCategorie(categorie);
        p.setDescription(description);
        p.setTempsPreparation(tempsPrep);
        p.setDisponible(true);
        p.setIngredients(new ArrayList<>(ingredients));
        p.setActif(true);
        return platRepository.save(p);
    }

    private List<TableRestaurant> seedTables() {
        List<TableRestaurant> tables = new ArrayList<>();
        String[] zones = {"Terrasse", "Salle principale", "VIP"};
        for (int i = 1; i <= 12; i++) {
            TableRestaurant t = new TableRestaurant();
            t.setNumero("T" + i);
            t.setCapacite(i % 4 == 0 ? 8 : (i % 3 == 0 ? 6 : 4));
            t.setZone(zones[(i - 1) % zones.length]);
            t.setStatut(StatutTable.LIBRE);
            t.setPositionX(((i - 1) % 4) * 120 + 40);
            t.setPositionY(((i - 1) / 4) * 120 + 40);
            tables.add(tableRepository.save(t));
        }
        return tables;
    }

    private void seedCommandesRestaurant(List<TableRestaurant> tables, Map<String, Plat> plats, Map<String, Utilisateur> users) {
        // Une commande servie aujourd'hui (historique) sur T3.
        TableRestaurant t3 = tables.get(2);
        Commande c1 = new Commande();
        c1.setTable(t3);
        c1.setType(TypeCommande.SUR_PLACE);
        c1.setServeur(users.get("mariam"));
        c1.setStatut(StatutCommande.SERVIE);
        c1.setDateCreation(LocalDateTime.now().minusHours(3));
        ajouterLigneCommande(c1, plats.get("COTEBOEUF"), 2, StatutLigneCommande.SERVI);
        ajouterLigneCommande(c1, plats.get("FRITES"), 2, StatutLigneCommande.SERVI);
        commandeRepository.save(c1);

        // Une commande en préparation sur T7 (pour l'écran cuisine).
        TableRestaurant t7 = tables.get(6);
        t7.setStatut(StatutTable.OCCUPEE);
        tableRepository.save(t7);
        Commande c2 = new Commande();
        c2.setTable(t7);
        c2.setType(TypeCommande.SUR_PLACE);
        c2.setServeur(users.get("mariam"));
        c2.setStatut(StatutCommande.EN_PREPARATION);
        c2.setDateCreation(LocalDateTime.now().minusMinutes(20));
        ajouterLigneCommande(c2, plats.get("POULETBRAISE"), 1, StatutLigneCommande.EN_PREPARATION);
        ajouterLigneCommande(c2, plats.get("RIZGRAS"), 1, StatutLigneCommande.EN_ATTENTE);
        commandeRepository.save(c2);

        // Une commande à emporter en attente.
        Commande c3 = new Commande();
        c3.setType(TypeCommande.EMPORTER);
        c3.setServeur(users.get("mariam"));
        c3.setStatut(StatutCommande.EN_ATTENTE);
        c3.setDateCreation(LocalDateTime.now().minusMinutes(5));
        ajouterLigneCommande(c3, plats.get("BROCHETTES"), 3, StatutLigneCommande.EN_ATTENTE);
        commandeRepository.save(c3);
    }

    private void ajouterLigneCommande(Commande commande, Plat plat, int quantite, StatutLigneCommande statut) {
        LigneCommande l = new LigneCommande();
        l.setCommande(commande);
        l.setPlat(plat);
        l.setQuantite(quantite);
        l.setPrixUnitaire(plat.getPrix());
        l.setMontant(plat.getPrix().multiply(BigDecimal.valueOf(quantite)));
        l.setStatut(statut);
        commande.getLignes().add(l);
    }

    // ---------------------------------------------------------------- Maquis

    private void seedCommandesMaquis(List<TableRestaurant> tables) {
        CommandeMaquis c = new CommandeMaquis();
        c.setTable(tables.get(9));
        c.setClientNom("Client de passage");
        c.setStatut(StatutCommandeMaquis.CLOTUREE);
        c.setDateCreation(LocalDateTime.now().minusHours(5));
        ajouterLigneMaquis(c, "Brochette de bœuf", 4, new BigDecimal("1000"));
        ajouterLigneMaquis(c, "Heineken", 3, new BigDecimal("1000"));
        commandeMaquisRepository.save(c);

        CommandeMaquis c2 = new CommandeMaquis();
        c2.setClientNom("Groupe (5 pers.)");
        c2.setStatut(StatutCommandeMaquis.EN_COURS);
        c2.setDateCreation(LocalDateTime.now().minusMinutes(30));
        ajouterLigneMaquis(c2, "Poisson braisé", 2, new BigDecimal("3500"));
        ajouterLigneMaquis(c2, "Attiéké", 2, new BigDecimal("1000"));
        commandeMaquisRepository.save(c2);
    }

    private void ajouterLigneMaquis(CommandeMaquis commande, String article, int quantite, BigDecimal prixUnitaire) {
        LigneCommandeMaquis l = new LigneCommandeMaquis();
        l.setCommande(commande);
        l.setArticleNom(article);
        l.setQuantite(quantite);
        l.setPrixUnitaire(prixUnitaire);
        l.setMontant(prixUnitaire.multiply(BigDecimal.valueOf(quantite)));
        commande.getLignes().add(l);
    }

    // ---------------------------------------------------------------- Remises

    private void seedRemises() {
        Remise r1 = new Remise();
        r1.setNom("Happy Hour -10%");
        r1.setType(Remise.Type.POURCENTAGE);
        r1.setValeur(new BigDecimal("10"));
        r1.setDateDebut(LocalDate.now().minusMonths(1));
        r1.setDateFin(LocalDate.now().plusMonths(2));
        r1.setActif(true);
        remiseRepository.save(r1);

        Remise r2 = new Remise();
        r2.setNom("Fidélité -2000 FCFA");
        r2.setType(Remise.Type.MONTANT_FIXE);
        r2.setValeur(new BigDecimal("2000"));
        r2.setDateDebut(LocalDate.now().minusMonths(1));
        r2.setDateFin(LocalDate.now().plusMonths(6));
        r2.setActif(true);
        remiseRepository.save(r2);
    }

    // ---------------------------------------------------------------- Finance

    private Map<String, CategorieDepense> seedCategoriesDepense() {
        Map<String, CategorieDepense> categories = new LinkedHashMap<>();
        categories.put("achats", categorieDepenseRepository.save(new CategorieDepense("Achats marchandises")));
        categories.put("salaires", categorieDepenseRepository.save(new CategorieDepense("Salaires")));
        categories.put("entretien", categorieDepenseRepository.save(new CategorieDepense("Entretien & maintenance")));
        categories.put("energie", categorieDepenseRepository.save(new CategorieDepense("Électricité & eau")));
        categories.put("transport", categorieDepenseRepository.save(new CategorieDepense("Transport & livraison")));
        return categories;
    }

    private void seedDepenses(Map<String, CategorieDepense> categories) {
        creerDepense(categories.get("achats"), new BigDecimal("250000"), "Réapprovisionnement cave à vin", "Import Vins Premium", Depense.Statut.VALIDEE, LocalDate.now().minusDays(4));
        creerDepense(categories.get("salaires"), new BigDecimal("850000"), "Salaires du personnel (mensuel)", null, Depense.Statut.VALIDEE, LocalDate.now().minusDays(2));
        creerDepense(categories.get("energie"), new BigDecimal("95000"), "Facture SONABEL", "SONABEL", Depense.Statut.VALIDEE, LocalDate.now().minusDays(1));
        creerDepense(categories.get("entretien"), new BigDecimal("35000"), "Réparation climatiseur salle VIP", "Frigo Services", Depense.Statut.EN_ATTENTE, LocalDate.now());
        creerDepense(categories.get("transport"), new BigDecimal("15000"), "Livraison marchandises", "Coursier local", Depense.Statut.VALIDEE, LocalDate.now());
    }

    private void creerDepense(CategorieDepense categorie, BigDecimal montant, String description, String fournisseur, Depense.Statut statut, LocalDate date) {
        Depense d = new Depense();
        d.setCategorie(categorie);
        d.setMontant(montant);
        d.setDescription(description);
        d.setFournisseur(fournisseur);
        d.setStatut(statut);
        d.setDate(date);
        depenseRepository.save(d);
    }

    // ---------------------------------------------------------------- Ventes & caisse

    private void seedVentesEtCaisse(Map<String, Produit> produits, Map<String, Utilisateur> users, Map<String, Boisson> boissons) {
        SessionCaisse session = new SessionCaisse();
        session.setCaisseNom("Caisse principale");
        session.setFondInitial(new BigDecimal("25000"));
        session.setOuvertPar(users.get("jean"));
        session.setStatut(SessionCaisse.Statut.OUVERTE);
        session.setDateOuverture(LocalDateTime.now().minusDays(7).withHour(8).withMinute(0));
        session = sessionCaisseRepository.save(session);

        List<Produit> catalogue = new ArrayList<>(produits.values());
        Random random = new Random(42);
        int year = Year.now().getValue();
        int sequence = 1;

        for (int i = 6; i >= 0; i--) {
            LocalDateTime jour = LocalDateTime.now().minusDays(i);
            int nombreTickets = 3 + random.nextInt(4);
            for (int t = 0; t < nombreTickets; t++) {
                Vente v = new Vente();
                v.setSessionCaisse(session);
                v.setCaissier(users.get("jean"));
                v.setStatut(StatutVente.PAYEE);
                v.setModePaiement(ModePaiement.values()[random.nextInt(ModePaiement.values().length)]);
                LocalDateTime dateVente = jour.withHour(9 + random.nextInt(11)).withMinute(random.nextInt(60));
                v.setDateVente(dateVente);
                v.setNumero(String.format("FV-%d-%04d", year, sequence++));

                BigDecimal sousTotal = BigDecimal.ZERO;
                int nbLignes = 1 + random.nextInt(3);
                for (int l = 0; l < nbLignes; l++) {
                    Produit produit = catalogue.get(random.nextInt(catalogue.size()));
                    int quantite = 1 + random.nextInt(3);
                    LigneVente ligne = new LigneVente();
                    ligne.setVente(v);
                    ligne.setProduit(produit);
                    ligne.setQuantite(quantite);
                    ligne.setPrixUnitaire(produit.getPrixVente());
                    ligne.setRemise(BigDecimal.ZERO);
                    BigDecimal montant = produit.getPrixVente().multiply(BigDecimal.valueOf(quantite));
                    ligne.setMontant(montant);
                    sousTotal = sousTotal.add(montant);
                    v.getLignes().add(ligne);
                }
                v.setSousTotal(sousTotal);
                v.setRemiseMontant(BigDecimal.ZERO);
                v.setTotal(sousTotal);
                venteRepository.save(v);

                Recette r = new Recette();
                r.setSource(SourceRecette.VENTE);
                r.setMontant(sousTotal);
                r.setDescription("Vente " + v.getNumero());
                r.setDate(dateVente);
                recetteRepository.save(r);
            }
        }
    }
}
