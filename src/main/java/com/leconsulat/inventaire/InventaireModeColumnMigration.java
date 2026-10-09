package com.leconsulat.inventaire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Correctif ponctuel : la colonne {@code mode} de {@code inventaires} a été renommée en
 * {@code mode_inventaire} ({@link com.leconsulat.inventaire.entity.Inventaire}) — {@code mode}
 * est un mot réservé PostgreSQL (fonction d'agrégat à ensemble ordonné) qui fait planter toute
 * requête générée par Hibernate la référençant. {@code ddl-auto=update} ajoute la nouvelle
 * colonne sans jamais migrer les données de l'ancienne ni la supprimer : ce rattrapage, exécuté à
 * chaque démarrage, copie les valeurs existantes. Recopie inconditionnelle (pas de garde "where
 * mode_inventaire is null") : la colonne a un DEFAULT, donc PostgreSQL (fast default, ≥ PG11)
 * répond déjà cette valeur par défaut pour les anciennes lignes sans qu'elles soient réellement
 * NULL — un tel filtre laisserait silencieusement les inventaires historiquement en mode COMPLET
 * basculer en apparence sur RAPIDE au lieu de reprendre leur vraie valeur. Ne fait plus rien dès
 * que la colonne legacy est absente (bases créées après ce correctif, ou nettoyage manuel futur). */
@Component
public class InventaireModeColumnMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InventaireModeColumnMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public InventaireModeColumnMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Défensif : un rattrapage au démarrage ne doit jamais empêcher l'application de démarrer,
        // même si le schéma est dans un état inattendu (ex. ddl-auto=update qui a échoué en
        // silence sur l'ALTER TABLE — exactement le scénario qui a motivé ce correctif).
        try {
            boolean colonneCibleExiste = colonneExiste("mode_inventaire");
            boolean colonneLegacyExiste = colonneExiste("mode");
            if (!colonneCibleExiste) {
                log.warn("[INVENTAIRES] La colonne 'mode_inventaire' n'existe pas encore sur 'inventaires' — "
                        + "le module Inventaires restera en erreur tant que le schéma n'aura pas été corrigé manuellement.");
                return;
            }
            if (!colonneLegacyExiste) {
                return;
            }
            int migrees = jdbcTemplate.update(
                    "update inventaires set mode_inventaire = \"mode\" where mode_inventaire is distinct from \"mode\"");
            if (migrees > 0) {
                log.info("[INVENTAIRES] {} inventaire(s) migré(s) depuis l'ancienne colonne 'mode' vers 'mode_inventaire'", migrees);
            }
        } catch (Exception e) {
            log.error("[INVENTAIRES] Échec du rattrapage de la colonne 'mode_inventaire' (l'application démarre quand même) : {}", e.getMessage(), e);
        }
    }

    private boolean colonneExiste(String nomColonne) {
        Integer n = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where lower(table_name) = 'inventaires' and lower(column_name) = ?",
                Integer.class, nomColonne);
        return n != null && n > 0;
    }
}
