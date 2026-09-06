package com.leconsulat.stock.repository;

import com.leconsulat.stock.entity.Depot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepotRepository extends JpaRepository<Depot, Long> {
}
