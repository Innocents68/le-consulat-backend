package com.leconsulat.stock.repository;

import com.leconsulat.stock.entity.CategorieProduit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategorieProduitRepository extends JpaRepository<CategorieProduit, Long> {
}
