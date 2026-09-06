package com.leconsulat.finance.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Read-only, auto-generated on session close. Never modified nor deleted. */
@Entity
@Table(name = "rapports_caisse")
public class RapportCaisse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_caisse_id", nullable = false)
    private Long sessionCaisseId;

    @Column(nullable = false)
    private LocalDateTime dateGeneration = LocalDateTime.now();

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal encaissements;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal decaissements;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal solde;

    public Long getId() {
        return id;
    }

    public Long getSessionCaisseId() {
        return sessionCaisseId;
    }

    public void setSessionCaisseId(Long sessionCaisseId) {
        this.sessionCaisseId = sessionCaisseId;
    }

    public LocalDateTime getDateGeneration() {
        return dateGeneration;
    }

    public void setDateGeneration(LocalDateTime dateGeneration) {
        this.dateGeneration = dateGeneration;
    }

    public BigDecimal getEncaissements() {
        return encaissements;
    }

    public void setEncaissements(BigDecimal encaissements) {
        this.encaissements = encaissements;
    }

    public BigDecimal getDecaissements() {
        return decaissements;
    }

    public void setDecaissements(BigDecimal decaissements) {
        this.decaissements = decaissements;
    }

    public BigDecimal getSolde() {
        return solde;
    }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }
}
