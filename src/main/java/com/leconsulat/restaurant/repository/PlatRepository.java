package com.leconsulat.restaurant.repository;

import com.leconsulat.restaurant.entity.Plat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlatRepository extends JpaRepository<Plat, Long> {

    @Query("select p from Plat p where " +
            "(cast(:categorieId as long) is null or p.categorie.id = :categorieId) and " +
            "(cast(:actif as boolean) is null or p.actif = :actif) and " +
            "(cast(:disponible as boolean) is null or p.disponible = :disponible) and " +
            "(:search is null or lower(p.nom) like lower(concat('%', cast(:search as string), '%')))")
    Page<Plat> search(@Param("categorieId") Long categorieId, @Param("actif") Boolean actif,
                       @Param("disponible") Boolean disponible, @Param("search") String search, Pageable pageable);
}
