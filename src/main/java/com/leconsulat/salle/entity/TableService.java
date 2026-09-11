package com.leconsulat.salle.entity;

import com.leconsulat.etablissement.entity.Etablissement;
import jakarta.persistence.*;

/** Table de salle (cahier des charges §6.2.1) — nommée `table_service` dans le modèle de données
 * du CDC (§8.1) pour éviter le mot réservé SQL "table". */
@Entity
@Table(name = "tables_service")
public class TableService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(nullable = false)
    private int capacite;

    @Column(length = 50)
    private String zone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutTable statut = StatutTable.LIBRE;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "etablissement_id", nullable = false)
    private Etablissement etablissement;

    @Column(nullable = false)
    private boolean actif = true;

    public TableService() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public StatutTable getStatut() {
        return statut;
    }

    public void setStatut(StatutTable statut) {
        this.statut = statut;
    }

    public Etablissement getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(Etablissement etablissement) {
        this.etablissement = etablissement;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
