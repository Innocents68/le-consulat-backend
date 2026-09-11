package com.leconsulat.depense.repository;

import com.leconsulat.depense.entity.Depense;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.vente.entity.ModePaiement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DepenseRepository extends JpaRepository<Depense, Long> {

    @Query("select d from Depense d where d.etablissement = :etablissement " +
            "and (cast(:categorieId as long) is null or d.categorie.id = :categorieId) " +
            "and (:mode is null or d.modePaiement = :mode) " +
            "and (cast(:dateDebut as date) is null or d.date >= :dateDebut) " +
            "and (cast(:dateFin as date) is null or d.date <= :dateFin) " +
            "order by d.date desc, d.dateCreation desc")
    Page<Depense> search(@Param("etablissement") Etablissement etablissement,
                          @Param("categorieId") Long categorieId,
                          @Param("mode") ModePaiement mode,
                          @Param("dateDebut") LocalDate dateDebut,
                          @Param("dateFin") LocalDate dateFin,
                          Pageable pageable);

    /** RG-088 : n'entrent dans le solde que les dépenses non annulées. */
    @Query("select coalesce(sum(d.montant), 0) from Depense d where d.etablissement = :etablissement " +
            "and d.annulee = false " +
            "and (cast(:dateDebut as date) is null or d.date >= :dateDebut) " +
            "and (cast(:dateFin as date) is null or d.date <= :dateFin) " +
            "and (cast(:utilisateurId as long) is null or d.utilisateur.id = :utilisateurId) " +
            "and (:mode is null or d.modePaiement = :mode)")
    BigDecimal sommeDepenses(@Param("etablissement") Etablissement etablissement,
                             @Param("dateDebut") LocalDate dateDebut,
                             @Param("dateFin") LocalDate dateFin,
                             @Param("utilisateurId") Long utilisateurId,
                             @Param("mode") ModePaiement mode);
}
