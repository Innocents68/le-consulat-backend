package com.leconsulat.vente.entity;

import com.leconsulat.utilisateur.entity.Utilisateur;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessions_caisse")
public class SessionCaisse {

    public enum Statut {OUVERTE, FERMEE}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String caisseNom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ouvert_par_id")
    private Utilisateur ouvertPar;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal fondInitial;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Statut statut = Statut.OUVERTE;

    @Column(nullable = false)
    private LocalDateTime dateOuverture = LocalDateTime.now();

    private LocalDateTime dateFermeture;

    @Column(precision = 14, scale = 2)
    private BigDecimal totalVentes;

    @Column(precision = 14, scale = 2)
    private BigDecimal totalEncaissements;

    @Column(precision = 14, scale = 2)
    private BigDecimal ecart;

    public Long getId() {
        return id;
    }

    public String getCaisseNom() {
        return caisseNom;
    }

    public void setCaisseNom(String caisseNom) {
        this.caisseNom = caisseNom;
    }

    public Utilisateur getOuvertPar() {
        return ouvertPar;
    }

    public void setOuvertPar(Utilisateur ouvertPar) {
        this.ouvertPar = ouvertPar;
    }

    public BigDecimal getFondInitial() {
        return fondInitial;
    }

    public void setFondInitial(BigDecimal fondInitial) {
        this.fondInitial = fondInitial;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateOuverture() {
        return dateOuverture;
    }

    public void setDateOuverture(LocalDateTime dateOuverture) {
        this.dateOuverture = dateOuverture;
    }

    public LocalDateTime getDateFermeture() {
        return dateFermeture;
    }

    public void setDateFermeture(LocalDateTime dateFermeture) {
        this.dateFermeture = dateFermeture;
    }

    public BigDecimal getTotalVentes() {
        return totalVentes;
    }

    public void setTotalVentes(BigDecimal totalVentes) {
        this.totalVentes = totalVentes;
    }

    public BigDecimal getTotalEncaissements() {
        return totalEncaissements;
    }

    public void setTotalEncaissements(BigDecimal totalEncaissements) {
        this.totalEncaissements = totalEncaissements;
    }

    public BigDecimal getEcart() {
        return ecart;
    }

    public void setEcart(BigDecimal ecart) {
        this.ecart = ecart;
    }
}
