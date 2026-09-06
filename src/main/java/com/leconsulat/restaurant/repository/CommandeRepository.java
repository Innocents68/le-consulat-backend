package com.leconsulat.restaurant.repository;

import com.leconsulat.restaurant.entity.Commande;
import com.leconsulat.restaurant.entity.StatutCommande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {

    @Query("select c from Commande c where " +
            "(cast(:statut as string) is null or c.statut = :statut) and " +
            "(cast(:tableId as long) is null or c.table.id = :tableId)")
    Page<Commande> search(@Param("statut") StatutCommande statut, @Param("tableId") Long tableId, Pageable pageable);

    List<Commande> findByTableIdAndStatutNotIn(Long tableId, List<StatutCommande> statuts);

    long countByStatutInAndDateCreationBetween(List<StatutCommande> statuts, LocalDateTime debut, LocalDateTime fin);

    List<Commande> findTop10ByOrderByDateCreationDesc();
}
