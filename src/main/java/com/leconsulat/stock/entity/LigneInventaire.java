package com.leconsulat.stock.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lignes_inventaire")
public class LigneInventaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventaire_id", nullable = false)
    private Inventaire inventaire;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    private int quantiteTheorique;

    @Column(nullable = false)
    private int quantiteReelle;

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

    public int getQuantiteTheorique() {
        return quantiteTheorique;
    }

    public void setQuantiteTheorique(int quantiteTheorique) {
        this.quantiteTheorique = quantiteTheorique;
    }

    public int getQuantiteReelle() {
        return quantiteReelle;
    }

    public void setQuantiteReelle(int quantiteReelle) {
        this.quantiteReelle = quantiteReelle;
    }

    public int getEcart() {
        return quantiteReelle - quantiteTheorique;
    }
}
