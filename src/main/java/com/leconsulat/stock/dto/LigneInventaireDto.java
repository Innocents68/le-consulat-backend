package com.leconsulat.stock.dto;

import com.leconsulat.stock.entity.LigneInventaire;

public record LigneInventaireDto(
        Long id,
        Long produitId,
        String produitNom,
        int quantiteTheorique,
        int quantiteReelle,
        int ecart
) {
    public static LigneInventaireDto from(LigneInventaire l) {
        return new LigneInventaireDto(l.getId(), l.getProduit().getId(), l.getProduit().getNom(),
                l.getQuantiteTheorique(), l.getQuantiteReelle(), l.getEcart());
    }
}
