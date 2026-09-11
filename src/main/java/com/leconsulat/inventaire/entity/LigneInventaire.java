package com.leconsulat.inventaire.entity;

import com.leconsulat.catalogue.entity.Produit;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_inventaire")
public class LigneInventaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventaire_id", nullable = false)
    private Inventaire inventaire;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    /** Snapshot du nom au moment de la création de l'inventaire — comme
     * {@code LigneCommande.articleNom}. */
    @Column(nullable = false, length = 100)
    private String produitNom;

    /** Figé à la création de l'inventaire = {@code Produit.quantiteStock} de l'instant. */
    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal stockTheorique;

    /** Saisi pendant le comptage (statut {@code EN_COMPTAGE}) — nul tant qu'aucun comptage
     * n'a été effectué sur cette ligne. */
    @Column(precision = 14, scale = 3)
    private BigDecimal stockPhysique;

    /** Calculés et figés à la clôture uniquement (RG-086/RG-087). */
    @Column(precision = 14, scale = 3)
    private BigDecimal ecartQuantite;

    @Column(precision = 14, scale = 0)
    private BigDecimal ecartValeur;

    public LigneInventaire() {
    }

    public Long getId() {
        return id;
    }

    public Inventaire getInventaire() {
        return inventaire;
    }

    public void setInventaire(Inventaire inventaire) {
        this.inventaire = inventaire;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public String getProduitNom() {
        return produitNom;
    }

    public void setProduitNom(String produitNom) {
        this.produitNom = produitNom;
    }

    public BigDecimal getStockTheorique() {
        return stockTheorique;
    }

    public void setStockTheorique(BigDecimal stockTheorique) {
        this.stockTheorique = stockTheorique;
    }

    public BigDecimal getStockPhysique() {
        return stockPhysique;
    }

    public void setStockPhysique(BigDecimal stockPhysique) {
        this.stockPhysique = stockPhysique;
    }

    public BigDecimal getEcartQuantite() {
        return ecartQuantite;
    }

    public void setEcartQuantite(BigDecimal ecartQuantite) {
        this.ecartQuantite = ecartQuantite;
    }

    public BigDecimal getEcartValeur() {
        return ecartValeur;
    }

    public void setEcartValeur(BigDecimal ecartValeur) {
        this.ecartValeur = ecartValeur;
    }
}
