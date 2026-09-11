package com.leconsulat.stock.service;

import com.leconsulat.common.exception.BadRequestException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.stock.dto.CreateFournisseurRequest;
import com.leconsulat.stock.dto.FournisseurDto;
import com.leconsulat.stock.entity.Fournisseur;
import com.leconsulat.stock.repository.FournisseurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Référentiel partagé (§6.6.4) — pas de cloisonnement établissement, ouvert aux deux profils. */
@Service
@Transactional
public class FournisseurService {

    private final FournisseurRepository repository;

    public FournisseurService(FournisseurRepository repository) {
        this.repository = repository;
    }

    public List<FournisseurDto> list(Boolean actif) {
        List<Fournisseur> fournisseurs = actif == null || actif ? repository.findByActifTrue() : repository.findAll();
        return fournisseurs.stream().map(FournisseurDto::from).toList();
    }

    @Transactional
    public FournisseurDto create(CreateFournisseurRequest req) {
        if (repository.existsByNomIgnoreCase(req.nom())) {
            throw new BadRequestException("Ce fournisseur existe déjà");
        }
        Fournisseur f = new Fournisseur();
        f.setNom(req.nom());
        f.setTelephone(req.telephone());
        f.setActif(true);
        return FournisseurDto.from(repository.save(f));
    }

    @Transactional
    public void toggleActif(Long id) {
        Fournisseur f = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Fournisseur", id));
        f.setActif(!f.isActif());
        repository.save(f);
    }
}
