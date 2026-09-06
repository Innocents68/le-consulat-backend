package com.leconsulat.finance.service;

import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.finance.dto.CategorieDepenseDto;
import com.leconsulat.finance.entity.CategorieDepense;
import com.leconsulat.finance.repository.CategorieDepenseRepository;
import com.leconsulat.finance.repository.DepenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategorieDepenseService {

    private final CategorieDepenseRepository repository;
    private final DepenseRepository depenseRepository;

    public CategorieDepenseService(CategorieDepenseRepository repository, DepenseRepository depenseRepository) {
        this.repository = repository;
        this.depenseRepository = depenseRepository;
    }

    public List<CategorieDepenseDto> list() {
        return repository.findAll().stream().map(CategorieDepenseDto::from).toList();
    }

    @Transactional
    public CategorieDepenseDto create(CategorieDepenseDto dto) {
        return CategorieDepenseDto.from(repository.save(new CategorieDepense(dto.nom())));
    }

    @Transactional
    public CategorieDepenseDto update(Long id, CategorieDepenseDto dto) {
        CategorieDepense c = findEntity(id);
        c.setNom(dto.nom());
        return CategorieDepenseDto.from(repository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        if (depenseRepository.existsByCategorieId(id)) {
            throw new BusinessRuleException("Impossible de supprimer une catégorie de dépense utilisée");
        }
        repository.delete(findEntity(id));
    }

    private CategorieDepense findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("CategorieDepense", id));
    }
}
