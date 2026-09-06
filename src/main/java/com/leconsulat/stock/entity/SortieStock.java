package com.leconsulat.stock.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sorties_stock")
public class SortieStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MotifSortieStock motif;

    @Column(nullable = false)
    private int quantite;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    private LocalDate dateSortie = LocalDate.now();

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

    public MotifSortieStock getMotif() {
        return motif;
    }

    public void setMotif(MotifSortieStock motif) {
        this.motif = motif;
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

    public LocalDate getDateSortie() {
        return dateSortie;
    }

    public void setDateSortie(LocalDate dateSortie) {
        this.dateSortie = dateSortie;
    }

    public StatutMouvementStock getStatut() {
        return statut;
    }

    public void setStatut(StatutMouvementStock statut) {
        this.statut = statut;
    }
}
