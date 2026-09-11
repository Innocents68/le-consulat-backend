package com.leconsulat.avoir.repository;

import com.leconsulat.avoir.entity.Avoir;
import com.leconsulat.etablissement.entity.Etablissement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface AvoirRepository extends JpaRepository<Avoir, Long> {

    Page<Avoir> findByEtablissementOrderByDateCreationDesc(Etablissement etablissement, Pageable pageable);

    List<Avoir> findByFactureId(Long factureId);

    /** RG-061 : cumul déjà émis sur une facture, pour vérifier qu'un nouvel avoir ne dépasse pas
     * le montant net restant. */
    @Query("select coalesce(sum(a.montant), 0) from Avoir a where a.facture.id = :factureId")
    BigDecimal sommeAvoirsExistants(@Param("factureId") Long factureId);

    /** §6.7.1 (EF-032/033, RG-088) : déduction des avoirs sur une période/utilisateur — pas de
     * filtre mode de paiement, un avoir porte un {@code ModeRemboursement}, notion différente
     * du {@code ModePaiement} d'une facture ou d'une dépense. */
    @Query("select coalesce(sum(a.montant), 0) from Avoir a where a.etablissement = :etablissement " +
            "and (cast(:dateDebut as timestamp) is null or a.dateCreation >= :dateDebut) " +
            "and (cast(:dateFin as timestamp) is null or a.dateCreation <= :dateFin) " +
            "and (cast(:utilisateurId as long) is null or a.auteur.id = :utilisateurId)")
    BigDecimal sommeAvoirsPeriode(@Param("etablissement") Etablissement etablissement,
                                  @Param("dateDebut") LocalDateTime dateDebut,
                                  @Param("dateFin") LocalDateTime dateFin,
                                  @Param("utilisateurId") Long utilisateurId);
}
