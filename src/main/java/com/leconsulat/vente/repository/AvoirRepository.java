package com.leconsulat.vente.repository;

import com.leconsulat.vente.entity.Avoir;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AvoirRepository extends JpaRepository<Avoir, Long> {

    @Query("select a from Avoir a where " +
            "(cast(:dateDebut as timestamp) is null or a.dateEmission >= :dateDebut) and " +
            "(cast(:dateFin as timestamp) is null or a.dateEmission <= :dateFin) " +
            "order by a.dateEmission desc")
    Page<Avoir> search(@Param("dateDebut") LocalDateTime dateDebut, @Param("dateFin") LocalDateTime dateFin, Pageable pageable);
}
