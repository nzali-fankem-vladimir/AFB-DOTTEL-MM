# PLAN — MIGRATION VERS UN MONOLITHE MODULAIRE

## Document maître du chantier MM.0 → MM.6

*Module Dotations Téléphoniques Mensuelles — Backend*

| | |
|---|---|
| **Objet** | Transformer le backend DOTTEL d'un monolithe en couches techniques vers un monolithe modulaire à frontières vérifiées |
| **Livrable** | 7 sous-sprints documentés, exécutables un par un dans des sessions Claude Code distinctes |
| **Durée totale estimée** | 6 à 9 sessions de travail |
| **Prérequis** | Sprint 6F.9 validé et commité (`d9be38c`) — le frontend n'est pas concerné par ce chantier |
| **Périmètre exclu** | Tout passage en microservices. Voir section 6 ci-dessous. |

---

## 0. Avertissement sur l'espace de travail

**Question résolue le 2026-07-31 (décision D de MM.0).** Ce chantier déplace des dizaines de fichiers Java. Il se déroule donc dans une **copie physique isolée**, jamais dans le dépôt de travail principal.

| Élément | Chantier (ici) | Projet d'origine (à ne pas toucher) |
|---|---|---|
| Chemin | `…\implementation\**afb-dottel-mm**` | `…\implementation\afb-dottel` |
| Dépôt git | Neuf, commit initial `cbbb497` | `d9be38c sprint-6F.9` |
| Remote | **aucun** | `origin → github.com/nzali-fankem-vladimir/afb-dottel.git` |
| Base de données | `afb_dotations_telephoniques_mm` | `afb_dotations_telephoniques` |

La copie exclut `.git`, `node_modules`, `target` et `dist`. Elle n'a **aucun remote**, ce qui rend impossible de pousser par erreur sur le dépôt d'origine.

### Vérification à faire au début de CHAQUE session du chantier

```powershell
(Get-Location).Path      # doit se terminer par afb-dottel-mm
git log --oneline -1     # ne doit PAS afficher d9be38c
git remote -v            # doit être vide
```

### Les trois variables d'environnement obligatoires

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/afb_dotations_telephoniques_mm"
$env:DB_PASSWORD="admin"
$env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"
```

`DB_URL` est **nouvelle par rapport au projet d'origine**, qui n'en exigeait que deux. L'oublier fait retomber silencieusement sur la base de production locale du projet d'origine, via le repli du placeholder dans `application-dev.yml`. Contrôle au premier démarrage : Flyway doit appliquer `V1` à `V4` sur une base vierge.

---

## 1. Constat de départ

### 1.1 Organisation actuelle : par couche technique

Le backend est aujourd'hui structuré selon les couches techniques décrites en section 3 de `CLAUDE.md`, toutes à plat sous `backend/src/main/java/com/afriland/dottel/` :

| Package | Contenu réel (fichiers `.java`) |
|---|---|
| `controller/` | 8 contrôleurs : `AuthController`, `BeneficiaireController`, `EnrolementController`, `FonctionEligibleController`, `GrilleTarifaireController`, `PieceJointeController`, `ProcessusMensuelController`, `ReportingController`, `UtilisateurAdminController` |
| `service/` | 23 classes, toutes au même niveau |
| `service/rules/` | 1 seule classe : `EligibiliteService` |
| `repository/` | 10 fichiers : 8 interfaces `JpaRepository` + `AuditLogSpecifications` + `BeneficiaireSpecifications` |
| `model/entity/` | 9 entités JPA |
| `model/enums/` | 5 énumérations |
| `security/` | `GlobalExceptionHandler`, `JwtUtil`, `RoleJwtAuthenticationConverter`, `SecurityConfig` |
| `config/` | `CorsConfig`, `EvenementClotureSerializer`, `KafkaConfig` |
| `exception/` | 32 classes d'exception métier |
| `model/dto/` | 58 fichiers, **déjà découpés par sous-domaine** (`auth/`, `beneficiaire/`, `ehr/`, `enrolement/`, `fonctioneligible/`, `grille/`, `importexcel/`, `processus/`, `reporting/`, `utilisateur/`) |

Conséquence : trois fichiers appartenant au même domaine métier — `controller/BeneficiaireController.java`, `service/BeneficiaireService.java`, `repository/BeneficiaireRepository.java` — vivent dans trois packages différents, et rien dans le langage n'empêche un service d'un domaine d'accéder au repository d'un autre.

Le seul endroit du code où un découpage par domaine existe déjà est `model/dto/`. C'est une base utile : le découpage cible peut s'en inspirer.

### 1.2 Le point favorable : aucune relation JPA entre entités

**Aucune des 9 entités n'utilise `@ManyToOne`, `@OneToMany`, `@OneToOne` ni `@ManyToMany`.** Toutes les tables sont plates, avec des clés étrangères *logiques* portées par de simples champs `Long` non mappés par Hibernate :

| Entité | Clés étrangères logiques (non mappées JPA) |
|---|---|
| `Utilisateur` | `idBeneficiaire` |
| `Beneficiaire` | aucune (`fonction` est un `VARCHAR` libre, **pas** une FK vers `FonctionEligible.code`) |
| `FonctionEligible` | aucune |
| `GrilleTarifaire` | `idFonctionEligible`, `idCreateur`, `idValidateur` |
| `ProcessusMensuel` | `idCreateur` |
| `LigneEtatMensuel` | `idProcessus`, `idBeneficiaire` (+ `fonctionRetenue`, copie figée volontaire) |
| `EtapeWorkflow` | `idProcessus`, `idActeur` |
| `PieceJointe` | `idProcessus` (contrainte `unique`) |
| `AuditLog` | `idUtilisateur`, `entiteCible`/`idEntite` (référence polymorphe) |

C'est une **très bonne nouvelle pour ce chantier** : il n'y a aucun graphe d'objets Hibernate entremêlé à démêler, obstacle habituel numéro un d'une modularisation.

### 1.3 Le point défavorable : tout le couplage est caché dans le code des services

Le revers exact de 1.2 : puisque rien n'est déclaré au niveau du modèle, **l'intégralité de l'intégrité référentielle inter-domaines vit dans les services**, sous forme d'appels manuels à `findById(...)` sur les repositories d'autres domaines. Ce couplage est invisible au compilateur, invisible dans le schéma, et aujourd'hui vérifié par aucun test.

Cinq foyers de couplage ont été identifiés par lecture du code :

| # | Foyer | Nature |
|---|---|---|
| C1 | `ProcessusMensuelService` | **16 dépendances injectées** (lignes 69-84) : 8 repositories + 8 services. Accède directement aux repositories de `Beneficiaire`, `FonctionEligible`, `GrilleTarifaire` et `Utilisateur`. Traité seul en **MM.2**. |
| C2 | Résolution de grille tarifaire | **Trois implémentations distinctes** du même parcours `code fonction → FonctionEligible → GrilleTarifaire ACTIVE + dateFin IS NULL` (détail en MM.3 §2.1). |
| C3 | `FonctionEligibleService` | Mute directement des entités `Beneficiaire` (cascade de renommage de code) et crée directement une `GrilleTarifaire`. Écriture cross-domaine bidirectionnelle. |
| C4 | `DocumentService` | Lit directement `BeneficiaireRepository` et `FonctionEligibleRepository` pour construire le PDF, alors que `LigneEtatMensuel` porte déjà un instantané figé (`fonctionRetenue`). |
| C5 | `ReportingService` / `HistoriqueExportService` | `ReportingService` lit 3 repositories de domaines différents ; `HistoriqueExportService` n'injecte que `ReportingService`. |

Auxquels s'ajoute un couplage transverse de nature différente :

| # | Foyer | Nature |
|---|---|---|
| C6 | `AuditService` | Appelé **en synchrone par 8 services** : `AuthService`, `EnrolementService`, `BeneficiaireService`, `BeneficiaireImportService`, `FonctionEligibleService`, `GrilleTarifaireService`, `ProcessusMensuelService`, `UtilisateurAdminService`. Traité seul en **MM.4**. |

### 1.4 Aucun garde-fou existant

Vérifié dans `backend/pom.xml` et dans l'ensemble de `backend/src/test/` :

- **Spring Modulith** (`org.springframework.modulith`) : **absent**
- **ArchUnit** : **absent**
- **Tests d'architecture** : **aucun**. Recherche de `ArchUnit`, `modulith`, `ArchRule` dans tout `backend/src` → 0 résultat.

Rien n'empêche aujourd'hui de recréer demain le couplage qu'on aura mis six sessions à casser. C'est la raison pour laquelle `ModularityTests` est introduit dès **MM.1**, et non en fin de parcours.

### 1.5 Deux frontières déjà exemplaires

Deux composants n'ont **besoin d'aucun changement** et servent de modèle de ce que « module bien isolé » veut dire dans ce code précis :

- `service/EhrIntegrationService.java` + `service/EhrIntegrationServiceStub.java` — interface et stub en mémoire, zéro dépendance à un repository ou à un autre service.
- `service/EvenementClotureService.java` — ne connaît que `KafkaTemplate`, reçoit tout son état en paramètres depuis l'appelant.

À citer en exemple pendant les revues de MM.2 et MM.3.

---

## 2. Vision cible

Un module = un domaine métier cohérent, contenant **son** contrôleur, **ses** services, **son** repository, **ses** entités et **ses** DTO, exposant une **API publique explicite** et gardant tout le reste invisible aux autres modules.

Le découpage précis (noms des packages, périmètre de chacun) est **une décision d'architecture qui n'est pas prise dans ce document** — elle fait l'objet de MM.0. La proposition soumise à validation y figure.

Trois propriétés doivent être vraies à la fin du chantier :

1. Aucun service n'injecte un repository appartenant à un autre module.
2. Toute communication inter-modules passe soit par une API publique déclarée, soit par un événement applicatif.
3. Un test automatisé échoue si l'une des deux règles ci-dessus est violée.

---

## 3. Les 7 sous-sprints

| Sprint | Objet | Code produit ? | Durée estimée | Bloque sur |
|---|---|---|---|---|
| **MM.0** | Cadrage : trancher 4 questions ouvertes | **Non** | 1 session de discussion | Décisions humaines |
| **MM.1** | Repackaging mécanique + Spring Modulith + `ModularityTests` | Oui | 1 à 2 sessions | MM.0 validé |
| **MM.2** | Refactor de `ProcessusMensuelService` (god service) | Oui | 2 à 3 sessions | MM.1 vert |
| **MM.3** | Casser les 4 couplages restants (C2 à C5) | Oui | 1 à 2 sessions | MM.2 vert |
| **MM.4** | Audit en événementiel (C6) | Oui | 1 session | MM.3 vert |
| **MM.5** | API publiques `@NamedInterface` | Oui | 1 session | MM.2/MM.3 terminés |
| **MM.6** | Vérification finale + documentation générée | Oui (doc) | 0,5 session | Tout le reste |

**Total : 6 à 9 sessions**, hors MM.0.

L'ordre n'est pas négociable sur trois points :
- **MM.1 avant tout le reste** : sans les nouveaux packages et sans `ModularityTests`, les sprints suivants n'ont ni cible ni boussole.
- **MM.2 avant MM.3** : `ProcessusMensuelService` est impliqué dans plusieurs des couplages de MM.3 ; le traiter d'abord évite de refaire deux fois le même travail.
- **MM.5 après MM.2 et MM.3** : marquer les API publiques n'a de sens qu'une fois les appels illégitimes supprimés, sinon on documenterait le couplage au lieu de le casser.

---

## 4. Règle de sécurité applicable à tous les sous-sprints

**Chaque étape se termine par une suite de tests complète au vert avant tout commit.**

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/afb_dotations_telephoniques_mm"
$env:DB_PASSWORD="admin"
$env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"
cd backend
.\mvnw.cmd test
```

Référence de non-régression au démarrage du chantier : **205 tests, 0 échec, 0 erreur, `BUILD SUCCESS`** (mesuré au commit `d9be38c`).

Trois précisions importantes :

1. **Le nombre de tests ne doit jamais diminuer.** Un refactoring qui « fait disparaître » des tests a supprimé de la couverture, ce qui est un échec, pas un succès. S'il augmente, tant mieux.
2. **Ces deux variables d'environnement sont obligatoires** depuis le Sprint 6F.9 : `dottel.security.jwt-secret` n'a plus aucune valeur de repli, et `DottelApplicationTests` charge le contexte Spring complet. Sans elles, le démarrage échoue volontairement sur `Could not resolve placeholder 'DOTTEL_JWT_SECRET'`.
3. **En MM.1, la suite complète tourne après chaque déplacement de fichier**, pas seulement en fin de sprint. Un import cassé se détecte en quelques secondes ; noyé dans 40 déplacements, il coûte une heure.

---

## 5. Ce que ce chantier ne change pas

Pour éviter toute dérive de périmètre, aucun sous-sprint n'a le droit de modifier :

- **Le schéma de base de données.** Aucune migration Flyway n'est prévue. Les scripts `V1` à `V4` de `backend/src/main/resources/db/migration/` restent intacts. Un refactoring de packages Java ne touche pas aux tables.
- **Les contrats API.** Les 34 endpoints de la section 8 de `CLAUDE.md` gardent leurs URL, leurs rôles `@PreAuthorize`, leurs codes HTTP et la forme de leurs DTO de réponse. Le frontend ne doit **rien** avoir à changer.
- **Les règles métier RG-01 à RG-12.** Elles doivent se comporter exactement pareil après qu'avant. C'est précisément le rôle des 205 tests de le prouver.
- **Le frontend.** Aucun fichier sous `frontend/` n'est concerné.
- **La configuration de déploiement.** `Dockerfile`, `k8s/`, `application*.yml` restent en l'état, sauf mention contraire explicite dans un sous-sprint.

---

## 6. Position sur les microservices

Ce chantier **s'arrête au monolithe modulaire**. Aucun passage en microservices n'est engagé, ni préparé de façon irréversible.

Rappel du cadre posé par `CLAUDE.md` :

- **Section 14** : « Chaque module BAOBAB, DOTTEL inclus, est exposé via son propre Service Kubernetes ». DOTTEL est déjà **un** microservice au sein de BAOBAB — la séparation par capacité métier existe donc déjà, mais au niveau de la plateforme, pas à l'intérieur de DOTTEL.
- **Section 14** encore : « Service registry / API Gateway : aucun pour le moment […] ne pas anticiper cette architecture dans le code actuel. »
- **Section 18, point 18** : « Introduire un service registry ou une gateway non demandée par la DSI » figure explicitement dans la liste des erreurs à ne jamais commettre.

Éclater DOTTEL lui-même en plusieurs microservices contredirait la frontière que la DSI a elle-même dessinée, et **exigerait une validation DSI explicite préalable**.

Cela dit, le travail fait ici n'est perdu dans aucun scénario :

- **Si la DSI demande un jour des microservices** : les frontières de modules devenues propres sont directement les frontières de services à extraire ; le plus dur de la conception est déjà fait.
- **Si DOTTEL reste un monolithe** : le code est plus lisible, les couplages cachés sont éliminés, et les tests d'architecture empêchent la dégradation.

---

## 7. Ordre de lecture des documents

| Fichier | À lire |
|---|---|
| `PLAN_MONOLITHE_MODULAIRE.md` | Ce document. Vue d'ensemble. |
| `MM.0_cadrage.md` | **En premier**, avant toute session d'implémentation. Contient 4 questions bloquantes. |
| `MM.1_repackaging_et_modularite.md` | Après validation de MM.0. |
| `MM.2_refactor_processus_mensuel_service.md` | Après MM.1 vert. |
| `MM.3_casser_couplages_restants.md` | Après MM.2 vert. |
| `MM.4_audit_evenementiel.md` | Après MM.3 vert. Contient une question ouverte sur RG-09. |
| `MM.5_api_publiques_named_interface.md` | Après MM.3 et MM.2 terminés. |
| `MM.6_verification_et_documentation.md` | En clôture. |

---

**Fin du document maître** — *MM.0 est la seule étape qui peut démarrer immédiatement*