package com.leconsulat.catalogue.repository;

import com.leconsulat.catalogue.entity.Categorie;
import com.leconsulat.etablissement.entity.Etablissement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    List<Categorie> findByEtablissementAndActifTrue(Etablissement etablissement);

    boolean existsByEtablissementAndNomIgnoreCase(Etablissement etablissement, String nom);

    Optional<Categorie> findByEtablissementAndNomIgnoreCase(Etablissement etablissement, String nom);
}
