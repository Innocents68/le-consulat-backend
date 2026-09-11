package com.leconsulat.depense.repository;

import com.leconsulat.depense.entity.CategorieDepense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategorieDepenseRepository extends JpaRepository<CategorieDepense, Long> {
    List<CategorieDepense> findByActifTrue();

    boolean existsByNomIgnoreCase(String nom);
}
