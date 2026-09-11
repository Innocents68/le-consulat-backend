package com.leconsulat.vente.entity;

import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.salle.entity.TableService;
import com.leconsulat.utilisateur.entity.Utilisateur;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Modèle unique de commande pour les 3 établissements (EF-006) — seul le filtre établissement
 * change, jamais la structure. */
@Entity
@Table(name = "commandes")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Généré à la création, jamais réutilisé même annulée (RG-020, RG-021). */
    @Column(nullable = false, unique = true, length = 40)
    private String numero;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "etablissement_id", nullable = false)
    private Etablissement etablissement;

    /** Nullable : vente à emporter/comptoir (PC-07). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "table_id")
    private TableService table;

    @Column(length = 100)
    private String clientNom;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignes = new ArrayList<>();

    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal montantBrut = BigDecimal.ZERO;

    /** Toujours 0 pour l'instant — module Remises non construit dans ce lot (§6.2.6, Lot 3). */
    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal remise = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal montantNet = BigDecimal.ZERO;

    @Column(length = 255)
    private String observations;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCommande statut = StatutCommande.NON_VALIDEE;

    @Column(length = 255)
    private String motifAnnulation;

    /** Échoue fort — une commande ne peut jamais être créée sans caissier (RG-095, même
     * principe que partout ailleurs dans ce projet). */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "caissier_id", nullable = false)
    private Utilisateur caissier;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    private LocalDateTime dateValidation;

    public Commande() {
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

    public TableService getTable() {
        return table;
    }

    public void setTable(TableService table) {
        this.table = table;
    }

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public List<LigneCommande> getLignes() {
        return lignes;
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

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public StatutCommande getStatut() {
        return statut;
    }

    public void setStatut(StatutCommande statut) {
        this.statut = statut;
    }

    public String getMotifAnnulation() {
        return motifAnnulation;
    }

    public void setMotifAnnulation(String motifAnnulation) {
        this.motifAnnulation = motifAnnulation;
    }

    public Utilisateur getCaissier() {
        return caissier;
    }

    public void setCaissier(Utilisateur caissier) {
        this.caissier = caissier;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(LocalDateTime dateValidation) {
        this.dateValidation = dateValidation;
    }
}
