package com.leconsulat.etablissement.entity;

import jakarta.persistence.*;

/**
 * Établissement (Maquis / Restaurant / Cave à vin) — stocké en base et non codé en dur
 * (cahier des charges RG-001), pour permettre l'ajout d'un 4ᵉ espace sans redéveloppement.
 */
@Entity
@Table(name = "etablissements")
public class Etablissement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Code court utilisé notamment dans la numérotation des documents (RG-020, ex. "MAQ"). */
    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false)
    private boolean actif = true;

    /** Seul le Restaurant en a un aujourd'hui (§6.3.2) — un indicateur, pas une chaîne magique
     * "RES" comparée en dur dans le code métier, pour rester cohérent avec RG-001/ENF-024
     * (un 4ᵉ établissement doit pouvoir se paramétrer sans redéveloppement). */
    @Column(nullable = false)
    private boolean gereCuisine = false;

    public Etablissement() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public boolean isGereCuisine() {
        return gereCuisine;
    }

    public void setGereCuisine(boolean gereCuisine) {
        this.gereCuisine = gereCuisine;
    }
}
