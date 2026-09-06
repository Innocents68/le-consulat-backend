package com.leconsulat.vente.entity;

import com.leconsulat.utilisateur.entity.Utilisateur;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventes")
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 30)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_caisse_id", nullable = false)
    private SessionCaisse sessionCaisse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caissier_id")
    private Utilisateur caissier;

    @Column(length = 150)
    private String clientNom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutVente statut = StatutVente.EN_COURS;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ModePaiement modePaiement;

    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneVente> lignes = new ArrayList<>();

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal sousTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal remiseMontant = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime dateVente = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public SessionCaisse getSessionCaisse() {
        return sessionCaisse;
    }

    public void setSessionCaisse(SessionCaisse sessionCaisse) {
        this.sessionCaisse = sessionCaisse;
    }

    public Utilisateur getCaissier() {
        return caissier;
    }

    public void setCaissier(Utilisateur caissier) {
        this.caissier = caissier;
    }

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public StatutVente getStatut() {
        return statut;
    }

    public void setStatut(StatutVente statut) {
        this.statut = statut;
    }

    public ModePaiement getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(ModePaiement modePaiement) {
        this.modePaiement = modePaiement;
    }

    public List<LigneVente> getLignes() {
        return lignes;
    }

    public BigDecimal getSousTotal() {
        return sousTotal;
    }

    public void setSousTotal(BigDecimal sousTotal) {
        this.sousTotal = sousTotal;
    }

    public BigDecimal getRemiseMontant() {
        return remiseMontant;
    }

    public void setRemiseMontant(BigDecimal remiseMontant) {
        this.remiseMontant = remiseMontant;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getDateVente() {
        return dateVente;
    }

    public void setDateVente(LocalDateTime dateVente) {
        this.dateVente = dateVente;
    }
}
