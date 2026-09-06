package com.leconsulat.cave.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Not deletable, not updatable — full accounting traceability (cahier des charges §2.3). */
@Entity
@Table(name = "mouvements_cave")
public class MouvementCave {

    public enum Type {ENTREE, SORTIE}

    public enum Motif {ACHAT, VENTE, CASSE, DEGUSTATION}

    /** How the quantity below is expressed for a sale-related movement. */
    public enum UniteVente {BOUTEILLE, VERRE}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "boisson_id", nullable = false)
    private Boisson boisson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Motif motif;

    /** Quantity in bottle-equivalent units (a glass sale is recorded as a fraction of a bottle). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantite;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private UniteVente uniteVente;

    @Column(nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public Boisson getBoisson() {
        return boisson;
    }

    public void setBoisson(Boisson boisson) {
        this.boisson = boisson;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Motif getMotif() {
        return motif;
    }

    public void setMotif(Motif motif) {
        this.motif = motif;
    }

    public BigDecimal getQuantite() {
        return quantite;
    }

    public void setQuantite(BigDecimal quantite) {
        this.quantite = quantite;
    }

    public UniteVente getUniteVente() {
        return uniteVente;
    }

    public void setUniteVente(UniteVente uniteVente) {
        this.uniteVente = uniteVente;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
