package com.leconsulat.finance.repository;

import com.leconsulat.finance.entity.Recette;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RecetteRepository extends JpaRepository<Recette, Long> {

    @Query("select r from Recette r where " +
            "(cast(:dateDebut as timestamp) is null or r.date >= :dateDebut) and " +
            "(cast(:dateFin as timestamp) is null or r.date <= :dateFin) " +
            "order by r.date desc")
    Page<Recette> search(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin, Pageable pageable);

    List<Recette> findByDateBetween(LocalDateTime debut, LocalDateTime fin);
}
