package com.leconsulat.security;

import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * RG-106 : une restauration de sauvegarde doit déconnecter tous les utilisateurs. L'authentification
 * étant un JWT sans état côté serveur, il n'existe pas de session à invalider individuellement —
 * on retient à la place un horodatage global : tout jeton émis avant cet instant est rejeté,
 * quelle que soit sa date d'expiration propre. En mémoire, volontairement : application
 * mono-instance, un redémarrage réinitialise l'invalidation, ce qui est sans risque (les jetons
 * émis avant restent soumis à leur propre expiration normale).
 */
@Component
public class TokenInvalidationRegistry {

    private volatile Instant invalideDepuis = Instant.EPOCH;

    public void invaliderToutesLesSessions() {
        invalideDepuis = Instant.now();
    }

    public boolean estEncoreValide(Instant emisLe) {
        return emisLe != null && emisLe.isAfter(invalideDepuis);
    }
}
