package com.leconsulat.remise.repository;

import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.remise.entity.Remise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RemiseRepository extends JpaRepository<Remise, Long> {
    List<Remise> findByEtablissementAndActifTrue(Etablissement etablissement);
}
