package com.leconsulat.vente.repository;

import com.leconsulat.vente.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    /** Consu_corrige.docx §4 : supprime le paiement lié quand une facture est supprimée
     * (Super Administrateur uniquement, cas exceptionnel). */
    void deleteByCommandeId(Long commandeId);
}
