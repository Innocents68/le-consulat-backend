package com.leconsulat.salle.repository;

import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.salle.entity.TableService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TableServiceRepository extends JpaRepository<TableService, Long> {

    List<TableService> findByEtablissementAndActifTrue(Etablissement etablissement);

    boolean existsByEtablissementAndNumeroIgnoreCase(Etablissement etablissement, String numero);
}
