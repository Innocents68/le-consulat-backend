package com.leconsulat.restaurant.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_commande_restaurant")
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plat_id", nullable = false)
    private Plat plat;

    @Column(nullable = false)
    private int quantite;

    @Column(length = 255)
    private String options;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutLigneCommande statut = StatutLigneCommande.EN_ATTENTE;

    public Long getId() {
        return id;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public Plat getPlat() {
        return plat;
    }

    public void setPlat(Plat plat) {
        this.plat = plat;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
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

    public StatutLigneCommande getStatut() {
        return statut;
    }

    public void setStatut(StatutLigneCommande statut) {
        this.statut = statut;
    }
}
