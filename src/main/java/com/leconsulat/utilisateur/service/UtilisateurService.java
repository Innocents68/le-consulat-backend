package com.leconsulat.utilisateur.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.BadRequestException;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.utilisateur.dto.ChangePasswordRequest;
import com.leconsulat.utilisateur.dto.CreateUtilisateurRequest;
import com.leconsulat.utilisateur.dto.UpdateUtilisateurRequest;
import com.leconsulat.utilisateur.dto.UtilisateurDto;
import com.leconsulat.utilisateur.entity.Role;
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
    private final PasswordEncoder passwordEncoder;
    private final JournalOperationService journal;

    public UtilisateurService(UtilisateurRepository repository, PasswordEncoder passwordEncoder, JournalOperationService journal) {
        this.repository = repository;
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
        Role role = parseRole(req.role());
        Utilisateur u = new Utilisateur();
        u.setUsername(req.username());
        u.setNom(req.nom());
        u.setEmail(req.email());
        u.setTelephone(req.telephone());
        u.setRole(role);
        u.setMotDePasse(passwordEncoder.encode(req.motDePasse()));
        u.setActif(true);
        Utilisateur saved = repository.save(u);
        journal.enregistrer("UTILISATEURS", "CREATION", "Création de l'utilisateur " + saved.getUsername() + " (rôle " + role + ")");
        return UtilisateurDto.from(saved);
    }

    @Transactional
    public UtilisateurDto update(Long id, UpdateUtilisateurRequest req) {
        Utilisateur u = findEntity(id);
        u.setNom(req.nom());
        u.setEmail(req.email());
        u.setTelephone(req.telephone());
        u.setRole(parseRole(req.role()));
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

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role.trim().toUpperCase());
        } catch (Exception e) {
            throw new BusinessRuleException("Rôle inconnu : " + role);
        }
    }

    Utilisateur findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", id));
    }
}
