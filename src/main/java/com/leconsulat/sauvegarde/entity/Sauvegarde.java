package com.leconsulat.sauvegarde.entity;

import com.leconsulat.utilisateur.entity.Utilisateur;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/** §6.10.2 — historique des sauvegardes (EF-044) : montre aussi bien les réussites que les
 * échecs, pas seulement les fichiers produits avec succès. */
@Entity
@Table(name = "sauvegardes")
public class Sauvegarde {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nomFichier;

    @Column
    private Long tailleOctets;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeSauvegarde type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutSauvegarde statut;

    /** Nul pour une sauvegarde automatique (aucun utilisateur n'est à l'origine). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "auteur_id")
    private Utilisateur auteur;

    @Column(length = 500)
    private String message;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public Sauvegarde() {
    }

    public Long getId() {
        return id;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public Long getTailleOctets() {
        return tailleOctets;
    }

    public void setTailleOctets(Long tailleOctets) {
        this.tailleOctets = tailleOctets;
    }

    public TypeSauvegarde getType() {
        return type;
    }

    public void setType(TypeSauvegarde type) {
        this.type = type;
    }

    public StatutSauvegarde getStatut() {
        return statut;
    }

    public void setStatut(StatutSauvegarde statut) {
        this.statut = statut;
    }

    public Utilisateur getAuteur() {
        return auteur;
    }

    public void setAuteur(Utilisateur auteur) {
        this.auteur = auteur;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
