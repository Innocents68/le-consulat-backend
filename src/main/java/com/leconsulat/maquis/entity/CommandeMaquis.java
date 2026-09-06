package com.leconsulat.maquis.entity;

import com.leconsulat.restaurant.entity.TableRestaurant;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commandes_maquis")
public class CommandeMaquis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private TableRestaurant table;

    @Column(length = 150)
    private String clientNom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCommandeMaquis statut = StatutCommandeMaquis.EN_COURS;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommandeMaquis> lignes = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public TableRestaurant getTable() {
        return table;
    }

    public void setTable(TableRestaurant table) {
        this.table = table;
    }

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public StatutCommandeMaquis getStatut() {
        return statut;
    }

    public void setStatut(StatutCommandeMaquis statut) {
        this.statut = statut;
    }

    public List<LigneCommandeMaquis> getLignes() {
        return lignes;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
