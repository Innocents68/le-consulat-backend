package com.leconsulat.finance.controller;

import com.leconsulat.finance.dto.VueEnsembleFinanceDto;
import com.leconsulat.finance.entity.Depense;
import com.leconsulat.finance.repository.DepenseRepository;
import com.leconsulat.finance.service.RecetteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/finance")
public class FinanceOverviewController {

    private final RecetteService recetteService;
    private final DepenseRepository depenseRepository;

    public FinanceOverviewController(RecetteService recetteService, DepenseRepository depenseRepository) {
        this.recetteService = recetteService;
        this.depenseRepository = depenseRepository;
    }

    @GetMapping("/vue-ensemble")
    public VueEnsembleFinanceDto vueEnsemble(@RequestParam(defaultValue = "jour") String periode) {
        LocalDate today = LocalDate.now();
        LocalDateTime debutJour = today.atStartOfDay();
        LocalDateTime finJour = today.atTime(LocalTime.MAX);

        BigDecimal recettesDuJour = recetteService.totalPeriode(debutJour, finJour);
        BigDecimal depensesDuJour = sumDepenses(today, today);
        BigDecimal beneficeDuJour = recettesDuJour.subtract(depensesDuJour);

        List<VueEnsembleFinanceDto.Evolution> evolution = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate jour = today.minusDays(i);
            BigDecimal recettes = recetteService.totalPeriode(jour.atStartOfDay(), jour.atTime(LocalTime.MAX));
            BigDecimal depenses = sumDepenses(jour, jour);
            evolution.add(new VueEnsembleFinanceDto.Evolution(jour, recettes, depenses));
        }

        return new VueEnsembleFinanceDto(recettesDuJour, depensesDuJour, beneficeDuJour, evolution);
    }

    private BigDecimal sumDepenses(LocalDate debut, LocalDate fin) {
        return depenseRepository.findByStatutAndDateBetween(Depense.Statut.VALIDEE, debut, fin).stream()
                .map(Depense::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
