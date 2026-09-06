package com.leconsulat.stock.repository;

import com.leconsulat.stock.entity.EntreeStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface EntreeStockRepository extends JpaRepository<EntreeStock, Long> {

    @Query("select e from EntreeStock e where " +
            "(cast(:produitId as long) is null or e.produit.id = :produitId) and " +
            "(cast(:dateDebut as date) is null or e.dateEntree >= :dateDebut) and " +
            "(cast(:dateFin as date) is null or e.dateEntree <= :dateFin)")
    Page<EntreeStock> search(@Param("produitId") Long produitId, @Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin, Pageable pageable);
}
