package com.leconsulat.vente.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.parametres.service.ParametresService;
import com.leconsulat.security.PerimetreGuard;
import com.leconsulat.vente.dto.FactureDto;
import com.leconsulat.vente.entity.Facture;
import com.leconsulat.vente.repository.FactureRepository;
import com.leconsulat.vente.util.TicketGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Consultation d'un document immuable (RG-052) — aucune méthode de modification n'existe ici
 * volontairement, seule la réimpression est possible et elle ne change pas le contenu. */
@Service
@Transactional
public class FactureService {

    private final FactureRepository repository;
    private final EtablissementRepository etablissementRepository;
    private final PerimetreGuard perimetreGuard;
    private final JournalOperationService journal;
    private final ParametresService parametresService;

    public FactureService(FactureRepository repository, EtablissementRepository etablissementRepository,
                           PerimetreGuard perimetreGuard, JournalOperationService journal, ParametresService parametresService) {
        this.repository = repository;
        this.etablissementRepository = etablissementRepository;
        this.perimetreGuard = perimetreGuard;
        this.journal = journal;
        this.parametresService = parametresService;
    }

    public Page<FactureDto> search(Long etablissementIdDemande, String search, Long commandeId,
                                    LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable) {
        Etablissement demande = etablissementIdDemande != null
                ? etablissementRepository.findById(etablissementIdDemande).orElse(null)
                : null;
        Etablissement cible = perimetreGuard.scopeEtablissement(demande);
        if (cible == null) {
            throw new BusinessRuleException("Précisez un établissement");
        }
        return repository.search(cible, search, commandeId, dateDebut, dateFin, pageable).map(FactureDto::from);
    }

    public FactureDto get(Long id) {
        return FactureDto.from(findEntityChecked(id));
    }

    /** RG-053 : toute réimpression est tracée (utilisateur, date, nombre d'impressions) et le
     * document porte la mention DUPLICATA — ajoutée au rendu, pas stockée en base. */
    @Transactional
    public FactureDto reimprimer(Long id) {
        Facture f = findEntityChecked(id);
        f.setNombreImpressions(f.getNombreImpressions() + 1);
        Facture saved = repository.save(f);
        journal.enregistrer("VENTES", "REIMPRESSION", "Réimpression de la facture " + saved.getNumero()
                + " (n°" + saved.getNombreImpressions() + ")");
        return FactureDto.from(saved);
    }

    /** Le PDF doit être généré à l'intérieur de la transaction : {@code Commande.lignes} est
     * chargée en LAZY, et le contrôleur n'a lui-même aucune session ouverte pour l'initialiser
     * après coup (chargement direct par un repository, hors couche service). */
    @Transactional
    public byte[] genererTicketPdf(Long id) {
        Facture facture = findEntityChecked(id);
        return TicketGenerator.genererPdf(facture, parametresService.getEntity());
    }

    private Facture findEntityChecked(Long id) {
        Facture f = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Facture", id));
        if (!perimetreGuard.aAcces(f.getEtablissement())) {
            throw ResourceNotFoundException.of("Facture", id);
        }
        return f;
    }
}
