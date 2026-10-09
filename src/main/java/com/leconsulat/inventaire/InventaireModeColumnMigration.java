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
 * chaque démarrage, copie les valeurs existantes puis ne fait plus rien une fois fait (NULL check,
 * colonne legacy absente sur toute base créée après ce correctif). */
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
        Integer colonneLegacyPresente = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where lower(table_name) = 'inventaires' and lower(column_name) = 'mode'",
                Integer.class);
        if (colonneLegacyPresente == null || colonneLegacyPresente == 0) {
            return;
        }
        int migrees = jdbcTemplate.update("update inventaires set mode_inventaire = \"mode\" where mode_inventaire is null");
        if (migrees > 0) {
            log.info("[INVENTAIRES] {} inventaire(s) migré(s) depuis l'ancienne colonne 'mode' vers 'mode_inventaire'", migrees);
        }
    }
}
