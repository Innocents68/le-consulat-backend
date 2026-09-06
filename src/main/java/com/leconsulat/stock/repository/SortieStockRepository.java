package com.leconsulat.stock.repository;

import com.leconsulat.stock.entity.SortieStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface SortieStockRepository extends JpaRepository<SortieStock, Long> {

    @Query("select s from SortieStock s where " +
            "(cast(:produitId as long) is null or s.produit.id = :produitId) and " +
            "(cast(:dateDebut as date) is null or s.dateSortie >= :dateDebut) and " +
            "(cast(:dateFin as date) is null or s.dateSortie <= :dateFin)")
    Page<SortieStock> search(@Param("produitId") Long produitId, @Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin, Pageable pageable);
}
