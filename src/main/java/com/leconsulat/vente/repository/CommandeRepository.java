package com.leconsulat.vente.repository;

import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.vente.entity.Commande;
import com.leconsulat.vente.entity.StatutCommande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {

    @Query("select c from Commande c where c.etablissement = :etablissement " +
            "and (cast(:statut as string) is null or c.statut = :statut) " +
            "and (cast(:tableId as long) is null or c.table.id = :tableId) " +
            "and (cast(:dateDebut as timestamp) is null or c.dateCreation >= :dateDebut) " +
            "and (cast(:dateFin as timestamp) is null or c.dateCreation <= :dateFin) " +
            "order by c.dateCreation desc")
    Page<Commande> search(@Param("etablissement") Etablissement etablissement,
                           @Param("statut") StatutCommande statut,
                           @Param("tableId") Long tableId,
                           @Param("dateDebut") LocalDateTime dateDebut,
                           @Param("dateFin") LocalDateTime dateFin,
                           Pageable pageable);

    List<Commande> findByTableIdAndStatutNotIn(Long tableId, List<StatutCommande> statuts);

    /** Écran Suivi cuisine (EF-018) : les plus anciennes en premier, les plus urgentes. */
    List<Commande> findByEtablissementAndStatutInOrderByDateValidationAsc(Etablissement etablissement, List<StatutCommande> statuts);
}
