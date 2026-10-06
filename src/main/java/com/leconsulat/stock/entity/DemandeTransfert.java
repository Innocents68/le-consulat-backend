package com.leconsulat.stock.entity;

import com.leconsulat.catalogue.entity.Produit;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.utilisateur.entity.Utilisateur;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Demandes_amelioration_logiciel_Le_Consulat_Professionnel.docx §5 : formalise la demande, la
 * notification et l'acceptation/refus d'un transfert de produit entre établissements — remplace
 * le transfert direct (RG-084, resté disponible pour le Super Administrateur, cf.
 * {@code MouvementStockService.transfert}) pour l'usage courant d'un Gérant/Caissier, qui n'a
 * jamais visibilité sur le catalogue de l'établissement destinataire (RG-002). Le produit
 * destination n'est donc choisi qu'à l'acceptation, par le responsable de cet établissement. */
@Entity
@Table(name = "demandes_transfert")
public class DemandeTransfert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "produit_source_id", nullable = false)
    private Produit produitSource;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantite;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "etablissement_destination_id", nullable = false)
    private Etablissement etablissementDestination;

    /** Renseigné uniquement à l'acceptation, par le responsable de l'établissement destinataire. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "produit_destination_id")
    private Produit produitDestination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutDemandeTransfert statut = StatutDemandeTransfert.EN_ATTENTE;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "demandeur_id", nullable = false)
    private Utilisateur demandeur;

    @Column(length = 500)
    private String commentaireDemande;

    /** Renseigné par le responsable en cas de refus (§5 : "pouvoir indiquer un motif de refus"). */
    @Column(length = 500)
    private String motifRefus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "traite_par_id")
    private Utilisateur traitePar;

    @Column(nullable = false)
    private LocalDateTime dateDemande = LocalDateTime.now();

    private LocalDateTime dateTraitement;

    public DemandeTransfert() {
    }

    public Long getId() {
        return id;
    }

    public Produit getProduitSource() {
        return produitSource;
    }

    public void setProduitSource(Produit produitSource) {
        this.produitSource = produitSource;
    }

    public BigDecimal getQuantite() {
        return quantite;
    }

    public void setQuantite(BigDecimal quantite) {
        this.quantite = quantite;
    }

    public Etablissement getEtablissementDestination() {
        return etablissementDestination;
    }

    public void setEtablissementDestination(Etablissement etablissementDestination) {
        this.etablissementDestination = etablissementDestination;
    }

    public Produit getProduitDestination() {
        return produitDestination;
    }

    public void setProduitDestination(Produit produitDestination) {
        this.produitDestination = produitDestination;
    }

    public StatutDemandeTransfert getStatut() {
        return statut;
    }

    public void setStatut(StatutDemandeTransfert statut) {
        this.statut = statut;
    }

    public Utilisateur getDemandeur() {
        return demandeur;
    }

    public void setDemandeur(Utilisateur demandeur) {
        this.demandeur = demandeur;
    }

    public String getCommentaireDemande() {
        return commentaireDemande;
    }

    public void setCommentaireDemande(String commentaireDemande) {
        this.commentaireDemande = commentaireDemande;
    }

    public String getMotifRefus() {
        return motifRefus;
    }

    public void setMotifRefus(String motifRefus) {
        this.motifRefus = motifRefus;
    }

    public Utilisateur getTraitePar() {
        return traitePar;
    }

    public void setTraitePar(Utilisateur traitePar) {
        this.traitePar = traitePar;
    }

    public LocalDateTime getDateDemande() {
        return dateDemande;
    }

    public LocalDateTime getDateTraitement() {
        return dateTraitement;
    }

    public void setDateTraitement(LocalDateTime dateTraitement) {
        this.dateTraitement = dateTraitement;
    }
}
