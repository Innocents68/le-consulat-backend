package com.leconsulat.sauvegardes.repository;

import com.leconsulat.sauvegardes.entity.Sauvegarde;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SauvegardeRepository extends JpaRepository<Sauvegarde, Long> {
    List<Sauvegarde> findAllByOrderByDateCreationDesc();
}
