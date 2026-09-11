package com.leconsulat.utilisateur.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BadRequestException;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.etablissement.repository.EtablissementRepository;
import com.leconsulat.utilisateur.dto.ChangePasswordRequest;
import com.leconsulat.utilisateur.dto.CreateUtilisateurRequest;
import com.leconsulat.utilisateur.dto.UpdateUtilisateurRequest;
import com.leconsulat.utilisateur.dto.UtilisateurDto;
import com.leconsulat.utilisateur.entity.Profil;
import com.leconsulat.utilisateur.entity.Utilisateur;
import com.leconsulat.utilisateur.repository.UtilisateurRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository repository;
    private final EtablissementRepository etablissementRepository;
    private final PasswordEncoder passwordEncoder;
    private final JournalOperationService journal;

    public UtilisateurService(UtilisateurRepository repository, EtablissementRepository etablissementRepository,
                               PasswordEncoder passwordEncoder, JournalOperationService journal) {
        this.repository = repository;
        this.etablissementRepository = etablissementRepository;
        this.passwordEncoder = passwordEncoder;
        this.journal = journal;
    }

    public Page<UtilisateurDto> list(String search, Pageable pageable) {
        Page<Utilisateur> page = (search == null || search.isBlank())
                ? repository.findAll(pageable)
                : repository.findByNomContainingIgnoreCaseOrUsernameContainingIgnoreCase(search, search, pageable);
        return page.map(UtilisateurDto::from);
    }

    public UtilisateurDto get(Long id) {
        return UtilisateurDto.from(findEntity(id));
    }

    @Transactional
    public UtilisateurDto create(CreateUtilisateurRequest req) {
        if (repository.existsByUsername(req.username())) {
            throw new BadRequestException("Ce nom d'utilisateur existe déjà");
        }
        Profil profil = parseProfil(req.profil());
        Utilisateur u = new Utilisateur();
        u.setUsername(req.username());
        u.setNom(req.nom());
        u.setEmail(req.email());
        u.setTelephone(req.telephone());
        u.setProfil(profil);
        u.setEtablissement(resoudreEtablissement(profil, req.etablissementId()));
        u.setMotDePasse(passwordEncoder.encode(req.motDePasse()));
        u.setActif(true);
        Utilisateur saved = repository.save(u);
        journal.enregistrer("UTILISATEURS", "CREATION", "Création de l'utilisateur " + saved.getUsername()
                + " (profil " + profil + (saved.getEtablissement() != null ? ", établissement " + saved.getEtablissement().getNom() : "") + ")");
        return UtilisateurDto.from(saved);
    }

    @Transactional
    public UtilisateurDto update(Long id, UpdateUtilisateurRequest req) {
        Utilisateur u = findEntity(id);
        Profil profil = parseProfil(req.profil());
        u.setNom(req.nom());
        u.setEmail(req.email());
        u.setTelephone(req.telephone());
        u.setProfil(profil);
        u.setEtablissement(resoudreEtablissement(profil, req.etablissementId()));
        Utilisateur saved = repository.save(u);
        journal.enregistrer("UTILISATEURS", "MODIFICATION", "Modification de l'utilisateur " + saved.getUsername());
        return UtilisateurDto.from(saved);
    }

    @Transactional
    public UtilisateurDto toggleStatut(Long id) {
        Utilisateur u = findEntity(id);
        u.setActif(!u.isActif());
        Utilisateur saved = repository.save(u);
        journal.enregistrer("UTILISATEURS", "ARCHIVAGE", "Utilisateur " + saved.getUsername() + " -> actif=" + saved.isActif());
        return UtilisateurDto.from(saved);
    }

    @Transactional
    public void resetPassword(Long id, ChangePasswordRequest req) {
        Utilisateur u = findEntity(id);
        u.setMotDePasse(passwordEncoder.encode(req.nouveauMotDePasse()));
        repository.save(u);
        journal.enregistrer("UTILISATEURS", "MODIFICATION", "Réinitialisation du mot de passe de " + u.getUsername());
    }

    private Profil parseProfil(String profil) {
        try {
            return Profil.valueOf(profil.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Profil inconnu : " + profil);
        }
    }

    /** Le Super Administrateur n'a aucun établissement (vision globale, RG-005). Tout autre
     * profil doit en avoir exactement un, choisi obligatoirement à la création (RG-004, RG-098). */
    private Etablissement resoudreEtablissement(Profil profil, Long etablissementId) {
        if (profil == Profil.SUPER_ADMINISTRATEUR) {
            return null;
        }
        if (etablissementId == null) {
            throw new BusinessRuleException("L'établissement est obligatoire pour ce profil");
        }
        return etablissementRepository.findById(etablissementId)
                .orElseThrow(() -> ResourceNotFoundException.of("Etablissement", etablissementId));
    }

    Utilisateur findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", id));
    }
}
