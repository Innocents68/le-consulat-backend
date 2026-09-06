# API CONTRACT — Le Consulat (fait foi pour le backend ET le frontend)

Les deux projets (backend Spring Boot et frontend React) sont développés séparément.
Ce document est la source de vérité commune : le backend DOIT exposer exactement ces
endpoints/formes de données, et le frontend DOIT les consommer tels quels. En cas de
détail non couvert ici, se référer au cahier des charges
(`Cahier_des_Charges_Le_Consulat2.md`, fourni dans le dossier `docs/` de chaque projet)
pour les règles de gestion, et faire un choix raisonnable et cohérent avec le reste.

Base URL : `http://localhost:8080/api/v1`
Documentation interactive : `/swagger-ui.html` (springdoc-openapi)

## 1. Conventions générales

- JSON partout, dates au format ISO-8601 (`2026-09-05T14:30:00Z` ou `2026-09-05` pour les dates seules).
- Champs en camelCase des deux côtés.
- Suppression = **suppression logique** par défaut : chaque entité "supprimable" a un
  champ booléen `actif` (true par défaut) ; `DELETE /{id}` met `actif=false` (archivage),
  ne supprime jamais la ligne en base. Les entités marquées "non supprimable" dans le
  cahier des charges (EntreeStock validée, SortieStock validée, Recette, RapportCaisse,
  JournalOperation, Avoir validé, MouvementStock cave) n'exposent PAS d'endpoint DELETE.
- Listes paginées : `GET /{resource}?page=0&size=20&search=...&sort=champ,asc` renvoie
  le format standard Spring Data `Page<T>` :
  ```json
  { "content": [...], "totalElements": 123, "totalPages": 7, "number": 0, "size": 20 }
  ```
  Le frontend lit toujours `content` + `totalElements`.
- Filtres additionnels par ressource : query params nommés explicitement dans la section
  de la ressource (ex. `?dateDebut=&dateFin=&categorieId=&statut=`).
- Erreurs (format uniforme, `GlobalExceptionHandler`) :
  ```json
  { "timestamp": "...", "status": 404, "error": "NOT_FOUND", "message": "Produit introuvable", "path": "/api/v1/produits/5", "errors": { "champ": "message" } }
  ```
  `errors` seulement présent pour les erreurs de validation (400).
- CORS : origine autorisée via variable d'env `FRONTEND_URL` (défaut `http://localhost:5173`).
- Toutes les routes sauf `/auth/login` exigent l'en-tête `Authorization: Bearer <token>`.
- Contrôle d'accès par rôle sur les actions sensibles (suppression, validation, remboursement)
  conformément à la matrice de droits du module Profils.

## 2. Authentification & Utilisateurs

Rôles (enum `Role`) — 4 profils (cahier des charges, matrice des droits) : `ADMIN` (Administrateur),
`GERANT` (Gérant), `CAISSIER_SERVEUR` (Caissier / Serveur — rôle fusionné), `CUISINIER` (Cuisinier).

- `POST /auth/login` — body `{ "username": "...", "password": "..." }` →
  ```json
  { "token": "eyJ...", "tokenType": "Bearer", "expiresIn": 86400,
    "user": { "id": 1, "username": "admin", "nom": "Administrateur", "role": "ADMIN", "email": "...", "avatarInitiales": "AD" } }
  ```
- `GET /auth/me` → objet `user` ci-dessus.
- `POST /auth/logout` → 204.

Utilisateurs (`/utilisateurs`) : CRUD complet (pas de DELETE physique, `PATCH /utilisateurs/{id}/statut` bascule actif/inactif).
Champs : `id, username, nom, email, telephone, role, actif, dateCreation`.
`PUT /utilisateurs/{id}/mot-de-passe` réinitialise le mot de passe (body `{ "nouveauMotDePasse" }`).

Profils & droits (`/profils`, `/droits`) : `GET /profils` liste les rôles avec leur
matrice de droits ; `GET /droits/matrice` renvoie `{ [role]: { [module]: { voir, ajouter, modifier, supprimer } } }`;
`PUT /droits/matrice` met à jour la matrice (ADMIN uniquement).

Journal des opérations (`/journal-operations`, lecture seule) :
`{ id, utilisateurId, utilisateurNom, module, action, details, dateOperation }`,
filtrable par `?utilisateurId=&module=&dateDebut=&dateFin=`. Généré automatiquement par le
backend (aspect / service commun) à chaque action de création/modification/suppression/validation
sur les modules sensibles (ventes, stocks, finances, utilisateurs).

## 3. Ventes / Caisse (`/ventes`, `/sessions-caisse`, `/remises`, `/avoirs`)

**Vente** : `{ id, numero, sessionCaisseId, caissierId, caissierNom, clientNom, statut: EN_COURS|PAYEE|ANNULEE, modePaiement: ESPECES|MOBILE_MONEY|CARTE, lignes: [LigneVente], sousTotal, remiseMontant, total, dateVente }`
**LigneVente** : `{ id, produitId, produitNom, quantite, prixUnitaire, remise, montant }`
- `POST /ventes` crée un ticket en cours (panier). `POST /ventes/{id}/lignes` ajoute une ligne.
  `DELETE /ventes/{id}/lignes/{ligneId}` retire une ligne (avant paiement uniquement).
- `POST /ventes/{id}/encaisser` — body `{ modePaiement, montantRecu }` → valide et paye le ticket (numérotation auto séquentielle `FV-2026-xxxx`, immuable après paiement).
- Après paiement, seule la création d'un **Avoir** est possible (pas de DELETE ni PUT sur les lignes).

**SessionCaisse** : `{ id, caisseNom, ouvertPar, fondInitial, statut: OUVERTE|FERMEE, dateOuverture, dateFermeture, totalVentes, totalEncaissements, ecart }`
- `POST /sessions-caisse` ouvre une caisse. `POST /sessions-caisse/{id}/cloturer` → calcule l'écart théorique/réel et génère automatiquement un `RapportCaisse` (voir §6).
- `GET /sessions-caisse?statut=OUVERTE` pour l'état temps réel des caisses.

**Remise** : `{ id, nom, type: POURCENTAGE|MONTANT_FIXE|CODE_PROMO, valeur, dateDebut, dateFin, actif }` — CRUD complet (archivage si déjà utilisée).

**Avoir** : `{ id, venteId, venteNumero, motif, montant, statut: VALIDE|ANNULE, dateEmission, validePar }` — `POST` uniquement (validation = création par un profil habilité) ; `POST /avoirs/{id}/annuler` (pas de suppression, traçabilité conservée).

**Factures** : `GET /factures` = vue en lecture de `/ventes` filtrée `statut=PAYEE`, avec `GET /factures/{id}/pdf` (génération PDF téléchargeable).

## 4. Restaurant (`/tables`, `/commandes-restaurant`, `/plats`, `/categories-plats`)

**Table** : `{ id, numero, capacite, zone, statut: LIBRE|OCCUPEE|RESERVEE, positionX, positionY }` — CRUD ; statut recalculé automatiquement selon les commandes en cours.

**CategoriePlat** : `{ id, nom, ordre }` — CRUD (suppression si vide seulement).

**Plat** : `{ id, nom, prix, categorieId, categorieNom, description, tempsPreparation, disponible, ingredients }` — CRUD (archivage).

**Commande** : `{ id, tableId, tableNumero, type: SUR_PLACE|EMPORTER|LIVRAISON, serveurId, serveurNom, statut: EN_ATTENTE|EN_PREPARATION|PRETE|SERVIE|ANNULEE, lignes: [LigneCommande], total, dateCreation }`
**LigneCommande** : `{ id, platId, platNom, quantite, options, prixUnitaire, montant, statut: EN_ATTENTE|EN_PREPARATION|PRET|SERVI }`
- `POST /commandes-restaurant` crée (table passe à OCCUPEE). Ajout/retrait de lignes autorisé tant que `statut=EN_ATTENTE` (pas encore envoyée en cuisine).
- `POST /commandes-restaurant/{id}/envoyer-cuisine` → passe en EN_PREPARATION, notifie l'écran cuisine.
- `PATCH /commandes-restaurant/{id}/lignes/{ligneId}/statut` met à jour le statut d'une ligne (suivi cuisine).
- `POST /commandes-restaurant/{id}/annuler` — body `{ motif }`, uniquement avant envoi cuisine.

**Temps réel** : WebSocket STOMP sur `/ws` (SockJS). Topics : `/topic/commandes` (création/maj commande), `/topic/tables` (changement de statut table), `/topic/cuisine` (nouvelle commande ou ligne prête). Le frontend s'abonne pour le plan de salle et l'écran cuisine ; en repli, polling REST toutes les 5s si WebSocket indisponible.

## 5. Cave à vin (`/boissons`, `/mouvements-cave`, `/fournisseurs`) & Maquis (`/commandes-maquis`)

**Boisson** : `{ id, nom, type: VIN_ROUGE|VIN_BLANC|CHAMPAGNE|WHISKY|BIERE|SPIRITUEUX|AUTRE, origine, millesime, prixAchatBouteille, prixVenteBouteille, prixVenteVerre, fournisseurId, fournisseurNom, quantiteStock, seuilAlerte, actif }` — CRUD (archivage). Vente possible à la bouteille ou au verre (décrémentation proportionnelle, gérée côté service lors d'une vente).

**MouvementCave** (non supprimable) : `{ id, boissonId, boissonNom, type: ENTREE|SORTIE, motif: ACHAT|VENTE|CASSE|DEGUSTATION, quantite, date }` — `POST` + `GET` (historique) uniquement ; génère une alerte si stock ≤ seuil.

**Fournisseur** : `{ id, nom, contact, telephone, email, conditions, actif }` — CRUD (archivage).

**CommandeMaquis** : `{ id, tableId, clientNom, statut: EN_COURS|CLOTUREE|ANNULEE, lignes: [{ id, articleNom, quantite, prixUnitaire, montant }], total, dateCreation }` — CRUD sur lignes avant clôture ; `POST /commandes-maquis/{id}/cloturer`.

## 6. Stocks (`/produits`, `/categories-produits`, `/entrees-stock`, `/sorties-stock`, `/inventaires`, `/depots`) & Finance (`/recettes`, `/depenses`, `/categories-depenses`, `/rapports-caisse`)

**Produit** : `{ id, code, nom, categorieId, categorieNom, unite, prixAchat, prixVente, seuilAlerte, quantiteStock, depotId, actif }` — CRUD (archivage), `code` unique.
**CategorieProduit** : `{ id, nom }` — CRUD.
**EntreeStock** (non modifiable/supprimable après validation) : `{ id, produitId, produitNom, fournisseur, quantite, prixUnitaire, montant, dateEntree, bonLivraison, statut: BROUILLON|VALIDE }` — `POST`, `PUT` seulement si `statut=BROUILLON`, `POST /{id}/valider`.
**SortieStock** (idem) : `{ id, produitId, produitNom, motif: VENTE|PERTE|CASSE|TRANSFERT, quantite, prixUnitaire, montant, dateSortie, statut }`.
**Inventaire** : `{ id, dateInventaire, type: PHYSIQUE|PARTIEL, statut: EN_COURS|VALIDE, lignes: [{ produitId, produitNom, quantiteTheorique, quantiteReelle, ecart }] }` — `POST` crée, `PUT` modifie les quantités comptées avant validation, `POST /{id}/valider` génère le rapport d'écart.
**Depot** : `{ id, nom, adresse, actif }` — CRUD (archivage), `POST /depots/transferts` pour les transferts inter-dépôts.

**Recette** (non supprimable) : `{ id, source: VENTE|AUTRE, montant, description, date }` — générée automatiquement à l'encaissement d'une vente, + saisie manuelle possible.
**Depense** : `{ id, categorieId, categorieNom, montant, description, fournisseur, justificatifUrl, statut: EN_ATTENTE|VALIDEE, date }` — `POST /depenses/{id}/justificatif` upload multipart.
**CategorieDepense** : `{ id, nom }` — CRUD.
**RapportCaisse** (lecture seule, généré automatiquement) : `{ id, sessionCaisseId, dateGeneration, encaissements, decaissements, solde }`, `GET /rapports-caisse/{id}/pdf`.

`GET /finance/vue-ensemble?periode=jour|semaine|mois` → `{ recettesDuJour, depensesDuJour, beneficeDuJour, evolution7Jours: [{ date, recettes, depenses }] }`.

## 7. Dashboard & Reporting (lecture seule)

- `GET /dashboard/summary` →
  ```json
  {
    "ventesDuJour": 2450000, "recettesDuJour": 3125000, "commandesRestaurant": 128,
    "consommationsMaquis": 86, "beneficeDuJour": 875000,
    "variations": { "ventes": 18.6, "recettes": 21.4, "commandes": 12.3, "consommations": 8.7, "benefice": 14.2 },
    "evolutionVentes": [{ "date": "2026-05-18", "montant": 2100000 }],
    "repartitionActivites": [{ "label": "Ventes", "pourcentage": 40 }],
    "alertes": [{ "type": "STOCK_FAIBLE|RUPTURE|REAPPRO", "niveau": "WARNING|CRITICAL|INFO", "message": "..." }],
    "topProduits": [{ "nom": "Château Margaux", "ventes": 156, "montant": 780000 }],
    "activiteRecente": [{ "type": "VENTE|COMMANDE|STOCK|DEPENSE|PAIEMENT", "libelle": "...", "montant": 150000, "ilYA": "5 min" }]
  }
  ```
- `GET /reporting/ventes?periode=&dateDebut=&dateFin=` → CA, quantités, panier moyen, ventes/jour, top produits.
- `GET /reporting/stocks` → valorisation, ruptures, top produits en stock faible, répartition par catégorie.
- `GET /reporting/recettes-depenses` → détail des flux, recettes/dépenses par jour, marge.
- `GET /reporting/benefices?periode=` → bénéfice net par jour/semaine/mois, évolution.
- Chaque endpoint de reporting a un pendant `/export?format=pdf|excel` renvoyant un fichier binaire.

## 8. Paramètres, Sauvegarde, Aide

- `GET/PUT /parametres` → `{ nomEtablissement, logoUrl, devise, tauxTva, seuilAlerteGlobal, modeSombreParDefaut }`.
- `GET /sauvegardes` (liste des sauvegardes), `POST /sauvegardes` (déclenche une sauvegarde manuelle), `POST /sauvegardes/{id}/restaurer`.
- `GET /aide/faq` liste statique de questions/réponses.

## 9. Comptes de démonstration (seed)

Établissement « Le Consulat » (cave-restaurant, palette rouge bordeaux/crème/or, cf. maquettes).
Utilisateurs de démo (mot de passe `password123` pour tous) : `admin` (Administrateur, ADMIN),
`jean` et `mariam` (Caissier/Serveur, CAISSIER_SERVEUR), `paul` et `sophie` (Gérant, GERANT),
`chef` (Cuisinier, CUISINIER).
Données de démo cohérentes avec les maquettes : produits Château Margaux, Heineken, Côte de Bœuf,
Jack Daniel's, Poulet Braisé, Rhum Diplomático, Champagne Moët, Vin Rouge, tables T1–T12, etc.
