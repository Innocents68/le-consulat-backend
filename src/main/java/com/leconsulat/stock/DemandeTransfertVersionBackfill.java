package com.leconsulat.stock;

import com.leconsulat.stock.repository.DemandeTransfertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Correctif ponctuel : l'ajout de {@code @Version} sur {@link com.leconsulat.stock.entity.DemandeTransfert}
 * (Cahier_de_corrections_Le_Consulat.docx §1.4) a laissé les lignes déjà en base avec une version
 * NULL — sur une instance où la colonne a été créée par Hibernate avant que son {@code
 * columnDefinition} par défaut ne soit ajouté, ce NULL subsiste malgré le DEFAULT (qui ne
 * s'applique qu'au moment de l'ALTER TABLE). Résultat : NullPointerException au premier
 * accepter/refuser sur une telle demande (Hibernate tente d'incrémenter une version nulle). Ce
 * rattrapage, exécuté à chaque démarrage, est un simple UPDATE filtré — sans effet (et quasi
 * instantané) une fois toutes les lignes corrigées. */
@Component
public class DemandeTransfertVersionBackfill implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemandeTransfertVersionBackfill.class);

    private final DemandeTransfertRepository repository;

    public DemandeTransfertVersionBackfill(DemandeTransfertRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int corrigees = repository.backfillVersionNulle();
        if (corrigees > 0) {
            log.info("[DEMANDES_TRANSFERT] {} demande(s) de transfert corrigée(s) (version nulle initialisée à 0)", corrigees);
        }
    }
}
