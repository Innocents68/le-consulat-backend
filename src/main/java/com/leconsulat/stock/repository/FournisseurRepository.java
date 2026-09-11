package com.leconsulat.stock.repository;

import com.leconsulat.stock.entity.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {
    List<Fournisseur> findByActifTrue();

    boolean existsByNomIgnoreCase(String nom);
}
