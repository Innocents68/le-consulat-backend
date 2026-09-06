package com.leconsulat.restaurant.repository;

import com.leconsulat.restaurant.entity.TableRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableRestaurantRepository extends JpaRepository<TableRestaurant, Long> {
    boolean existsByNumero(String numero);
}
