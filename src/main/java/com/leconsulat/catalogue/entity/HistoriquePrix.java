package com.leconsulat.catalogue.entity;

import com.leconsulat.utilisateur.entity.Utilisateur;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Historisation des changements de tarif (RG-068) — écrite automatiquement, jamais en écriture
 * directe, et ne modifie jamais les commandes déjà enregistrées (RG-040). */
@Entity
@Table(name = "historique_prix")
public class HistoriquePrix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal ancienPrix;

    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal nouveauPrix;

    @Column(nullable = false)
    private LocalDateTime dateEffet = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "auteur_id")
    private Utilisateur auteur;

    public HistoriquePrix() {
    }

    public HistoriquePrix(Produit produit, BigDecimal ancienPrix, BigDecimal nouveauPrix, Utilisateur auteur) {
        this.produit = produit;
        this.ancienPrix = ancienPrix;
        this.nouveauPrix = nouveauPrix;
        this.auteur = auteur;
        this.dateEffet = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Produit getProduit() {
        return produit;
    }

    public BigDecimal getAncienPrix() {
        return ancienPrix;
    }

    public BigDecimal getNouveauPrix() {
        return nouveauPrix;
    }

    public LocalDateTime getDateEffet() {
        return dateEffet;
    }

    public Utilisateur getAuteur() {
        return auteur;
    }
}
