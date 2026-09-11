package com.leconsulat.inventaire.repository;

import com.leconsulat.inventaire.entity.LigneInventaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LigneInventaireRepository extends JpaRepository<LigneInventaire, Long> {

    /** Rapport des stocks (§6.9.3) : écarts des inventaires validés (RG-086/087, les seuls dont
     * les écarts sont définitifs) dont la validation tombe dans la période choisie. */
    @Query("select l from LigneInventaire l where l.inventaire.etablissement.id = :etablissementId " +
            "and l.inventaire.statut = com.leconsulat.inventaire.entity.StatutInventaire.VALIDE " +
            "and l.ecartQuantite is not null and l.ecartQuantite <> 0 " +
            "and (cast(:dateDebut as timestamp) is null or l.inventaire.dateValidation >= :dateDebut) " +
            "and (cast(:dateFin as timestamp) is null or l.inventaire.dateValidation <= :dateFin) " +
            "order by l.inventaire.dateValidation desc")
    List<LigneInventaire> findEcarts(@Param("etablissementId") Long etablissementId,
                                      @Param("dateDebut") LocalDateTime dateDebut,
                                      @Param("dateFin") LocalDateTime dateFin);
}
