package com.leconsulat.dashboard;

import com.leconsulat.cave.repository.BoissonRepository;
import com.leconsulat.finance.entity.Depense;
import com.leconsulat.finance.entity.Recette;
import com.leconsulat.finance.repository.DepenseRepository;
import com.leconsulat.finance.repository.RecetteRepository;
import com.leconsulat.maquis.entity.StatutCommandeMaquis;
import com.leconsulat.maquis.repository.CommandeMaquisRepository;
import com.leconsulat.restaurant.entity.Commande;
import com.leconsulat.restaurant.entity.StatutCommande;
import com.leconsulat.restaurant.repository.CommandeRepository;
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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DashboardService {

    private final VenteRepository venteRepository;
    private final LigneVenteRepository ligneVenteRepository;
    private final RecetteRepository recetteRepository;
    private final DepenseRepository depenseRepository;
    private final CommandeRepository commandeRepository;
    private final CommandeMaquisRepository commandeMaquisRepository;
    private final ProduitRepository produitRepository;
    private final BoissonRepository boissonRepository;

    public DashboardService(VenteRepository venteRepository, LigneVenteRepository ligneVenteRepository,
                             RecetteRepository recetteRepository, DepenseRepository depenseRepository,
                             CommandeRepository commandeRepository, CommandeMaquisRepository commandeMaquisRepository,
                             ProduitRepository produitRepository, BoissonRepository boissonRepository) {
        this.venteRepository = venteRepository;
        this.ligneVenteRepository = ligneVenteRepository;
        this.recetteRepository = recetteRepository;
        this.depenseRepository = depenseRepository;
        this.commandeRepository = commandeRepository;
        this.commandeMaquisRepository = commandeMaquisRepository;
        this.produitRepository = produitRepository;
        this.boissonRepository = boissonRepository;
    }

    public DashboardSummaryDto summary() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        BigDecimal ventesDuJour = totalVentes(today, today);
        BigDecimal ventesHier = totalVentes(yesterday, yesterday);

        BigDecimal recettesDuJour = totalRecettes(today, today);
        BigDecimal recettesHier = totalRecettes(yesterday, yesterday);

        long commandesDuJour = countCommandesRestaurant(today, today);
        long commandesHier = countCommandesRestaurant(yesterday, yesterday);

        long consommationsDuJour = countConsommationsMaquis(today, today);
        long consommationsHier = countConsommationsMaquis(yesterday, yesterday);

        BigDecimal depensesDuJour = totalDepenses(today, today);
        BigDecimal depensesHier = totalDepenses(yesterday, yesterday);
        BigDecimal beneficeDuJour = recettesDuJour.subtract(depensesDuJour);
        BigDecimal beneficeHier = recettesHier.subtract(depensesHier);

        DashboardSummaryDto.Variations variations = new DashboardSummaryDto.Variations(
                variation(ventesDuJour, ventesHier),
                variation(recettesDuJour, recettesHier),
                variation(BigDecimal.valueOf(commandesDuJour), BigDecimal.valueOf(commandesHier)),
                variation(BigDecimal.valueOf(consommationsDuJour), BigDecimal.valueOf(consommationsHier)),
                variation(beneficeDuJour, beneficeHier)
        );

        List<DashboardSummaryDto.PointEvolution> evolution = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate jour = today.minusDays(i);
            evolution.add(new DashboardSummaryDto.PointEvolution(jour, totalVentes(jour, jour)));
        }

        List<DashboardSummaryDto.RepartitionActivite> repartition = repartitionActivites(today);
        List<DashboardSummaryDto.Alerte> alertes = alertes();
        List<DashboardSummaryDto.TopProduit> topProduits = topProduits();
        List<DashboardSummaryDto.ActiviteRecente> activiteRecente = activiteRecente();

        return new DashboardSummaryDto(ventesDuJour, recettesDuJour, commandesDuJour, consommationsDuJour,
                beneficeDuJour, variations, evolution, repartition, alertes, topProduits, activiteRecente);
    }

    private BigDecimal totalVentes(LocalDate debut, LocalDate fin) {
        return venteRepository.findByStatutAndDateVenteBetween(StatutVente.PAYEE, debut.atStartOfDay(), fin.atTime(LocalTime.MAX))
                .stream().map(Vente::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalRecettes(LocalDate debut, LocalDate fin) {
        return recetteRepository.findByDateBetween(debut.atStartOfDay(), fin.atTime(LocalTime.MAX))
                .stream().map(Recette::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalDepenses(LocalDate debut, LocalDate fin) {
        return depenseRepository.findByStatutAndDateBetween(Depense.Statut.VALIDEE, debut, fin)
                .stream().map(Depense::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long countCommandesRestaurant(LocalDate debut, LocalDate fin) {
        return commandeRepository.countByStatutInAndDateCreationBetween(
                List.of(StatutCommande.EN_ATTENTE, StatutCommande.EN_PREPARATION, StatutCommande.PRETE, StatutCommande.SERVIE),
                debut.atStartOfDay(), fin.atTime(LocalTime.MAX));
    }

    private long countConsommationsMaquis(LocalDate debut, LocalDate fin) {
        return commandeMaquisRepository.countByStatutAndDateCreationBetween(
                StatutCommandeMaquis.CLOTUREE, debut.atStartOfDay(), fin.atTime(LocalTime.MAX));
    }

    private double variation(BigDecimal actuel, BigDecimal precedent) {
        if (precedent == null || precedent.compareTo(BigDecimal.ZERO) == 0) {
            return actuel != null && actuel.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return actuel.subtract(precedent).divide(precedent, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    private List<DashboardSummaryDto.RepartitionActivite> repartitionActivites(LocalDate today) {
        BigDecimal ventes = totalVentes(today, today);
        BigDecimal restaurant = commandeRepository.search(StatutCommande.SERVIE, null, PageRequest.of(0, 1000))
                .stream().map(c -> ligneCommandeTotal(c)).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal maquis = commandeMaquisRepository.findByStatutAndDateCreationBetween(
                        StatutCommandeMaquis.CLOTUREE, today.atStartOfDay(), today.atTime(LocalTime.MAX)).stream()
                .flatMap(c -> c.getLignes().stream())
                .map(l -> l.getMontant())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cave = BigDecimal.ZERO; // Cave revenue is tracked via mouvements, aggregated in the cave/reporting module.

        BigDecimal total = ventes.add(restaurant).add(maquis).add(cave);
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return List.of(
                    new DashboardSummaryDto.RepartitionActivite("Ventes", 0),
                    new DashboardSummaryDto.RepartitionActivite("Restaurant", 0),
                    new DashboardSummaryDto.RepartitionActivite("Maquis", 0),
                    new DashboardSummaryDto.RepartitionActivite("Cave", 0)
            );
        }
        return List.of(
                new DashboardSummaryDto.RepartitionActivite("Ventes", pourcentage(ventes, total)),
                new DashboardSummaryDto.RepartitionActivite("Restaurant", pourcentage(restaurant, total)),
                new DashboardSummaryDto.RepartitionActivite("Maquis", pourcentage(maquis, total)),
                new DashboardSummaryDto.RepartitionActivite("Cave", pourcentage(cave, total))
        );
    }

    private BigDecimal ligneCommandeTotal(Commande c) {
        return c.getLignes().stream().map(l -> l.getMontant()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private double pourcentage(BigDecimal part, BigDecimal total) {
        return part.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP).doubleValue();
    }

    private List<DashboardSummaryDto.Alerte> alertes() {
        List<DashboardSummaryDto.Alerte> alertes = new ArrayList<>();
        for (Produit p : produitRepository.findEnAlerte()) {
            String niveau = p.getQuantiteStock() <= 0 ? "CRITICAL" : "WARNING";
            String type = p.getQuantiteStock() <= 0 ? "RUPTURE" : "STOCK_FAIBLE";
            alertes.add(new DashboardSummaryDto.Alerte(type, niveau,
                    "Stock faible pour " + p.getNom() + " (" + p.getQuantiteStock() + " restant(s))"));
        }
        boissonRepository.findEnAlerte().forEach(b -> {
            String niveau = b.getQuantiteStock().compareTo(BigDecimal.ZERO) <= 0 ? "CRITICAL" : "WARNING";
            String type = b.getQuantiteStock().compareTo(BigDecimal.ZERO) <= 0 ? "RUPTURE" : "STOCK_FAIBLE";
            alertes.add(new DashboardSummaryDto.Alerte(type, niveau,
                    "Stock cave faible pour " + b.getNom() + " (" + b.getQuantiteStock() + " bouteille(s))"));
        });
        return alertes;
    }

    private List<DashboardSummaryDto.TopProduit> topProduits() {
        LocalDateTime depuis = LocalDate.now().minusDays(30).atStartOfDay();
        return ligneVenteRepository.topProduitsDepuis(depuis, PageRequest.of(0, 5)).stream()
                .map(p -> new DashboardSummaryDto.TopProduit(p.getNom(), p.getQuantite(), p.getMontant()))
                .toList();
    }

    private List<DashboardSummaryDto.ActiviteRecente> activiteRecente() {
        record Horodate(LocalDateTime date, DashboardSummaryDto.ActiviteRecente activite) {
        }
        List<Horodate> activites = new ArrayList<>();

        for (Vente v : venteRepository.findTop10ByStatutOrderByDateVenteDesc(StatutVente.PAYEE)) {
            activites.add(new Horodate(v.getDateVente(),
                    new DashboardSummaryDto.ActiviteRecente("PAIEMENT", "Vente " + v.getNumero(), v.getTotal(), ilYA(v.getDateVente()))));
        }
        for (Commande c : commandeRepository.findTop10ByOrderByDateCreationDesc()) {
            activites.add(new Horodate(c.getDateCreation(),
                    new DashboardSummaryDto.ActiviteRecente("COMMANDE",
                            "Commande " + c.getType() + (c.getTable() != null ? " - Table " + c.getTable().getNumero() : ""),
                            ligneCommandeTotal(c), ilYA(c.getDateCreation()))));
        }

        return activites.stream()
                .sorted(Comparator.comparing(Horodate::date).reversed())
                .limit(10)
                .map(Horodate::activite)
                .toList();
    }

    private String ilYA(LocalDateTime date) {
        if (date == null) {
            return "-";
        }
        long minutes = Duration.between(date, LocalDateTime.now()).toMinutes();
        if (minutes < 1) {
            return "à l'instant";
        }
        if (minutes < 60) {
            return minutes + " min";
        }
        long heures = ChronoUnit.HOURS.between(date, LocalDateTime.now());
        if (heures < 24) {
            return heures + " h";
        }
        long jours = ChronoUnit.DAYS.between(date, LocalDateTime.now());
        return jours + " j";
    }
}
