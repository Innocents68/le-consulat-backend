package com.leconsulat.journal.entity;

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
}
