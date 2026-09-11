package com.leconsulat.parametres.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** §6.10.1 — ligne singleton, toujours d'id 1 (une seule structure gérée par cette instance de
 * l'application, RG-105 : accès réservé au Super Administrateur pour la modification). */
@Entity
@Table(name = "parametres_generaux")
public class ParametresGeneraux {

    @Id
    private Long id = 1L;

    @Column(length = 255)
    private String logoUrl;

    @Column(nullable = false, length = 100)
    private String nomMagasin = "Le Consulat";

    @Column(length = 255)
    private String adresse;

    @Column(length = 30)
    private String telephone;

    @Column(length = 100)
    private String email;

    /** « Info final » — pied de ticket (RG-103). */
    @Column(length = 255)
    private String messageFin;

    @Column(nullable = false, length = 10)
    private String devise = "FCFA";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FormatTicket formatTicket = FormatTicket.MM_80;

    @Column(nullable = false)
    private int nombreCopies = 1;

    /** Valeur par défaut appliquée aux nouveaux articles suivis en stock sans seuil précisé. */
    @Column(precision = 14, scale = 3)
    private BigDecimal seuilAlerteDefaut;

    /** Plafonds globaux (pas de plafond si nul) — RG-104 (personnalisation par établissement)
     * est priorité S dans le CDC et reste hors périmètre de ce lot. */
    @Column(precision = 5, scale = 2)
    private BigDecimal plafondRemisePourcentage;

    @Column(precision = 14, scale = 0)
    private BigDecimal plafondRemiseMontant;

    public ParametresGeneraux() {
    }

    public Long getId() {
        return id;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getNomMagasin() {
        return nomMagasin;
    }

    public void setNomMagasin(String nomMagasin) {
        this.nomMagasin = nomMagasin;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessageFin() {
        return messageFin;
    }

    public void setMessageFin(String messageFin) {
        this.messageFin = messageFin;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public FormatTicket getFormatTicket() {
        return formatTicket;
    }

    public void setFormatTicket(FormatTicket formatTicket) {
        this.formatTicket = formatTicket;
    }

    public int getNombreCopies() {
        return nombreCopies;
    }

    public void setNombreCopies(int nombreCopies) {
        this.nombreCopies = nombreCopies;
    }

    public BigDecimal getSeuilAlerteDefaut() {
        return seuilAlerteDefaut;
    }

    public void setSeuilAlerteDefaut(BigDecimal seuilAlerteDefaut) {
        this.seuilAlerteDefaut = seuilAlerteDefaut;
    }

    public BigDecimal getPlafondRemisePourcentage() {
        return plafondRemisePourcentage;
    }

    public void setPlafondRemisePourcentage(BigDecimal plafondRemisePourcentage) {
        this.plafondRemisePourcentage = plafondRemisePourcentage;
    }

    public BigDecimal getPlafondRemiseMontant() {
        return plafondRemiseMontant;
    }

    public void setPlafondRemiseMontant(BigDecimal plafondRemiseMontant) {
        this.plafondRemiseMontant = plafondRemiseMontant;
    }
}
