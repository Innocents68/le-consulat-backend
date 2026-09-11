package com.leconsulat.reporting.repository;

import com.leconsulat.vente.entity.LigneCommande;
import com.leconsulat.vente.entity.ModePaiement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/** §6.9.3 (Rapport des ventes) — aucun autre module n'accédait {@code LigneCommande} en dehors
 * de {@code Commande.lignes} jusqu'ici ; les requêtes ci-dessous ont besoin d'un accès direct
 * pour agréger/filtrer au niveau de la ligne plutôt que de la commande.
 *
 * {@code etablissementId} est nullable partout : nul seulement quand le Super Administrateur
 * choisit la vue globale comparée (RG-101) — un Gérant/Caissier reçoit toujours un id concret via
 * {@code PerimetreGuard.scopeEtablissement}, jamais nul. */
public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Long> {

    /** Vente = commande validée (a une date de validation) et non annulée. Le mode de paiement
     * n'est pas porté par la ligne : filtré via une sous-requête corrélée sur {@code Facture}. */
    @Query("select l from LigneCommande l where " +
            "(cast(:etablissementId as long) is null or l.commande.etablissement.id = :etablissementId) " +
            "and l.commande.dateValidation is not null and l.commande.statut <> com.leconsulat.vente.entity.StatutCommande.ANNULEE " +
            "and (cast(:dateDebut as timestamp) is null or l.commande.dateValidation >= :dateDebut) " +
            "and (cast(:dateFin as timestamp) is null or l.commande.dateValidation <= :dateFin) " +
            "and (cast(:categorieId as long) is null or l.produit.categorie.id = :categorieId) " +
            "and (cast(:produitId as long) is null or l.produit.id = :produitId) " +
            "and (cast(:utilisateurId as long) is null or l.commande.caissier.id = :utilisateurId) " +
            "and (:mode is null or exists (select 1 from Facture f where f.commande = l.commande and f.mode = :mode)) " +
            "order by l.commande.dateValidation desc")
    Page<LigneCommande> searchVentes(@Param("etablissementId") Long etablissementId,
                                      @Param("dateDebut") LocalDateTime dateDebut,
                                      @Param("dateFin") LocalDateTime dateFin,
                                      @Param("categorieId") Long categorieId,
                                      @Param("produitId") Long produitId,
                                      @Param("utilisateurId") Long utilisateurId,
                                      @Param("mode") ModePaiement mode,
                                      Pageable pageable);

    /** Même filtre que {@link #searchVentes}, sans pagination — utilisé pour le résumé
     * (quantité/montant total, panier moyen) et les exports, qui portent sur l'ensemble filtré
     * et non la seule page affichée. */
    @Query("select l from LigneCommande l where " +
            "(cast(:etablissementId as long) is null or l.commande.etablissement.id = :etablissementId) " +
            "and l.commande.dateValidation is not null and l.commande.statut <> com.leconsulat.vente.entity.StatutCommande.ANNULEE " +
            "and (cast(:dateDebut as timestamp) is null or l.commande.dateValidation >= :dateDebut) " +
            "and (cast(:dateFin as timestamp) is null or l.commande.dateValidation <= :dateFin) " +
            "and (cast(:categorieId as long) is null or l.produit.categorie.id = :categorieId) " +
            "and (cast(:produitId as long) is null or l.produit.id = :produitId) " +
            "and (cast(:utilisateurId as long) is null or l.commande.caissier.id = :utilisateurId) " +
            "and (:mode is null or exists (select 1 from Facture f where f.commande = l.commande and f.mode = :mode))")
    List<LigneCommande> findAllVentes(@Param("etablissementId") Long etablissementId,
                                       @Param("dateDebut") LocalDateTime dateDebut,
                                       @Param("dateFin") LocalDateTime dateFin,
                                       @Param("categorieId") Long categorieId,
                                       @Param("produitId") Long produitId,
                                       @Param("utilisateurId") Long utilisateurId,
                                       @Param("mode") ModePaiement mode);

    /** Top produits (Dashboard) — agrégé en base, limité via {@code Pageable} (ex.
     * {@code PageRequest.of(0, 10)}). */
    @Query("select l.produit.id, l.produit.nom, sum(l.quantite), sum(l.montant) from LigneCommande l " +
            "where (cast(:etablissementId as long) is null or l.commande.etablissement.id = :etablissementId) " +
            "and l.commande.dateValidation is not null and l.commande.statut <> com.leconsulat.vente.entity.StatutCommande.ANNULEE " +
            "and (cast(:dateDebut as timestamp) is null or l.commande.dateValidation >= :dateDebut) " +
            "and (cast(:dateFin as timestamp) is null or l.commande.dateValidation <= :dateFin) " +
            "group by l.produit.id, l.produit.nom order by sum(l.quantite) desc")
    List<Object[]> topProduits(@Param("etablissementId") Long etablissementId,
                                @Param("dateDebut") LocalDateTime dateDebut,
                                @Param("dateFin") LocalDateTime dateFin,
                                Pageable pageable);

    /** Rapport des bénéfices (§6.9.3) : coût des marchandises vendues = Σ quantité × prix d'achat
     * sur les lignes vendues de la période — {@code coalesce(prixAchat, 0)} pour un produit sans
     * prix d'achat renseigné (même simplification que l'écart de valeur d'un inventaire, Lot 4b). */
    @Query("select coalesce(sum(l.quantite * coalesce(l.produit.prixAchat, 0)), 0) from LigneCommande l " +
            "where (cast(:etablissementId as long) is null or l.commande.etablissement.id = :etablissementId) " +
            "and l.commande.dateValidation is not null and l.commande.statut <> com.leconsulat.vente.entity.StatutCommande.ANNULEE " +
            "and (cast(:dateDebut as timestamp) is null or l.commande.dateValidation >= :dateDebut) " +
            "and (cast(:dateFin as timestamp) is null or l.commande.dateValidation <= :dateFin)")
    java.math.BigDecimal sommeCoutMarchandisesVendues(@Param("etablissementId") Long etablissementId,
                                                        @Param("dateDebut") LocalDateTime dateDebut,
                                                        @Param("dateFin") LocalDateTime dateFin);
}
