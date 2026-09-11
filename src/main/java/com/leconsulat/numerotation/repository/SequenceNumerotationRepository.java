package com.leconsulat.numerotation.repository;

import com.leconsulat.numerotation.entity.SequenceNumerotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SequenceNumerotationRepository extends JpaRepository<SequenceNumerotation, Long> {

    /** Upsert atomique (Postgres) : crée la séquence à 1 si elle n'existe pas encore pour ce
     * triplet (établissement, type, année), sinon l'incrémente — en une seule opération, sans
     * risque de course entre deux premières commandes de l'année créées au même instant
     * (un simple "lire puis verrouiller" ne fonctionne pas ici puisque la ligne n'existe pas
     * encore à ce moment-là pour le tout premier document). */
    @Modifying
    @Query(value = "insert into sequences_numerotation (etablissement_id, type, annee, dernier_numero) " +
            "values (:etablissementId, :type, :annee, 1) " +
            "on conflict (etablissement_id, type, annee) " +
            "do update set dernier_numero = sequences_numerotation.dernier_numero + 1", nativeQuery = true)
    void upsertEtIncrementer(@Param("etablissementId") Long etablissementId, @Param("type") String type, @Param("annee") int annee);

    @Query(value = "select dernier_numero from sequences_numerotation where etablissement_id = :etablissementId and type = :type and annee = :annee", nativeQuery = true)
    int lireNumeroCourant(@Param("etablissementId") Long etablissementId, @Param("type") String type, @Param("annee") int annee);
}
