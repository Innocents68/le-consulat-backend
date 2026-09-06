package com.leconsulat.stock.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "entrees_stock")
public class EntreeStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(length = 150)
    private String fournisseur;

    @Column(nullable = false)
    private int quantite;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    private LocalDate dateEntree = LocalDate.now();

    @Column(length = 60)
    private String bonLivraison;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutMouvementStock statut = StatutMouvementStock.BROUILLON;

    public Long getId() {
        return id;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public String getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(String fournisseur) {
        this.fournisseur = fournisseur;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public LocalDate getDateEntree() {
        return dateEntree;
    }

    public void setDateEntree(LocalDate dateEntree) {
        this.dateEntree = dateEntree;
    }

    public String getBonLivraison() {
        return bonLivraison;
    }

    public void setBonLivraison(String bonLivraison) {
        this.bonLivraison = bonLivraison;
    }

    public StatutMouvementStock getStatut() {
        return statut;
    }

    public void setStatut(StatutMouvementStock statut) {
        this.statut = statut;
    }
}
