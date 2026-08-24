# DOTTEL — Dotations Téléphoniques Mensuelles

Module de la plateforme BAOBAB (Afriland First Bank) qui digitalise le
paiement des dotations téléphoniques mensuelles des collaborateurs éligibles.
Il remplace un circuit papier par un workflow électronique à trois niveaux
de validation (ARH → CRH → DRH), depuis l'enrôlement d'un bénéficiaire
jusqu'à la clôture du processus mensuel et la publication d'un événement
vers le module comptable.

Backend Spring Boot 4 (monolithe modulaire, Spring Modulith) et frontend
React 19, connectés à un realm Keycloak partagé pour l'authentification.

---

## Prérequis

- Java 21 (Temurin)
- Maven (ou le wrapper `./mvnw` fourni dans `backend/`)
- Node.js 20+
- PostgreSQL 16
- Docker (pour lancer Keycloak et Kafka en local)

### Variables d'environnement

Liste établie en lisant directement `application.yml`, `application-dev.yml`
et `application-prod.yml` (pas recopiée d'un autre document) : **14
variables**, dont **2 sans valeur de repli** — le démarrage échoue sans elles.

| Variable | Obligatoire | Valeur par défaut (dev) | Rôle |
|---|---|---|---|
| `DB_PASSWORD` | **Oui** | *aucune* | Mot de passe PostgreSQL |
| `DOTTEL_KEYCLOAK_ISSUER_URI` | **Oui** | *aucune* | URL du realm Keycloak (ex. `http://localhost:8180/realms/dottel-dev`) |
| `DB_URL` | non | `jdbc:postgresql://localhost:5432/afb_dotations_telephoniques` | URL JDBC — utiliser une base nommée explicitement en local (ex. `afb_dotations_telephoniques_mm`), le défaut ne le fait pas |
| `DB_USER` | non | `postgres` | Utilisateur PostgreSQL |
| `KAFKA_BOOTSTRAP_SERVERS` | non | `localhost:9092` | Broker Kafka (événement de clôture) |
| `APP_CORS_ALLOWED_ORIGINS` | non | `http://localhost:3000` | Origine autorisée pour le frontend |
| `DOTTEL_NOTIFICATIONS_ENABLED` | non | `false` | `true` active l'envoi SMTP réel, `false` journalise seulement (`NotificationServiceStub`) |
| `DOTTEL_NOTIFICATIONS_EXPEDITEUR` | non | `dottel-noreply@afrilandfirstbank.cm` | Adresse d'expédition des notifications |
| `DOTTEL_MAIL_HOST` | non | *(vide)* | Serveur SMTP, lu seulement si notifications activées |
| `DOTTEL_MAIL_PORT` | non | `587` | Port SMTP |
| `DOTTEL_MAIL_USERNAME` | non | *(vide)* | Identifiant SMTP |
| `DOTTEL_MAIL_PASSWORD` | non | *(vide)* | Mot de passe SMTP |
| `DOTTEL_DOCUMENTS_CHEMIN_STOCKAGE` | non | `./documents/` | Répertoire de stockage des PDF générés |
| `DOTTEL_DOCUMENTS_CHAPITRE_DEFAUT` | non | `37210100` | Repli si l'EHR ne fournit pas de chapitre budgétaire |

En production (`SPRING_PROFILES_ACTIVE=prod`), `DB_URL`, `DB_USER` et
`KAFKA_BOOTSTRAP_SERVERS` deviennent également obligatoires (pas de repli
`localhost`). Les manifests `k8s/configmap.yaml` (non sensible) et
`k8s/secret.yaml` (sensible) tiennent la liste à jour pour un déploiement
Kubernetes.

---

## Démarrage rapide

### 1. Infrastructure locale (Keycloak + Kafka)

```bash
docker compose up -d
```

Démarre Keycloak (realm `dottel-dev`, console sur `http://localhost:8180`,
identifiants dans les variables du conteneur) et Kafka (`localhost:9092`).
Le realm `dottel-dev` fournit 5 comptes de test, un par rôle
(`RoleEnum` : EMPLOYE, ARH, CRH, DRH, ADMIN), avec mot de passe `Test1234`.

### 2. Backend

```bash
export DB_URL="jdbc:postgresql://localhost:5432/afb_dotations_telephoniques_mm"
export DB_PASSWORD="votre_mot_de_passe_postgres"
export DOTTEL_KEYCLOAK_ISSUER_URI="http://localhost:8180/realms/dottel-dev"
cd backend
mvn spring-boot:run
```

Flyway applique automatiquement les migrations au démarrage. API disponible
sur `http://localhost:8080/api`, documentation interactive sur
`http://localhost:8080/api/swagger-ui.html`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Application sur `http://localhost:3000`. La connexion passe par une
redirection Keycloak (Authorization Code + PKCE) — pas de formulaire de
login local.

---

## Architecture

Backend organisé en **monolithe modulaire** (Spring Modulith), 7 modules
métier et 2 modules transverses, chacun avec ses propres sous-packages
(`api`, `controller`, `service`, `repository`, `model`) :

| Module | Rôle |
|---|---|
| `utilisateurs` | Comptes, rôles, administration |
| `beneficiaires` | Enrôlement, gestion des bénéficiaires, import/export, intégration EHR |
| `referentiel` | Fonctions éligibles, grilles tarifaires, règles d'éligibilité |
| `processus` | Processus mensuel, workflow ARH/CRH/DRH, génération de document |
| `reporting` | Tableau de bord, historique, export — lecture agrégée seule |
| `audit` | Journalisation événementielle de toute action métier |
| `notifications` | Notification des acteurs à chaque transition de workflow, découplée de la transaction métier (écouteur `AFTER_COMMIT`, extrait au Sprint MM.13) |
| `security` *(transverse)* | Configuration Spring Security, conversion des rôles JWT, gestion centralisée des erreurs |
| `config` *(transverse)* | CORS, Kafka, sérialisation |

Un module ne peut accéder au repository ou à l'entité d'un autre module : il
passe par l'interface `api/` que ce module expose. `ModularityTests`
(Spring Modulith + ArchUnit) vérifie ces frontières à chaque build.

**Flux d'authentification.** Keycloak (conteneur local en développement,
realm réel de la banque en production) est l'unique émetteur de jetons,
via redirection Authorization Code + PKCE — jamais de mot de passe vu par
l'application. L'identité d'un utilisateur DOTTEL est résolue par
l'adresse email portée par le jeton, pas par le matricule.

**Autres briques** : PostgreSQL 16 (persistance), React 19 + Vite +
Tailwind (frontend), Kafka (événement de clôture consommé par le module
comptable, hors périmètre DOTTEL), iText (génération PDF), Apache POI
(import/export Excel).

Ports : backend `8080` (préfixe `/api`), frontend `3000`.

---

## Modèle de données

**10 tables** (`backend/src/main/resources/db/migration/`, V1 à V8) :
`utilisateurs`, `beneficiaires`, `fonction_eligible`, `grille_tarifaire`,
`ligne_etat_mensuel` (pivot processus ↔ bénéficiaires), `processus_mensuel`,
`etape_workflow`, `piece_jointe`, `regle_eligibilite`, `audit_log`.

25 fonctions éligibles et leurs grilles tarifaires initiales sont insérées
par la migration V2.

---

## API

**42 endpoints réels** (comptés par grep des annotations
`@GetMapping`/`@PostMapping`/`@PatchMapping`/`@DeleteMapping` dans tous les
contrôleurs — voir `CLAUDE.md` section 8 pour la liste complète et les rôles
autorisés par endpoint). Documentation interactive : `/api/swagger-ui.html`
une fois le backend démarré. Contrat détaillé :
`docs/reference/contrats_api_dotations_v3.md`.

Toute route (hors `/actuator/health`) exige un jeton Bearer Keycloak valide.

---

## Règles métier

| Règle | Résumé |
|---|---|
| RG-01 | Une fonction éligible active donne droit à la dotation ; fonction inconnue ou inactive → non éligible |
| RG-02 | Corps de contrôle et assimilés avec grade "NON GRADE" → non éligible |
| RG-03 | Un matricule ne peut être enrôlé qu'une seule fois (409 si doublon) |
| RG-04 | Le montant vient toujours de la grille tarifaire active, jamais d'une valeur codée en dur |
| RG-05 | Séquence de validation stricte ARH → CRH → DRH, aucun saut |
| RG-06 | Un seul PDF par processus, enrichi de signature en signature |
| RG-07 | Tout retour exige un motif textuel non vide |
| RG-08 | Séparation des tâches : l'acteur d'une étape ne peut pas valider l'étape suivante du même processus ou de la même grille |
| RG-09 | Toute modification d'attribut métier est tracée avec un delta avant/après dans `audit_log` |
| RG-10 | Une grille tarifaire suit un circuit ARH → CRH → DRH avant activation ; une seule grille active par fonction à la fois |
| RG-11 | L'import Excel valide chaque ligne, insère les valides, rapporte les rejets |
| RG-12 | Unicité du processus mensuel **normal** par mois/année ; un rattrapage peut en revanche être déclenché sur une période déjà close |

Détail complet dans `CLAUDE.md` section 7.

---

## Rapport de recette

La recette fonctionnelle complète (5 rôles, scénarios RG-08 réels avec
promotion de rôle, workflow de grille à trois acteurs, rattrapage) a été
jouée au Sprint 7.2 : voir `docs/chantier-livraison-mm/7.2_rapport_recette.md`.

---

## Backlog technique

**Vide** — les 4 items techniques hérités de la version d'origine du projet
ont été vérifiés et soldés au Sprint 7.2 (3 déjà réglés en amont, 1 corrigé
par la migration `V8__correction_accents_fonctions_eligibles.sql`).

---

## Points en attente de la DSI

Voir la fiche de liaison DSI (`docs/chantier-livraison-mm/fiche_liaison_dsi.md`)
pour le détail et l'impact de chacun sur la mise en production. En résumé :
realm Keycloak réel de la banque, accès EHR réel, stockage persistant des
PDF, broker Kafka réel, pipeline CI/CD et cluster Kubernetes cible.
