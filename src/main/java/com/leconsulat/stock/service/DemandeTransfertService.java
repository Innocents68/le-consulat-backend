package com.leconsulat.stock.service;

import com.leconsulat.catalogue.entity.Produit;
import com.leconsulat.catalogue.repository.ProduitRepository;
import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.common.exception.UnauthorizedException;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.security.CustomUserDetails;
import com.leconsulat.security.PerimetreGuard;
import com.leconsulat.stock.dto.AccepterDemandeTransfertRequest;
import com.leconsulat.stock.dto.CreerDemandeTransfertRequest;
import com.leconsulat.stock.dto.DemandeTransfertDto;
import com.leconsulat.stock.dto.RefuserDemandeTransfertRequest;
import com.leconsulat.stock.entity.DemandeTransfert;
import com.leconsulat.stock.entity.StatutDemandeTransfert;
import com.leconsulat.stock.repository.DemandeTransfertRepository;
import com.leconsulat.utilisateur.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Demandes_amelioration_logiciel_Le_Consulat_Professionnel.docx §5 : formalise le transfert de
 * produit entre établissements — unique chemin désormais (Cahier_de_corrections_Le_Consulat.docx
 * §1.1 : le transfert immédiat, ex RG-084, a été retiré). N'importe quel utilisateur peut demander
 * un transfert depuis son propre établissement (il choisit son produit, qu'il voit), mais le
 * mouvement de stock n'a lieu qu'après acceptation explicite par le responsable de l'établissement
 * destinataire (qui choisit alors le produit correspondant dans son propre catalogue — jamais
 * visible pour le demandeur, RG-002). */
@Service
@Transactional
public class DemandeTransfertService {

    private final DemandeTransfertRepository repository;
    private final ProduitRepository produitRepository;
    private final EtablissementRepository etablissementRepository;
    private final PerimetreGuard perimetreGuard;
    private final MouvementStockService mouvementStockService;
    private final JournalOperationService journal;

    public DemandeTransfertService(DemandeTransfertRepository repository, ProduitRepository produitRepository,
                                    EtablissementRepository etablissementRepository, PerimetreGuard perimetreGuard,
                                    MouvementStockService mouvementStockService, JournalOperationService journal) {
        this.repository = repository;
        this.produitRepository = produitRepository;
        this.etablissementRepository = etablissementRepository;
        this.perimetreGuard = perimetreGuard;
        this.mouvementStockService = mouvementStockService;
        this.journal = journal;
    }

    @Transactional
    public DemandeTransfertDto creer(CreerDemandeTransfertRequest req) {
        Produit source = produitRepository.findById(req.produitSourceId())
                .orElseThrow(() -> ResourceNotFoundException.of("Produit", req.produitSourceId()));
        if (!perimetreGuard.aAcces(source.getEtablissement())) {
            throw ResourceNotFoundException.of("Produit", req.produitSourceId());
        }
        Etablissement destination = etablissementRepository.findById(req.etablissementDestinationId())
                .orElseThrow(() -> ResourceNotFoundException.of("Etablissement", req.etablissementDestinationId()));
        if (destination.getId().equals(source.getEtablissement().getId())) {
            throw new BusinessRuleException("Un transfert doit se faire entre deux établissements différents");
        }
        if (req.quantite().compareTo(source.getQuantiteStock()) > 0) {
            throw new BusinessRuleException("Stock insuffisant pour " + source.getNom());
        }
        DemandeTransfert d = new DemandeTransfert();
        d.setProduitSource(source);
        d.setQuantite(req.quantite());
        d.setEtablissementDestination(destination);
        d.setDemandeur(currentUtilisateur());
        d.setCommentaireDemande(req.commentaire());
        DemandeTransfert saved = repository.save(d);
        journal.enregistrer("STOCKS", "DEMANDE_TRANSFERT_CREATION",
                "Demande de transfert de " + req.quantite() + " " + source.getUnite() + " de " + source.getNom()
                        + " (" + source.getEtablissement().getNom() + ") vers " + destination.getNom());
        return DemandeTransfertDto.from(saved);
    }

    /** Demandes reçues par l'établissement en paramètre (ou celui de l'utilisateur connecté) —
     * celles qu'il doit accepter ou refuser. */
    public Page<DemandeTransfertDto> recues(Long etablissementIdDemande, String statut, Pageable pageable) {
        Etablissement cible = resoudreEtablissementCible(etablissementIdDemande);
        Page<DemandeTransfert> page = statut != null
                ? repository.findByEtablissementDestination_IdAndStatutOrderByDateDemandeDesc(cible.getId(), parseStatut(statut), pageable)
                : repository.findByEtablissementDestination_IdOrderByDateDemandeDesc(cible.getId(), pageable);
        return page.map(DemandeTransfertDto::from);
    }

    /** Demandes émises depuis l'établissement en paramètre — leur suivi côté demandeur. */
    public Page<DemandeTransfertDto> emises(Long etablissementIdDemande, String statut, Pageable pageable) {
        Etablissement cible = resoudreEtablissementCible(etablissementIdDemande);
        Page<DemandeTransfert> page = statut != null
                ? repository.findByProduitSource_Etablissement_IdAndStatutOrderByDateDemandeDesc(cible.getId(), parseStatut(statut), pageable)
                : repository.findByProduitSource_Etablissement_IdOrderByDateDemandeDesc(cible.getId(), pageable);
        return page.map(DemandeTransfertDto::from);
    }

    /** Badge de notification in-app (§5) : nombre de demandes en attente adressées à
     * l'établissement de l'utilisateur connecté — toutes établissements confondus pour le Super
     * Administrateur si aucun établissement n'est précisé. */
    public long compteEnAttente(Long etablissementIdDemande) {
        if (etablissementIdDemande == null && perimetreGuard.isSuperAdmin()) {
            return repository.countByStatut(StatutDemandeTransfert.EN_ATTENTE);
        }
        Etablissement cible = resoudreEtablissementCible(etablissementIdDemande);
        return repository.countByEtablissementDestination_IdAndStatut(cible.getId(), StatutDemandeTransfert.EN_ATTENTE);
    }

    @Transactional
    public DemandeTransfertDto accepter(Long id, AccepterDemandeTransfertRequest req) {
        DemandeTransfert d = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("DemandeTransfert", id));
        if (!perimetreGuard.aAcces(d.getEtablissementDestination())) {
            throw ResourceNotFoundException.of("DemandeTransfert", id);
        }
        if (d.getStatut() != StatutDemandeTransfert.EN_ATTENTE) {
            throw new BusinessRuleException("Cette demande a déjà été traitée");
        }
        Produit destination = produitRepository.findById(req.produitDestinationId())
                .orElseThrow(() -> ResourceNotFoundException.of("Produit", req.produitDestinationId()));
        if (!destination.getEtablissement().getId().equals(d.getEtablissementDestination().getId())) {
            throw new BusinessRuleException("Ce produit n'appartient pas à votre établissement");
        }
        // Le stock source a pu changer depuis la demande : effectuerTransfert revérifie la
        // suffisance et échoue proprement (BusinessRuleException) si elle ne l'est plus.
        mouvementStockService.effectuerTransfert(d.getProduitSource(), destination, d.getQuantite());
        d.setProduitDestination(destination);
        d.setStatut(StatutDemandeTransfert.ACCEPTEE);
        d.setTraitePar(currentUtilisateur());
        d.setDateTraitement(LocalDateTime.now());
        DemandeTransfert saved = sauvegarderSansDoubleTraitement(d);
        journal.enregistrer("STOCKS", "DEMANDE_TRANSFERT_ACCEPTEE",
                "Demande de transfert de " + d.getProduitSource().getNom() + " vers " + d.getEtablissementDestination().getNom()
                        + " acceptée (produit : " + destination.getNom() + ")");
        return DemandeTransfertDto.from(saved);
    }

    @Transactional
    public DemandeTransfertDto refuser(Long id, RefuserDemandeTransfertRequest req) {
        DemandeTransfert d = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("DemandeTransfert", id));
        if (!perimetreGuard.aAcces(d.getEtablissementDestination())) {
            throw ResourceNotFoundException.of("DemandeTransfert", id);
        }
        if (d.getStatut() != StatutDemandeTransfert.EN_ATTENTE) {
            throw new BusinessRuleException("Cette demande a déjà été traitée");
        }
        d.setStatut(StatutDemandeTransfert.REFUSEE);
        d.setMotifRefus(req.motif());
        d.setTraitePar(currentUtilisateur());
        d.setDateTraitement(LocalDateTime.now());
        DemandeTransfert saved = sauvegarderSansDoubleTraitement(d);
        journal.enregistrer("STOCKS", "DEMANDE_TRANSFERT_REFUSEE",
                "Demande de transfert de " + d.getProduitSource().getNom() + " vers " + d.getEtablissementDestination().getNom()
                        + " refusée" + (req.motif() != null ? " (motif : " + req.motif() + ")" : ""));
        return DemandeTransfertDto.from(saved);
    }

    /** Cahier_de_corrections_Le_Consulat.docx §1.4 : {@code saveAndFlush} force la vérification du
     * verrou optimiste ({@code @Version}) à l'intérieur de cette méthode (donc rattrapable ici),
     * plutôt qu'à la toute fin de la transaction Spring où elle échapperait à ce {@code catch}. */
    private DemandeTransfert sauvegarderSansDoubleTraitement(DemandeTransfert d) {
        try {
            return repository.saveAndFlush(d);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessRuleException("Cette demande a déjà été traitée");
        }
    }

    private StatutDemandeTransfert parseStatut(String statut) {
        try {
            return StatutDemandeTransfert.valueOf(statut.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Statut de demande de transfert inconnu : " + statut);
        }
    }

    private Etablissement resoudreEtablissementCible(Long etablissementIdDemande) {
        Etablissement demande = etablissementIdDemande != null
                ? etablissementRepository.findById(etablissementIdDemande)
                        .orElseThrow(() -> ResourceNotFoundException.of("Etablissement", etablissementIdDemande))
                : null;
        Etablissement cible = perimetreGuard.scopeEtablissement(demande);
        if (cible == null) {
            throw new BusinessRuleException("L'établissement est obligatoire");
        }
        return cible;
    }

    private Utilisateur currentUtilisateur() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUtilisateur();
        }
        throw new UnauthorizedException("Utilisateur non authentifié");
    }
}
