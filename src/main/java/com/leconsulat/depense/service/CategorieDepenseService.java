package com.leconsulat.depense.service;

import com.leconsulat.common.exception.BadRequestException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.depense.dto.CategorieDepenseDto;
import com.leconsulat.depense.dto.CreateCategorieDepenseRequest;
import com.leconsulat.depense.dto.UpdateCategorieDepenseRequest;
import com.leconsulat.depense.entity.CategorieDepense;
import com.leconsulat.depense.repository.CategorieDepenseRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** EF-036 : référentiel partagé, paramétrable exclusivement par le Super Administrateur — mais
 * la liste reste lisible des deux profils (un Gérant/Caissier doit pouvoir en choisir une pour
 * sa dépense, RG-090). */
@Service
@Transactional
public class CategorieDepenseService {

    private final CategorieDepenseRepository repository;

    public CategorieDepenseService(CategorieDepenseRepository repository) {
        this.repository = repository;
    }

    public List<CategorieDepenseDto> list(Boolean actif) {
        List<CategorieDepense> categories = actif == null || actif ? repository.findByActifTrue() : repository.findAll();
        return categories.stream().map(CategorieDepenseDto::from).toList();
    }

    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    @Transactional
    public CategorieDepenseDto create(CreateCategorieDepenseRequest req) {
        if (repository.existsByNomIgnoreCase(req.nom())) {
            throw new BadRequestException("Cette catégorie existe déjà");
        }
        CategorieDepense c = new CategorieDepense();
        c.setNom(req.nom());
        c.setActif(true);
        return CategorieDepenseDto.from(repository.save(c));
    }

    /** §6.10.1 : « renommage » explicitement listé — réservé au Super Administrateur comme
     * la création (EF-036). */
    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    @Transactional
    public CategorieDepenseDto update(Long id, UpdateCategorieDepenseRequest req) {
        CategorieDepense c = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("CategorieDepense", id));
        if (!c.getNom().equalsIgnoreCase(req.nom()) && repository.existsByNomIgnoreCase(req.nom())) {
            throw new BadRequestException("Cette catégorie existe déjà");
        }
        c.setNom(req.nom());
        return CategorieDepenseDto.from(repository.save(c));
    }

    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    @Transactional
    public void toggleActif(Long id) {
        CategorieDepense c = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("CategorieDepense", id));
        c.setActif(!c.isActif());
        repository.save(c);
    }
}
