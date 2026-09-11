package com.leconsulat.etablissement.repository;

import com.leconsulat.etablissement.entity.Etablissement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtablissementRepository extends JpaRepository<Etablissement, Long> {

    List<Etablissement> findByActifTrue();

    Optional<Etablissement> findByCode(String code);

    boolean existsByCode(String code);
}
