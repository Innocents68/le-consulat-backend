package com.leconsulat.journal.repository;

import com.leconsulat.journal.entity.JournalOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface JournalOperationRepository extends JpaRepository<JournalOperation, Long> {

    @Query("select j from JournalOperation j where " +
            "(cast(:utilisateurId as long) is null or j.utilisateurId = :utilisateurId) and " +
            "(:module is null or lower(j.module) = lower(cast(:module as string))) and " +
            "(cast(:dateDebut as timestamp) is null or j.dateOperation >= :dateDebut) and " +
            "(cast(:dateFin as timestamp) is null or j.dateOperation <= :dateFin) " +
            "order by j.dateOperation desc")
    Page<JournalOperation> search(@Param("utilisateurId") Long utilisateurId,
                                   @Param("module") String module,
                                   @Param("dateDebut") LocalDateTime dateDebut,
                                   @Param("dateFin") LocalDateTime dateFin,
                                   Pageable pageable);
}
