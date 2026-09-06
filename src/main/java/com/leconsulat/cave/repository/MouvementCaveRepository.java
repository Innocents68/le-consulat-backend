package com.leconsulat.cave.repository;

import com.leconsulat.cave.entity.MouvementCave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface MouvementCaveRepository extends JpaRepository<MouvementCave, Long> {

    @Query("select m from MouvementCave m where " +
            "(cast(:boissonId as long) is null or m.boisson.id = :boissonId) and " +
            "(cast(:dateDebut as timestamp) is null or m.date >= :dateDebut) and " +
            "(cast(:dateFin as timestamp) is null or m.date <= :dateFin) " +
            "order by m.date desc")
    Page<MouvementCave> search(@Param("boissonId") Long boissonId, @Param("dateDebut") LocalDateTime dateDebut,
                                @Param("dateFin") LocalDateTime dateFin, Pageable pageable);
}
