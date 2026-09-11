package com.leconsulat.catalogue.service;

import com.leconsulat.catalogue.dto.CategorieDto;
import com.leconsulat.catalogue.dto.CreateCategorieRequest;
import com.leconsulat.catalogue.entity.Categorie;
import com.leconsulat.catalogue.repository.CategorieRepository;
import com.leconsulat.common.exception.BadRequestException;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.security.PerimetreGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategorieService {

    private final CategorieRepository repository;
    private final EtablissementRepository etablissementRepository;
    private final PerimetreGuard perimetreGuard;

    public CategorieService(CategorieRepository repository, EtablissementRepository etablissementRepository,
                             PerimetreGuard perimetreGuard) {
        this.repository = repository;
        this.etablissementRepository = etablissementRepository;
        this.perimetreGuard = perimetreGuard;
    }

    public List<CategorieDto> list(Long etablissementIdDemande) {
        Etablissement demande = etablissementIdDemande != null
                ? etablissementRepository.findById(etablissementIdDemande).orElse(null)
                : null;
        Etablissement cible = perimetreGuard.scopeEtablissement(demande);
        List<Categorie> categories = cible != null
                ? repository.findByEtablissementAndActifTrue(cible)
                : repository.findAll();
        return categories.stream().map(CategorieDto::from).toList();
    }

    @Transactional
    public CategorieDto create(CreateCategorieRequest req) {
        Etablissement cible = resoudreEtablissementCible(req.etablissementId());
        if (repository.existsByEtablissementAndNomIgnoreCase(cible, req.nom())) {
            throw new BadRequestException("Cette catégorie existe déjà pour cet établissement");
        }
        Categorie c = new Categorie();
        c.setNom(req.nom());
        c.setEtablissement(cible);
        c.setActif(true);
        return CategorieDto.from(repository.save(c));
    }

    @Transactional
    public void toggleActif(Long id) {
        Categorie c = findEntityChecked(id);
        c.setActif(!c.isActif());
        repository.save(c);
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

    private Categorie findEntityChecked(Long id) {
        Categorie c = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Categorie", id));
        if (!perimetreGuard.aAcces(c.getEtablissement())) {
            throw ResourceNotFoundException.of("Categorie", id);
        }
        return c;
    }
}
