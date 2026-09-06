package com.leconsulat.maquis.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.maquis.dto.*;
import com.leconsulat.maquis.entity.CommandeMaquis;
import com.leconsulat.maquis.entity.LigneCommandeMaquis;
import com.leconsulat.maquis.entity.StatutCommandeMaquis;
import com.leconsulat.maquis.repository.CommandeMaquisRepository;
import com.leconsulat.restaurant.entity.TableRestaurant;
import com.leconsulat.restaurant.repository.TableRestaurantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class CommandeMaquisService {

    private final CommandeMaquisRepository repository;
    private final TableRestaurantRepository tableRepository;
    private final JournalOperationService journal;

    public CommandeMaquisService(CommandeMaquisRepository repository, TableRestaurantRepository tableRepository, JournalOperationService journal) {
        this.repository = repository;
        this.tableRepository = tableRepository;
        this.journal = journal;
    }

    public Page<CommandeMaquisDto> search(StatutCommandeMaquis statut, Pageable pageable) {
        return repository.search(statut, pageable).map(CommandeMaquisDto::from);
    }

    public CommandeMaquisDto get(Long id) {
        return CommandeMaquisDto.from(findEntity(id));
    }

    @Transactional
    public CommandeMaquisDto create(CreateCommandeMaquisRequest req) {
        CommandeMaquis c = new CommandeMaquis();
        c.setClientNom(req.clientNom());
        if (req.tableId() != null) {
            TableRestaurant table = tableRepository.findById(req.tableId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Table", req.tableId()));
            c.setTable(table);
        }
        c.setStatut(StatutCommandeMaquis.EN_COURS);
        return CommandeMaquisDto.from(repository.save(c));
    }

    @Transactional
    public CommandeMaquisDto ajouterLigne(Long commandeId, AddLigneMaquisRequest req) {
        CommandeMaquis c = findEntity(commandeId);
        if (c.getStatut() != StatutCommandeMaquis.EN_COURS) {
            throw new BusinessRuleException("Impossible de modifier une commande maquis déjà clôturée ou annulée");
        }
        LigneCommandeMaquis ligne = new LigneCommandeMaquis();
        ligne.setCommande(c);
        ligne.setArticleNom(req.articleNom());
        ligne.setQuantite(req.quantite());
        ligne.setPrixUnitaire(req.prixUnitaire());
        ligne.setMontant(req.prixUnitaire().multiply(BigDecimal.valueOf(req.quantite())));
        c.getLignes().add(ligne);
        return CommandeMaquisDto.from(repository.save(c));
    }

    @Transactional
    public CommandeMaquisDto retirerLigne(Long commandeId, Long ligneId) {
        CommandeMaquis c = findEntity(commandeId);
        if (c.getStatut() != StatutCommandeMaquis.EN_COURS) {
            throw new BusinessRuleException("Impossible de modifier une commande maquis déjà clôturée ou annulée");
        }
        boolean removed = c.getLignes().removeIf(l -> l.getId().equals(ligneId));
        if (!removed) {
            throw ResourceNotFoundException.of("LigneCommandeMaquis", ligneId);
        }
        return CommandeMaquisDto.from(repository.save(c));
    }

    @Transactional
    public CommandeMaquisDto cloturer(Long id) {
        CommandeMaquis c = findEntity(id);
        if (c.getStatut() != StatutCommandeMaquis.EN_COURS) {
            throw new BusinessRuleException("Cette commande maquis est déjà clôturée ou annulée");
        }
        c.setStatut(StatutCommandeMaquis.CLOTUREE);
        CommandeMaquis saved = repository.save(c);
        journal.enregistrer("MAQUIS", "VALIDATION", "Clôture de la commande maquis #" + saved.getId());
        return CommandeMaquisDto.from(saved);
    }

    @Transactional
    public CommandeMaquisDto annuler(Long id) {
        CommandeMaquis c = findEntity(id);
        if (c.getStatut() != StatutCommandeMaquis.EN_COURS) {
            throw new BusinessRuleException("Cette commande maquis est déjà clôturée ou annulée");
        }
        c.setStatut(StatutCommandeMaquis.ANNULEE);
        return CommandeMaquisDto.from(repository.save(c));
    }

    private CommandeMaquis findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("CommandeMaquis", id));
    }
}
