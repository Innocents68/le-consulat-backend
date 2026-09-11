package com.leconsulat.utilisateur.entity;

import com.leconsulat.etablissement.entity.Etablissement;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(nullable = false)
    private String motDePasse;

    @Column(nullable = false, length = 120)
    private String nom;

    @Column(length = 120)
    private String email;

    @Column(length = 30)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Profil profil;

    /** Nullable : seul le Super Administrateur n'est rattaché à aucun établissement (vision
     * globale, RG-005). Un Gérant/Caissier en a obligatoirement un et un seul (RG-098).
     * EAGER : lu jusque dans des points d'entrée non transactionnels (login), et c'est une
     * référence à une table minuscule (3-4 lignes) — pas de risque de N+1 significatif. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "etablissement_id")
    private Etablissement etablissement;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public Utilisateur() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Profil getProfil() {
        return profil;
    }

    public void setProfil(Profil profil) {
        this.profil = profil;
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

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    /** Initials used by the frontend as avatar placeholder (e.g. "Jean Ouedraogo" -> "JO"). */
    public String getAvatarInitiales() {
        if (nom == null || nom.isBlank()) {
            return "";
        }
        String[] parts = nom.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            if (!parts[i].isEmpty()) {
                sb.append(Character.toUpperCase(parts[i].charAt(0)));
            }
        }
        return sb.toString();
    }
}
