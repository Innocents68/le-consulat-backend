# CAHIER DES CHARGES

## Logiciel de Gestion Intégré « Le Consulat »

---

## 1. Contexte et objectifs du projet

### 1.1 Contexte
« Le Consulat » est un établissement multi-activités (restaurant, cave à vin, maquis, ventes au comptoir) nécessitant un outil unique capable de piloter l'ensemble des opérations quotidiennes : ventes, restauration, stocks, finances et personnel.

### 1.2 Objet du contrat
Le présent contrat a pour objet la conception, le développement, les tests, la livraison, l'installation, la formation des utilisateurs et la mise en service d'un logiciel de gestion intégré dénommé provisoirement **« Logiciel de Gestion Le Consulat »**.

### 1.3 Objectifs
- Centraliser la gestion des ventes, du restaurant, de la cave à vin et du maquis dans un seul outil.
- Fiabiliser le suivi des stocks et réduire les pertes/ruptures.
- Sécuriser les opérations financières et faciliter le contrôle de caisse.
- Fournir une vision claire de l'activité via des tableaux de bord et rapports.
- Offrir une interface simple, rapide et agréable pour le personnel (serveurs, caissiers, cuisine, gérants).

### 1.4 Périmètre
Le logiciel fonctionnera **en ligne**, sous forme d'application web accessible via navigateur (frontend React) connectée en permanence à l'API backend (Spring Boot) hébergée sur un serveur/cloud. Une connexion Internet stable est donc requise sur le site d'exploitation.

---

## 2. Exigences fonctionnelles détaillées (par module et par entité)

> Pour chaque entité métier, le tableau ci-dessous précise les opérations **CRUD** attendues (Créer / Consulter / Modifier / Supprimer), ainsi que les règles de gestion particulières. La suppression est, sauf mention contraire, une **suppression logique** (archivage) et non physique, afin de conserver l'historique et la traçabilité comptable.

### 2.1 Gestion des ventes / caisse

**Entités : Vente (ticket), Ligne de vente, Session de caisse, Remise/Promotion, Avoir**

| Entité | Créer | Consulter | Modifier | Supprimer | Règles particulières |
|---|---|---|---|---|---|
| Vente / Ticket | Nouvelle vente à partir du panier ou d'une commande restaurant | Liste filtrable (date, caisse, caissier, statut) + détail du ticket | Uniquement avant clôture/paiement (ajout/retrait de lignes) | Interdite après paiement ; seule l'émission d'un **avoir** est possible | Numérotation automatique et séquentielle, non modifiable |
| Ligne de vente | Ajout d'un produit/plat au ticket, avec quantité et prix | Détail par ticket | Modifier quantité/prix avant validation | Retirer une ligne avant validation | Le prix appliqué doit tenir compte des remises actives |
| Session de caisse | Ouverture avec fond de caisse initial | Consultation de l'état en temps réel (ventes, encaissements) | — | Clôture de session (Z de caisse) | Une session ne peut être ouverte que par un utilisateur autorisé ; écart théorique/réel calculé automatiquement |
| Remise / Promotion | Créer une règle (%, montant fixe, code promo, plage horaire) | Liste des promotions actives/expirées | Modifier les conditions (dates, montants) | Désactiver ou supprimer une promotion non utilisée | Historique conservé même après désactivation |
| Avoir | Émission liée à un ticket existant, motif obligatoire | Liste des avoirs par période/client | Non modifiable une fois validé | Annulation possible avec traçabilité (pas de suppression) | Validation par un profil habilité (gérant) |

**Fonctionnalités transverses :** encaissement multi-modes de paiement (espèces, mobile money, carte) ; édition de factures/tickets personnalisés (logo, en-tête) ; gestion de plusieurs caisses simultanées ; gestion multi-devises (optionnelle).

---

### 2.2 Gestion du restaurant

**Entités : Table, Commande, Ligne de commande, Menu/Plat, Catégorie**

| Entité | Créer | Consulter | Modifier | Supprimer | Règles particulières |
|---|---|---|---|---|---|
| Table | Ajouter une table (numéro, capacité, zone) au plan de salle | Vue plan de salle avec statut (libre/occupée/réservée) | Modifier position, capacité, zone | Retirer une table du plan | Statut mis à jour automatiquement selon les commandes en cours |
| Commande | Créer une commande liée à une table, un client à emporter ou une livraison | Suivi en temps réel par table/statut | Ajouter/retirer des plats tant que non envoyée en cuisine | Annuler avant envoi cuisine (motif requis) | Fusion/partage d'addition entre tables possible |
| Ligne de commande | Ajout d'un plat avec quantité, options, suppléments | Détail par commande | Modifier quantité/options avant préparation | Retirer avant préparation | Statut individuel (en attente / en préparation / prêt / servi) |
| Menu / Plat | Créer une fiche plat (nom, prix, catégorie, ingrédients, temps de préparation) | Catalogue consultable par catégorie | Modifier prix, disponibilité, description | Retirer un plat (archivage, historique conservé) | Gestion des plats du jour et ruptures ponctuelles |
| Catégorie | Créer une catégorie de menu | Liste des catégories | Renommer, réordonner | Supprimer si vide | — |

**Fonctionnalités transverses :** écran cuisine (ou impression automatique bon de préparation) ; gestion des statuts de commande en temps réel.

---

### 2.3 Gestion de la cave à vin

**Entités : Référence boisson, Mouvement de stock, Fournisseur**

| Entité | Créer | Consulter | Modifier | Supprimer | Règles particulières |
|---|---|---|---|---|---|
| Référence boisson | Créer une fiche (nom, type, origine, millésime, prix d'achat/vente, fournisseur) | Fiche détaillée + niveau de stock | Modifier prix, fournisseur, description | Archiver si non vendue | Vente possible à la bouteille ou au verre (décrémentation proportionnelle) |
| Mouvement de stock | Enregistrer une entrée (achat) ou sortie (vente/casse/dégustation) | Historique complet par référence et par période | Correction d'un mouvement erroné (avec justification) | Non supprimable (traçabilité comptable) | Génère une alerte si le seuil minimal est atteint |
| Fournisseur | Créer une fiche fournisseur (contact, conditions) | Liste des fournisseurs et historique des achats | Modifier coordonnées/conditions | Archiver | — |

**Fonctionnalités transverses :** alertes de réapprovisionnement automatiques (seuil configurable) ; historique des entrées/sorties.

---

### 2.4 Gestion du maquis

**Entités : Commande maquis, Consommation**

| Entité | Créer | Consulter | Modifier | Supprimer | Règles particulières |
|---|---|---|---|---|---|
| Commande maquis | Créer une commande client (boissons, grillades, etc.) | Suivi en temps réel | Ajouter/retirer des articles avant clôture | Annuler avant clôture | Peut être liée à une table ou à un client de passage |
| Consommation | Enregistrer un article consommé | Historique par client/table/période | Corriger avant clôture de la commande | — | Alimente automatiquement le tableau de bord du maquis |

**Fonctionnalités transverses :** tableau de bord d'activité journalière, mensuelle et annuelle propre au maquis.

---

### 2.5 Gestion des stocks

**Entités : Produit, Catégorie, Entrée de stock, Sortie de stock, Inventaire, Dépôt**

| Entité | Créer | Consulter | Modifier | Supprimer | Règles particulières |
|---|---|---|---|---|---|
| Produit | Créer une fiche produit (nom, code, catégorie, unité, seuil d'alerte, prix) | Fiche + niveau de stock en temps réel | Modifier prix, seuil, catégorie | Archiver (historique conservé) | Code produit unique (code-barres possible) |
| Catégorie | Créer une catégorie de produits | Liste | Renommer | Supprimer si vide | — |
| Entrée de stock | Enregistrer une réception (fournisseur, quantité, date, bon de livraison) | Historique des entrées par produit/période | Corriger avant validation comptable | Non supprimable après validation | Impacte automatiquement la valorisation du stock |
| Sortie de stock | Enregistrer une sortie (vente, perte, casse, transfert) | Historique des sorties | Corriger avant validation | Non supprimable après validation | Motif obligatoire pour les sorties hors vente |
| Inventaire | Lancer un inventaire (physique ou partiel) | Historique des inventaires et écarts | Modifier les quantités comptées avant validation | — | Génère automatiquement un rapport d'écart (théorique vs réel) |
| Dépôt / Magasin | Créer un dépôt (si multi-sites) | Liste des dépôts et stocks associés | Modifier les informations | Archiver | Permet les transferts inter-dépôts |

**Fonctionnalités transverses :** valorisation des stocks (FIFO ou CMUP, à définir avec le prestataire) ; alertes de rupture et de péremption.

---

### 2.6 Gestion financière

**Entités : Recette, Dépense, Rapport de caisse (Z), Catégorie de dépense**

| Entité | Créer | Consulter | Modifier | Supprimer | Règles particulières |
|---|---|---|---|---|---|
| Recette | Enregistrement automatique depuis les ventes, ou saisie manuelle (autres recettes) | Liste par jour/mois/année | Correction avec justification (profil habilité uniquement) | Non supprimable (traçabilité) | Rapprochement automatique avec les tickets de caisse |
| Dépense | Saisir une dépense (montant, catégorie, justificatif, fournisseur) | Liste filtrable par période/catégorie | Modifier avant validation comptable | Non supprimable après validation | Pièce justificative attachable (photo/scan) |
| Catégorie de dépense | Créer une catégorie (achats, salaires, entretien, etc.) | Liste | Renommer | Supprimer si inutilisée | — |
| Rapport de caisse (Z) | Généré automatiquement à la clôture de session | Consultation et export | Non modifiable | Non supprimable | Document de référence pour le contrôle de caisse |

**Fonctionnalités transverses :** états financiers simplifiés (résultat journalier/mensuel/annuel) ; export comptable (Excel, PDF).

---

### 2.7 Gestion des utilisateurs

**Entités : Utilisateur, Profil/Rôle, Droit d'accès, Journal des opérations**

| Entité | Créer | Consulter | Modifier | Supprimer | Règles particulières |
|---|---|---|---|---|---|
| Utilisateur | Créer un compte (nom, identifiant, mot de passe/PIN, profil) | Liste des utilisateurs et statut (actif/inactif) | Modifier informations, réinitialiser mot de passe | Désactiver (jamais de suppression physique) | Un utilisateur désactivé conserve son historique d'actions |
| Profil / Rôle | Créer un profil type (gérant, caissier, serveur, cuisinier, comptable, admin) | Liste des profils et droits associés | Modifier les droits d'un profil | Supprimer si aucun utilisateur associé | Un utilisateur peut avoir un ou plusieurs profils |
| Droit d'accès | Attribuer un droit (lecture/création/modification/suppression) par module | Matrice des droits par profil | Modifier les droits | Retirer un droit | Doit être vérifié à chaque action sensible (ex. : suppression, remboursement) |
| Journal des opérations | Généré automatiquement à chaque action | Consultation filtrable (utilisateur, module, date, type d'action) | Non modifiable | Non supprimable | Élément clé de sécurité et d'audit |

**Fonctionnalités transverses :** authentification sécurisée (mot de passe, PIN ou code) ; déconnexion automatique après inactivité.

---

### 2.8 Reporting (module de lecture et d'export, sans CRUD classique)

| Rapport | Contenu | Filtres disponibles | Export |
|---|---|---|---|
| Rapport de ventes | CA, quantités vendues, panier moyen | Période, produit, employé, mode de paiement | PDF / Excel |
| Rapport de stocks | Mouvements, valorisation, ruptures | Période, catégorie, dépôt | PDF / Excel |
| Rapport de recettes/dépenses | Détail des flux financiers | Période, catégorie | PDF / Excel |
| Tableau de bord de gestion | CA, marge, top produits, indicateurs clés | Période, module | Vue écran + export |
| Rapport des bénéfices | Bénéfice net par jour/semaine/mois | Période | PDF / Excel |

**Fonctionnalités transverses :** envoi automatique programmé des rapports par e-mail ; personnalisation des indicateurs affichés sur le tableau de bord principal.

---

## 3. Architecture de navigation (menu de l'application)

| # | Module | Icône |
|---|--------|-------|
| 1 | Tableau de bord | 🏠 |
| 2 | Ventes / Caisse | 💰 |
| 3 | Factures | 🧾 |
| 4 | Gestion des caisses | 💳 |
| 5 | Remises et promotions | 🎁 |
| 6 | Avoirs | ↩️ |
| 7 | Gestion des tables | 🍽️ |
| 8 | Commandes restaurant | 📝 |
| 9 | Suivi cuisine | 👨‍🍳 |
| 10 | Menus et tarifs | 🍲 |
| 11 | Cave à vin | 🍷 |
| 12 | Stocks et inventaire | 📦 |
| 13 | Entrées en stock | 📥 |
| 14 | Sorties de stock | 📤 |
| 15 | Gestion financière | 💰 |
| 16 | Dépenses | 💸 |
| 17 | Utilisateurs | 👥 |
| 18 | Profils et droits | 🔐 |
| 19 | Journal des opérations | 📜 |
| 20 | Dashboard reporting | 📊 |
| 21 | Rapport des ventes | 📈 |
| 22 | Rapport des stocks | 📦 |
| 23 | Rapport des recettes | 💵 |
| 24 | Rapport des bénéfices | 💹 |
| 25 | Paramètres généraux | ⚙️ |
| 26 | Sauvegarde / Restauration | 💾 |
| 27 | Aide / Support | ❓ |

*(Modules 25 à 27 ajoutés : indispensables pour la configuration, la sécurité des données et l'assistance aux utilisateurs.)*

---

## 4. Charte graphique et ergonomie

| Élément | Couleur / usage |
|---|---|
| 🔴 Rouge bordeaux | Couleur principale (en-têtes, boutons d'action, éléments de marque) |
| 🤍 Blanc / crème | Fond d'interface, lisibilité et confort visuel |
| 🖤 Texte sombre | Texte principal, excellente lisibilité |
| 🟡 Doré discret | Accents premium (bordures, séparateurs, icônes actives) |
| 🟢 Vert | Statuts positifs (paiement validé, stock disponible, table libre) |
| 🟠 Orange | Statuts d'alerte (stock faible, commande en attente) |
| 🔴 Rouge (statut) | Statuts critiques (rupture de stock, écart de caisse, erreur) |

### Exigences d'ergonomie
- Interface responsive (adaptée tablette et écran tactile pour la prise de commande) ;
- Navigation par menu latéral fixe avec icônes + libellés ;
- Mode sombre optionnel (confort en salle le soir) ;
- Temps de réponse à l'écran inférieur à 2 secondes pour les actions courantes (encaissement, prise de commande).

---

## 5. Architecture technique

### 5.1 Stack technologique imposée

| Couche | Technologie | Précisions |
|---|---|---|
| Frontend | **React** (JavaScript/TypeScript) | Interface web responsive, tactile (tablette) ; state management (Redux, Zustand ou Context API, au choix du prestataire) ; librairie UI conseillée (ex. Tailwind CSS / MUI) respectant la charte graphique du point 4 |
| Backend | **Spring Boot** (Java 17) | Exposition d'une **API REST** sécurisée (JSON) consommée par le frontend React ; architecture en couches (Controller / Service / Repository) |
| Base de données | **PostgreSQL** | Hébergée sur le même VPS Hostinger que le backend (ou instance séparée selon le volume de données) ; sauvegardes automatiques planifiées |
| Authentification | **JWT** (JSON Web Token) ou session sécurisée | Gestion des rôles/droits directement liée à la matrice de droits du point 2.7 |
| Communication temps réel | WebSocket ou polling (à définir) | Nécessaire pour le suivi cuisine en direct et les mises à jour du plan de salle |
| Impression | Intégration imprimantes thermiques (tickets/bons cuisine) | Via service d'impression réseau ou plugin navigateur/OS |

### 5.2 Architecture applicative

- **Frontend React** : Single Page Application (SPA), consommant exclusivement l'API REST du backend (aucun accès direct à la base de données) ; découpage par module (Ventes, Restaurant, Stocks, Finances, Utilisateurs, Reporting) correspondant au menu du point 3.
- **Backend Spring Boot** : expose les endpoints REST versionnés (ex. `/api/v1/...`) ; gère la logique métier, les règles CRUD et de validation décrites au point 2 ; gère la sécurité (Spring Security) et les droits d'accès par rôle.
- **Base de données** : modèle relationnel reflétant les entités du point 2 (Vente, Ligne de vente, Produit, Utilisateur, etc.), avec clés étrangères assurant l'intégrité référentielle.

### 5.3 Modes de déploiement

- **Hébergement** : plan **VPS Hostinger** (le backend Java/Spring Boot et sa base PostgreSQL nécessitent un VPS, non compatible avec l'hébergement mutualisé classique).
- **Backend** : conteneurisé (Docker), déployé sur le VPS Hostinger.
- **Base de données** : PostgreSQL installée sur le VPS (installation en un clic disponible chez Hostinger) ou sur une instance séparée si le volume de données le justifie.
- **Frontend** : build React déployé en **application web en ligne**, accessible depuis n'importe quel poste (PC, tablette) via navigateur, sur le réseau de l'établissement et/ou via Internet.
- **Environnements** : prévoir a minima un environnement de test/recette et un environnement de production distincts.
- **Nom de domaine et certificat SSL** : à prévoir pour un accès sécurisé (HTTPS) à l'application.
- **Mise à jour** : mécanisme de mise à jour du frontend et du backend sans perte de données ni interruption prolongée de service, avec versionning (ex. via Git) et déploiement continu (CI/CD) si possible.

### 5.4 Documentation technique attendue

- Documentation de l'API REST (ex. Swagger/OpenAPI généré automatiquement par Spring Boot) ;
- Schéma de la base de données (modèle entité-relation) ;
- Guide d'installation et de déploiement (Docker Compose ou équivalent) ;
- Code source versionné et livré (dépôt Git privé transmis au client).

---

## 6. Exigences non fonctionnelles

### 6.1 Performance
- Le système doit supporter l'utilisation simultanée d'au moins [X] postes/utilisateurs sans dégradation notable.
- Temps de génération des rapports inférieur à 10 secondes pour des périodes standards (jour, mois).

### 6.2 Sécurité
- Chiffrement des données sensibles (mots de passe, données financières) ;
- Sauvegardes automatiques régulières (quotidienne a minima) avec possibilité de restauration ;
- Journalisation infalsifiable des opérations critiques (caisse, stocks, suppression de données) ;
- Déconnexion automatique après inactivité prolongée.

### 6.3 Disponibilité et continuité
- Le logiciel fonctionnant en ligne, une **connexion Internet stable et redondante** (ex. double opérateur ou 4G de secours) est fortement recommandée sur le site d'exploitation ;
- Message clair à l'utilisateur en cas de perte de connexion, avec tentative de reconnexion automatique ;
- Sauvegardes automatiques régulières côté serveur pour limiter tout risque de perte de données en cas d'incident ;
- Procédure de reprise après incident serveur (redémarrage automatique, monitoring, alerte à l'administrateur).

### 6.4 Compatibilité
- Compatible avec les imprimantes thermiques standards (tickets de caisse, bons de cuisine) ;
- Compatible avec les terminaux de paiement mobile money/carte courants sur le marché local ;
- Export de données dans des formats standards (PDF, Excel, CSV).

### 6.5 Évolutivité
- Architecture modulaire (frontend React en composants réutilisables, backend Spring Boot en modules/packages par domaine métier) permettant l'ajout futur de fonctionnalités (ex. : réservation en ligne, programme de fidélité, application mobile client).

---

## 7. Livrables attendus

1. Spécifications fonctionnelles détaillées (validées avec le client) ;
2. Maquettes graphiques (UI/UX) de chaque module ;
3. Code source complet : **frontend React** et **backend Spring Boot**, livré via dépôt Git ;
4. Base de données documentée (schéma entité-relation) ;
5. Documentation de l'API REST (Swagger/OpenAPI) ;
6. Fichiers de configuration et scripts de déploiement (ex. Docker Compose) ;
7. Manuel d'utilisation pour chaque profil (gérant, caissier, serveur, cuisine, comptable) ;
8. Formation des utilisateurs sur site ;
9. Documentation technique (pour la maintenance future) ;
10. Support et garantie post-livraison (durée à définir, ex. 3 à 6 mois).

---

## 8. Modalités de réalisation

### 8.1 Phasage indicatif
| Phase | Description | Durée estimée |
|---|---|---|
| 1 | Analyse et validation des spécifications | À définir |
| 2 | Conception (maquettes, modèle de données, contrats d'API) | À définir |
| 3 | Développement backend (Spring Boot) | À définir |
| 4 | Développement frontend (React) | À définir |
| 5 | Intégration front/back et tests | À définir |
| 6 | Recette et corrections | À définir |
| 7 | Formation et déploiement | À définir |
| 8 | Support post-lancement | À définir |

### 8.2 Critères de réception
Le logiciel sera réceptionné après :
- Vérification de la conformité fonctionnelle avec le présent cahier des charges ;
- Tests réussis en conditions réelles (au moins une journée complète d'exploitation test) ;
- Validation de la formation des utilisateurs clés ;
- Absence de bug bloquant.

---

## 9. Points à clarifier avec le prestataire

- Quel plan VPS Hostinger (CPU/RAM/stockage) selon le nombre d'utilisateurs simultanés attendu ?
- Nom de domaine à utiliser et gestion du certificat SSL ?
- Fournisseur d'accès Internet et solution de secours prévue en cas de coupure sur le site ?
- Moyens de paiement mobile money à intégrer (nom des opérateurs, disponibilité d'une API) ?
- Besoin d'une application mobile complémentaire (gérant/livreur) à terme, pouvant réutiliser la même API Spring Boot ?
- Qui gèrera l'administration du VPS et les mises à jour après la livraison (le client ou le prestataire) ?
- Budget et délai global souhaités ?

---

*Document à valider et compléter avec le prestataire retenu avant signature du contrat de développement.*
