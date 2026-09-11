package com.leconsulat.common.audit;

import com.leconsulat.journal.entity.JournalOperation;
import com.leconsulat.journal.repository.JournalOperationRepository;
import com.leconsulat.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Central audit-trail writer. Called explicitly by services on every sensitive action
 * (création, modification, archivage, validation) on the modules named in
 * API_CONTRACT.md §2 : ventes, stocks, finances, utilisateurs, restaurant, cave.
 * Deliberately joins the caller's existing transaction (no REQUIRES_NEW): if the
 * business action rolls back, the audit entry must roll back with it — otherwise the
 * journal would record actions that never actually happened.
 */
@Component
public class JournalOperationService {

    private final JournalOperationRepository repository;

    public JournalOperationService(JournalOperationRepository repository) {
        this.repository = repository;
    }

    public void enregistrer(String module, String action, String details) {
        Long userId = null;
        String userNom = "SYSTEME";
        JournalOperation entry;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            userId = cud.getId();
            userNom = cud.getUtilisateur().getNom();
            entry = new JournalOperation(userId, userNom, module, action, details);
            entry.setEtablissement(cud.getUtilisateur().getEtablissement());
        } else {
            entry = new JournalOperation(userId, userNom, module, action, details);
        }
        repository.save(entry);
    }
}
