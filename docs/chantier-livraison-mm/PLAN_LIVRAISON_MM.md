# PLAN — Livraison (Sprint 7 adapté au monolithe modulaire)

*Module Dotations Téléphoniques Mensuelles — chantier de clôture, copie `afb-dottel-mm`*

## 0. Origine de ce chantier

Ce dossier adapte trois documents rédigés pour la version **monolithique
d'origine** (`sprint7_7_1.md`, `sprint7_7_2.md`, `sprint7_7_3.md`, section 16
de `CLAUDE.md` : Sprint 7.1 à 7.3) à l'état réel de la copie
`afb-dottel-mm` après les chantiers MM.0–MM.14 (repackaging modulaire,
Keycloak provisoire, ajouts métier) et D.0–D.4 (design frontend).

Les trois originaux supposaient une architecture qui n'existe plus dans la
copie : JWT émis en interne (`JwtUtil`/`AuthService`, supprimés au Sprint
MM.7), un backlog technique figé au moment de leur rédaction (dont trois des
quatre items sont déjà soldés — voir 7.2), et un périmètre métier arrêté à
RG-01–RG-12 sans rattrapage ni workflow de grille à trois acteurs. Ils sont
adaptés ici, pas recopiés.

## 1. Les trois documents

| Document | Objet | Écart principal avec l'original |
|---|---|---|
| [7.1_tests_integration_mm.md](7.1_tests_integration_mm.md) | Tests d'intégration bout en bout | Authentification par jeton Keycloak mocké (plus de login JWT interne), 8 flux au lieu de 6 (rattrapage, workflow grille à trois acteurs, notifications), scénarios RG-08 reconstruits sur le vrai risque (promotion de rôle) |
| [7.2_recette_fonctionnelle_mm.md](7.2_recette_fonctionnelle_mm.md) | Recette fonctionnelle et corrections | 3 des 4 items du backlog original déjà soldés (vérifié dans le code) ; scénarios de recette étendus aux écrans et rôles introduits par MM.8–MM.14 et D.0–D.4 |
| [7.3_documentation_livraison_mm.md](7.3_documentation_livraison_mm.md) | README, CLAUDE.md à jour, fiche de liaison DSI | CLAUDE.md est déjà partiellement tenu à jour au fil des chantiers — le travail restant est un écart précis (module `notifications` absent de la section 3, section 13 obsolète, section 16 sans l'historique MM/D), pas une réécriture complète |

## 2. Ce qui a été vérifié avant d'écrire ces guides

Pour ne pas reconduire des instructions caduques, l'état réel de la copie a
été vérifié avant rédaction :

- **7 modules** dans `backend/src/main/java/com/afriland/dottel/` : les 6
  attendus (`utilisateurs`, `beneficiaires`, `referentiel`, `processus`,
  `reporting`, `audit`) plus **`notifications`** (extrait au Sprint MM.13,
  non documenté en section 3 de `CLAUDE.md`).
- **`ModularityTests`** et **`DocumentationTests`** existent déjà (vert
  depuis MM.5) ; aucune classe de test d'intégration métier n'existe encore.
- **Authentification** : `JwtUtil` et `AuthController`/`AuthService`
  n'existent plus. Keycloak (local, `docker-compose.yml`, realm
  `dottel-dev`, port `8180`) est l'unique émetteur depuis MM.7. Les rôles
  sont lus depuis `realm_access.roles`, l'identité résolue par **email**
  (`AuthenticatedUserService`, décision I-2).
- **Backlog technique de l'original Sprint 7.2** : GAP RG-02 déjà corrigé
  (`ProcessusMensuelService` interroge `gradeDe()`), `GET /processus`
  (liste) déjà implémenté, `JwtUtil.genererToken()` n'existe plus (donc
  sans objet). Seul l'écart d'accents dans `V2__insertion_donnees_reference.sql`
  (« Charge » au lieu de « Chargé ») est encore réel — 7 migrations Flyway
  existent déjà (V1 à V7), une correction irait donc en **V8**.
- **Workflow des grilles tarifaires** (MM.12) : `POST /grilles-tarifaires/{id}/valider`
  est un point d'entrée unique partagé CRH/DRH (même modèle que
  `POST /processus/{id}/valider`), la séparation des tâches étant vérifiée
  par `SeparationTachesGrilleService` via les colonnes
  `id_decideur_crh`/`date_decision_crh` (V7).
- **Rattrapage** (MM.11) : RG-12 a évolué vers un index unique **partiel**
  (`idx_processus_mensuel_normal_unique ... WHERE rattrapage = FALSE`,
  V6) — un second processus normal sur le même mois/année échoue toujours
  en 409, mais un rattrapage sur une période déjà close est désormais un
  cas **valide**, pas une violation de RG-12.
- **Notifications** (MM.13) : écouteur `AFTER_COMMIT` avec filet try/catch
  (décision N-2), implémentation par défaut = `NotificationServiceStub`
  (`dottel.notifications.enabled` absent ou `false`) — aucun SMTP requis
  pour les tests.

## 3. Ordre et prérequis

Identique à l'original : 7.1 → 7.2 → 7.3, chacun préalable au suivant.
Prérequis d'entrée : D.4 validé (dernier commit `25a4fb3`, chantier design
clos).

## 4. Rappel — espace de travail

Comme pour tout chantier MM/D, chaque session de ces trois sprints doit
commencer par la vérification d'espace de travail (copie `afb-dottel-mm`,
pas de remote, pas d'historique de l'original). Le bloc exact est répété
dans chacun des trois documents.
