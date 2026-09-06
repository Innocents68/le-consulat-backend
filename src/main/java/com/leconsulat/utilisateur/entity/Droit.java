package com.leconsulat.utilisateur.entity;

import jakarta.persistence.*;

/** One cell of the permissions matrix: what a given {@link Role} may do on a given module. */
@Entity
@Table(name = "droits", uniqueConstraints = @UniqueConstraint(columnNames = {"role", "module"}))
public class Droit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, length = 60)
    private String module;

    private boolean voir;
    private boolean ajouter;
    private boolean modifier;
    private boolean supprimer;

    public Droit() {
    }

    public Droit(Role role, String module, boolean voir, boolean ajouter, boolean modifier, boolean supprimer) {
        this.role = role;
        this.module = module;
        this.voir = voir;
        this.ajouter = ajouter;
        this.modifier = modifier;
        this.supprimer = supprimer;
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public boolean isVoir() {
        return voir;
    }

    public void setVoir(boolean voir) {
        this.voir = voir;
    }

    public boolean isAjouter() {
        return ajouter;
    }

    public void setAjouter(boolean ajouter) {
        this.ajouter = ajouter;
    }

    public boolean isModifier() {
        return modifier;
    }

    public void setModifier(boolean modifier) {
        this.modifier = modifier;
    }

    public boolean isSupprimer() {
        return supprimer;
    }

    public void setSupprimer(boolean supprimer) {
        this.supprimer = supprimer;
    }
}
