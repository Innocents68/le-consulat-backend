package com.leconsulat.cave.service;

import com.leconsulat.cave.dto.CreateMouvementCaveRequest;
import com.leconsulat.cave.dto.MouvementCaveDto;
import com.leconsulat.cave.entity.Boisson;
import com.leconsulat.cave.entity.MouvementCave;
import com.leconsulat.cave.repository.BoissonRepository;
import com.leconsulat.cave.repository.MouvementCaveRepository;
import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Records cave stock movements. A by-the-glass sale is converted to its bottle-equivalent
 * (1 bottle = {@link Boisson#VERRES_PAR_BOUTEILLE} glasses) before decrementing stock, so
 * bottle and glass sales share the same underlying quantiteStock unit — cf. cahier des
 * charges §2.3 "vente possible à la bouteille ou au verre (décrémentation proportionnelle)".
 */
@Service
@Transactional
public class MouvementCaveService {

    private final MouvementCaveRepository repository;
    private final BoissonRepository boissonRepository;
    private final JournalOperationService journal;

    public MouvementCaveService(MouvementCaveRepository repository, BoissonRepository boissonRepository, JournalOperationService journal) {
        this.repository = repository;
        this.boissonRepository = boissonRepository;
        this.journal = journal;
    }

    public Page<MouvementCaveDto> search(Long boissonId, LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable) {
        return repository.search(boissonId, dateDebut, dateFin, pageable).map(MouvementCaveDto::from);
    }

    @Transactional
    public MouvementCaveDto create(CreateMouvementCaveRequest req) {
        Boisson boisson = boissonRepository.findById(req.boissonId())
                .orElseThrow(() -> ResourceNotFoundException.of("Boisson", req.boissonId()));

        MouvementCave.Type type = parseEnum(MouvementCave.Type.class, req.type(), "type de mouvement");
        MouvementCave.Motif motif = parseEnum(MouvementCave.Motif.class, req.motif(), "motif");
        MouvementCave.UniteVente unite = (req.uniteVente() == null || req.uniteVente().isBlank())
                ? MouvementCave.UniteVente.BOUTEILLE
                : parseEnum(MouvementCave.UniteVente.class, req.uniteVente(), "unité de vente");

        BigDecimal quantiteEnBouteilles = unite == MouvementCave.UniteVente.VERRE
                ? req.quantite().divide(BigDecimal.valueOf(Boisson.VERRES_PAR_BOUTEILLE), 2, RoundingMode.HALF_UP)
                : req.quantite();

        if (type == MouvementCave.Type.SORTIE) {
            if (boisson.getQuantiteStock().compareTo(quantiteEnBouteilles) < 0) {
                throw new BusinessRuleException("Stock insuffisant pour " + boisson.getNom());
            }
            boisson.setQuantiteStock(boisson.getQuantiteStock().subtract(quantiteEnBouteilles));
        } else {
            boisson.setQuantiteStock(boisson.getQuantiteStock().add(quantiteEnBouteilles));
        }
        boissonRepository.save(boisson);

        MouvementCave m = new MouvementCave();
        m.setBoisson(boisson);
        m.setType(type);
        m.setMotif(motif);
        m.setQuantite(req.quantite());
        m.setUniteVente(unite);
        m.setDate(LocalDateTime.now());
        MouvementCave saved = repository.save(m);

        journal.enregistrer("CAVE", type.name(), type + " " + req.quantite() + " " + unite + " de " + boisson.getNom() + " (" + motif + ")");

        if (boisson.getQuantiteStock().compareTo(BigDecimal.valueOf(boisson.getSeuilAlerte())) <= 0) {
            journal.enregistrer("CAVE", "ALERTE_STOCK", "Seuil d'alerte atteint pour " + boisson.getNom()
                    + " (stock=" + boisson.getQuantiteStock() + ")");
        }

        return MouvementCaveDto.from(saved);
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, String label) {
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Valeur invalide pour " + label + " : " + value);
        }
    }
}
