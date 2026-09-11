package com.leconsulat.depense.dto;

import com.leconsulat.depense.entity.Depense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DepenseDto(
        Long id,
        LocalDate date,
        Long etablissementId,
        String etablissementNom,
        Long categorieId,
        String categorieNom,
        String libelle,
        BigDecimal montant,
        String modePaiement,
        String beneficiaire,
        Long utilisateurId,
        String utilisateurNom,
        String commentaire,
        LocalDateTime dateCreation,
        boolean annulee,
        String motifAnnulation
) {
    public static DepenseDto from(Depense d) {
        return new DepenseDto(d.getId(), d.getDate(),
                d.getEtablissement().getId(), d.getEtablissement().getNom(),
                d.getCategorie().getId(), d.getCategorie().getNom(),
                d.getLibelle(), d.getMontant(), d.getModePaiement().name(), d.getBeneficiaire(),
                d.getUtilisateur().getId(), d.getUtilisateur().getNom(),
                d.getCommentaire(), d.getDateCreation(), d.isAnnulee(), d.getMotifAnnulation());
    }
}
