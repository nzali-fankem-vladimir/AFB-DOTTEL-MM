# RÉCAPITULATIF DU CHANTIER MONOLITHE MODULAIRE

Module : Dotations Téléphoniques Mensuelles — Backend

*Projet AFRILAND HORIZON 2030 — Module INTRA*

| **Référence** | **AFB_CHANTIER_MM_RECAP_V1_2026** |
| --- | --- |
| Version | 1.0 |
| Date | 3 août 2026 |
| Sprints couverts | MM.0 (cadrage) à MM.6 (clôture) |
| Périmètre | Restructuration du backend DOTTEL, d'un monolithe en couches techniques vers un monolithe modulaire à 6 modules métier, sans changement de comportement métier ni de contrat API |
| Base de comparaison | Commit `d9be38c` (dépôt d'origine `afb-dottel`, avant chantier) |

---

## 0. Synthèse

Le backend DOTTEL est passé d'une organisation en couches techniques (`controller/`, `service/`, `repository/`, `model/entity/`, tous à plat) à **6 modules métier** (`utilisateurs`, `beneficiaires`, `referentiel`, `processus`, `reporting`, `audit`) plus 2 packages transverses déclarés `OPEN` (`security`, `config`). Un test d'architecture (`ModularityTests`, Spring Modulith + ArchUnit) garde désormais ces frontières et échoue sur toute régression.

`ModularityTests` est **vert**, avec **une seule exception explicitement pinée** : le cycle `beneficiaires` ↔ `referentiel`, issu de deux dépendances légitimes mais opposées (résolution d'éligibilité/grille d'un côté, cascade de renommage de code de fonction de l'autre). Spring Modulith ne permet pas de déclarer un cycle comme légitime — seul un refactor événementiel le romprait réellement. Ce correctif est **programmé sous le nom MM.8**, prérequis réel avant clôture définitive du chantier, pas une note vague.

La documentation d'architecture (PlantUML + canvases Spring Modulith) a été générée et versionnée sous `docs/monolithe-modulaire/architecture/`. La suite de tests est passée de 205 à **226 tests, 0 échec, 0 erreur**. Le frontend n'a été touché par aucun sprint MM et reste fonctionnel (`oxlint`, `npm run build`). Un démarrage réel du backend a confirmé que le repackaging n'a cassé ni le scan de composants Spring ni le scan d'entités JPA.

---

## 1. Tableau AVANT / APRÈS

| Indicateur | Avant (commit `d9be38c`) | Après |
| --- | --- | --- |
| Packages | Couches techniques (`controller/`, `service/`, `service/rules/`, `repository/`, `model/entity/`, `model/dto/`, `security/`, `config/`, `exception/`) | 6 modules métier (`utilisateurs`, `beneficiaires`, `referentiel`, `processus`, `reporting`, `audit`), chacun avec son propre `controller/`, `service/`, `repository/`, `model/`, `exception/` ; `security/` et `config/` restent transverses |
| Modules Spring Modulith déclarés | 0 | 8 (6 modules métier + `security`/`config` déclarés `OPEN`) |
| Tests d'architecture | 0 | 1 (`ModularityTests`) + 1 test de génération de documentation (`DocumentationTests`) |
| Dépendances de `ProcessusMensuelService` | 16 (8 repositories + 8 services, dont 4 repositories étrangers : `Beneficiaire`, `FonctionEligible`, `GrilleTarifaire`, `Utilisateur`) | 13 (repositories/services intra-module `processus` + 6 API publiques d'autres modules : `BeneficiaireApi`, `GrilleTarifaireApi`, `UtilisateurApi`, `EligibiliteService`, `FonctionEligibleApi`, `AuthenticatedUserService`) — **0 repository étranger injecté** |
| Repositories étrangers injectés (tous services) | Plusieurs foyers identifiés en MM.0/MM.1 (C1 à C5) : `ProcessusMensuelService`, `DocumentService`, `ReportingService`/`HistoriqueExportService` | **0**, à une exception explicitement déclarée près : `reporting` lit directement `BeneficiaireRepository`, `ProcessusMensuelRepository`, `LigneEtatMensuelRepository` (lecture agrégée seule, décision R-2 actée en MM.3, déclarée dans `reporting/package-info.java`) |
| Implémentations de la résolution de grille (RG-04) | 3 (dupliquées entre plusieurs services) | 1 (`GrilleTarifaireApi.resoudrePourFonction()`, module `referentiel`) |
| Services injectant `AuditService` de façon synchrone | 8 (`AuthService`, `EnrolementService`, `BeneficiaireService`, `BeneficiaireImportService`, `FonctionEligibleService`, `GrilleTarifaireService`, `ProcessusMensuelService`, `UtilisateurAdminService`) | 0 — audit découplé par `EvenementAudit` + `ApplicationEventPublisher` (MM.4), `AuditServiceImpl` en écouteur |
| Nombre de tests | 205 | 226 |
| Violations `ModularityTests` | Mesurées qualitativement en MM.1 (plusieurs familles a/b/c/d, cf. `MM.1_repackaging_et_modularite.md` §7) — test alors non vert | 1, explicitement filtrée et documentée (cycle `beneficiaires` ↔ `referentiel`, prérequis de correction réel : MM.8) |

---

## 2. Décisions d'architecture actées pendant le chantier

| Décision | Sprint d'origine | Résumé |
| --- | --- | --- |
| Découpage des modules (questions A, B, C de MM.0) | MM.0 (2026-07-31) | **A1** — `EligibiliteService` rattachée au module `referentiel` (sa seule dépendance technique). **B1** — un seul module `referentiel` regroupant `FonctionEligible` et `GrilleTarifaire` (couplage bidirectionnel assumé, décision métier du Sprint 6F.7bis). **C** — 6 modules retenus (`utilisateurs`, `beneficiaires`, `referentiel`, `processus`, `reporting`, `audit`), module `integration` proposé puis **rejeté** (composants répartis dans les modules consommateurs), `processus` non scindé, `security`/`config` transverses, exceptions réparties par module. |
| Espace de travail et base | MM.0 (2026-07-31) | Copie physique isolée `afb-dottel-mm` (sans remote), base dédiée `afb_dotations_telephoniques_mm`, pour ne jamais risquer le dépôt d'origine `afb-dottel` (`d9be38c`). |
| Mode transactionnel de l'audit | MM.4 | Audit découplé en écouteur d'événements applicatifs (`EvenementAudit` + `ApplicationEventPublisher`), plutôt qu'en appel synchrone direct depuis les 8 services métier. |
| Approche reporting (R-1 vs R-2) | MM.3 | **R-2** retenue : `ReportingService`/`HistoriqueExportService` lisent directement 3 repositories étrangers en lecture seule, plutôt que de faire exposer des projections dédiées par chaque module propriétaire (R-1, écartée). Exception architecturale assumée et déclarée dans `reporting/package-info.java`. |
| Cascade de renommage de code de fonction (B-1 vs B-2) | MM.3 | Cascade conservée comme appel direct `referentiel → beneficiaires::api` (et non un événement asynchrone), ce qui est la cause du cycle de modules pinné dans `ModularityTests` — correctif réel prévu en MM.8. |
| Cycle `beneficiaires` ↔ `referentiel` (couplage C3) | MM.5 (isolation), décision de clôture actée le 2026-08-03 | Isolé via `Violations.filter(...)` dans `ModularityTests` plutôt que masqué : le test échoue sur toute violation nouvelle ou différente. Le vrai correctif (événement applicatif, sur le modèle de MM.4) est un **prérequis réel** programmé sous le nom **MM.8**, pas une intention vague. |

---

## 3. Dépendances inter-modules déclarées comme légitimes

| Dépendance (`allowedDependencies`) | Module déclarant | Justification |
| --- | --- | --- |
| `referentiel :: api`, `referentiel :: exception` | `beneficiaires` | Éligibilité et résolution de grille (RG-01, RG-02, RG-04) ; réutilisation intentionnelle de `GrilleTarifaireIntrouvableException` pour préserver le contrat API V3.1 (MM.3). |
| `utilisateurs :: api`, `utilisateurs :: entity` | `beneficiaires`, `processus`, `referentiel` | `AuthenticatedUserService` (acteur courant). La dépendance vers `utilisateurs :: entity` est une **dette tracée**, prérequis MM.8 (voir `utilisateurs/model/entity/package-info.java`). |
| `audit :: api` | `beneficiaires`, `processus`, `referentiel` | Publication de `EvenementAudit` (RG-09, découplage MM.4). |
| `beneficiaires :: api` | `processus`, `referentiel` | `processus` : identité/dotation du bénéficiaire (MM.2). `referentiel` : cascade de renommage de code de fonction vers les bénéficiaires concernés (couplage C3, MM.3) — **c'est cette dépendance, combinée à celle de `beneficiaires` vers `referentiel`, qui forme le cycle pinné dans `ModularityTests`**. |
| `referentiel :: api` | `processus` | Éligibilité et résolution de grille (MM.2). |
| `utilisateurs :: enums` | `processus` | `RoleEnum` pour la séparation des tâches (RG-08, `SeparationTachesService`). |
| `beneficiaires :: repository`, `processus :: repository`, `processus :: entity`, `processus :: enums`, `audit :: api` | `reporting` | Lecture agrégée en lecture seule sur 3 repositories étrangers (décision R-2, MM.3/MM.5) — aucune écriture, méthodes publiques annotées `readOnly = true`. |
| `security` type `OPEN` | `security` | Transverse par nature : `GlobalExceptionHandler` doit voir les exceptions des 6 modules métier ; `JwtUtil` a besoin de `Utilisateur`/`RoleEnum`. |
| `config` type `OPEN` | `config` | Transverse par nature : `KafkaConfig`/`EvenementClotureSerializer` ont besoin de `processus.api.EvenementClotureDto`. |

---

## 4. Points laissés ouverts

- **MM.8 (prérequis réel, non optionnel)** — Casser réellement le cycle `beneficiaires` ↔ `referentiel` par un refactor événementiel (sur le modèle de MM.4) pour la cascade de renommage de code de fonction, plutôt que de le laisser filtré dans `ModularityTests`. Tant que MM.8 n'est pas fait, ce point n'est **pas clos**.
- **Dette tracée `utilisateurs :: entity`** — plusieurs modules (`beneficiaires`, `processus`, `referentiel`) dépendent du retour entité de `AuthenticatedUserService.utilisateurCourant()` plutôt que d'un DTO stable. Documentée dans `utilisateurs/model/entity/package-info.java`, à traiter avec MM.8.
- **MM.7 (Keycloak provisoire)** — sprint distinct déjà documenté (`MM.7_keycloak_provisoire.md`), hors périmètre de MM.6, non traité ici.
- **Passage en microservices** — explicitement hors périmètre du chantier (`PLAN_MONOLITHE_MODULAIRE.md` §6, `CLAUDE.md` sections 14 et 18) ; exigerait une validation DSI explicite préalable.
