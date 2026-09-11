package com.leconsulat.avoir.entity;

import com.leconsulat.catalogue.entity.Produit;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_avoir")
public class LigneAvoir {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avoir_id", nullable = false)
    private Avoir avoir;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false, length = 100)
    private String articleNom;

    @Column(nullable = false)
    private int quantite;

    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal montant;

    public LigneAvoir() {
    }

    public Long getId() {
        return id;
    }

    public Avoir getAvoir() {
        return avoir;
    }

    public void setAvoir(Avoir avoir) {
        this.avoir = avoir;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public String getArticleNom() {
        return articleNom;
    }

    public void setArticleNom(String articleNom) {
        this.articleNom = articleNom;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }
}
