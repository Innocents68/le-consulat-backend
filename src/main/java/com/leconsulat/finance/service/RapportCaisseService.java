package com.leconsulat.finance.service;

import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.finance.dto.RapportCaisseDto;
import com.leconsulat.finance.entity.RapportCaisse;
import com.leconsulat.finance.repository.RapportCaisseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class RapportCaisseService {

    private final RapportCaisseRepository repository;

    public RapportCaisseService(RapportCaisseRepository repository) {
        this.repository = repository;
    }

    public List<RapportCaisseDto> list() {
        return repository.findAll().stream().map(RapportCaisseDto::from).toList();
    }

    public RapportCaisseDto get(Long id) {
        return RapportCaisseDto.from(findEntity(id));
    }

    public RapportCaisse findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("RapportCaisse", id));
    }

    /** Auto-generated when a cash session is closed — cf. API_CONTRACT.md §3 & §6. */
    @Transactional
    public RapportCaisse generer(Long sessionCaisseId, BigDecimal encaissements, BigDecimal decaissements) {
        RapportCaisse r = new RapportCaisse();
        r.setSessionCaisseId(sessionCaisseId);
        r.setEncaissements(encaissements);
        r.setDecaissements(decaissements);
        r.setSolde(encaissements.subtract(decaissements));
        return repository.save(r);
    }
}
