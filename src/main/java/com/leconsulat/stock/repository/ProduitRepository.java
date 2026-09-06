package com.leconsulat.stock.repository;

import com.leconsulat.stock.entity.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProduitRepository extends JpaRepository<Produit, Long> {

    boolean existsByCode(String code);

    Optional<Produit> findByCode(String code);

    @Query("select p from Produit p where " +
            "(:search is null or lower(p.nom) like lower(concat('%', cast(:search as string), '%')) or lower(p.code) like lower(concat('%', cast(:search as string), '%'))) and " +
            "(cast(:categorieId as long) is null or p.categorie.id = :categorieId) and " +
            "(cast(:actif as boolean) is null or p.actif = :actif)")
    Page<Produit> search(@Param("search") String search, @Param("categorieId") Long categorieId, @Param("actif") Boolean actif, Pageable pageable);

    List<Produit> findByActifTrueAndQuantiteStockLessThanEqual(int seuil);

    @Query("select p from Produit p where p.actif = true and p.quantiteStock <= p.seuilAlerte")
    List<Produit> findEnAlerte();

    long countByActifTrue();
}
