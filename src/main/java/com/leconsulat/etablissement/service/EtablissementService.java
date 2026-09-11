package com.leconsulat.etablissement.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BadRequestException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.etablissement.dto.CreateEtablissementRequest;
import com.leconsulat.etablissement.dto.EtablissementDto;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EtablissementService {

    private final EtablissementRepository repository;
    private final JournalOperationService journal;

    public EtablissementService(EtablissementRepository repository, JournalOperationService journal) {
        this.repository = repository;
        this.journal = journal;
    }

    public List<EtablissementDto> list(boolean actifsSeulement) {
        List<Etablissement> etablissements = actifsSeulement ? repository.findByActifTrue() : repository.findAll();
        return etablissements.stream().map(EtablissementDto::from).toList();
    }

    @Transactional
    public EtablissementDto create(CreateEtablissementRequest req) {
        if (repository.existsByCode(req.code())) {
            throw new BadRequestException("Ce code d'établissement existe déjà");
        }
        Etablissement e = new Etablissement();
        e.setCode(req.code().trim().toUpperCase());
        e.setNom(req.nom());
        e.setActif(true);
        Etablissement saved = repository.save(e);
        journal.enregistrer("ETABLISSEMENTS", "CREATION", "Création de l'établissement " + saved.getNom());
        return EtablissementDto.from(saved);
    }

    @Transactional
    public EtablissementDto toggleActif(Long id) {
        Etablissement e = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Etablissement", id));
        e.setActif(!e.isActif());
        Etablissement saved = repository.save(e);
        journal.enregistrer("ETABLISSEMENTS", "MODIFICATION", "Établissement " + saved.getNom() + " -> actif=" + saved.isActif());
        return EtablissementDto.from(saved);
    }
}
