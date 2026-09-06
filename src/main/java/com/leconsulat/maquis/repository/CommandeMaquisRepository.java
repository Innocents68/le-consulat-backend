package com.leconsulat.maquis.repository;

import com.leconsulat.maquis.entity.CommandeMaquis;
import com.leconsulat.maquis.entity.StatutCommandeMaquis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CommandeMaquisRepository extends JpaRepository<CommandeMaquis, Long> {

    @Query("select c from CommandeMaquis c where (cast(:statut as string) is null or c.statut = :statut)")
    Page<CommandeMaquis> search(@Param("statut") StatutCommandeMaquis statut, Pageable pageable);

    long countByStatutAndDateCreationBetween(StatutCommandeMaquis statut, LocalDateTime debut, LocalDateTime fin);

    List<CommandeMaquis> findByStatutAndDateCreationBetween(StatutCommandeMaquis statut, LocalDateTime debut, LocalDateTime fin);
}
