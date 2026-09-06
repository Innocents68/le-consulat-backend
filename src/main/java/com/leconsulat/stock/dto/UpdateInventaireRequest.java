package com.leconsulat.stock.dto;

import java.util.List;

public record UpdateInventaireRequest(List<LigneComptageRequest> lignes) {
    public record LigneComptageRequest(Long ligneId, int quantiteReelle) {
    }
}
