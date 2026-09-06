package com.leconsulat.finance.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "depenses")
public class Depense {

    public enum Statut {EN_ATTENTE, VALIDEE}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    private CategorieDepense categorie;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montant;

    @Column(length = 500)
    private String description;

    @Column(length = 150)
    private String fournisseur;

    @Column(length = 500)
    private String justificatifUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Statut statut = Statut.EN_ATTENTE;

    @Column(nullable = false)
    private LocalDate date = LocalDate.now();

    public Long getId() {
        return id;
    }

    public CategorieDepense getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieDepense categorie) {
        this.categorie = categorie;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(String fournisseur) {
        this.fournisseur = fournisseur;
    }

    public String getJustificatifUrl() {
        return justificatifUrl;
    }

    public void setJustificatifUrl(String justificatifUrl) {
        this.justificatifUrl = justificatifUrl;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
