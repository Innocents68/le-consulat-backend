package com.leconsulat.catalogue.repository;

import com.leconsulat.catalogue.entity.HistoriquePrix;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriquePrixRepository extends JpaRepository<HistoriquePrix, Long> {

    List<HistoriquePrix> findByProduitIdOrderByDateEffetDesc(Long produitId);
}
