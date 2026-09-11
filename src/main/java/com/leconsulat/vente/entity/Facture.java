package com.leconsulat.vente.entity;

import com.leconsulat.etablissement.entity.Etablissement;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Document immuable (RG-052) généré automatiquement à l'encaissement (EF-014) — aucun
 * endpoint de modification n'existe sur cette entité. */
@Entity
@Table(name = "factures")
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String numero;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "etablissement_id", nullable = false)
    private Etablissement etablissement;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "commande_id", nullable = false, unique = true)
    private Commande commande;

    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal montantBrut;

    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal remise;

    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal montantNet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModePaiement mode;

    @Column(precision = 14, scale = 0)
    private BigDecimal montantRecu;

    @Column(precision = 14, scale = 0)
    private BigDecimal monnaieRendue;

    @Column(nullable = false)
    private LocalDateTime dateEmission = LocalDateTime.now();

    /** RG-053 : toute réimpression est tracée ; le gabarit affiche "DUPLICATA" dès que > 0. */
    @Column(nullable = false)
    private int nombreImpressions = 0;

    public Facture() {
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

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public BigDecimal getMontantBrut() {
        return montantBrut;
    }

    public void setMontantBrut(BigDecimal montantBrut) {
        this.montantBrut = montantBrut;
    }

    public BigDecimal getRemise() {
        return remise;
    }

    public void setRemise(BigDecimal remise) {
        this.remise = remise;
    }

    public BigDecimal getMontantNet() {
        return montantNet;
    }

    public void setMontantNet(BigDecimal montantNet) {
        this.montantNet = montantNet;
    }

    public ModePaiement getMode() {
        return mode;
    }

    public void setMode(ModePaiement mode) {
        this.mode = mode;
    }

    public BigDecimal getMontantRecu() {
        return montantRecu;
    }

    public void setMontantRecu(BigDecimal montantRecu) {
        this.montantRecu = montantRecu;
    }

    public BigDecimal getMonnaieRendue() {
        return monnaieRendue;
    }

    public void setMonnaieRendue(BigDecimal monnaieRendue) {
        this.monnaieRendue = monnaieRendue;
    }

    public LocalDateTime getDateEmission() {
        return dateEmission;
    }

    public void setDateEmission(LocalDateTime dateEmission) {
        this.dateEmission = dateEmission;
    }

    public int getNombreImpressions() {
        return nombreImpressions;
    }

    public void setNombreImpressions(int nombreImpressions) {
        this.nombreImpressions = nombreImpressions;
    }
}
