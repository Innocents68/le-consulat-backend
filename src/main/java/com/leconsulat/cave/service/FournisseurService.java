package com.leconsulat.cave.service;

import com.leconsulat.cave.dto.FournisseurDto;
import com.leconsulat.cave.entity.Fournisseur;
import com.leconsulat.cave.repository.FournisseurRepository;
import com.leconsulat.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FournisseurService {

    private final FournisseurRepository repository;

    public FournisseurService(FournisseurRepository repository) {
        this.repository = repository;
    }

    public List<FournisseurDto> list() {
        return repository.findAll().stream().map(FournisseurDto::from).toList();
    }

    @Transactional
    public FournisseurDto create(FournisseurDto dto) {
        Fournisseur f = new Fournisseur();
        apply(f, dto);
        f.setActif(true);
        return FournisseurDto.from(repository.save(f));
    }

    @Transactional
    public FournisseurDto update(Long id, FournisseurDto dto) {
        Fournisseur f = findEntity(id);
        apply(f, dto);
        return FournisseurDto.from(repository.save(f));
    }

    @Transactional
    public void archive(Long id) {
        Fournisseur f = findEntity(id);
        f.setActif(false);
        repository.save(f);
    }

    private void apply(Fournisseur f, FournisseurDto dto) {
        f.setNom(dto.nom());
        f.setContact(dto.contact());
        f.setTelephone(dto.telephone());
        f.setEmail(dto.email());
        f.setConditions(dto.conditions());
    }

    private Fournisseur findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Fournisseur", id));
    }
}
