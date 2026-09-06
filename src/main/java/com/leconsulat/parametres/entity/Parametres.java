package com.leconsulat.parametres.entity;

import jakarta.persistence.*;

/** Singleton row (id is always 1L) holding the establishment's general settings — API_CONTRACT.md §8. */
@Entity
@Table(name = "parametres")
public class Parametres {

    public static final Long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(nullable = false, length = 150)
    private String nomEtablissement = "Le Consulat";

    @Column(length = 500)
    private String logoUrl;

    @Column(nullable = false, length = 10)
    private String devise = "FCFA";

    @Column(nullable = false)
    private double tauxTva = 18.0;

    @Column(nullable = false)
    private int seuilAlerteGlobal = 10;

    @Column(nullable = false)
    private boolean modeSombreParDefaut = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomEtablissement() {
        return nomEtablissement;
    }

    public void setNomEtablissement(String nomEtablissement) {
        this.nomEtablissement = nomEtablissement;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public double getTauxTva() {
        return tauxTva;
    }

    public void setTauxTva(double tauxTva) {
        this.tauxTva = tauxTva;
    }

    public int getSeuilAlerteGlobal() {
        return seuilAlerteGlobal;
    }

    public void setSeuilAlerteGlobal(int seuilAlerteGlobal) {
        this.seuilAlerteGlobal = seuilAlerteGlobal;
    }

    public boolean isModeSombreParDefaut() {
        return modeSombreParDefaut;
    }

    public void setModeSombreParDefaut(boolean modeSombreParDefaut) {
        this.modeSombreParDefaut = modeSombreParDefaut;
    }
}
