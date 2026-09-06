package com.leconsulat.vente.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.security.CustomUserDetails;
import com.leconsulat.utilisateur.entity.Utilisateur;
import com.leconsulat.vente.dto.AvoirDto;
import com.leconsulat.vente.dto.CreateAvoirRequest;
import com.leconsulat.vente.entity.Avoir;
import com.leconsulat.vente.entity.StatutVente;
import com.leconsulat.vente.entity.Vente;
import com.leconsulat.vente.repository.AvoirRepository;
import com.leconsulat.vente.repository.VenteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Émission d'avoirs — cahier des charges §2.1 : "Validation par un profil habilité" ;
 * un avoir n'est jamais supprimé, seulement annulé (traçabilité conservée).
 */
@Service
@Transactional
public class AvoirService {

    private final AvoirRepository repository;
    private final VenteRepository venteRepository;
    private final JournalOperationService journal;

    public AvoirService(AvoirRepository repository, VenteRepository venteRepository, JournalOperationService journal) {
        this.repository = repository;
        this.venteRepository = venteRepository;
        this.journal = journal;
    }

    public Page<AvoirDto> search(LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable) {
        return repository.search(dateDebut, dateFin, pageable).map(AvoirDto::from);
    }

    @Transactional
    public AvoirDto create(CreateAvoirRequest req) {
        Vente vente = venteRepository.findById(req.venteId())
                .orElseThrow(() -> ResourceNotFoundException.of("Vente", req.venteId()));
        if (vente.getStatut() != StatutVente.PAYEE) {
            throw new BusinessRuleException("Un avoir ne peut être émis que pour une vente payée");
        }
        Avoir a = new Avoir();
        a.setVente(vente);
        a.setMotif(req.motif());
        a.setMontant(vente.getTotal());
        a.setStatut(Avoir.Statut.VALIDE);
        a.setDateEmission(LocalDateTime.now());
        a.setValidePar(currentUtilisateur());
        Avoir saved = repository.save(a);
        journal.enregistrer("VENTES", "VALIDATION", "Émission d'un avoir pour la vente " + vente.getNumero() + " : " + req.motif());
        return AvoirDto.from(saved);
    }

    @Transactional
    public AvoirDto annuler(Long id) {
        Avoir a = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Avoir", id));
        if (a.getStatut() == Avoir.Statut.ANNULE) {
            throw new BusinessRuleException("Cet avoir est déjà annulé");
        }
        a.setStatut(Avoir.Statut.ANNULE);
        Avoir saved = repository.save(a);
        journal.enregistrer("VENTES", "ANNULATION", "Annulation de l'avoir #" + saved.getId());
        return AvoirDto.from(saved);
    }

    private Utilisateur currentUtilisateur() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUtilisateur();
        }
        return null;
    }
}
