package com.leconsulat.security;

import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.utilisateur.entity.Profil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Cloisonnement établissement (cahier des charges RG-006, RG-010, RG-011) : l'établissement
 * transmis par le client n'est jamais la source de vérité, il est toujours recalculé depuis la
 * session de l'utilisateur connecté. Remplace l'ancien {@code DroitGuard} — avec seulement deux
 * profils fixes, les permissions "réservées au Super Administrateur" se gèrent directement avec
 * {@code @PreAuthorize("hasRole('SUPER_ADMINISTRATEUR')")} ; ce composant ne gère que l'axe
 * établissement, orthogonal et réutilisé par tous les futurs modules métier.
 */
@Component
public class PerimetreGuard {

    /** Un Super Administrateur n'a pas d'établissement propre : {@code requested} passe tel
     * quel (vision globale, RG-016). Tout Gérant/Caissier est forcé sur son propre
     * établissement, quoi qu'il ait demandé. */
    public Etablissement scopeEtablissement(Etablissement requested) {
        CustomUserDetails principal = currentPrincipal();
        if (principal == null) {
            return requested;
        }
        Etablissement own = principal.getUtilisateur().getEtablissement();
        return own != null ? own : requested;
    }

    public boolean isSuperAdmin() {
        CustomUserDetails principal = currentPrincipal();
        return principal != null && principal.getUtilisateur().getProfil() == Profil.SUPER_ADMINISTRATEUR;
    }

    /** À utiliser pour vérifier qu'un enregistrement déjà chargé (par exemple via
     * {@code repository.findById}) appartient bien au périmètre de l'utilisateur courant —
     * compare les identifiants, jamais les références d'objet : {@code entite.getEtablissement()}
     * et l'établissement de l'utilisateur connecté sont presque toujours chargés dans deux
     * sessions Hibernate différentes (l'authentification JWT en ouvre une propre avant même que
     * la transaction du contrôleur ne démarre), donc représenter la même ligne ne les rend pas
     * {@code ==} ni même {@code equals()} par défaut sur une entité JPA sans identité métier. */
    public boolean aAcces(Etablissement cible) {
        if (cible == null || cible.getId() == null) {
            return false;
        }
        Etablissement scope = scopeEtablissement(cible);
        return scope != null && cible.getId().equals(scope.getId());
    }

    private CustomUserDetails currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails principal)) {
            return null;
        }
        return principal;
    }
}
