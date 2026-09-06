package com.leconsulat.restaurant.entity;

import com.leconsulat.utilisateur.entity.Utilisateur;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commandes_restaurant")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private TableRestaurant table;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeCommande type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serveur_id")
    private Utilisateur serveur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignes = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(length = 255)
    private String motifAnnulation;

    public Long getId() {
        return id;
    }

    public TableRestaurant getTable() {
        return table;
    }

    public void setTable(TableRestaurant table) {
        this.table = table;
    }

    public TypeCommande getType() {
        return type;
    }

    public void setType(TypeCommande type) {
        this.type = type;
    }

    public Utilisateur getServeur() {
        return serveur;
    }

    public void setServeur(Utilisateur serveur) {
        this.serveur = serveur;
    }

    public StatutCommande getStatut() {
        return statut;
    }

    public void setStatut(StatutCommande statut) {
        this.statut = statut;
    }

    public List<LigneCommande> getLignes() {
        return lignes;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getMotifAnnulation() {
        return motifAnnulation;
    }

    public void setMotifAnnulation(String motifAnnulation) {
        this.motifAnnulation = motifAnnulation;
    }
}
