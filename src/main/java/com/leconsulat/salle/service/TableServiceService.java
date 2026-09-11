package com.leconsulat.salle.service;

import com.leconsulat.common.exception.BadRequestException;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.salle.dto.CreateTableRequest;
import com.leconsulat.salle.dto.TableServiceDto;
import com.leconsulat.salle.dto.UpdateTableRequest;
import com.leconsulat.salle.entity.StatutTable;
import com.leconsulat.salle.entity.TableService;
import com.leconsulat.salle.repository.TableServiceRepository;
import com.leconsulat.security.PerimetreGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TableServiceService {

    private final TableServiceRepository repository;
    private final EtablissementRepository etablissementRepository;
    private final PerimetreGuard perimetreGuard;

    public TableServiceService(TableServiceRepository repository, EtablissementRepository etablissementRepository,
                                PerimetreGuard perimetreGuard) {
        this.repository = repository;
        this.etablissementRepository = etablissementRepository;
        this.perimetreGuard = perimetreGuard;
    }

    public List<TableServiceDto> list(Long etablissementIdDemande) {
        Etablissement demande = etablissementIdDemande != null
                ? etablissementRepository.findById(etablissementIdDemande).orElse(null)
                : null;
        Etablissement cible = perimetreGuard.scopeEtablissement(demande);
        List<TableService> tables = cible != null
                ? repository.findByEtablissementAndActifTrue(cible)
                : repository.findAll();
        return tables.stream().map(TableServiceDto::from).toList();
    }

    @Transactional
    public TableServiceDto create(CreateTableRequest req) {
        Etablissement cible = resoudreEtablissementCible(req.etablissementId());
        if (repository.existsByEtablissementAndNumeroIgnoreCase(cible, req.numero())) {
            throw new BadRequestException("Cette table existe déjà pour cet établissement");
        }
        TableService t = new TableService();
        t.setEtablissement(cible);
        t.setNumero(req.numero());
        t.setCapacite(req.capacite());
        t.setZone(req.zone());
        t.setStatut(StatutTable.LIBRE);
        t.setActif(true);
        return TableServiceDto.from(repository.save(t));
    }

    @Transactional
    public TableServiceDto update(Long id, UpdateTableRequest req) {
        TableService t = findEntityChecked(id);
        t.setNumero(req.numero());
        t.setCapacite(req.capacite());
        t.setZone(req.zone());
        return TableServiceDto.from(repository.save(t));
    }

    @Transactional
    public void delete(Long id) {
        TableService t = findEntityChecked(id);
        if (t.getStatut() != StatutTable.LIBRE) {
            throw new BusinessRuleException("Impossible de supprimer une table qui n'est pas libre");
        }
        repository.delete(t);
    }

    @Transactional
    public void toggleActif(Long id) {
        TableService t = findEntityChecked(id);
        if (t.isActif() && t.getStatut() != StatutTable.LIBRE) {
            throw new BusinessRuleException("Impossible de désactiver une table qui n'est pas libre");
        }
        t.setActif(!t.isActif());
        repository.save(t);
    }

    private Etablissement resoudreEtablissementCible(Long etablissementIdDemande) {
        Etablissement demande = etablissementIdDemande != null
                ? etablissementRepository.findById(etablissementIdDemande)
                        .orElseThrow(() -> ResourceNotFoundException.of("Etablissement", etablissementIdDemande))
                : null;
        Etablissement cible = perimetreGuard.scopeEtablissement(demande);
        if (cible == null) {
            throw new BusinessRuleException("L'établissement est obligatoire");
        }
        return cible;
    }

    private TableService findEntityChecked(Long id) {
        TableService t = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Table", id));
        if (!perimetreGuard.aAcces(t.getEtablissement())) {
            throw ResourceNotFoundException.of("Table", id);
        }
        return t;
    }
}
