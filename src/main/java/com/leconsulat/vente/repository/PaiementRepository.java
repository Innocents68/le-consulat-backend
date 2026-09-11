package com.leconsulat.vente.repository;

import com.leconsulat.vente.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {
}
