package com.leconsulat.restaurant.service;

import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.restaurant.dto.CategoriePlatDto;
import com.leconsulat.restaurant.entity.CategoriePlat;
import com.leconsulat.restaurant.repository.CategoriePlatRepository;
import com.leconsulat.restaurant.repository.PlatRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoriePlatService {

    private final CategoriePlatRepository repository;
    private final PlatRepository platRepository;

    public CategoriePlatService(CategoriePlatRepository repository, PlatRepository platRepository) {
        this.repository = repository;
        this.platRepository = platRepository;
    }

    public List<CategoriePlatDto> list() {
        return repository.findAllByOrderByOrdreAsc().stream().map(CategoriePlatDto::from).toList();
    }

    @Transactional
    public CategoriePlatDto create(CategoriePlatDto dto) {
        CategoriePlat c = new CategoriePlat();
        c.setNom(dto.nom());
        c.setOrdre(dto.ordre());
        return CategoriePlatDto.from(repository.save(c));
    }

    @Transactional
    public CategoriePlatDto update(Long id, CategoriePlatDto dto) {
        CategoriePlat c = findEntity(id);
        c.setNom(dto.nom());
        c.setOrdre(dto.ordre());
        return CategoriePlatDto.from(repository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        CategoriePlat c = findEntity(id);
        boolean vide = platRepository.search(id, null, null, null, Pageable.unpaged()).isEmpty();
        if (!vide) {
            throw new BusinessRuleException("Impossible de supprimer une catégorie non vide");
        }
        repository.delete(c);
    }

    private CategoriePlat findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("CategoriePlat", id));
    }
}
