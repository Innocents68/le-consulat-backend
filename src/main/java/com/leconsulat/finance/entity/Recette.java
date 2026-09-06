package com.leconsulat.finance.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Not deletable — traceability with the cash tickets (cahier des charges §2.6). */
@Entity
@Table(name = "recettes")
public class Recette {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SourceRecette source;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal montant;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public SourceRecette getSource() {
        return source;
    }

    public void setSource(SourceRecette source) {
        this.source = source;
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
