package com.leconsulat.vente.entity;

import com.leconsulat.catalogue.entity.Produit;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_commande")
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    /** Snapshot du nom au moment de l'ajout — un produit désactivé/renommé plus tard ne doit
     * pas changer l'apparence d'une commande déjà enregistrée. */
    @Column(nullable = false, length = 100)
    private String articleNom;

    @Column(nullable = false)
    private int quantite;

    /** Figé au moment de l'ajout (RG-040) — une modification ultérieure du tarif du produit ne
     * modifie jamais cette ligne. */
    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal prixUnitaire;

    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal montant;

    public LigneCommande() {
    }

    public Long getId() {
        return id;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
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
