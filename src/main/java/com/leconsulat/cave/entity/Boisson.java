package com.leconsulat.cave.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "boissons")
public class Boisson {

    /** Number of glasses poured from one bottle, used to proportionally decrement stock on a by-the-glass sale. */
    public static final int VERRES_PAR_BOUTEILLE = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeBoisson type;

    @Column(length = 100)
    private String origine;

    private Integer millesime;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal prixAchatBouteille;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal prixVenteBouteille;

    @Column(precision = 14, scale = 2)
    private BigDecimal prixVenteVerre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    /** Stock expressed in bottle-equivalent units (a glass sale decrements 1/VERRES_PAR_BOUTEILLE). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantiteStock = BigDecimal.ZERO;

    @Column(nullable = false)
    private int seuilAlerte = 5;

    @Column(nullable = false)
    private boolean actif = true;

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public TypeBoisson getType() {
        return type;
    }

    public void setType(TypeBoisson type) {
        this.type = type;
    }

    public String getOrigine() {
        return origine;
    }

    public void setOrigine(String origine) {
        this.origine = origine;
    }

    public Integer getMillesime() {
        return millesime;
    }

    public void setMillesime(Integer millesime) {
        this.millesime = millesime;
    }

    public BigDecimal getPrixAchatBouteille() {
        return prixAchatBouteille;
    }

    public void setPrixAchatBouteille(BigDecimal prixAchatBouteille) {
        this.prixAchatBouteille = prixAchatBouteille;
    }

    public BigDecimal getPrixVenteBouteille() {
        return prixVenteBouteille;
    }

    public void setPrixVenteBouteille(BigDecimal prixVenteBouteille) {
        this.prixVenteBouteille = prixVenteBouteille;
    }

    public BigDecimal getPrixVenteVerre() {
        return prixVenteVerre;
    }

    public void setPrixVenteVerre(BigDecimal prixVenteVerre) {
        this.prixVenteVerre = prixVenteVerre;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    public BigDecimal getQuantiteStock() {
        return quantiteStock;
    }

    public void setQuantiteStock(BigDecimal quantiteStock) {
        this.quantiteStock = quantiteStock;
    }

    public int getSeuilAlerte() {
        return seuilAlerte;
    }

    public void setSeuilAlerte(int seuilAlerte) {
        this.seuilAlerte = seuilAlerte;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
