package com.leconsulat.finance.repository;

import com.leconsulat.finance.entity.Depense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DepenseRepository extends JpaRepository<Depense, Long> {

    @Query("select d from Depense d where " +
            "(cast(:categorieId as long) is null or d.categorie.id = :categorieId) and " +
            "(cast(:dateDebut as date) is null or d.date >= :dateDebut) and " +
            "(cast(:dateFin as date) is null or d.date <= :dateFin) " +
            "order by d.date desc")
    Page<Depense> search(@Param("categorieId") Long categorieId, @Param("dateDebut") LocalDate dateDebut,
                          @Param("dateFin") LocalDate dateFin, Pageable pageable);

    boolean existsByCategorieId(Long categorieId);

    List<Depense> findByStatutAndDateBetween(Depense.Statut statut, LocalDate debut, LocalDate fin);
}
