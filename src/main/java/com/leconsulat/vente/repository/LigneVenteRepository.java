package com.leconsulat.vente.repository;

import com.leconsulat.vente.entity.LigneVente;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LigneVenteRepository extends JpaRepository<LigneVente, Long> {

    @Query("select l.produit.nom as nom, sum(l.quantite) as quantite, sum(l.montant) as montant " +
            "from LigneVente l where l.vente.statut = com.leconsulat.vente.entity.StatutVente.PAYEE " +
            "and l.vente.dateVente >= :depuis " +
            "group by l.produit.nom order by sum(l.montant) desc")
    List<TopProduitProjection> topProduitsDepuis(@Param("depuis") LocalDateTime depuis, Pageable pageable);

    interface TopProduitProjection {
        String getNom();
        Long getQuantite();
        java.math.BigDecimal getMontant();
    }
}
