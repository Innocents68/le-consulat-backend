package com.leconsulat.reporting;

import com.leconsulat.finance.entity.Depense;
import com.leconsulat.finance.entity.Recette;
import com.leconsulat.finance.repository.DepenseRepository;
import com.leconsulat.finance.repository.RecetteRepository;
import com.leconsulat.stock.entity.Produit;
import com.leconsulat.stock.repository.ProduitRepository;
import com.leconsulat.vente.entity.StatutVente;
import com.leconsulat.vente.entity.Vente;
import com.leconsulat.vente.repository.LigneVenteRepository;
import com.leconsulat.vente.repository.VenteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportingService {

    private final VenteRepository venteRepository;
    private final LigneVenteRepository ligneVenteRepository;
    private final ProduitRepository produitRepository;
    private final RecetteRepository recetteRepository;
    private final DepenseRepository depenseRepository;

    public ReportingService(VenteRepository venteRepository, LigneVenteRepository ligneVenteRepository,
                             ProduitRepository produitRepository, RecetteRepository recetteRepository,
                             DepenseRepository depenseRepository) {
        this.venteRepository = venteRepository;
        this.ligneVenteRepository = ligneVenteRepository;
        this.produitRepository = produitRepository;
        this.recetteRepository = recetteRepository;
        this.depenseRepository = depenseRepository;
    }

    public static LocalDate[] resolvePeriode(String periode, LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut != null && dateFin != null) {
            return new LocalDate[]{dateDebut, dateFin};
        }
        LocalDate today = LocalDate.now();
        String p = periode == null ? "mois" : periode.toLowerCase();
        return switch (p) {
            case "jour" -> new LocalDate[]{today, today};
            case "semaine" -> new LocalDate[]{today.minusDays(6), today};
            case "annee" -> new LocalDate[]{today.withDayOfYear(1), today};
            default -> new LocalDate[]{today.withDayOfMonth(1), today};
        };
    }

    public RapportVentesDto rapportVentes(String periode, LocalDate dateDebut, LocalDate dateFin) {
        LocalDate[] bornes = resolvePeriode(periode, dateDebut, dateFin);
        LocalDate debut = bornes[0], fin = bornes[1];

        List<Vente> ventes = venteRepository.findByStatutAndDateVenteBetween(StatutVente.PAYEE, debut.atStartOfDay(), fin.atTime(LocalTime.MAX));
        BigDecimal ca = ventes.stream().map(Vente::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        long quantite = ventes.stream().flatMap(v -> v.getLignes().stream()).mapToLong(l -> l.getQuantite()).sum();
        BigDecimal panierMoyen = ventes.isEmpty() ? BigDecimal.ZERO : ca.divide(BigDecimal.valueOf(ventes.size()), 2, RoundingMode.HALF_UP);

        Map<LocalDate, BigDecimal> montantParJour = new LinkedHashMap<>();
        Map<LocalDate, Long> ticketsParJour = new LinkedHashMap<>();
        for (LocalDate d = debut; !d.isAfter(fin); d = d.plusDays(1)) {
            montantParJour.put(d, BigDecimal.ZERO);
            ticketsParJour.put(d, 0L);
        }
        for (Vente v : ventes) {
            LocalDate jour = v.getDateVente().toLocalDate();
            montantParJour.merge(jour, v.getTotal(), BigDecimal::add);
            ticketsParJour.merge(jour, 1L, Long::sum);
        }
        List<RapportVentesDto.VentesParJour> parJour = new ArrayList<>();
        montantParJour.forEach((jour, montant) -> parJour.add(new RapportVentesDto.VentesParJour(jour, montant, ticketsParJour.get(jour))));

        List<RapportVentesDto.TopProduitVente> topProduits = ligneVenteRepository
                .topProduitsDepuis(debut.atStartOfDay(), PageRequest.of(0, 10)).stream()
                .map(p -> new RapportVentesDto.TopProduitVente(p.getNom(), p.getQuantite(), p.getMontant()))
                .toList();

        return new RapportVentesDto(debut, fin, ca, quantite, panierMoyen, parJour, topProduits);
    }

    public RapportStocksDto rapportStocks() {
        List<Produit> produits = produitRepository.findAll().stream().filter(Produit::isActif).toList();
        BigDecimal valorisation = produits.stream()
                .map(p -> p.getPrixAchat().multiply(BigDecimal.valueOf(p.getQuantiteStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long ruptures = produits.stream().filter(p -> p.getQuantiteStock() <= 0).count();

        List<RapportStocksDto.ProduitStockFaible> stockFaible = produitRepository.findEnAlerte().stream()
                .map(p -> new RapportStocksDto.ProduitStockFaible(p.getNom(), p.getQuantiteStock(), p.getSeuilAlerte()))
                .toList();

        Map<String, BigDecimal> parCategorie = new LinkedHashMap<>();
        for (Produit p : produits) {
            String categorie = p.getCategorie() != null ? p.getCategorie().getNom() : "Non catégorisé";
            BigDecimal valeur = p.getPrixAchat().multiply(BigDecimal.valueOf(p.getQuantiteStock()));
            parCategorie.merge(categorie, valeur, BigDecimal::add);
        }
        List<RapportStocksDto.RepartitionCategorie> repartition = parCategorie.entrySet().stream()
                .map(e -> new RapportStocksDto.RepartitionCategorie(e.getKey(), e.getValue()))
                .toList();

        return new RapportStocksDto(valorisation, ruptures, stockFaible, repartition);
    }

    public RapportRecettesDepensesDto rapportRecettesDepenses(String periode, LocalDate dateDebut, LocalDate dateFin) {
        LocalDate[] bornes = resolvePeriode(periode, dateDebut, dateFin);
        LocalDate debut = bornes[0], fin = bornes[1];

        List<Recette> recettes = recetteRepository.findByDateBetween(debut.atStartOfDay(), fin.atTime(LocalTime.MAX));
        List<Depense> depenses = depenseRepository.findByStatutAndDateBetween(Depense.Statut.VALIDEE, debut, fin);

        BigDecimal totalRecettes = recettes.stream().map(Recette::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDepenses = depenses.stream().map(Depense::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<LocalDate, BigDecimal> recettesParJour = new LinkedHashMap<>();
        Map<LocalDate, BigDecimal> depensesParJour = new LinkedHashMap<>();
        for (LocalDate d = debut; !d.isAfter(fin); d = d.plusDays(1)) {
            recettesParJour.put(d, BigDecimal.ZERO);
            depensesParJour.put(d, BigDecimal.ZERO);
        }
        recettes.forEach(r -> recettesParJour.merge(r.getDate().toLocalDate(), r.getMontant(), BigDecimal::add));
        depenses.forEach(d -> depensesParJour.merge(d.getDate(), d.getMontant(), BigDecimal::add));

        List<RapportRecettesDepensesDto.FluxParJour> flux = new ArrayList<>();
        recettesParJour.forEach((jour, montant) -> flux.add(new RapportRecettesDepensesDto.FluxParJour(jour, montant, depensesParJour.get(jour))));

        return new RapportRecettesDepensesDto(debut, fin, totalRecettes, totalDepenses, totalRecettes.subtract(totalDepenses), flux);
    }

    public RapportBeneficesDto rapportBenefices(String periode, LocalDate dateDebut, LocalDate dateFin) {
        RapportRecettesDepensesDto base = rapportRecettesDepenses(periode, dateDebut, dateFin);
        List<RapportBeneficesDto.BeneficeParJour> evolution = base.fluxParJour().stream()
                .map(f -> new RapportBeneficesDto.BeneficeParJour(f.date(), f.recettes(), f.depenses(), f.recettes().subtract(f.depenses())))
                .toList();
        return new RapportBeneficesDto(base.dateDebut(), base.dateFin(), base.marge(), evolution);
    }
}
