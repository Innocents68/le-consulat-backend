package com.leconsulat.restaurant.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.restaurant.dto.*;
import com.leconsulat.restaurant.entity.*;
import com.leconsulat.restaurant.repository.CommandeRepository;
import com.leconsulat.restaurant.repository.TableRestaurantRepository;
import com.leconsulat.restaurant.ws.RestaurantEventPublisher;
import com.leconsulat.security.CustomUserDetails;
import com.leconsulat.utilisateur.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommandeService {

    private final CommandeRepository repository;
    private final TableRestaurantRepository tableRepository;
    private final PlatService platService;
    private final RestaurantEventPublisher publisher;
    private final JournalOperationService journal;

    public CommandeService(CommandeRepository repository, TableRestaurantRepository tableRepository,
                            PlatService platService, RestaurantEventPublisher publisher, JournalOperationService journal) {
        this.repository = repository;
        this.tableRepository = tableRepository;
        this.platService = platService;
        this.publisher = publisher;
        this.journal = journal;
    }

    public Page<CommandeDto> search(StatutCommande statut, Long tableId, Pageable pageable) {
        return repository.search(statut, tableId, pageable).map(CommandeDto::from);
    }

    public CommandeDto get(Long id) {
        return CommandeDto.from(findEntity(id));
    }

    @Transactional
    public CommandeDto create(CreateCommandeRequest req) {
        TypeCommande type;
        try {
            type = TypeCommande.valueOf(req.type().trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Type de commande inconnu : " + req.type());
        }

        Commande commande = new Commande();
        commande.setType(type);
        commande.setStatut(StatutCommande.EN_ATTENTE);
        commande.setServeur(currentUtilisateur());

        if (req.tableId() != null) {
            TableRestaurant table = tableRepository.findById(req.tableId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Table", req.tableId()));
            commande.setTable(table);
            table.setStatut(StatutTable.OCCUPEE);
            tableRepository.save(table);
            publisher.tableMiseAJour(TableRestaurantDto.from(table));
        }

        Commande saved = repository.save(commande);
        CommandeDto dto = CommandeDto.from(saved);
        publisher.commandeMiseAJour(dto);
        return dto;
    }

    @Transactional
    public CommandeDto ajouterLigne(Long commandeId, AddLigneCommandeRequest req) {
        Commande commande = findEntity(commandeId);
        if (commande.getStatut() != StatutCommande.EN_ATTENTE) {
            throw new BusinessRuleException("Impossible de modifier une commande déjà envoyée en cuisine");
        }
        Plat plat = platService.findEntity(req.platId());
        LigneCommande ligne = new LigneCommande();
        ligne.setCommande(commande);
        ligne.setPlat(plat);
        ligne.setQuantite(req.quantite());
        ligne.setOptions(req.options());
        ligne.setPrixUnitaire(plat.getPrix());
        ligne.setMontant(plat.getPrix().multiply(java.math.BigDecimal.valueOf(req.quantite())));
        ligne.setStatut(StatutLigneCommande.EN_ATTENTE);
        commande.getLignes().add(ligne);

        Commande saved = repository.save(commande);
        CommandeDto dto = CommandeDto.from(saved);
        publisher.commandeMiseAJour(dto);
        return dto;
    }

    @Transactional
    public CommandeDto retirerLigne(Long commandeId, Long ligneId) {
        Commande commande = findEntity(commandeId);
        if (commande.getStatut() != StatutCommande.EN_ATTENTE) {
            throw new BusinessRuleException("Impossible de modifier une commande déjà envoyée en cuisine");
        }
        boolean removed = commande.getLignes().removeIf(l -> l.getId().equals(ligneId));
        if (!removed) {
            throw ResourceNotFoundException.of("LigneCommande", ligneId);
        }
        Commande saved = repository.save(commande);
        CommandeDto dto = CommandeDto.from(saved);
        publisher.commandeMiseAJour(dto);
        return dto;
    }

    @Transactional
    public CommandeDto envoyerCuisine(Long commandeId) {
        Commande commande = findEntity(commandeId);
        if (commande.getStatut() != StatutCommande.EN_ATTENTE) {
            throw new BusinessRuleException("Cette commande a déjà été envoyée en cuisine");
        }
        if (commande.getLignes().isEmpty()) {
            throw new BusinessRuleException("Impossible d'envoyer une commande vide en cuisine");
        }
        commande.setStatut(StatutCommande.EN_PREPARATION);
        Commande saved = repository.save(commande);
        CommandeDto dto = CommandeDto.from(saved);
        publisher.commandeMiseAJour(dto);
        publisher.evenementCuisine(dto);
        journal.enregistrer("RESTAURANT", "VALIDATION", "Commande #" + saved.getId() + " envoyée en cuisine");
        return dto;
    }

    @Transactional
    public CommandeDto updateStatutLigne(Long commandeId, Long ligneId, UpdateLigneStatutRequest req) {
        Commande commande = findEntity(commandeId);
        LigneCommande ligne = commande.getLignes().stream()
                .filter(l -> l.getId().equals(ligneId))
                .findFirst()
                .orElseThrow(() -> ResourceNotFoundException.of("LigneCommande", ligneId));

        StatutLigneCommande statut;
        try {
            statut = StatutLigneCommande.valueOf(req.statut().trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Statut de ligne inconnu : " + req.statut());
        }
        ligne.setStatut(statut);

        boolean toutesServies = commande.getLignes().stream().allMatch(l -> l.getStatut() == StatutLigneCommande.SERVI);
        boolean toutesPretes = commande.getLignes().stream()
                .allMatch(l -> l.getStatut() == StatutLigneCommande.PRET || l.getStatut() == StatutLigneCommande.SERVI);

        if (toutesServies) {
            commande.setStatut(StatutCommande.SERVIE);
            libererTableSiInactive(commande);
        } else if (toutesPretes && commande.getStatut() == StatutCommande.EN_PREPARATION) {
            commande.setStatut(StatutCommande.PRETE);
        }

        Commande saved = repository.save(commande);
        CommandeDto dto = CommandeDto.from(saved);
        publisher.commandeMiseAJour(dto);
        publisher.evenementCuisine(dto);
        return dto;
    }

    @Transactional
    public CommandeDto annuler(Long commandeId, AnnulerCommandeRequest req) {
        Commande commande = findEntity(commandeId);
        if (commande.getStatut() != StatutCommande.EN_ATTENTE) {
            throw new BusinessRuleException("Une commande déjà envoyée en cuisine ne peut plus être annulée");
        }
        commande.setStatut(StatutCommande.ANNULEE);
        commande.setMotifAnnulation(req.motif());
        libererTableSiInactive(commande);

        Commande saved = repository.save(commande);
        CommandeDto dto = CommandeDto.from(saved);
        publisher.commandeMiseAJour(dto);
        journal.enregistrer("RESTAURANT", "ANNULATION", "Commande #" + saved.getId() + " annulée : " + req.motif());
        return dto;
    }

    private void libererTableSiInactive(Commande commande) {
        if (commande.getTable() == null) {
            return;
        }
        TableRestaurant table = commande.getTable();
        List<Commande> autresActives = repository.findByTableIdAndStatutNotIn(table.getId(),
                List.of(StatutCommande.SERVIE, StatutCommande.ANNULEE));
        boolean aucuneAutreActive = autresActives.stream().allMatch(c -> c.getId().equals(commande.getId()));
        if (aucuneAutreActive) {
            table.setStatut(StatutTable.LIBRE);
            tableRepository.save(table);
            publisher.tableMiseAJour(TableRestaurantDto.from(table));
        }
    }

    private Utilisateur currentUtilisateur() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUtilisateur();
        }
        return null;
    }

    private Commande findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Commande", id));
    }
}
