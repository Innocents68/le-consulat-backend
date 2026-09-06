package com.leconsulat.finance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories_depenses")
public class CategorieDepense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    public CategorieDepense() {
    }

    public CategorieDepense(String nom) {
        this.nom = nom;
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
}
