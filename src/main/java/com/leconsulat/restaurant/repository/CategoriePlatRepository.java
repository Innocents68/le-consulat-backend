package com.leconsulat.restaurant.repository;

import com.leconsulat.restaurant.entity.CategoriePlat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriePlatRepository extends JpaRepository<CategoriePlat, Long> {
    List<CategoriePlat> findAllByOrderByOrdreAsc();
}
