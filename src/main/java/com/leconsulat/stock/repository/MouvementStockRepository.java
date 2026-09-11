package com.leconsulat.stock.repository;

import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.stock.entity.MouvementStock;
import com.leconsulat.stock.entity.TypeMouvementStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {

    @Query("select m from MouvementStock m where m.etablissement = :etablissement " +
            "and (cast(:produitId as long) is null or m.produit.id = :produitId) " +
            "and (:type is null or m.type = :type) " +
            "and (cast(:dateDebut as timestamp) is null or m.dateMouvement >= :dateDebut) " +
            "and (cast(:dateFin as timestamp) is null or m.dateMouvement <= :dateFin) " +
            "order by m.dateMouvement desc")
    Page<MouvementStock> search(@Param("etablissement") Etablissement etablissement,
                                 @Param("produitId") Long produitId,
                                 @Param("type") TypeMouvementStock type,
                                 @Param("dateDebut") LocalDateTime dateDebut,
                                 @Param("dateFin") LocalDateTime dateFin,
                                 Pageable pageable);

    /** Rapport des stocks (§6.9.3) : tous les mouvements de la période, pour un regroupement par
     * type en mémoire — même approche que le regroupement par jour/mode du Rapport des recettes. */
    @Query("select m from MouvementStock m where m.etablissement = :etablissement " +
            "and (cast(:dateDebut as timestamp) is null or m.dateMouvement >= :dateDebut) " +
            "and (cast(:dateFin as timestamp) is null or m.dateMouvement <= :dateFin)")
    List<MouvementStock> findForReporting(@Param("etablissement") Etablissement etablissement,
                                           @Param("dateDebut") LocalDateTime dateDebut,
                                           @Param("dateFin") LocalDateTime dateFin);
}
