package com.leconsulat.journal.entity;

import com.leconsulat.etablissement.entity.Etablissement;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Immutable audit trail row. Never updated nor deleted (cf. cahier des charges §2.7 :
 * "Journalisation infalsifiable des opérations critiques").
 */
@Entity
@Table(name = "journal_operations")
public class JournalOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utilisateur_id")
    private Long utilisateurId;

    @Column(name = "utilisateur_nom", length = 150)
    private String utilisateurNom;

    @Column(nullable = false, length = 60)
    private String module;

    @Column(nullable = false, length = 60)
    private String action;

    @Column(length = 2000)
    private String details;

    /** Nullable : une connexion ou une action globale du Super Administrateur n'a pas
     * forcément d'établissement (RG-017 s'applique aux données métier, pas à toute ligne
     * d'audit). Déduit automatiquement de l'auteur par {@code JournalOperationService}.
     * EAGER pour la même raison que {@code Utilisateur.etablissement}. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "etablissement_id")
    private Etablissement etablissement;

    @Column(nullable = false)
    private LocalDateTime dateOperation = LocalDateTime.now();

    public JournalOperation() {
    }

    public JournalOperation(Long utilisateurId, String utilisateurNom, String module, String action, String details) {
        this.utilisateurId = utilisateurId;
        this.utilisateurNom = utilisateurNom;
        this.module = module;
        this.action = action;
        this.details = details;
        this.dateOperation = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public String getUtilisateurNom() {
        return utilisateurNom;
    }

    public String getModule() {
        return module;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getDateOperation() {
        return dateOperation;
    }

    public Etablissement getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(Etablissement etablissement) {
        this.etablissement = etablissement;
    }
}
