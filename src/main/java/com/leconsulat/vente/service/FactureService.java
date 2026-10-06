package com.leconsulat.vente.service;

import com.leconsulat.avoir.repository.AvoirRepository;
import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.parametres.service.ParametresService;
import com.leconsulat.security.PerimetreGuard;
import com.leconsulat.stock.service.MouvementStockService;
import com.leconsulat.vente.dto.DeleteFactureRequest;
import com.leconsulat.vente.dto.FactureDto;
import com.leconsulat.vente.entity.Commande;
import com.leconsulat.vente.entity.Facture;
import com.leconsulat.vente.entity.LigneCommande;
import com.leconsulat.vente.entity.StatutCommande;
import com.leconsulat.vente.repository.CommandeRepository;
import com.leconsulat.vente.repository.FactureRepository;
import com.leconsulat.vente.repository.PaiementRepository;
import com.leconsulat.vente.util.TicketGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Consultation d'un document immuable (RG-052) — pas de méthode de modification ici, seule la
 * réimpression est possible et elle ne change pas le contenu. {@link #supprimer} fait exception,
 * ajoutée par Consu_corrige.docx §4 : un vrai retrait (pas une correction par avoir), réservé au
 * Super Administrateur et à des cas exceptionnels. */
@Service
@Transactional
public class FactureService {

    private final FactureRepository repository;
    private final EtablissementRepository etablissementRepository;
    private final PerimetreGuard perimetreGuard;
    private final JournalOperationService journal;
    private final ParametresService parametresService;
    private final CommandeRepository commandeRepository;
    private final PaiementRepository paiementRepository;
    private final AvoirRepository avoirRepository;
    private final MouvementStockService mouvementStockService;

    public FactureService(FactureRepository repository, EtablissementRepository etablissementRepository,
                           PerimetreGuard perimetreGuard, JournalOperationService journal, ParametresService parametresService,
                           CommandeRepository commandeRepository, PaiementRepository paiementRepository,
                           AvoirRepository avoirRepository, MouvementStockService mouvementStockService) {
        this.repository = repository;
        this.etablissementRepository = etablissementRepository;
        this.perimetreGuard = perimetreGuard;
        this.journal = journal;
        this.parametresService = parametresService;
        this.commandeRepository = commandeRepository;
        this.paiementRepository = paiementRepository;
        this.avoirRepository = avoirRepository;
        this.mouvementStockService = mouvementStockService;
    }

    /** Consu_corrige.docx §4/§6 : suppression réservée au Super Administrateur — coexiste avec
     * "Créer un avoir" plutôt que de le remplacer (la correction normale d'une vente reste
     * l'avoir ; ceci est le retrait complet, pour une vraie erreur). Refusée si des avoirs ont
     * déjà été émis sur cette facture (FK avoirs.facture_id, et on ne veut pas effacer une
     * correction déjà tracée). Réintègre le stock des produits suivis, supprime le paiement, et
     * repasse la commande en ANNULEE (conservée, pas supprimée — RG-020/021 : son numéro ne doit
     * jamais réapparaître comme si elle n'avait pas existé) plutôt que de la supprimer elle aussi. */
    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    @Transactional
    public void supprimer(Long id, DeleteFactureRequest req) {
        Facture facture = findEntityChecked(id);
        if (avoirRepository.existsByFactureId(id)) {
            throw new BusinessRuleException("Cette facture a déjà un ou plusieurs avoirs — suppression impossible");
        }
        Commande commande = facture.getCommande();
        for (LigneCommande ligne : commande.getLignes()) {
            if (ligne.getProduit().isSuiviStock()) {
                mouvementStockService.enregistrerAnnulationFacture(ligne.getProduit(), BigDecimal.valueOf(ligne.getQuantite()));
            }
        }
        paiementRepository.deleteByCommandeId(commande.getId());
        commande.setStatut(StatutCommande.ANNULEE);
        commande.setMotifAnnulation("Facture " + facture.getNumero() + " supprimée : " + req.motif());
        commandeRepository.save(commande);

        String numero = facture.getNumero();
        repository.delete(facture);
        journal.enregistrer("VENTES", "SUPPRESSION", "Facture " + numero + " supprimée (motif : " + req.motif()
                + ") — commande " + commande.getNumero() + " repassée en annulée, stock réintégré");
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
