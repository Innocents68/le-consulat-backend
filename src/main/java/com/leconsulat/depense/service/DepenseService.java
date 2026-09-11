package com.leconsulat.depense.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.common.exception.UnauthorizedException;
import com.leconsulat.depense.dto.AnnulerDepenseRequest;
import com.leconsulat.depense.dto.CreateDepenseRequest;
import com.leconsulat.depense.dto.DepenseDto;
import com.leconsulat.depense.dto.UpdateDepenseRequest;
import com.leconsulat.depense.entity.CategorieDepense;
import com.leconsulat.depense.entity.Depense;
import com.leconsulat.depense.repository.CategorieDepenseRepository;
import com.leconsulat.depense.repository.DepenseRepository;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.security.CustomUserDetails;
import com.leconsulat.security.PerimetreGuard;
import com.leconsulat.utilisateur.entity.Utilisateur;
import com.leconsulat.vente.entity.ModePaiement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** §6.7.2 — cœur de la traçabilité financière (RG-095/096). */
@Service
@Transactional
public class DepenseService {

    private final DepenseRepository repository;
    private final CategorieDepenseRepository categorieRepository;
    private final EtablissementRepository etablissementRepository;
    private final JournalOperationService journal;
    private final PerimetreGuard perimetreGuard;

    public DepenseService(DepenseRepository repository, CategorieDepenseRepository categorieRepository,
                           EtablissementRepository etablissementRepository, JournalOperationService journal,
                           PerimetreGuard perimetreGuard) {
        this.repository = repository;
        this.categorieRepository = categorieRepository;
        this.etablissementRepository = etablissementRepository;
        this.journal = journal;
        this.perimetreGuard = perimetreGuard;
    }

    public Page<DepenseDto> search(Long etablissementIdDemande, Long categorieId, String modePaiement,
                                    LocalDate dateDebut, LocalDate dateFin, Pageable pageable) {
        Etablissement cible = resoudreEtablissementCible(etablissementIdDemande);
        ModePaiement mode = modePaiement != null ? parseMode(modePaiement) : null;
        return repository.search(cible, categorieId, mode, dateDebut, dateFin, pageable).map(DepenseDto::from);
    }

    public DepenseDto get(Long id) {
        return DepenseDto.from(findEntityChecked(id));
    }

    /** RG-089/090 : toute dépense est rattachée à un établissement, automatiquement le sien pour
     * un Gérant/Caissier. */
    @Transactional
    public DepenseDto create(CreateDepenseRequest req) {
        if (req.date().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("La date d'une dépense ne peut pas être postérieure à aujourd'hui");
        }
        Etablissement cible = resoudreEtablissementCible(req.etablissementId());
        CategorieDepense categorie = categorieRepository.findById(req.categorieId())
                .orElseThrow(() -> ResourceNotFoundException.of("CategorieDepense", req.categorieId()));

        Depense d = new Depense();
        d.setDate(req.date());
        d.setEtablissement(cible);
        d.setCategorie(categorie);
        d.setLibelle(req.libelle());
        d.setMontant(req.montant());
        d.setModePaiement(parseMode(req.modePaiement()));
        d.setBeneficiaire(req.beneficiaire());
        d.setCommentaire(req.commentaire());
        d.setUtilisateur(currentUtilisateur());

        Depense saved = repository.save(d);
        journal.enregistrer("FINANCIER", "DEPENSE_CREATION", "Dépense de " + saved.getMontant() + " FCFA ("
                + categorie.getNom() + ") enregistrée pour " + cible.getNom());
        return DepenseDto.from(saved);
    }

    /** RG-094 : réservé au Super Administrateur, motif obligatoire. */
    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    @Transactional
    public DepenseDto update(Long id, UpdateDepenseRequest req) {
        Depense d = findEntityChecked(id);
        if (d.isAnnulee()) {
            throw new BusinessRuleException("Cette dépense est annulée");
        }
        if (req.date().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("La date d'une dépense ne peut pas être postérieure à aujourd'hui");
        }
        CategorieDepense categorie = categorieRepository.findById(req.categorieId())
                .orElseThrow(() -> ResourceNotFoundException.of("CategorieDepense", req.categorieId()));

        d.setDate(req.date());
        d.setCategorie(categorie);
        d.setLibelle(req.libelle());
        d.setMontant(req.montant());
        d.setModePaiement(parseMode(req.modePaiement()));
        d.setBeneficiaire(req.beneficiaire());
        d.setCommentaire(req.commentaire());

        Depense saved = repository.save(d);
        journal.enregistrer("FINANCIER", "DEPENSE_MODIFICATION", "Dépense " + saved.getId()
                + " modifiée — motif : " + req.motif());
        return DepenseDto.from(saved);
    }

    /** RG-094 : réservé au Super Administrateur, motif obligatoire — la ligne reste visible
     * (traçabilité RG-095/096) plutôt que d'être supprimée physiquement. */
    @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")
    @Transactional
    public DepenseDto annuler(Long id, AnnulerDepenseRequest req) {
        Depense d = findEntityChecked(id);
        if (d.isAnnulee()) {
            throw new BusinessRuleException("Cette dépense est déjà annulée");
        }
        d.setAnnulee(true);
        d.setMotifAnnulation(req.motif());
        Depense saved = repository.save(d);
        journal.enregistrer("FINANCIER", "DEPENSE_ANNULATION", "Dépense " + saved.getId()
                + " annulée — motif : " + req.motif());
        return DepenseDto.from(saved);
    }

    private ModePaiement parseMode(String mode) {
        try {
            return ModePaiement.valueOf(mode.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Mode de paiement inconnu : " + mode);
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

    private Depense findEntityChecked(Long id) {
        Depense d = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Depense", id));
        if (!perimetreGuard.aAcces(d.getEtablissement())) {
            throw ResourceNotFoundException.of("Depense", id);
        }
        return d;
    }

    private Utilisateur currentUtilisateur() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUtilisateur();
        }
        throw new UnauthorizedException("Utilisateur non authentifié");
    }
}
