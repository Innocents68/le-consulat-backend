package com.leconsulat.inventaire.repository;

import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.inventaire.entity.Inventaire;
import com.leconsulat.inventaire.entity.StatutInventaire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface InventaireRepository extends JpaRepository<Inventaire, Long> {

    @Query("select i from Inventaire i where i.etablissement = :etablissement " +
            "and (:statut is null or i.statut = :statut) " +
            "and (cast(:dateDebut as date) is null or i.dateInventaire >= :dateDebut) " +
            "and (cast(:dateFin as date) is null or i.dateInventaire <= :dateFin) " +
            "order by i.dateCreation desc")
    Page<Inventaire> search(@Param("etablissement") Etablissement etablissement,
                             @Param("statut") StatutInventaire statut,
                             @Param("dateDebut") LocalDate dateDebut,
                             @Param("dateFin") LocalDate dateFin,
                             Pageable pageable);
}
