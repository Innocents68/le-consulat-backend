package com.leconsulat.catalogue.repository;

import com.leconsulat.catalogue.entity.Produit;
import com.leconsulat.etablissement.entity.Etablissement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProduitRepository extends JpaRepository<Produit, Long> {

    List<Produit> findByEtablissementAndActifTrueAndSuiviStockTrue(Etablissement etablissement);

    @Query("select p from Produit p where p.etablissement = :etablissement " +
            "and (cast(:categorieId as long) is null or p.categorie.id = :categorieId) " +
            "and (cast(:actif as boolean) is null or p.actif = :actif) " +
            "and (cast(:suiviStock as boolean) is null or p.suiviStock = :suiviStock) " +
            "and (cast(:search as string) is null or lower(p.nom) like lower(concat('%', cast(:search as string), '%')))")
    Page<Produit> search(@Param("etablissement") Etablissement etablissement,
                          @Param("categorieId") Long categorieId,
                          @Param("actif") Boolean actif,
                          @Param("suiviStock") Boolean suiviStock,
                          @Param("search") String search,
                          Pageable pageable);

    boolean existsByEtablissementAndNomIgnoreCase(Etablissement etablissement, String nom);
}
