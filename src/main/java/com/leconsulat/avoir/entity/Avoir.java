package com.leconsulat.avoir.entity;

import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.utilisateur.entity.Utilisateur;
import com.leconsulat.vente.entity.Facture;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Seul moyen de corriger une commande déjà validée (§6.2.7, RG-030). Immuable une fois créé
 * (RG-065) — aucun endpoint de modification n'existe sur cette entité, même principe que
 * {@link Facture}. */
@Entity
@Table(name = "avoirs")
public class Avoir {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String numero;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "etablissement_id", nullable = false)
    private Etablissement etablissement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;

    @OneToMany(mappedBy = "avoir", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneAvoir> lignes = new ArrayList<>();

    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MotifAvoir motif;

    @Column(length = 255)
    private String motifDetail;

    /** RG-062 : si vrai, {@code AvoirService.create()} réintègre la quantité retournée dans
     * {@code Produit.quantiteStock} via {@code MouvementStockService} (produits suivis uniquement). */
    @Column(nullable = false)
    private boolean remiseEnStock = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModeRemboursement modeRemboursement;

    /** Recommandations et corrections.md §4 : part de {@code montant} déjà déduite d'une facture
     * ultérieure. Un avoir reste immuable (RG-065) sur son contenu d'origine — seul ce compteur
     * évolue, via {@code AvoirService.enregistrerUtilisation()}. Solde restant = montant - ce champ. */
    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal montantUtilise = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "auteur_id", nullable = false)
    private Utilisateur auteur;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public Avoir() {
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Etablissement getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(Etablissement etablissement) {
        this.etablissement = etablissement;
    }

    public Facture getFacture() {
        return facture;
    }

    public void setFacture(Facture facture) {
        this.facture = facture;
    }

    public List<LigneAvoir> getLignes() {
        return lignes;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public MotifAvoir getMotif() {
        return motif;
    }

    public void setMotif(MotifAvoir motif) {
        this.motif = motif;
    }

    public String getMotifDetail() {
        return motifDetail;
    }

    public void setMotifDetail(String motifDetail) {
        this.motifDetail = motifDetail;
    }

    public boolean isRemiseEnStock() {
        return remiseEnStock;
    }

    public void setRemiseEnStock(boolean remiseEnStock) {
        this.remiseEnStock = remiseEnStock;
    }

    public ModeRemboursement getModeRemboursement() {
        return modeRemboursement;
    }

    public void setModeRemboursement(ModeRemboursement modeRemboursement) {
        this.modeRemboursement = modeRemboursement;
    }

    public BigDecimal getMontantUtilise() {
        return montantUtilise;
    }

    public void setMontantUtilise(BigDecimal montantUtilise) {
        this.montantUtilise = montantUtilise;
    }

    public Utilisateur getAuteur() {
        return auteur;
    }

    public void setAuteur(Utilisateur auteur) {
        this.auteur = auteur;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
