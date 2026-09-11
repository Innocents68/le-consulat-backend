package com.leconsulat.stock.entity;

import jakarta.persistence.*;

/** Référentiel fournisseurs partagé (§6.6.4) — pas rattaché à un établissement, contrairement
 * à presque tout le reste du modèle (RG-002 en fait explicitement une exception). */
@Entity
@Table(name = "fournisseurs")
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 30)
    private String telephone;

    @Column(nullable = false)
    private boolean actif = true;

    public Fournisseur() {
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
