package com.leconsulat.numerotation.entity;

import com.leconsulat.etablissement.entity.Etablissement;
import jakarta.persistence.*;

/** Compteur de numérotation par établissement, type de document et année (RG-020, RG-021) —
 * continu, sans trou, jamais réinitialisé sauf changement d'année. */
@Entity
@Table(name = "sequences_numerotation", uniqueConstraints = @UniqueConstraint(columnNames = {"etablissement_id", "type", "annee"}))
public class SequenceNumerotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etablissement_id", nullable = false)
    private Etablissement etablissement;

    @Column(nullable = false, length = 10)
    private String type;

    @Column(nullable = false)
    private int annee;

    @Column(nullable = false)
    private int dernierNumero = 0;

    public SequenceNumerotation() {
    }

    public Long getId() {
        return id;
    }

    public Etablissement getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(Etablissement etablissement) {
        this.etablissement = etablissement;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }

    public int getDernierNumero() {
        return dernierNumero;
    }

    public void setDernierNumero(int dernierNumero) {
        this.dernierNumero = dernierNumero;
    }
}
