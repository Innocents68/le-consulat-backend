package com.leconsulat.inventaire.entity;

import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.utilisateur.entity.Utilisateur;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** §6.6.5 : compare le stock théorique (issu des mouvements) au stock physique compté. */
@Entity
@Table(name = "inventaires")
public class Inventaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String numero;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "etablissement_id", nullable = false)
    private Etablissement etablissement;

    @Column(nullable = false)
    private LocalDate dateInventaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutInventaire statut = StatutInventaire.BROUILLON;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "auteur_id", nullable = false)
    private Utilisateur auteur;

    /** Renseigné uniquement à la validation (RG-086). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "validateur_id")
    private Utilisateur validateur;

    /** Justification des écarts (tableau §6.6.5) — saisie à la clôture. */
    @Column(length = 500)
    private String commentaire;

    @OneToMany(mappedBy = "inventaire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneInventaire> lignes = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    private LocalDateTime dateCloture;

    private LocalDateTime dateValidation;

    public Inventaire() {
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

    public LocalDate getDateInventaire() {
        return dateInventaire;
    }

    public void setDateInventaire(LocalDate dateInventaire) {
        this.dateInventaire = dateInventaire;
    }

    public StatutInventaire getStatut() {
        return statut;
    }

    public void setStatut(StatutInventaire statut) {
        this.statut = statut;
    }

    public Utilisateur getAuteur() {
        return auteur;
    }

    public void setAuteur(Utilisateur auteur) {
        this.auteur = auteur;
    }

    public Utilisateur getValidateur() {
        return validateur;
    }

    public void setValidateur(Utilisateur validateur) {
        this.validateur = validateur;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public List<LigneInventaire> getLignes() {
        return lignes;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public LocalDateTime getDateCloture() {
        return dateCloture;
    }

    public void setDateCloture(LocalDateTime dateCloture) {
        this.dateCloture = dateCloture;
    }

    public LocalDateTime getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(LocalDateTime dateValidation) {
        this.dateValidation = dateValidation;
    }
}
