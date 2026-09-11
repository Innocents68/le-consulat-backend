package com.leconsulat.catalogue.entity;

import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.stock.entity.Fournisseur;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Un produit appartient à un seul établissement (RG-002) — pas de table de jointure
 * produit×établissement : le même article conceptuel ("Coca-Cola") existe sous forme de lignes
 * séparées, une par établissement qui le vend, chacune avec son propre prix (RG-066).
 */
@Entity
@Table(name = "produits")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UniteProduit unite;

    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal prixVente;

    @Column(precision = 14, scale = 0)
    private BigDecimal prixAchat;

    @Column(nullable = false)
    private boolean disponible = true;

    @Column(nullable = false)
    private boolean actif = true;

    /** Détermine si la vente de ce produit décrémente automatiquement le stock (RG-031,
     * RG-073/074) — un plat sans fiche technique (PC-02 non tranché) reste hors stock. */
    @Column(nullable = false)
    private boolean suiviStock = false;

    /** Matérialisée : jamais modifiable directement via {@code ProduitService.update()},
     * uniquement par {@code MouvementStockService} (§6.6.4). */
    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantiteStock = BigDecimal.ZERO;

    @Column(precision = 14, scale = 3)
    private BigDecimal seuilAlerte;

    @Column(length = 100)
    private String emplacement;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "etablissement_id", nullable = false)
    private Etablissement etablissement;

    public Produit() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UniteProduit getUnite() {
        return unite;
    }

    public void setUnite(UniteProduit unite) {
        this.unite = unite;
    }

    public BigDecimal getPrixVente() {
        return prixVente;
    }

    public void setPrixVente(BigDecimal prixVente) {
        this.prixVente = prixVente;
    }

    public BigDecimal getPrixAchat() {
        return prixAchat;
    }

    public void setPrixAchat(BigDecimal prixAchat) {
        this.prixAchat = prixAchat;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public boolean isSuiviStock() {
        return suiviStock;
    }

    public void setSuiviStock(boolean suiviStock) {
        this.suiviStock = suiviStock;
    }

    public Etablissement getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(Etablissement etablissement) {
        this.etablissement = etablissement;
    }

    public BigDecimal getQuantiteStock() {
        return quantiteStock;
    }

    public void setQuantiteStock(BigDecimal quantiteStock) {
        this.quantiteStock = quantiteStock;
    }

    public BigDecimal getSeuilAlerte() {
        return seuilAlerte;
    }

    public void setSeuilAlerte(BigDecimal seuilAlerte) {
        this.seuilAlerte = seuilAlerte;
    }

    public String getEmplacement() {
        return emplacement;
    }

    public void setEmplacement(String emplacement) {
        this.emplacement = emplacement;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }
}
