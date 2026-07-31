# SPRINT MM.0

## Cadrage — décisions à trancher avant toute ligne de code

*Module Dotations Téléphoniques Mensuelles — Backend*

| | |
|---|---|
| **Objet** | Trancher les 4 questions ouvertes qui conditionnent tout le découpage modulaire |
| **Livrable** | Ce document complété avec les décisions actées, daté et signé |
| **Durée** | Une session de discussion — aucune écriture de code |
| **Prérequis** | `PLAN_MONOLITHE_MODULAIRE.md` lu |

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
[ ] A1 — module référentiel
[ ] A2 — module dédié, nom retenu : ______________
[ ] Autre : ______________

Décidé le : ____________
```

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
[ ] B1 — un seul module, nom retenu : ______________
[ ] B2 — deux modules, noms retenus : ______________ / ______________

Décidé le : ____________
```

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

C.1 — processus scindé ?    [ ] non   [ ] oui, en : ______________
C.2 — reporting est-il un module ?   [ ] oui   [ ] non, alternative : ______________
C.3 — integration regroupé ?  [ ] oui, module dédié   [ ] non, réparti dans les modules consommateurs
Exceptions : [ ] réparties par module   [ ] transverses
security/ et config/ : [ ] transverses (confirmé)   [ ] autre : ______________

Décidé le : ____________
```

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
     [ ] Copie physique du dossier, chemin : ______________

D.2 (si copie physique)  [ ] Base distincte, nom : ______________
                         [ ] Même base, risque accepté

Décidé le : ____________
```

## 6. Critères de validation

| Élément | Statut attendu |
|---|---|
| Question A tranchée et écrite | Fait |
| Question B tranchée et écrite | Fait |
| Question C tranchée et écrite, y compris C.1, C.2, C.3, exceptions, security/config | Fait |
| Question D tranchée et écrite | Fait |
| Aucun fichier de code modifié pendant MM.0 | Vérifié |
| Ce document mis à jour avec les 4 décisions datées | Fait |

## 7. Comment enchaîner sur MM.1

Une fois les quatre blocs de décision remplis dans ce fichier, ouvrir une nouvelle session avec ce prompt :

```
Tu es mon assistant de developpement pour le projet DOTTEL (Afriland
First Bank).

AVANT TOUT :
1. Lis CLAUDE.md dans son integralite.
2. Lis docs/monolithe-modulaire/PLAN_MONOLITHE_MODULAIRE.md
3. Lis docs/monolithe-modulaire/MM.0_cadrage.md -- les 4 decisions
   y sont desormais actees, section par section. Confirme-moi en
   quelques lignes le decoupage retenu avant de commencer.
4. Lance /graphify . --update

CONTEXTE : Sprint MM.1, repackaging mecanique + Spring Modulith.
Le decoupage a suivre est celui acte en MM.0, pas celui propose --
si tu constates un ecart entre les deux, arrete-toi et signale-le.

METHODE DE TRAVAIL :
- Suis docs/monolithe-modulaire/MM.1_repackaging_et_modularite.md
  etape par etape.
- mvn test complet apres CHAQUE deplacement de fichier, pas
  seulement a la fin. Si le nombre de tests passe sous 205, tu
  t'arretes et tu me previens.
- Tu ne modifies aucun comportement metier dans ce sprint : uniquement
  des deplacements de fichiers et des corrections d'import.

PREMIERE ACTION : confirme le decoupage acte en MM.0, puis attaque
l'etape 1 de MM.1.
```

## Commit

MM.0 ne produit aucun code. Le seul commit est celui de ce document complété.

```bash
git add docs/monolithe-modulaire/MM.0_cadrage.md
git commit -m "mm.0: cadrage du chantier monolithe modulaire, 4 decisions actees"
```

---

**Fin du Sprint MM.0** — *MM.1 ne peut pas démarrer tant que les 4 blocs de décision ne sont pas remplis*