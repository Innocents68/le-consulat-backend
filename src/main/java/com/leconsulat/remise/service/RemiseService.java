package com.leconsulat.remise.service;

import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.remise.dto.CreateRemiseRequest;
import com.leconsulat.remise.dto.RemiseDto;
import com.leconsulat.remise.entity.Remise;
import com.leconsulat.remise.entity.TypeRemise;
import com.leconsulat.remise.repository.RemiseRepository;
import com.leconsulat.security.PerimetreGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class RemiseService {

    private final RemiseRepository repository;
    private final EtablissementRepository etablissementRepository;
    private final PerimetreGuard perimetreGuard;

    public RemiseService(RemiseRepository repository, EtablissementRepository etablissementRepository,
                          PerimetreGuard perimetreGuard) {
        this.repository = repository;
        this.etablissementRepository = etablissementRepository;
        this.perimetreGuard = perimetreGuard;
    }

    public List<RemiseDto> list(Long etablissementIdDemande) {
        Etablissement demande = etablissementIdDemande != null
                ? etablissementRepository.findById(etablissementIdDemande).orElse(null)
                : null;
        Etablissement cible = perimetreGuard.scopeEtablissement(demande);
        List<Remise> remises = cible != null ? repository.findByEtablissementAndActifTrue(cible) : repository.findAll();
        return remises.stream().map(RemiseDto::from).toList();
    }

    @Transactional
    public RemiseDto create(CreateRemiseRequest req) {
        Etablissement cible = resoudreEtablissementCible(req.etablissementId());
        TypeRemise type = parseType(req.type());
        if (type == TypeRemise.POURCENTAGE && req.valeur().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessRuleException("Une remise en pourcentage ne peut pas dépasser 100");
        }
        Remise r = new Remise();
        r.setLibelle(req.libelle());
        r.setType(type);
        r.setValeur(req.valeur());
        r.setDateDebut(req.dateDebut());
        r.setDateFin(req.dateFin());
        r.setHeureDebut(req.heureDebut());
        r.setHeureFin(req.heureFin());
        r.setEtablissement(cible);
        r.setActif(true);
        return RemiseDto.from(repository.save(r));
    }

    @Transactional
    public void toggleActif(Long id) {
        Remise r = findEntityChecked(id);
        r.setActif(!r.isActif());
        repository.save(r);
    }

    private TypeRemise parseType(String type) {
        try {
            return TypeRemise.valueOf(type.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Type de remise inconnu : " + type);
        }
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

    private Remise findEntityChecked(Long id) {
        Remise r = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Remise", id));
        if (!perimetreGuard.aAcces(r.getEtablissement())) {
            throw ResourceNotFoundException.of("Remise", id);
        }
        return r;
    }

    /** Utilisé par {@code CommandeService.appliquerRemise} — pas de contrôle d'accès ici, déjà
     * fait par l'appelant sur la commande elle-même (même établissement obligatoire vérifié
     * là-bas). */
    public Remise findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Remise", id));
    }
}
