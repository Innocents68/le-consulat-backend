package com.leconsulat.financier.service;

import com.leconsulat.avoir.repository.AvoirRepository;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.depense.repository.DepenseRepository;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.financier.dto.SyntheseEtablissementDto;
import com.leconsulat.financier.dto.SyntheseFinanciereDto;
import com.leconsulat.vente.entity.ModePaiement;
import com.leconsulat.vente.repository.FactureRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** §6.7.1 — module entièrement réservé au Super Administrateur (EF-031), la garde d'accès est
 * posée au niveau du contrôleur. */
@Service
public class FinancierService {

    private final EtablissementRepository etablissementRepository;
    private final FactureRepository factureRepository;
    private final AvoirRepository avoirRepository;
    private final DepenseRepository depenseRepository;

    public FinancierService(EtablissementRepository etablissementRepository, FactureRepository factureRepository,
                             AvoirRepository avoirRepository, DepenseRepository depenseRepository) {
        this.etablissementRepository = etablissementRepository;
        this.factureRepository = factureRepository;
        this.avoirRepository = avoirRepository;
        this.depenseRepository = depenseRepository;
    }

    public SyntheseFinanciereDto synthese(LocalDate dateDebut, LocalDate dateFin, Long etablissementId,
                                           Long utilisateurId, String modePaiement) {
        ModePaiement mode = modePaiement != null ? parseMode(modePaiement) : null;
        LocalDateTime debutTimestamp = dateDebut != null ? dateDebut.atStartOfDay() : null;
        LocalDateTime finTimestamp = dateFin != null ? dateFin.atTime(23, 59, 59) : null;

        List<Etablissement> etablissements = etablissementId != null
                ? List.of(etablissementRepository.findById(etablissementId)
                        .orElseThrow(() -> ResourceNotFoundException.of("Etablissement", etablissementId)))
                : etablissementRepository.findByActifTrue();

        List<SyntheseEtablissementDto> lignes = etablissements.stream()
                .map(e -> calculer(e, debutTimestamp, finTimestamp, dateDebut, dateFin, utilisateurId, mode))
                .toList();

        SyntheseEtablissementDto total = null;
        if (etablissementId == null) {
            BigDecimal recettes = lignes.stream().map(SyntheseEtablissementDto::recettes).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avoirs = lignes.stream().map(SyntheseEtablissementDto::avoirs).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal depenses = lignes.stream().map(SyntheseEtablissementDto::depenses).reduce(BigDecimal.ZERO, BigDecimal::add);
            total = SyntheseEtablissementDto.of(null, "TOTAL", recettes, avoirs, depenses);
        }

        return new SyntheseFinanciereDto(dateDebut, dateFin, lignes, total);
    }

    private SyntheseEtablissementDto calculer(Etablissement e, LocalDateTime debutTimestamp, LocalDateTime finTimestamp,
                                               LocalDate dateDebut, LocalDate dateFin, Long utilisateurId, ModePaiement mode) {
        BigDecimal recettes = factureRepository.sommeRecettes(e, debutTimestamp, finTimestamp, utilisateurId, mode);
        BigDecimal avoirs = avoirRepository.sommeAvoirsPeriode(e, debutTimestamp, finTimestamp, utilisateurId);
        BigDecimal depenses = depenseRepository.sommeDepenses(e, dateDebut, dateFin, utilisateurId, mode);
        return SyntheseEtablissementDto.of(e.getId(), e.getNom(), recettes, avoirs, depenses);
    }

    private ModePaiement parseMode(String mode) {
        try {
            return ModePaiement.valueOf(mode.trim().toUpperCase());
        } catch (Exception ex) {
            throw new BusinessRuleException("Mode de paiement inconnu : " + mode);
        }
    }
}
