package com.leconsulat.maquis.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_commande_maquis")
public class LigneCommandeMaquis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commande_id", nullable = false)
    private CommandeMaquis commande;

    @Column(nullable = false, length = 150)
    private String articleNom;

    @Column(nullable = false)
    private int quantite;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montant;

    public Long getId() {
        return id;
    }

    public CommandeMaquis getCommande() {
        return commande;
    }

    public void setCommande(CommandeMaquis commande) {
        this.commande = commande;
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
}
