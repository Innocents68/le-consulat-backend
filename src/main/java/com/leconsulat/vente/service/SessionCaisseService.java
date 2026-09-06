package com.leconsulat.vente.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.finance.entity.Depense;
import com.leconsulat.finance.repository.DepenseRepository;
import com.leconsulat.finance.service.RapportCaisseService;
import com.leconsulat.security.CustomUserDetails;
import com.leconsulat.utilisateur.entity.Utilisateur;
import com.leconsulat.vente.dto.ClotureSessionRequest;
import com.leconsulat.vente.dto.OuvrirSessionRequest;
import com.leconsulat.vente.dto.SessionCaisseDto;
import com.leconsulat.vente.entity.ModePaiement;
import com.leconsulat.vente.entity.SessionCaisse;
import com.leconsulat.vente.entity.StatutVente;
import com.leconsulat.vente.entity.Vente;
import com.leconsulat.vente.repository.SessionCaisseRepository;
import com.leconsulat.vente.repository.VenteRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Ouverture/clôture de caisse (§2.1). La clôture calcule l'écart théorique/réel et
 * génère automatiquement le RapportCaisse (Z) correspondant — API_CONTRACT.md §3 & §6.
 */
@Service
@Transactional
public class SessionCaisseService {

    private final SessionCaisseRepository repository;
    private final VenteRepository venteRepository;
    private final DepenseRepository depenseRepository;
    private final RapportCaisseService rapportCaisseService;
    private final JournalOperationService journal;

    public SessionCaisseService(SessionCaisseRepository repository, VenteRepository venteRepository,
                                 DepenseRepository depenseRepository, RapportCaisseService rapportCaisseService,
                                 JournalOperationService journal) {
        this.repository = repository;
        this.venteRepository = venteRepository;
        this.depenseRepository = depenseRepository;
        this.rapportCaisseService = rapportCaisseService;
        this.journal = journal;
    }

    public List<SessionCaisseDto> list(SessionCaisse.Statut statut) {
        List<SessionCaisse> sessions = statut != null ? repository.findByStatut(statut) : repository.findAll();
        return sessions.stream().map(SessionCaisseDto::from).toList();
    }

    public SessionCaisseDto get(Long id) {
        return SessionCaisseDto.from(findEntity(id));
    }

    @Transactional
    public SessionCaisseDto ouvrir(OuvrirSessionRequest req) {
        SessionCaisse s = new SessionCaisse();
        s.setCaisseNom(req.caisseNom());
        s.setFondInitial(req.fondInitial());
        s.setOuvertPar(currentUtilisateur());
        s.setStatut(SessionCaisse.Statut.OUVERTE);
        SessionCaisse saved = repository.save(s);
        journal.enregistrer("VENTES", "CREATION", "Ouverture de la caisse " + saved.getCaisseNom() + " (fond initial " + saved.getFondInitial() + ")");
        return SessionCaisseDto.from(saved);
    }

    @Transactional
    public SessionCaisseDto cloturer(Long id, ClotureSessionRequest req) {
        SessionCaisse s = findEntity(id);
        if (s.getStatut() != SessionCaisse.Statut.OUVERTE) {
            throw new BusinessRuleException("Cette session de caisse est déjà fermée");
        }

        List<Vente> ventesPayees = venteRepository.findBySessionCaisseIdAndStatut(s.getId(), StatutVente.PAYEE);
        BigDecimal totalVentes = ventesPayees.stream().map(Vente::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalEspeces = ventesPayees.stream()
                .filter(v -> v.getModePaiement() == ModePaiement.ESPECES)
                .map(Vente::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal theoriqueEspeces = s.getFondInitial().add(totalEspeces);
        BigDecimal ecart = req.montantReel().subtract(theoriqueEspeces);

        s.setStatut(SessionCaisse.Statut.FERMEE);
        s.setDateFermeture(java.time.LocalDateTime.now());
        s.setTotalVentes(totalVentes);
        s.setTotalEncaissements(totalVentes);
        s.setEcart(ecart);
        SessionCaisse saved = repository.save(s);

        LocalDate today = LocalDate.now();
        BigDecimal decaissements = depenseRepository.findByStatutAndDateBetween(Depense.Statut.VALIDEE,
                        saved.getDateOuverture().toLocalDate(), today).stream()
                .map(Depense::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
        rapportCaisseService.generer(saved.getId(), totalVentes, decaissements);

        journal.enregistrer("VENTES", "VALIDATION", "Clôture de la caisse " + saved.getCaisseNom()
                + " : écart=" + ecart + " (rapport de caisse généré)");

        return SessionCaisseDto.from(saved);
    }

    private Utilisateur currentUtilisateur() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUtilisateur();
        }
        return null;
    }

    private SessionCaisse findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("SessionCaisse", id));
    }
}
