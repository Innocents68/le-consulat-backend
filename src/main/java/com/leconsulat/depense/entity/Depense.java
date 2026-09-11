package com.leconsulat.depense.entity;

import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.utilisateur.entity.Utilisateur;
import com.leconsulat.vente.entity.ModePaiement;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** §6.7.2. Pas de champ {@code numero} : absent du tableau de champs du CDC, contrairement à
 * Commande/Facture/Avoir/Inventaire qui le portent explicitement (RG-020). */
@Entity
@Table(name = "depenses")
public class Depense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "etablissement_id", nullable = false)
    private Etablissement etablissement;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    private CategorieDepense categorie;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModePaiement modePaiement;

    @Column(nullable = false, length = 100)
    private String beneficiaire;

    /** Couvre à la fois « utilisateur ayant effectué l'opération » et « responsable de
     * l'établissement » du tableau §6.7.2 — la même personne dans ce système à un seul profil
     * Gérant/Caissier par établissement (RG-090). */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(length = 255)
    private String commentaire;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    /** RG-094 : la suppression est réservée au Super Administrateur et journalisée — comme
     * {@code Commande.ANNULEE}, on garde la ligne visible (traçabilité RG-095/096) plutôt que de
     * la supprimer physiquement. */
    @Column(nullable = false)
    private boolean annulee = false;

    @Column(length = 255)
    private String motifAnnulation;

    public Depense() {
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Etablissement getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(Etablissement etablissement) {
        this.etablissement = etablissement;
    }

    public CategorieDepense getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieDepense categorie) {
        this.categorie = categorie;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public ModePaiement getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(ModePaiement modePaiement) {
        this.modePaiement = modePaiement;
    }

    public String getBeneficiaire() {
        return beneficiaire;
    }

    public void setBeneficiaire(String beneficiaire) {
        this.beneficiaire = beneficiaire;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public boolean isAnnulee() {
        return annulee;
    }

    public void setAnnulee(boolean annulee) {
        this.annulee = annulee;
    }

    public String getMotifAnnulation() {
        return motifAnnulation;
    }

    public void setMotifAnnulation(String motifAnnulation) {
        this.motifAnnulation = motifAnnulation;
    }
}
