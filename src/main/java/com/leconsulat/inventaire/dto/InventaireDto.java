package com.leconsulat.inventaire.dto;

import com.leconsulat.inventaire.entity.Inventaire;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InventaireDto(
        Long id,
        String numero,
        Long etablissementId,
        String etablissementNom,
        LocalDate dateInventaire,
        String statut,
        Long auteurId,
        String auteurNom,
        Long validateurId,
        String validateurNom,
        String commentaire,
        List<LigneInventaireDto> lignes,
        LocalDateTime dateCreation,
        LocalDateTime dateCloture,
        LocalDateTime dateValidation
) {
    public static InventaireDto from(Inventaire i) {
        return new InventaireDto(i.getId(), i.getNumero(),
                i.getEtablissement().getId(), i.getEtablissement().getNom(),
                i.getDateInventaire(), i.getStatut().name(),
                i.getAuteur().getId(), i.getAuteur().getNom(),
                i.getValidateur() != null ? i.getValidateur().getId() : null,
                i.getValidateur() != null ? i.getValidateur().getNom() : null,
                i.getCommentaire(),
                i.getLignes().stream().map(LigneInventaireDto::from).toList(),
                i.getDateCreation(), i.getDateCloture(), i.getDateValidation());
    }

    /** Résumé de liste, sans les lignes — évite de charger toutes les lignes de tous les
     * inventaires pour un simple tableau paginé. */
    public static InventaireDto summary(Inventaire i) {
        return new InventaireDto(i.getId(), i.getNumero(),
                i.getEtablissement().getId(), i.getEtablissement().getNom(),
                i.getDateInventaire(), i.getStatut().name(),
                i.getAuteur().getId(), i.getAuteur().getNom(),
                i.getValidateur() != null ? i.getValidateur().getId() : null,
                i.getValidateur() != null ? i.getValidateur().getNom() : null,
                i.getCommentaire(),
                null,
                i.getDateCreation(), i.getDateCloture(), i.getDateValidation());
    }
}
