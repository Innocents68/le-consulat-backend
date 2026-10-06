package com.leconsulat.sauvegarde.repository;

import com.leconsulat.sauvegarde.entity.Sauvegarde;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SauvegardeRepository extends JpaRepository<Sauvegarde, Long> {

    Page<Sauvegarde> findAllByOrderByDateCreationDesc(Pageable pageable);

    /** EF-045 : purge de rétention (30 jours minimum). */
    List<Sauvegarde> findByDateCreationBefore(LocalDateTime seuil);

    /** Consu_corrige.docx §9 : un utilisateur ayant déjà déclenché une sauvegarde ne peut pas
     * être supprimé (FK sauvegardes.auteur_id). */
    boolean existsByAuteurId(Long auteurId);
}
