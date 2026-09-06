package com.leconsulat.cave.repository;

import com.leconsulat.cave.entity.Boisson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoissonRepository extends JpaRepository<Boisson, Long> {

    @Query("select b from Boisson b where " +
            "(:search is null or lower(b.nom) like lower(concat('%', cast(:search as string), '%'))) and " +
            "(cast(:type as string) is null or b.type = :type) and " +
            "(cast(:actif as boolean) is null or b.actif = :actif)")
    Page<Boisson> search(@Param("search") String search, @Param("type") com.leconsulat.cave.entity.TypeBoisson type,
                          @Param("actif") Boolean actif, Pageable pageable);

    @Query("select b from Boisson b where b.actif = true and b.quantiteStock <= b.seuilAlerte")
    List<Boisson> findEnAlerte();

    long countByActifTrue();
}
