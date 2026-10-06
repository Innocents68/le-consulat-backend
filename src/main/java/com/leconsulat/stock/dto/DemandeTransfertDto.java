package com.leconsulat.stock.dto;

import com.leconsulat.stock.entity.DemandeTransfert;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DemandeTransfertDto(
        Long id,
        Long produitSourceId,
        String produitSourceNom,
        Long etablissementSourceId,
        String etablissementSourceNom,
        BigDecimal quantite,
        Long etablissementDestinationId,
        String etablissementDestinationNom,
        Long produitDestinationId,
        String produitDestinationNom,
        String statut,
        Long demandeurId,
        String demandeurNom,
        String commentaireDemande,
        String motifRefus,
        Long traiteParId,
        String traiteParNom,
        LocalDateTime dateDemande,
        LocalDateTime dateTraitement
) {
    public static DemandeTransfertDto from(DemandeTransfert d) {
        return new DemandeTransfertDto(
                d.getId(),
                d.getProduitSource().getId(), d.getProduitSource().getNom(),
                d.getProduitSource().getEtablissement().getId(), d.getProduitSource().getEtablissement().getNom(),
                d.getQuantite(),
                d.getEtablissementDestination().getId(), d.getEtablissementDestination().getNom(),
                d.getProduitDestination() != null ? d.getProduitDestination().getId() : null,
                d.getProduitDestination() != null ? d.getProduitDestination().getNom() : null,
                d.getStatut().name(),
                d.getDemandeur().getId(), d.getDemandeur().getNom(),
                d.getCommentaireDemande(),
                d.getMotifRefus(),
                d.getTraitePar() != null ? d.getTraitePar().getId() : null,
                d.getTraitePar() != null ? d.getTraitePar().getNom() : null,
                d.getDateDemande(), d.getDateTraitement());
    }
}
