package com.leconsulat.vente.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Entité à part (pas un simple champ sur Commande) pour absorber le paiement mixte (RG-049)
 * plus tard sans migration — une seule ligne par commande pour l'instant (PC-08). */
@Entity
@Table(name = "paiements")
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModePaiement mode;

    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal montant;

    /** Nullable : uniquement renseigné pour un paiement en espèces (RG-048). */
    @Column(precision = 14, scale = 0)
    private BigDecimal montantRecu;

    @Column(precision = 14, scale = 0)
    private BigDecimal monnaieRendue;

    @Column(nullable = false)
    private LocalDateTime datePaiement = LocalDateTime.now();

    public Paiement() {
    }

    public Long getId() {
        return id;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public ModePaiement getMode() {
        return mode;
    }

    public void setMode(ModePaiement mode) {
        this.mode = mode;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
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

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }
}
