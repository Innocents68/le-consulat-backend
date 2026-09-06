# Le Consulat — Backend

API REST Spring Boot 3 / Java 17 pour le logiciel de gestion intégré **Le Consulat**
(restaurant, cave à vin, maquis, ventes au comptoir). Implémente exactement le contrat
défini dans [`docs/API_CONTRACT.md`](docs/API_CONTRACT.md) ; les règles de gestion
détaillées viennent du [`docs/Cahier_des_Charges_Le_Consulat.md`](docs/Cahier_des_Charges_Le_Consulat.md).

## Stack

- Java 17, Spring Boot 3.3, Spring Security + JWT (`io.jsonwebtoken`)
- Spring Data JPA / Hibernate
- H2 (fichier local) en développement, PostgreSQL en production
- WebSocket STOMP/SockJS (`/ws`) pour le temps réel restaurant
- springdoc-openapi (Swagger UI)
- OpenPDF (exports PDF) et Apache POI (exports Excel)

## Démarrage rapide (développement, sans rien installer d'autre que Java/Maven)

```bash
cd backend
mvn spring-boot:run
```

Le profil actif par défaut est `dev` : la base est un fichier H2 local (`./data/leconsulat.mv.db`,
créé automatiquement), donc **aucune base de données externe n'est nécessaire**. Au premier
démarrage (base vide), le `DataSeeder` peuple automatiquement des données de démonstration
réalistes (utilisateurs, produits, boissons, plats, tables, ventes des 7 derniers jours, etc.)
afin que le tableau de bord et les rapports ne soient pas vides.

- API : http://localhost:8080/api/v1
- Swagger UI : http://localhost:8080/swagger-ui.html
- Console H2 (dev uniquement) : http://localhost:8080/h2-console
  (JDBC URL `jdbc:h2:file:./data/leconsulat`, utilisateur `sa`, mot de passe vide)

### Comptes de démonstration

Mot de passe pour tous les comptes : **`password123`**

| Utilisateur | Rôle | Nom |
|---|---|---|
| `admin` | ADMIN | Administrateur |
| `jean` | CAISSIER | Jean Ouedraogo |
| `mariam` | SERVEUR | Mariam Kaboré |
| `paul` | MANAGER | Paul Sawadogo |
| `sophie` | COMPTABLE | Sophie Compaoré |
| `chef` | CUISINIER | Issa Traoré (compte additionnel pour tester l'écran cuisine) |

## Lancer avec Docker Compose (mode production-like, PostgreSQL)

```bash
cd backend
docker compose up --build
```

Cela démarre un conteneur PostgreSQL et le backend en profil `prod` (voir `docker-compose.yml`
pour les variables d'environnement `DB_*`, `JWT_SECRET`, `FRONTEND_URL`). L'API est exposée sur
`http://localhost:8080`. **Attention** : contrairement au profil `dev`, la base PostgreSQL démarre
vide — le `DataSeeder` s'exécute aussi en profil `prod` (même logique, tant que la table
`utilisateurs` est vide) et créera donc les mêmes comptes/données de démonstration au premier lancement.

## Variables d'environnement (profil `prod`)

| Variable | Défaut | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/leconsulat` | URL JDBC PostgreSQL |
| `DB_USER` | `leconsulat` | Utilisateur PostgreSQL |
| `DB_PASSWORD` | `leconsulat` | Mot de passe PostgreSQL |
| `JWT_SECRET` | valeur de développement | **À changer en production** (secret HMAC pour les JWT) |
| `JWT_EXPIRATION_MS` | `86400000` (24h) | Durée de validité du token |
| `FRONTEND_URL` | `http://localhost:5173` | Origine autorisée pour CORS |
| `UPLOADS_DIR` | `./uploads` | Dossier de stockage des justificatifs de dépenses |
| `BACKUPS_DIR` | `./backups` | Dossier de sortie des sauvegardes |

## Architecture du code

Organisation en packages par domaine métier (`com.leconsulat.<module>`), chacun en couches
`entity / repository / service / controller / dto` :

```
auth            authentification JWT (login/me/logout)
utilisateur     utilisateurs, rôles, profils, matrice des droits
journal         journal des opérations (audit trail, lecture seule)
stock           produits, catégories, entrées/sorties de stock, inventaires, dépôts
restaurant      tables, catégories de plats, plats, commandes + WebSocket temps réel
cave            boissons, mouvements de cave, fournisseurs
maquis          commandes maquis
vente           ventes/tickets, sessions de caisse, remises, avoirs, factures
finance         recettes, dépenses, catégories de dépenses, rapports de caisse
dashboard       agrégation du tableau de bord (§7 du contrat)
reporting       rapports ventes/stocks/recettes-dépenses/bénéfices + export PDF/Excel
parametres      paramètres généraux (ligne singleton)
sauvegardes     déclenchement/restauration de sauvegardes (pg_dump en prod, copie du fichier H2 en dev)
aide            FAQ statique
security/config sécurité Spring Security, JWT, CORS, WebSocket, OpenAPI
common          gestion d'erreurs uniforme, pagination, audit, PDF/Excel
seed            jeu de données de démonstration (DataSeeder)
```

## Règles métier notables implémentées

- **Vente non modifiable après paiement** : une fois `PAYEE`, un ticket ne peut plus être
  modifié ; seule l'émission d'un `Avoir` est possible. Le stock est décrémenté et la
  `Recette` correspondante est créée automatiquement à l'encaissement.
- **Numérotation séquentielle des factures** : `FV-{année}-{séquence}`, attribuée à
  l'encaissement, jamais réutilisée.
- **Clôture de caisse** : calcule l'écart théorique (fond initial + ventes espèces) vs le
  montant réel compté, et génère automatiquement un `RapportCaisse`.
- **Cave à vin** : la vente au verre décrémente le stock proportionnellement
  (1 bouteille = 6 verres) par rapport à une vente à la bouteille.
- **Stocks** : une `EntreeStock`/`SortieStock` n'impacte le stock qu'après validation
  (`POST /{id}/valider`) ; non modifiable ensuite.
- **Restaurant temps réel** : le statut d'une table suit automatiquement le cycle de vie
  de ses commandes (occupée à la création, libérée quand toutes les commandes actives sont
  servies/annulées) ; diffusion WebSocket sur `/topic/commandes`, `/topic/tables`, `/topic/cuisine`.
- **Journal des opérations** : `JournalOperationService` est appelé explicitement par les
  services métier à chaque action sensible (création/modification/archivage/validation sur
  utilisateurs, ventes, stocks, finances, restaurant, cave) et persiste une ligne immuable.
- **Sécurité** : JWT stateless, `@PreAuthorize` par rôle sur chaque endpoint sensible,
  mots de passe chiffrés BCrypt, CORS restreint à `FRONTEND_URL`.

## Écarts / simplifications volontaires par rapport au contrat

Ces choix sont raisonnables et documentés ici pour information du client :

- **Matrice des droits (Profils)** : elle est persistée et pilotable via
  `GET/PUT /droits/matrice`, mais l'autorisation **réellement appliquée** par l'API repose
  sur les rôles Spring Security (`@PreAuthorize`) codés dans chaque contrôleur, pas sur une
  relecture dynamique de la matrice à chaque requête. La matrice sert donc surtout à piloter
  l'affichage du frontend ; une évolution future pourrait brancher un `PermissionEvaluator`
  personnalisé sur la table `droits` pour une application dynamique complète.
- **Remises appliquées à l'encaissement** : une remise globale peut être appliquée via
  `remiseId` dans `POST /ventes/{id}/encaisser` (pourcentage ou montant fixe) ; l'association
  fine remise-ligne n'est pas modélisée séparément (le champ `remise` existe par ligne pour
  une réduction manuelle ponctuelle).
- **Transferts inter-dépôts** (`POST /depots/transferts`) : chaque `Produit` est rattaché à un
  seul dépôt ; un transfert décrémente le produit source et incrémente (ou crée) l'enregistrement
  `Produit` équivalent (même code) au dépôt destination.
- **Sauvegardes** : fonctionnalité réelle (pas un stub) — `pg_dump`/`psql` en profil `prod`
  (le client `postgresql-client` est installé dans l'image Docker), copie du fichier `.mv.db`
  en profil `dev`. La restauration en production nécessite que `pg_dump`/`psql` soient
  accessibles depuis le conteneur backend, ce qui est le cas dans le `docker-compose.yml`
  fourni ; un redémarrage de l'application est recommandé après une restauration.
- **Numérotation des factures** : calculée par comptage (`COUNT` des numéros déjà émis pour
  l'année courante, méthode `synchronized`), suffisant pour un mono-instance ; un déploiement
  multi-instances nécessiterait une séquence en base dédiée.
- **`GET /finance/vue-ensemble?periode=`** : le paramètre `periode` est accepté (`jour|semaine|mois`)
  mais la réponse renvoie toujours `recettesDuJour`/`depensesDuJour` du jour courant plus
  l'évolution sur 7 jours, conformément à la forme du contrat.
- **Un compte de démonstration additionnel `chef` (rôle CUISINIER)** a été ajouté en plus des
  5 comptes explicitement listés dans le contrat, afin de pouvoir tester l'écran cuisine avec
  un rôle dédié.

## Limitation connue de cet environnement de génération

Ce projet a été écrit dans un environnement d'exécution dont la politique réseau bloque
l'accès à Maven Central et à tous ses miroirs habituels (dépôts Spring, Google, Sonatype,
JitPack...). Il n'a donc **pas été possible d'exécuter `mvn compile` ni de démarrer
l'application dans cet environnement** pour vérifier la compilation de bout en bout. Le code
a été écrit et relu avec soin (types, imports, signatures, annotations JPA/Spring) pour être
correct, mais un premier `mvn clean package` dans un environnement avec accès réseau normal
reste la vérification à faire avant mise en production. Aucune dépendance exotique n'est
utilisée : Spring Boot Starter (web, security, data-jpa, validation, websocket, actuator),
H2, PostgreSQL driver, jjwt, springdoc-openapi, OpenPDF, Apache POI, Lombok — toutes des
versions stables largement utilisées.

## Build & tests manuels une fois le réseau disponible

```bash
mvn -DskipTests compile        # vérifie la compilation
mvn spring-boot:run            # démarre en profil dev
curl -s http://localhost:8080/swagger-ui.html -o /dev/null -w "%{http_code}\n"
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```

## Schéma de base de données

Le schéma est généré automatiquement par Hibernate (`ddl-auto=update`) à partir des entités
JPA du code source — voir les packages `*/entity` de chaque module pour le détail des tables,
colonnes, contraintes et relations (clés étrangères).
