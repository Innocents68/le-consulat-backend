package com.leconsulat.stock.service;

import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.stock.dto.CategorieProduitDto;
import com.leconsulat.stock.entity.CategorieProduit;
import com.leconsulat.stock.repository.CategorieProduitRepository;
import com.leconsulat.stock.repository.ProduitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategorieProduitService {

    private final CategorieProduitRepository repository;
    private final ProduitRepository produitRepository;

    public CategorieProduitService(CategorieProduitRepository repository, ProduitRepository produitRepository) {
        this.repository = repository;
        this.produitRepository = produitRepository;
    }

    public List<CategorieProduitDto> list() {
        return repository.findAll().stream().map(CategorieProduitDto::from).toList();
    }

    @Transactional
    public CategorieProduitDto create(CategorieProduitDto dto) {
        CategorieProduit c = new CategorieProduit(dto.nom());
        return CategorieProduitDto.from(repository.save(c));
    }

    @Transactional
    public CategorieProduitDto update(Long id, CategorieProduitDto dto) {
        CategorieProduit c = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("CategorieProduit", id));
        c.setNom(dto.nom());
        return CategorieProduitDto.from(repository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        CategorieProduit c = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("CategorieProduit", id));
        boolean used = produitRepository.findAll().stream().anyMatch(p -> p.getCategorie() != null && p.getCategorie().getId().equals(id));
        if (used) {
            throw new BusinessRuleException("Impossible de supprimer une catégorie utilisée par des produits");
        }
        repository.delete(c);
    }
}
