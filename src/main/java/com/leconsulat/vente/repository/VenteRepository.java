package com.leconsulat.vente.repository;

import com.leconsulat.vente.entity.StatutVente;
import com.leconsulat.vente.entity.Vente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VenteRepository extends JpaRepository<Vente, Long> {

    @Query("select v from Vente v where " +
            "(cast(:statut as string) is null or v.statut = :statut) and " +
            "(cast(:sessionCaisseId as long) is null or v.sessionCaisse.id = :sessionCaisseId) and " +
            "(cast(:caissierId as long) is null or v.caissier.id = :caissierId) and " +
            "(cast(:dateDebut as timestamp) is null or v.dateVente >= :dateDebut) and " +
            "(cast(:dateFin as timestamp) is null or v.dateVente <= :dateFin) " +
            "order by v.dateVente desc")
    Page<Vente> search(@Param("statut") StatutVente statut, @Param("sessionCaisseId") Long sessionCaisseId,
                        @Param("caissierId") Long caissierId, @Param("dateDebut") LocalDateTime dateDebut,
                        @Param("dateFin") LocalDateTime dateFin, Pageable pageable);

    long countByNumeroStartingWith(String prefix);

    List<Vente> findBySessionCaisseIdAndStatut(Long sessionCaisseId, StatutVente statut);

    List<Vente> findByStatutAndDateVenteBetween(StatutVente statut, LocalDateTime debut, LocalDateTime fin);

    List<Vente> findTop10ByStatutOrderByDateVenteDesc(StatutVente statut);
}
