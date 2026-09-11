package com.leconsulat.seed;

import com.leconsulat.depense.entity.CategorieDepense;
import com.leconsulat.depense.repository.CategorieDepenseRepository;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.utilisateur.entity.Profil;
import com.leconsulat.utilisateur.entity.Utilisateur;
import com.leconsulat.utilisateur.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Au premier démarrage sur une base vide : crée les 3 établissements du cahier des charges
 * (Maquis, Restaurant, Cave à vin — RG-001, stockés en base et non codés en dur) puis un compte
 * Super Administrateur pour pouvoir se connecter et commencer à affecter des Gérants/Caissiers.
 * {@code @Order(1)} : doit tourner avant {@link ProduitSeeder}, qui a besoin des établissements.
 */
@Component
@Order(1)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UtilisateurRepository utilisateurRepository;
    private final EtablissementRepository etablissementRepository;
    private final CategorieDepenseRepository categorieDepenseRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String[] CATEGORIES_DEPENSES = {
            "Achat de marchandises", "Alimentation", "Boissons", "Transport", "Électricité", "Eau",
            "Internet", "Entretien", "Maintenance", "Réparation", "Loyer", "Salaires", "Fournitures",
            "Communication", "Autres dépenses",
    };

    public DataSeeder(UtilisateurRepository utilisateurRepository, EtablissementRepository etablissementRepository,
                       CategorieDepenseRepository categorieDepenseRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.etablissementRepository = etablissementRepository;
        this.categorieDepenseRepository = categorieDepenseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Indépendant du early-return ci-dessous : doit s'exécuter même sur une base déjà
        // peuplée d'utilisateurs (Lot 5a ajouté après coup sur une base de dev existante).
        if (categorieDepenseRepository.count() == 0) {
            for (String nom : CATEGORIES_DEPENSES) {
                CategorieDepense c = new CategorieDepense();
                c.setNom(nom);
                c.setActif(true);
                categorieDepenseRepository.save(c);
            }
            log.info("[SEED] {} catégories de dépenses créées (§6.7.2).", CATEGORIES_DEPENSES.length);
        }

        if (utilisateurRepository.count() > 0) {
            log.info("[SEED] Base déjà initialisée — seed ignoré.");
            return;
        }

        creerEtablissement("MAQ", "Maquis", false);
        creerEtablissement("RES", "Restaurant", true);
        creerEtablissement("CAV", "Cave à vin", false);

        Utilisateur admin = new Utilisateur();
        admin.setUsername("admin");
        admin.setNom("Administrateur");
        admin.setEmail("admin@example.com");
        admin.setProfil(Profil.SUPER_ADMINISTRATEUR);
        admin.setMotDePasse(passwordEncoder.encode("admin123"));
        admin.setActif(true);
        utilisateurRepository.save(admin);

        log.info("[SEED] 3 établissements créés et compte Super Administrateur créé (admin / admin123).");
    }

    private void creerEtablissement(String code, String nom, boolean gereCuisine) {
        Etablissement e = new Etablissement();
        e.setCode(code);
        e.setNom(nom);
        e.setActif(true);
        e.setGereCuisine(gereCuisine);
        etablissementRepository.save(e);
    }
}
