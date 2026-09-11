package com.leconsulat.seed;

import com.leconsulat.catalogue.entity.Categorie;
import com.leconsulat.catalogue.entity.Produit;
import com.leconsulat.catalogue.entity.UniteProduit;
import com.leconsulat.catalogue.repository.CategorieRepository;
import com.leconsulat.catalogue.repository.ProduitRepository;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogue réel fourni par le client (fichiers {@code produits_prix_unitaires.md} — boissons du
 * Maquis, 3 vins rattachés à la Cave à vin — et {@code menu.md} — carte du Restaurant). Idempotent
 * comme {@link DataSeeder} : chaque produit n'est créé que s'il n'existe pas déjà pour son
 * établissement (vérifié par nom), donc sans risque à chaque redémarrage. Tourne après
 * {@link DataSeeder} (qui crée les 3 établissements) grâce à {@code @Order}.
 */
@Component
@Order(2)
public class ProduitSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProduitSeeder.class);

    private final EtablissementRepository etablissementRepository;
    private final CategorieRepository categorieRepository;
    private final ProduitRepository produitRepository;

    public ProduitSeeder(EtablissementRepository etablissementRepository, CategorieRepository categorieRepository,
                          ProduitRepository produitRepository) {
        this.etablissementRepository = etablissementRepository;
        this.categorieRepository = categorieRepository;
        this.produitRepository = produitRepository;
    }

    private record ProduitSeed(String categorie, String nom, int prix) {
    }

    // --- Maquis (produits_prix_unitaires.md — bières, sodas, eau) ---
    private static final List<ProduitSeed> MAQUIS = List.of(
            new ProduitSeed("Bières", "Sobbra", 700),
            new ProduitSeed("Bières", "Brakina", 700),
            new ProduitSeed("Bières", "Castel 65", 900),
            new ProduitSeed("Bières", "Castel 50", 700),
            new ProduitSeed("Bières", "Guinness 65", 1500),
            new ProduitSeed("Bières", "Guinness 33", 800),
            new ProduitSeed("Bières", "Beaufort 65", 900),
            new ProduitSeed("Bières", "Beaufort 50", 700),
            new ProduitSeed("Bières", "Xxl", 700),
            new ProduitSeed("Bières", "Heineken", 900),
            new ProduitSeed("Bières", "Faxe", 800),
            new ProduitSeed("Bières", "Martens", 800),
            new ProduitSeed("Boissons gazeuses et jus", "Malta", 700),
            new ProduitSeed("Boissons gazeuses et jus", "Dopel", 700),
            new ProduitSeed("Boissons gazeuses et jus", "Tequila", 700),
            new ProduitSeed("Boissons gazeuses et jus", "Libs", 700),
            new ProduitSeed("Boissons gazeuses et jus", "Chill", 700),
            new ProduitSeed("Boissons gazeuses et jus", "Youki 33", 500),
            new ProduitSeed("Boissons gazeuses et jus", "Jus ananas", 500),
            new ProduitSeed("Boissons gazeuses et jus", "Fanta", 600),
            new ProduitSeed("Boissons gazeuses et jus", "Coca-cola", 600),
            new ProduitSeed("Boissons gazeuses et jus", "Schweppes", 500),
            new ProduitSeed("Boissons gazeuses et jus", "Dopel énergie", 700),
            new ProduitSeed("Boissons gazeuses et jus", "Gros coca", 1000),
            new ProduitSeed("Autres boissons", "Eau lafi", 600),
            new ProduitSeed("Autres boissons", "Dafani", 1200)
    );

    // --- Cave à vin (produits_prix_unitaires.md — les 3 vins) ---
    private static final List<ProduitSeed> CAVE = List.of(
            new ProduitSeed("Vins", "Château 18 petit", 500),
            new ProduitSeed("Vins", "Château de France", 1000),
            new ProduitSeed("Vins", "Don Simon", 1500)
    );

    // --- Restaurant (menu.md) — les prix doubles ("500/1000") sont scindés en deux produits ---
    private static final List<ProduitSeed> RESTAURANT = List.of(
            new ProduitSeed("Boissons chaudes", "Lipton", 200),
            new ProduitSeed("Boissons chaudes", "Nescafé", 100),
            new ProduitSeed("Boissons chaudes", "Carte noire", 200),
            new ProduitSeed("Boissons chaudes", "Cafe Express", 200),
            new ProduitSeed("Boissons chaudes", "Nespresso", 500),
            new ProduitSeed("Petit-déjeuner", "Omelette", 600),
            new ProduitSeed("Petit-déjeuner", "Friand", 700),
            new ProduitSeed("Petit-déjeuner", "Pain au raisin", 700),
            new ProduitSeed("Petit-déjeuner", "Pain au chocolat", 600),
            new ProduitSeed("Petit-déjeuner", "Croissant", 500),
            new ProduitSeed("Petit-déjeuner", "Madeleine", 100),
            new ProduitSeed("Sandwichs", "Sandwich viande hachée/avocat", 300),
            new ProduitSeed("Sandwichs", "Sandwich boulette", 400),
            new ProduitSeed("Pâtes", "Spaghetti simple", 600),
            new ProduitSeed("Pâtes", "Spaghetti au soumbala", 700),
            new ProduitSeed("Pâtes", "Spaghetti bolonaise", 1000),
            new ProduitSeed("Accompagnements", "Frites (petite portion)", 500),
            new ProduitSeed("Accompagnements", "Frites (grande portion)", 1000),
            new ProduitSeed("Accompagnements", "Crudités", 500),
            new ProduitSeed("Accompagnements", "Pommes de terre sautées (petite portion)", 500),
            new ProduitSeed("Accompagnements", "Pommes de terre sautées (grande portion)", 1000),
            new ProduitSeed("Accompagnements", "Jardinier/Garniture", 1000),
            new ProduitSeed("Viandes grillées", "Biftek", 1000),
            new ProduitSeed("Viandes grillées", "Filet", 1000),
            new ProduitSeed("Viandes grillées", "Foie", 1000),
            new ProduitSeed("Viandes grillées", "Royons", 1000),
            new ProduitSeed("Viandes grillées", "Jarret", 1000),
            new ProduitSeed("Plats traditionnels", "Attiéké (petite portion)", 1000),
            new ProduitSeed("Plats traditionnels", "Attiéké (grande portion)", 1500),
            new ProduitSeed("Plats traditionnels", "Riz sauce arachide", 600),
            new ProduitSeed("Plats traditionnels", "Riz sauce tomate", 600),
            new ProduitSeed("Plats traditionnels", "Riz sauce feuille", 700),
            new ProduitSeed("Plats traditionnels", "Riz sauce graine", 800),
            new ProduitSeed("Plats traditionnels", "Riz gras", 600),
            new ProduitSeed("Plats traditionnels", "Tot", 500),
            new ProduitSeed("Plats traditionnels", "Foutou", 1000),
            new ProduitSeed("Plats traditionnels", "Couscous", 800),
            new ProduitSeed("Plats traditionnels", "Ragout pomme de terre", 800),
            new ProduitSeed("Plats traditionnels", "Ragout d'igname", 800),
            new ProduitSeed("Soupes", "Soupe de bœuf", 1000),
            new ProduitSeed("Soupes", "Soupe de boyaux", 1000),
            new ProduitSeed("Soupes", "Soupe de poisson carpe", 1000),
            new ProduitSeed("Soupes", "Soupe de poisson chinchard", 750),
            new ProduitSeed("Soupes", "Soupe de mouton", 1000),
            new ProduitSeed("Soupes", "Soupe de poulet 1/4", 1000)
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seed("MAQ", MAQUIS, UniteProduit.BOUTEILLE);
        seed("CAV", CAVE, UniteProduit.BOUTEILLE);
        seed("RES", RESTAURANT, UniteProduit.PORTION);
    }

    private void seed(String codeEtablissement, List<ProduitSeed> produits, UniteProduit unite) {
        Etablissement etablissement = etablissementRepository.findByCode(codeEtablissement).orElse(null);
        if (etablissement == null) {
            return; // DataSeeder n'a pas encore tourné (ordre garanti par @Order, filet de sécurité)
        }

        Map<String, Categorie> categoriesCache = new HashMap<>();
        int crees = 0;
        for (ProduitSeed seed : produits) {
            if (produitRepository.existsByEtablissementAndNomIgnoreCase(etablissement, seed.nom())) {
                continue;
            }
            Categorie categorie = categoriesCache.computeIfAbsent(seed.categorie(),
                    nom -> trouverOuCreerCategorie(etablissement, nom));

            Produit p = new Produit();
            p.setEtablissement(etablissement);
            p.setCategorie(categorie);
            p.setNom(seed.nom());
            p.setUnite(unite);
            p.setPrixVente(BigDecimal.valueOf(seed.prix()));
            p.setActif(true);
            p.setDisponible(true);
            p.setSuiviStock(false);
            produitRepository.save(p);
            crees++;
        }
        if (crees > 0) {
            log.info("[SEED] {} produit(s) créé(s) pour {} ({})", crees, etablissement.getNom(), codeEtablissement);
        }
    }

    private Categorie trouverOuCreerCategorie(Etablissement etablissement, String nom) {
        return categorieRepository.findByEtablissementAndActifTrue(etablissement).stream()
                .filter(c -> c.getNom().equalsIgnoreCase(nom))
                .findFirst()
                .orElseGet(() -> {
                    Categorie c = new Categorie();
                    c.setEtablissement(etablissement);
                    c.setNom(nom);
                    c.setActif(true);
                    return categorieRepository.save(c);
                });
    }
}
