package com.leconsulat.vente.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.finance.service.RecetteService;
import com.leconsulat.security.CustomUserDetails;
import com.leconsulat.stock.entity.Produit;
import com.leconsulat.stock.repository.ProduitRepository;
import com.leconsulat.stock.service.SortieStockService;
import com.leconsulat.utilisateur.entity.Utilisateur;
import com.leconsulat.vente.dto.*;
import com.leconsulat.vente.entity.*;
import com.leconsulat.vente.repository.RemiseRepository;
import com.leconsulat.vente.repository.SessionCaisseRepository;
import com.leconsulat.vente.repository.VenteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;

/**
 * Cœur du module caisse. Règles clés (cahier des charges §2.1 / API_CONTRACT.md §3) :
 * - un ticket est un panier modifiable tant que EN_COURS ;
 * - l'encaissement attribue un numéro séquentiel FV-{année}-{séquence}, décrémente le
 *   stock réel (création de SortieStock validées) et génère automatiquement une Recette ;
 * - une fois PAYEE, un ticket est totalement immuable (seul un Avoir peut être émis).
 */
@Service
@Transactional
public class VenteService {

    private final VenteRepository repository;
    private final SessionCaisseRepository sessionCaisseRepository;
    private final ProduitRepository produitRepository;
    private final RemiseRepository remiseRepository;
    private final SortieStockService sortieStockService;
    private final RecetteService recetteService;
    private final JournalOperationService journal;

    public VenteService(VenteRepository repository, SessionCaisseRepository sessionCaisseRepository,
                         ProduitRepository produitRepository, RemiseRepository remiseRepository,
                         SortieStockService sortieStockService, RecetteService recetteService,
                         JournalOperationService journal) {
        this.repository = repository;
        this.sessionCaisseRepository = sessionCaisseRepository;
        this.produitRepository = produitRepository;
        this.remiseRepository = remiseRepository;
        this.sortieStockService = sortieStockService;
        this.recetteService = recetteService;
        this.journal = journal;
    }

    public Page<VenteDto> search(StatutVente statut, Long sessionCaisseId, Long caissierId,
                                  LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable) {
        return repository.search(statut, sessionCaisseId, caissierId, dateDebut, dateFin, pageable).map(VenteDto::from);
    }

    public VenteDto get(Long id) {
        return VenteDto.from(findEntity(id));
    }

    Vente findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Vente", id));
    }

    @Transactional
    public VenteDto create(CreateVenteRequest req) {
        SessionCaisse session = sessionCaisseRepository.findById(req.sessionCaisseId())
                .orElseThrow(() -> ResourceNotFoundException.of("SessionCaisse", req.sessionCaisseId()));
        if (session.getStatut() != SessionCaisse.Statut.OUVERTE) {
            throw new BusinessRuleException("Impossible de créer une vente sur une session de caisse fermée");
        }
        Vente v = new Vente();
        v.setSessionCaisse(session);
        v.setCaissier(currentUtilisateur());
        v.setClientNom(req.clientNom());
        v.setStatut(StatutVente.EN_COURS);
        v.setDateVente(LocalDateTime.now());
        return VenteDto.from(repository.save(v));
    }

    @Transactional
    public VenteDto ajouterLigne(Long venteId, AddLigneVenteRequest req) {
        Vente v = findEntity(venteId);
        assertModifiable(v);
        Produit produit = produitRepository.findById(req.produitId())
                .orElseThrow(() -> ResourceNotFoundException.of("Produit", req.produitId()));

        LigneVente ligne = new LigneVente();
        ligne.setVente(v);
        ligne.setProduit(produit);
        ligne.setQuantite(req.quantite());
        ligne.setPrixUnitaire(produit.getPrixVente());
        BigDecimal remise = req.remise() != null ? req.remise() : BigDecimal.ZERO;
        ligne.setRemise(remise);
        BigDecimal montantBrut = produit.getPrixVente().multiply(BigDecimal.valueOf(req.quantite()));
        ligne.setMontant(montantBrut.subtract(remise).max(BigDecimal.ZERO));
        v.getLignes().add(ligne);

        recalculerTotaux(v, BigDecimal.ZERO);
        return VenteDto.from(repository.save(v));
    }

    @Transactional
    public VenteDto retirerLigne(Long venteId, Long ligneId) {
        Vente v = findEntity(venteId);
        assertModifiable(v);
        boolean removed = v.getLignes().removeIf(l -> l.getId().equals(ligneId));
        if (!removed) {
            throw ResourceNotFoundException.of("LigneVente", ligneId);
        }
        recalculerTotaux(v, v.getRemiseMontant());
        return VenteDto.from(repository.save(v));
    }

    @Transactional
    public VenteDto encaisser(Long venteId, EncaisserVenteRequest req) {
        Vente v = findEntity(venteId);
        assertModifiable(v);
        if (v.getLignes().isEmpty()) {
            throw new BusinessRuleException("Impossible d'encaisser un ticket vide");
        }

        ModePaiement mode;
        try {
            mode = ModePaiement.valueOf(req.modePaiement().trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Mode de paiement inconnu : " + req.modePaiement());
        }

        BigDecimal remiseGlobale = BigDecimal.ZERO;
        if (req.remiseId() != null) {
            Remise remise = remiseRepository.findById(req.remiseId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Remise", req.remiseId()));
            if (!remise.isActif()) {
                throw new BusinessRuleException("Cette remise n'est plus active");
            }
            remiseGlobale = remise.getType() == Remise.Type.POURCENTAGE
                    ? v.getSousTotal().multiply(remise.getValeur()).divide(BigDecimal.valueOf(100))
                    : remise.getValeur();
        }
        recalculerTotaux(v, remiseGlobale);

        if (req.montantRecu().compareTo(v.getTotal()) < 0) {
            throw new BusinessRuleException("Le montant reçu (" + req.montantRecu() + ") est insuffisant pour le total (" + v.getTotal() + ")");
        }

        // Décrémentation réelle du stock, ligne par ligne, avec sortie de stock validée automatiquement.
        for (LigneVente ligne : v.getLignes()) {
            sortieStockService.enregistrerSortieVenteValidee(ligne.getProduit(), ligne.getQuantite(), ligne.getPrixUnitaire());
        }

        v.setNumero(genererNumero());
        v.setModePaiement(mode);
        v.setStatut(StatutVente.PAYEE);
        v.setDateVente(LocalDateTime.now());
        Vente saved = repository.save(v);

        recetteService.enregistrerDepuisVente(saved.getTotal(), "Vente " + saved.getNumero());
        journal.enregistrer("VENTES", "VALIDATION", "Encaissement du ticket " + saved.getNumero()
                + " (" + saved.getTotal() + " FCFA, " + mode + ")");

        return VenteDto.from(saved);
    }

    private synchronized String genererNumero() {
        int year = Year.now().getValue();
        String prefix = "FV-" + year + "-";
        long count = repository.countByNumeroStartingWith(prefix);
        return prefix + String.format("%04d", count + 1);
    }

    private void recalculerTotaux(Vente v, BigDecimal remiseGlobale) {
        BigDecimal sousTotal = v.getLignes().stream().map(LigneVente::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
        v.setSousTotal(sousTotal);
        v.setRemiseMontant(remiseGlobale);
        v.setTotal(sousTotal.subtract(remiseGlobale).max(BigDecimal.ZERO));
    }

    private void assertModifiable(Vente v) {
        if (v.getStatut() != StatutVente.EN_COURS) {
            throw new BusinessRuleException("Cette vente est " + v.getStatut() + " : elle n'est plus modifiable (seul un avoir est possible après paiement)");
        }
    }

    private Utilisateur currentUtilisateur() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUtilisateur();
        }
        return null;
    }
}
