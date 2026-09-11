package com.leconsulat.stock.entity;

import com.leconsulat.catalogue.entity.Produit;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.utilisateur.entity.Utilisateur;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Historique immuable de tout changement de {@code Produit.quantiteStock} (§6.6.2). */
@Entity
@Table(name = "mouvements_stock")
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    /** Dérivable de {@code produit.etablissement}, mais RG-081 l'exige explicitement porté par
     * le mouvement lui-même (traçabilité même si le produit change un jour d'établissement). */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "etablissement_id", nullable = false)
    private Etablissement etablissement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeMouvementStock type;

    /** Toujours positive — le {@code type} porte le sens (entrée/sortie). Seule exception :
     * {@code AJUSTEMENT_INVENTAIRE} (§6.6.5, Lot 4b) n'a pas de sens univoque porté par son type
     * (un inventaire peut révéler un surplus ou une perte) et porte donc une quantité signée
     * (positive = surplus, négative = perte). */
    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantite;

    /** Obligatoire en service pour une {@code SORTIE} manuelle (RG-081), libre sinon. */
    @Column(length = 255)
    private String motif;

    @Column(precision = 14, scale = 0)
    private BigDecimal prixUnitaire;

    @Column(precision = 14, scale = 0)
    private BigDecimal montant;

    /** Identifiant commun aux deux mouvements (sortant + entrant) d'un même transfert. */
    @Column(length = 40)
    private String referenceTransfert;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "auteur_id", nullable = false)
    private Utilisateur auteur;

    @Column(nullable = false)
    private LocalDateTime dateMouvement = LocalDateTime.now();

    public MouvementStock() {
    }

    public Long getId() {
        return id;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public Etablissement getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(Etablissement etablissement) {
        this.etablissement = etablissement;
    }

    public TypeMouvementStock getType() {
        return type;
    }

    public void setType(TypeMouvementStock type) {
        this.type = type;
    }

    public BigDecimal getQuantite() {
        return quantite;
    }

    public void setQuantite(BigDecimal quantite) {
        this.quantite = quantite;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
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

    public String getReferenceTransfert() {
        return referenceTransfert;
    }

    public void setReferenceTransfert(String referenceTransfert) {
        this.referenceTransfert = referenceTransfert;
    }

    public Utilisateur getAuteur() {
        return auteur;
    }

    public void setAuteur(Utilisateur auteur) {
        this.auteur = auteur;
    }

    public LocalDateTime getDateMouvement() {
        return dateMouvement;
    }

    public void setDateMouvement(LocalDateTime dateMouvement) {
        this.dateMouvement = dateMouvement;
    }
}
