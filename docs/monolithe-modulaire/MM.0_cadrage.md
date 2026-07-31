# SPRINT MM.0 — **CLÔTURÉ le 2026-07-31**

## Cadrage — décisions actées

*Module Dotations Téléphoniques Mensuelles — Backend*

| | |
|---|---|
| **Objet** | Trancher les 4 questions ouvertes qui conditionnent tout le découpage modulaire |
| **Livrable** | Ce document complété avec les décisions actées, daté et signé |
| **Durée** | Une session de discussion — aucune écriture de code |
| **Prérequis** | `PLAN_MONOLITHE_MODULAIRE.md` lu |
| **Statut** | ✅ **Les 4 décisions sont actées.** MM.1 peut démarrer. |

> ## Récapitulatif des décisions — à lire en premier
>
> | # | Décision | Retenu |
> |---|---|---|
> | **A** | `EligibiliteService` | **A1** — rattaché au module `referentiel`, exposé dans son API publique |
> | **B** | `FonctionEligible` + `GrilleTarifaire` | **B1** — un seul module `referentiel` |
> | **C** | Découpage | Proposition retenue, avec `processus` non scindé, `reporting` module à part entière, `integration` **non** regroupé (réparti), exceptions par module, `security`/`config` transverses |
> | **D** | Espace de travail | **Copie physique** `afb-dottel-mm` + base **`afb_dotations_telephoniques_mm`** |
>
> Le détail et le motif de chaque décision figurent en fin de section correspondante.

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Discussion et arbitrage (étapes 1-4) | Opus 4.8 ou Sonnet 5 | Medium |

Sprint sans code. L'effort High n'apporterait rien ici : la difficulté n'est pas technique, elle est décisionnelle. Ce qui compte est que les quatre réponses soient **écrites** avant MM.1, pas qu'elles soient trouvées vite.

## 0. Nouvelle session : rappel Graphify

```
/graphify . --update
```

Le graphe complet a été régénéré le 2026-07-31 (1893 nœuds, 5056 arêtes, 135 communautés). Un `--update` suffit tant qu'aucun fichier Java n'a bougé.

## 1. Contexte

`PLAN_MONOLITHE_MODULAIRE.md` établit le constat : le backend est organisé en couches techniques, tout le couplage inter-domaines vit dans le code des services, et aucun test ne garde les frontières.

Avant de déplacer le moindre fichier, quatre questions doivent recevoir une réponse écrite. Chacune, laissée en suspens, produirait un découpage instable qu'il faudrait défaire ensuite — et **une frontière de module mal choisie coûte un refactoring complet**, pas une simple correction.

**Aucune de ces quatre questions n'est tranchée dans ce document.** Ce qui suit sont des propositions argumentées, à valider, amender ou rejeter.

## 2. Étape 1. Question A — Où doit vivre `EligibiliteService` ?

### Constat factuel

`backend/src/main/java/com/afriland/dottel/service/rules/EligibiliteService.java` est la **seule** classe du sous-package `service/rules/`. Elle n'injecte qu'une dépendance : `FonctionEligibleRepository`. C'est une règle métier pure, sans état.

Elle est appelée par **trois services appartenant à trois domaines différents** :

| Appelant | Domaine de l'appelant |
|---|---|
| `service/EnrolementService.java` (ligne 47) | Enrôlement |
| `service/BeneficiaireService.java` | Bénéficiaires |
| `service/ProcessusMensuelService.java` | Processus mensuel |

Elle porte les règles **RG-01** (fonction active dans `fonction_eligible`) et **RG-02** (grade `NON GRADE` interdit pour les corps de contrôle et assimilés), toutes deux définies en section 7 de `CLAUDE.md`.

### Les trois options

| Option | Description | Conséquence |
|---|---|---|
| **A1** | La rattacher au module **référentiel** (avec `FonctionEligible`) | Cohérent : c'est la seule dépendance qu'elle a. Mais le référentiel devient exportateur d'une règle qui concerne surtout les bénéficiaires. |
| **A2** | En faire un **module partagé dédié** (`eligibilite`, ou `regles`) | Frontière explicite, une seule raison de changer. Mais crée un module minuscule (1 classe) — sur-découpage possible. |
| **A3** | La dupliquer / l'inliner dans chaque module appelant | **À écarter** : RG-01 et RG-02 seraient réimplémentées 3 fois, avec un risque de divergence directement contraire à `CLAUDE.md` section 18 point 13. |

### Proposition soumise à validation

**A1 — rattacher au module référentiel**, et l'exposer dans son API publique. Motif : sa seule dépendance technique est `FonctionEligibleRepository`, et les deux règles qu'elle porte sont des règles *sur le référentiel* (« cette fonction est-elle éligible ? »), pas des règles *sur le bénéficiaire*.

**Ce n'est pas une décision actée.** A2 est défendable si tu préfères une frontière explicite pour les règles métier partagées.

### Réponse à consigner

```
DÉCISION A — Où vit EligibiliteService :
[X] A1 — module référentiel
[ ] A2 — module dédié
[ ] Autre

Décidé le : 2026-07-31
```

**Motif retenu.** Sa seule dépendance technique est `FonctionEligibleRepository`, et RG-01/RG-02 sont des règles portant sur l'**éligibilité d'une fonction**, pas sur le bénéficiaire.

**Conséquence pour MM.1** : `service/rules/EligibiliteService.java` rejoint le module `referentiel`. Le sous-package `service/rules/` disparaît — il ne contenait que cette classe.

**Conséquence pour MM.2 et MM.3** : `EligibiliteService` doit figurer dans l'API publique du module `referentiel`, puisque `beneficiaires` (via `EnrolementService` et `BeneficiaireService`) et `processus` l'appellent tous les trois. Cohérent avec l'API de résolution de grille conçue en MM.2, qui vit dans le même module.

## 3. Étape 2. Question B — `FonctionEligible` et `GrilleTarifaire` : un module ou deux ?

### Constat factuel

Ces deux entités sont fortement liées dans le code réel :

- `GrilleTarifaire.idFonctionEligible` est une clé étrangère logique vers `FonctionEligible`.
- `service/FonctionEligibleService.java` injecte `GrilleTarifaireRepository` et **crée directement une `GrilleTarifaire`** lors de la création d'une fonction éligible. C'est documenté dans `docs/reference/contrats_api_dotations_v3.md` §3bis : « la grille tarifaire initiale est obligatoire dans le même appel, et passe directement au statut ACTIVE ».
- `service/GrilleTarifaireService.java` injecte `FonctionEligibleRepository` pour résoudre le code et le libellé de la fonction.

Le couplage est donc **bidirectionnel et volontaire**, ancré dans une décision métier actée au Sprint 6F.7bis.

En revanche, leurs cycles de vie diffèrent : `FonctionEligible` est un référentiel stable géré par l'ADMIN, tandis que `GrilleTarifaire` suit un workflow de validation ARH → DRH (RG-10) avec quatre statuts.

### Les deux options

| Option | Description | Conséquence |
|---|---|---|
| **B1** | **Un seul module `referentiel`** contenant `FonctionEligible` + `GrilleTarifaire` | Le couplage bidirectionnel devient interne au module, donc légitime. Aucune API à inventer entre les deux. Module plus gros. |
| **B2** | **Deux modules** (`referentiel` et `tarification`) avec une API entre eux | Frontières plus fines. Mais il faut concevoir une API dans les **deux sens**, et la création couplée fonction+grille devient un appel inter-modules transactionnel. |

### Proposition soumise à validation

**B1 — un seul module `referentiel`.** Motif : le couplage bidirectionnel n'est pas un accident à corriger, c'est une décision métier documentée. Deux modules qui s'appellent mutuellement dans les deux sens ne sont pas deux modules — c'est un module avec une frontière artificielle au milieu.

**Ce n'est pas une décision actée.** B2 se défend si la tarification doit évoluer indépendamment du référentiel à moyen terme.

### Réponse à consigner

```
DÉCISION B — Référentiel et grilles :
[X] B1 — un seul module, nom retenu : referentiel
[ ] B2 — deux modules

Décidé le : 2026-07-31
```

**Motif retenu.** Le couplage bidirectionnel entre `FonctionEligible` et `GrilleTarifaire` n'est pas un accident à corriger : c'est une décision métier actée au Sprint 6F.7bis (création couplée fonction + grille initiale ACTIVE, documentée au contrat API §3bis).

**Conséquence directe et importante pour MM.3** : la création d'une `GrilleTarifaire` par `FonctionEligibleService` devient du **couplage intra-module, donc légitime**. Le couplage C3 se réduit à la seule cascade de renommage vers `Beneficiaire` — voir la consigne explicite en MM.3 étape 3 (« si la décision B de MM.0 a retenu un module référentiel unique, NE TOUCHE PAS à la création de GrilleTarifaire »).

## 4. Étape 3. Question C — Nom exact et périmètre de chaque module

### Proposition soumise à validation

Découpage proposé sur la base des 23 services, 8 contrôleurs, 10 repositories et 9 entités réellement présents. Le préfixe complet serait `com.afriland.dottel.<module>`.

| Module proposé | Contrôleurs | Services | Repositories | Entités |
|---|---|---|---|---|
| **`utilisateurs`** | `AuthController`, `UtilisateurAdminController` | `AuthService`, `UtilisateurAdminService`, `AuthenticatedUserService` | `UtilisateurRepository` | `Utilisateur` |
| **`beneficiaires`** | `BeneficiaireController`, `EnrolementController` | `BeneficiaireService`, `BeneficiaireImportService`, `BeneficiaireExportService`, `EnrolementService` | `BeneficiaireRepository`, `BeneficiaireSpecifications` | `Beneficiaire` |
| **`referentiel`** | `FonctionEligibleController`, `GrilleTarifaireController` | `FonctionEligibleService`, `GrilleTarifaireService`, *(+ `EligibiliteService` si A1)* | `FonctionEligibleRepository`, `GrilleTarifaireRepository` | `FonctionEligible`, `GrilleTarifaire` |
| **`processus`** | `ProcessusMensuelController`, `PieceJointeController` | `ProcessusMensuelService`, `SeparationTachesService`, `EcartMensuelService`, `DocumentService`, `SignatureService`, `SignatureServiceAutonome` | `ProcessusMensuelRepository`, `LigneEtatMensuelRepository`, `EtapeWorkflowRepository`, `PieceJointeRepository` | `ProcessusMensuel`, `LigneEtatMensuel`, `EtapeWorkflow`, `PieceJointe` |
| **`reporting`** | `ReportingController` | `ReportingService`, `HistoriqueExportService` | *(aucun en propre — voir MM.3 §2.4)* | *(aucune)* |
| **`audit`** | *(aucun)* | `AuditService`, `AuditServiceImpl` | `AuditLogRepository`, `AuditLogSpecifications` | `AuditLog` |
| **`integration`** | *(aucun)* | `EhrIntegrationService`, `EhrIntegrationServiceStub`, `EvenementClotureService`, `NotificationService`, `NotificationServiceStub` | *(aucun)* | *(aucune)* |

### Trois points de ce découpage qui méritent discussion

**C.1 — Le module `processus` est gros.** Il concentre 4 entités, 4 repositories et 6 services. C'est le cœur métier du module DOTTEL, donc c'est attendu — mais si tu veux le scinder (par exemple isoler `DocumentService`/`SignatureService` dans un module `documents`), c'est maintenant qu'il faut le dire. Note que `PieceJointe` a une contrainte `unique` sur `idProcessus` (RG-06) et que `DocumentService` gère le compteur `nombre_signatures` de 1 à 3 : les deux sont indissociables du cycle de vie du processus.

**C.2 — Le module `reporting` n'aurait aucune entité ni repository en propre.** C'est volontaire : `ReportingService` ne fait que de la lecture agrégée sur les données d'autres modules. MM.3 §2.4 traite précisément de la façon dont il obtiendra ces données sans accéder aux repositories étrangers. Question ouverte : est-ce un vrai module, ou une couche applicative au-dessus des modules ?

**C.3 — Le module `integration` regroupe des frontières externes hétérogènes** (EHR, Kafka, notifications). Elles partagent une propriété — être des ports vers l'extérieur — mais pas un domaine métier. Alternative : les rattacher chacune au module qui les consomme (`EhrIntegrationService` → `beneficiaires`, `EvenementClotureService` → `processus`, `NotificationService` → `processus`). Cette alternative a un mérite : `EhrIntegrationService` et `EvenementClotureService` sont déjà exemplaires en termes d'isolation (voir `PLAN_MONOLITHE_MODULAIRE.md` §1.5), les déplacer n'apporterait peut-être rien.

### Ce qui reste transverse dans tous les cas

`security/` (4 classes), `config/` (3 classes) et `DottelApplication.java` ne sont pas des modules métier. Ils restent des packages techniques accessibles à tous. À confirmer explicitement, car Spring Modulith les traitera par défaut comme des modules à part entière et le `ModularityTests` pourrait signaler des violations si ce n'est pas déclaré.

Idem pour les 32 classes de `exception/` : soit elles se répartissent dans le module qui les lève, soit elles restent transverses. **Point à trancher aussi.**

### Réponse à consigner

```
DÉCISION C — Découpage retenu :
[ ] Le découpage proposé ci-dessus, tel quel
[ ] Le découpage proposé, avec ces amendements : ______________

C.1 — processus scindé ?    [X] non
C.2 — reporting est-il un module ?   [X] oui
C.3 — integration regroupé ?  [ ] oui, module dédié   [X] non, réparti dans les modules consommateurs
Exceptions : [X] réparties par module
security/ et config/ : [X] transverses (confirmé)

Décidé le : 2026-07-31
```

### Découpage final acté — **6 modules**

Le module `integration` proposé **n'existe pas**. Ses composants sont répartis :

| Module | Contrôleurs | Services | Repositories | Entités |
|---|---|---|---|---|
| **`utilisateurs`** | `AuthController`, `UtilisateurAdminController` | `AuthService`, `UtilisateurAdminService`, `AuthenticatedUserService` | `UtilisateurRepository` | `Utilisateur` |
| **`beneficiaires`** | `BeneficiaireController`, `EnrolementController` | `BeneficiaireService`, `BeneficiaireImportService`, `BeneficiaireExportService`, `EnrolementService`, **`EhrIntegrationService`**, **`EhrIntegrationServiceStub`** | `BeneficiaireRepository`, `BeneficiaireSpecifications` | `Beneficiaire` |
| **`referentiel`** | `FonctionEligibleController`, `GrilleTarifaireController` | `FonctionEligibleService`, `GrilleTarifaireService`, **`EligibiliteService`** *(décision A)* | `FonctionEligibleRepository`, `GrilleTarifaireRepository` | `FonctionEligible`, `GrilleTarifaire` |
| **`processus`** | `ProcessusMensuelController`, `PieceJointeController` | `ProcessusMensuelService`, `SeparationTachesService`, `EcartMensuelService`, `DocumentService`, `SignatureService`, `SignatureServiceAutonome`, **`EvenementClotureService`**, **`NotificationService`**, **`NotificationServiceStub`** | `ProcessusMensuelRepository`, `LigneEtatMensuelRepository`, `EtapeWorkflowRepository`, `PieceJointeRepository` | `ProcessusMensuel`, `LigneEtatMensuel`, `EtapeWorkflow`, `PieceJointe` |
| **`reporting`** | `ReportingController` | `ReportingService`, `HistoriqueExportService` | *(aucun en propre)* | *(aucune)* |
| **`audit`** | *(aucun)* | `AuditService`, `AuditServiceImpl` | `AuditLogRepository`, `AuditLogSpecifications` | `AuditLog` |

**Motifs retenus.**

- **C.1 — `processus` non scindé.** `PieceJointe`, `DocumentService` et `SignatureService` sont indissociables du cycle de vie du processus : RG-06 impose un seul PDF par processus avec un compteur `nombre_signatures` de 1 à 3, piloté au fil des validations ARH → CRH → DRH.
- **C.2 — `reporting` reste un module.** Il possède déjà son propre contrôleur (`ReportingController`), ce qui en fait une unité identifiable ; plus simple à raisonner avec Spring Modulith qu'une couche transverse hors modèle.
- **C.3 — `integration` non regroupé.** `EhrIntegrationService` et `EvenementClotureService` sont déjà exemplaires en isolation (voir `PLAN_MONOLITHE_MODULAIRE.md` §1.5) ; les regrouper artificiellement dans un module qui n'a pas de cohérence métier n'apporterait rien.

**Conséquences à retenir pour les sprints suivants.**

- `EvenementClotureService` et `NotificationService` rejoignant `processus`, ils ne sont **plus des dépendances inter-modules** de `ProcessusMensuelService`. MM.2 n'a donc à traiter que les **4 repositories étrangers** (`Beneficiaire`, `FonctionEligible`, `GrilleTarifaire`, `Utilisateur`), pas ces deux services.
- `EhrIntegrationService` rejoignant `beneficiaires`, l'appel d'`EnrolementService` vers l'EHR devient intra-module.
- Les DTO suivent leur module : `model/dto/ehr/` → `beneficiaires`, `model/dto/importexcel/` → `beneficiaires`.
- **Exceptions réparties par module** : `security/GlobalExceptionHandler` importera donc les 32 exceptions réparties dans les 6 modules. Point explicitement à déclarer en MM.5 étape 6 via `allowedDependencies`.

## 5. Étape 4. Question D — Espace de travail et base de données

### Constat factuel

Vérifié le 2026-07-31 :

| Élément | Valeur observée |
|---|---|
| Chemin du dépôt | `D:\stage afriland\...\implementation\afb-dottel` |
| Dernier commit | `d9be38c sprint-6F.9` |
| Remote git | `origin → https://github.com/nzali-fankem-vladimir/afb-dottel.git` |
| Base de données (profil `dev`) | `afb_dotations_telephoniques`, via `${DB_URL:jdbc:postgresql://localhost:5432/afb_dotations_telephoniques}` dans `backend/src/main/resources/application-dev.yml` |

Il s'agit du dépôt de travail principal, **pas** d'une copie isolée.

### Deux décisions liées

**D.1 — Isolation du code.** Voir `PLAN_MONOLITHE_MODULAIRE.md` §0 : branche git dédiée (option A, recommandée) ou copie physique du dossier (option B).

**D.2 — Isolation de la base de données.** Uniquement pertinente si D.1 = option B (copie physique). Deux instances qui pointent sur la même base `afb_dotations_telephoniques` se marcheraient dessus : Flyway est configuré avec `baseline-on-migrate: true` et `ddl-auto: validate`, donc une divergence de schéma entre les deux copies produirait des échecs de validation difficiles à diagnostiquer.

Si copie physique retenue, il faudrait une base distincte (par exemple `afb_dotations_telephoniques_mm`), positionnée via la variable d'environnement existante `DB_URL` — **aucune modification de fichier n'est nécessaire**, le placeholder est déjà externalisé.

Note : ce chantier ne prévoit **aucune migration Flyway** (voir `PLAN_MONOLITHE_MODULAIRE.md` §5). Le risque de divergence de schéma est donc théorique. La question reste posée par prudence.

### Réponse à consigner

```
DÉCISION D — Espace de travail :
D.1  [ ] Branche git dédiée, nom : ______________
     [X] Copie physique du dossier, chemin :
         D:\stage afriland\formation spécialisée DSI\projet de gestion
         des absences\implementation\afb-dottel-mm

D.2  [X] Base distincte, nom : afb_dotations_telephoniques_mm
     [ ] Même base, risque accepté

Décidé le : 2026-07-31
```

### Mise en œuvre — **effectuée et vérifiée le 2026-07-31**

| Élément | État vérifié |
|---|---|
| Dossier `afb-dottel-mm` | Créé — 675 fichiers, 29 Mo |
| Exclusions de la copie | `.git`, `node_modules`, `target`, `dist` |
| Dépôt git de la copie | Neuf, branche `main`, commit initial `cbbb497`, 639 fichiers versionnés |
| Remote de la copie | **Aucun** — pousser par erreur sur le dépôt d'origine est impossible |
| Base `afb_dotations_telephoniques_mm` | Créée sur PostgreSQL 16, **0 table** |
| Dépôt d'origine `afb-dottel` | **Intact** au commit `d9be38c` |

### Comment connecter la base `_mm` — aucun fichier à modifier

`backend/src/main/resources/application-dev.yml` porte déjà `url: ${DB_URL:jdbc:postgresql://localhost:5432/afb_dotations_telephoniques}`. Le placeholder étant externalisé, il suffit de définir `DB_URL` dans l'environnement du shell.

**Trois variables sont donc obligatoires dans ce workspace**, contre deux dans le projet d'origine :

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/afb_dotations_telephoniques_mm"
$env:DB_PASSWORD="admin"
$env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"
```

**Oublier `DB_URL` fait silencieusement retomber sur la base du projet d'origine** — le repli du placeholder s'active sans aucun avertissement. C'est le principal piège opérationnel de ce chantier. Contrôle simple au premier démarrage d'une session : Flyway doit appliquer `V1` à `V4` sur une base vierge. S'il annonce un schéma déjà à jour, c'est qu'on est sur la mauvaise base.

Au premier démarrage sur `_mm`, Flyway rejouera `V1__creation_tables.sql` → `V4__ajout_chapitre_beneficiaires.sql`, recréant les 10 tables, les 25 fonctions éligibles avec leurs grilles ACTIVE, et les 5 utilisateurs de test (`1847` ARH, `2093` CRH, `1562` DRH, `2201` EMPLOYE, `1734` ADMIN).

## 6. Critères de validation

| Élément | Statut |
|---|---|
| Question A tranchée et écrite | ✅ **A1** — module `referentiel` |
| Question B tranchée et écrite | ✅ **B1** — un seul module `referentiel` |
| Question C tranchée et écrite (C.1, C.2, C.3, exceptions, security/config) | ✅ **6 modules**, `integration` réparti |
| Question D tranchée et écrite | ✅ copie `afb-dottel-mm` + base `_mm` |
| Copie physique créée et vérifiée | ✅ 675 fichiers, sans `.git`, sans remote |
| Base dédiée créée et vide | ✅ `afb_dotations_telephoniques_mm`, 0 table |
| Dépôt d'origine intact | ✅ `d9be38c`, aucun fichier de code modifié |
| Aucun fichier de code modifié pendant MM.0 | ✅ |
| Ce document mis à jour avec les 4 décisions datées | ✅ 2026-07-31 |

## 7. Comment enchaîner sur MM.1

Les quatre décisions sont actées. **Ouvrir une nouvelle session Claude Code dont le dossier de travail est `afb-dottel-mm`** — pas le dépôt d'origine — puis coller ce prompt :

```
Tu es mon assistant de developpement pour le projet DOTTEL (Afriland
First Bank).

AVANT TOUT -- VERIFICATION D'ESPACE DE TRAVAIL, BLOQUANTE :
Confirme-moi, en lancant reellement les commandes, que :
  a) ton repertoire de travail se termine bien par "afb-dottel-mm"
     et NON "afb-dottel"
  b) `git log --oneline -1` montre le commit initial du chantier,
     pas d9be38c
  c) `git remote -v` ne retourne AUCUN remote
Si l'un des trois est faux, ARRETE-TOI immediatement : tu es dans le
depot de travail principal, pas dans la copie du chantier.

ENSUITE :
1. Lis CLAUDE.md dans son integralite.
2. Lis docs/monolithe-modulaire/PLAN_MONOLITHE_MODULAIRE.md
3. Lis docs/monolithe-modulaire/MM.0_cadrage.md -- les 4 decisions y
   sont actees. Confirme-moi en quelques lignes le decoupage en
   6 MODULES retenu (attention : le module "integration" propose
   initialement N'EXISTE PAS, ses composants sont repartis).
4. Lance /graphify . --update

CONTEXTE : Sprint MM.1, repackaging mecanique + Spring Modulith.
Le decoupage a suivre est celui ACTE en MM.0 section 4, pas celui
propose plus haut dans le meme document -- si tu constates un ecart,
arrete-toi et signale-le.

VARIABLES D'ENVIRONNEMENT (TROIS, pas deux -- DB_URL est nouvelle) :
  $env:DB_URL="jdbc:postgresql://localhost:5432/afb_dotations_telephoniques_mm"
  $env:DB_PASSWORD="admin"
  $env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"
Oublier DB_URL fait retomber SILENCIEUSEMENT sur la base du projet
d'origine. Au premier demarrage, verifie que Flyway applique bien V1
a V4 sur une base vierge -- s'il annonce un schema deja a jour, tu es
sur la mauvaise base, arrete-toi.

METHODE DE TRAVAIL :
- Suis docs/monolithe-modulaire/MM.1_repackaging_et_modularite.md
  etape par etape.
- mvn test complet apres CHAQUE deplacement de module, pas seulement
  a la fin. Si le nombre de tests passe sous 205, tu t'arretes et tu
  me previens.
- Tu ne modifies aucun comportement metier dans ce sprint : uniquement
  des deplacements de fichiers et des corrections d'import.

PREMIERE ACTION : la verification d'espace de travail ci-dessus, puis
confirme le decoupage acte, puis attaque l'etape 1 de MM.1.
```

## Commit

MM.0 ne produit aucun code.

```bash
git add docs/monolithe-modulaire/
git commit -m "mm.0: cadrage acte, 6 modules retenus, workspace et base dedies"
```

---

**Fin du Sprint MM.0** — *MM.1 peut démarrer, dans une session ouverte sur `afb-dottel-mm`*