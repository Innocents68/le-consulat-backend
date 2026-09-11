package com.leconsulat.depense.entity;

import jakarta.persistence.*;

/** Référentiel partagé (comme {@code Fournisseur}, Lot 4a) — pas rattaché à un établissement.
 * EF-036 : paramétrable exclusivement par le Super Administrateur. */
@Entity
@Table(name = "categories_depenses")
public class CategorieDepense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(nullable = false)
    private boolean actif = true;

    public CategorieDepense() {
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

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
