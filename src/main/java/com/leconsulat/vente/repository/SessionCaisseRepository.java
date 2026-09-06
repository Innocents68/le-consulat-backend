package com.leconsulat.vente.repository;

import com.leconsulat.vente.entity.SessionCaisse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionCaisseRepository extends JpaRepository<SessionCaisse, Long> {
    List<SessionCaisse> findByStatut(SessionCaisse.Statut statut);
}
