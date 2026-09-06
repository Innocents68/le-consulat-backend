package com.leconsulat.restaurant.service;

import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.restaurant.dto.PlatDto;
import com.leconsulat.restaurant.dto.PlatRequest;
import com.leconsulat.restaurant.entity.CategoriePlat;
import com.leconsulat.restaurant.entity.Plat;
import com.leconsulat.restaurant.repository.CategoriePlatRepository;
import com.leconsulat.restaurant.repository.PlatRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@Transactional
public class PlatService {

    private final PlatRepository repository;
    private final CategoriePlatRepository categorieRepository;

    public PlatService(PlatRepository repository, CategoriePlatRepository categorieRepository) {
        this.repository = repository;
        this.categorieRepository = categorieRepository;
    }

    public Page<PlatDto> search(Long categorieId, Boolean actif, Boolean disponible, String search, Pageable pageable) {
        return repository.search(categorieId, actif, disponible, search, pageable).map(PlatDto::from);
    }

    public PlatDto get(Long id) {
        return PlatDto.from(findEntity(id));
    }

    Plat findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Plat", id));
    }

    @Transactional
    public PlatDto create(PlatRequest req) {
        Plat p = new Plat();
        apply(p, req);
        p.setActif(true);
        return PlatDto.from(repository.save(p));
    }

    @Transactional
    public PlatDto update(Long id, PlatRequest req) {
        Plat p = findEntity(id);
        apply(p, req);
        return PlatDto.from(repository.save(p));
    }

    @Transactional
    public void archive(Long id) {
        Plat p = findEntity(id);
        p.setActif(false);
        repository.save(p);
    }

    private void apply(Plat p, PlatRequest req) {
        p.setNom(req.nom());
        p.setPrix(req.prix());
        p.setDescription(req.description());
        p.setTempsPreparation(req.tempsPreparation());
        if (req.disponible() != null) {
            p.setDisponible(req.disponible());
        }
        p.setIngredients(req.ingredients() != null ? req.ingredients() : new ArrayList<>());
        if (req.categorieId() != null) {
            CategoriePlat cat = categorieRepository.findById(req.categorieId())
                    .orElseThrow(() -> ResourceNotFoundException.of("CategoriePlat", req.categorieId()));
            p.setCategorie(cat);
        } else {
            p.setCategorie(null);
        }
    }
}
