package com.leconsulat.stock.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories_produits")
public class CategorieProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    public CategorieProduit() {
    }

    public CategorieProduit(String nom) {
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
