package com.leconsulat.vente.repository;

import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.vente.entity.Facture;
import com.leconsulat.vente.entity.ModePaiement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface FactureRepository extends JpaRepository<Facture, Long> {

    /** §6.9 (Reporting) : mêmes filtres que {@link #sommeRecettes}, mais la liste complète pour
     * un regroupement en mémoire (par jour, par mode, par utilisateur) — évite d'introduire du
     * SQL natif spécifique Postgres pour un {@code group by date}. */
    @Query("select f from Facture f where f.etablissement = :etablissement " +
            "and (cast(:dateDebut as timestamp) is null or f.dateEmission >= :dateDebut) " +
            "and (cast(:dateFin as timestamp) is null or f.dateEmission <= :dateFin) " +
            "and (cast(:utilisateurId as long) is null or f.commande.caissier.id = :utilisateurId) " +
            "and (:mode is null or f.mode = :mode) " +
            "order by f.dateEmission asc")
    List<Facture> findForReporting(@Param("etablissement") Etablissement etablissement,
                                    @Param("dateDebut") LocalDateTime dateDebut,
                                    @Param("dateFin") LocalDateTime dateFin,
                                    @Param("utilisateurId") Long utilisateurId,
                                    @Param("mode") ModePaiement mode);

    /** §6.7.1 (EF-032/033, RG-088) : recettes encaissées sur une période/utilisateur/mode. */
    @Query("select coalesce(sum(f.montantNet), 0) from Facture f where f.etablissement = :etablissement " +
            "and (cast(:dateDebut as timestamp) is null or f.dateEmission >= :dateDebut) " +
            "and (cast(:dateFin as timestamp) is null or f.dateEmission <= :dateFin) " +
            "and (cast(:utilisateurId as long) is null or f.commande.caissier.id = :utilisateurId) " +
            "and (:mode is null or f.mode = :mode)")
    BigDecimal sommeRecettes(@Param("etablissement") Etablissement etablissement,
                             @Param("dateDebut") LocalDateTime dateDebut,
                             @Param("dateFin") LocalDateTime dateFin,
                             @Param("utilisateurId") Long utilisateurId,
                             @Param("mode") ModePaiement mode);

    @Query("select f from Facture f where f.etablissement = :etablissement " +
            "and (cast(:search as string) is null or lower(f.numero) like lower(concat('%', cast(:search as string), '%'))) " +
            "and (cast(:commandeId as long) is null or f.commande.id = :commandeId) " +
            "and (cast(:dateDebut as timestamp) is null or f.dateEmission >= :dateDebut) " +
            "and (cast(:dateFin as timestamp) is null or f.dateEmission <= :dateFin) " +
            "order by f.dateEmission desc")
    Page<Facture> search(@Param("etablissement") Etablissement etablissement,
                          @Param("search") String search,
                          @Param("commandeId") Long commandeId,
                          @Param("dateDebut") LocalDateTime dateDebut,
                          @Param("dateFin") LocalDateTime dateFin,
                          Pageable pageable);
}
