package com.leconsulat.stock.repository;

import com.leconsulat.stock.entity.Inventaire;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventaireRepository extends JpaRepository<Inventaire, Long> {
}
