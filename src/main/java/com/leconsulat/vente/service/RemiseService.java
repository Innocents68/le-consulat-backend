package com.leconsulat.vente.service;

import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.vente.dto.RemiseDto;
import com.leconsulat.vente.entity.Remise;
import com.leconsulat.vente.repository.RemiseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RemiseService {

    private final RemiseRepository repository;

    public RemiseService(RemiseRepository repository) {
        this.repository = repository;
    }

    public List<RemiseDto> list() {
        return repository.findAll().stream().map(RemiseDto::from).toList();
    }

    @Transactional
    public RemiseDto create(RemiseDto dto) {
        Remise r = new Remise();
        apply(r, dto);
        r.setActif(true);
        return RemiseDto.from(repository.save(r));
    }

    @Transactional
    public RemiseDto update(Long id, RemiseDto dto) {
        Remise r = findEntity(id);
        apply(r, dto);
        return RemiseDto.from(repository.save(r));
    }

    /** Deactivation only — history is preserved even after a discount is no longer offered. */
    @Transactional
    public void desactiver(Long id) {
        Remise r = findEntity(id);
        r.setActif(false);
        repository.save(r);
    }

    private void apply(Remise r, RemiseDto dto) {
        r.setNom(dto.nom());
        r.setType(Remise.Type.valueOf(dto.type().trim().toUpperCase()));
        r.setValeur(dto.valeur());
        r.setDateDebut(dto.dateDebut());
        r.setDateFin(dto.dateFin());
    }

    private Remise findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Remise", id));
    }
}
