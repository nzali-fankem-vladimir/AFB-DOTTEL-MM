# SPRINT MM.2

## Refactor du god service `ProcessusMensuelService`

*Module Dotations Téléphoniques Mensuelles — Backend*

| | |
|---|---|
| **Objet** | Ramener `ProcessusMensuelService` à un orchestrateur qui appelle des API de modules, au lieu d'accéder directement aux repositories de quatre autres domaines |
| **Livrable** | `ProcessusMensuelService` sans aucun repository étranger injecté, API de modules créées côté `beneficiaires`, `referentiel` et `utilisateurs` |
| **Durée** | Deux à trois journées |
| **Prérequis** | MM.1 terminé, suite de tests ≥ 205 au vert, tableau des violations produit |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Cartographie des usages (étape 2) | Sonnet 5 | Medium |
| Conception des API de modules (étape 3) | Opus 4.8 ou Sonnet 5 | **High** |
| Extraction, un couplage à la fois (étapes 4-7) | Sonnet 5 | **High** |
| Vérification RG par RG (étape 8) | Sonnet 5 | High |

**C'est le sprint le plus risqué du chantier.** `ProcessusMensuelService` porte à lui seul RG-04, RG-05, RG-06, RG-07, RG-08, RG-11 et RG-12. Une régression y est invisible à la lecture et coûteuse en production. L'effort High se justifie sur toute la durée : un refactoring de workflow bâclé est pire que pas de refactoring du tout.

## 0. Nouvelle session : rappel Graphify

```
/graphify . --update
```

## 1. Contexte

### 1.1 État réel du service

`backend/src/main/java/com/afriland/dottel/service/ProcessusMensuelService.java` — chemin d'origine ; après MM.1 il vit dans le module `processus`.

**16 dépendances injectées**, lignes 69 à 84 du fichier d'origine :

| Repositories (8) | Services (8) |
|---|---|
| `ProcessusMensuelRepository` | `AuditService` |
| `BeneficiaireRepository` ← **étranger** | `EligibiliteService` |
| `FonctionEligibleRepository` ← **étranger** | `AuthenticatedUserService` |
| `GrilleTarifaireRepository` ← **étranger** | `DocumentService` |
| `LigneEtatMensuelRepository` | `SignatureService` |
| `EtapeWorkflowRepository` | `NotificationService` |
| `UtilisateurRepository` ← **étranger** | `SeparationTachesService` |
| `PieceJointeRepository` | `EvenementClotureService` |

Les quatre repositories marqués « étranger » appartiennent à d'autres modules après MM.1. **Ce sont eux, et eux seuls, que ce sprint doit éliminer.**

### 1.2 Les usages exacts à traiter

Relevés par lecture du fichier :

| Ligne | Appel | Ce qu'il fait réellement |
|---|---|---|
| 107 | `beneficiaireRepository.findByActifTrue()` | Liste des bénéficiaires à inclure au déclenchement |
| 198 | `beneficiaireRepository.findAllById(idsBeneficiaires)` | Résolution en masse pour construire le détail du processus |
| 623 | `beneficiaireRepository.findById(idBeneficiaire)` | Récupère le **grade**, pour revérifier RG-02 lors d'un ajustement |
| 680 | `fonctionEligibleRepository.findByCode(codeFonction)` | Dans `resoudreGrillePourFonction()` |
| 689 | `grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(...)` | Idem — c'est le cœur de RG-04 |
| 354 | `utilisateurRepository.findByRoleAndActifTrue(RoleEnum.CRH)` | Destinataires de notification après validation ARH |
| 413 | `utilisateurRepository.findByRoleAndActifTrue(RoleEnum.DRH)` | Destinataires après validation CRH |
| 558 | `utilisateurRepository.findById(processus.getIdCreateur())` | ARH créateur, à notifier lors d'un retour |

Deux méthodes privées structurent la résolution de grille :

- `resoudreGrillePourFonction(String codeFonction)` — ligne 679, privée, retourne un `record ResolutionGrille(GrilleTarifaire grilleActive, String motifExclusion)` défini ligne 698.
- Appelée aux lignes 112 (déclenchement) et 607 (ajustement de fonction retenue).

Les quatre motifs d'exclusion sont des constantes du service, lignes 64 à 67 : `MOTIF_GRILLE_INTROUVABLE`, `MOTIF_FONCTION_DESACTIVEE`, `MOTIF_FONCTION_INCONNUE`, `MOTIF_GRADE_NON_ELIGIBLE`.

### 1.3 Ce qui n'est PAS un problème

Quatre dépendances de services restent **légitimes** et ne doivent pas être touchées :

- `SeparationTachesService` — n'injecte que `EtapeWorkflowRepository`, appartient au même module `processus`.
- `DocumentService` / `SignatureService` — même module `processus` (selon décision C de MM.0). `signatureService.signer(...)` est bien appelé, ligne 346.
- `EvenementClotureService` — frontière externe déjà exemplaire.
- `NotificationService` — reçoit un `Utilisateur` en paramètre, ne dépend d'aucun repository.

`AuditService` reste appelé en direct dans ce sprint : il est traité en **MM.4**, pas ici. Ne pas mélanger les deux chantiers.

## 2. Étape 1. Ouvrir la session

```
Tu es mon assistant de developpement pour le projet DOTTEL (Afriland
First Bank).

AVANT TOUT -- VERIFICATION D'ESPACE DE TRAVAIL, BLOQUANTE :
Confirme-moi, en lancant reellement les commandes, que :
  a) ton repertoire de travail se termine bien par "afb-dottel-mm"
     et NON "afb-dottel"
  b) `git log --oneline -1` ne montre PAS d9be38c
  c) `git remote -v` ne retourne AUCUN remote
Si l'un des trois est faux, ARRETE-TOI immediatement.

ENSUITE :
1. Lis CLAUDE.md dans son integralite, en particulier la section 7
   (regles metier RG-01 a RG-12) et la section 17.
2. Lis docs/monolithe-modulaire/PLAN_MONOLITHE_MODULAIRE.md
3. Lis docs/monolithe-modulaire/MM.0_cadrage.md (decoupage acte en
   6 modules -- le module "integration" n'existe pas)
4. Lance /graphify . --update

CONTEXTE : Sprint MM.2, le plus risque du chantier. Objectif unique :
ProcessusMensuelService ne doit plus injecter AUCUN repository
appartenant a un autre module (BeneficiaireRepository,
FonctionEligibleRepository, GrilleTarifaireRepository,
UtilisateurRepository).

CE SPRINT NE TOUCHE PAS :
- AuditService (traite en MM.4)
- Les couplages de DocumentService, ReportingService,
  FonctionEligibleService (traites en MM.3)
- Le schema de base de donnees (aucune migration Flyway)
- Les contrats API (34 endpoints inchanges)
- EvenementClotureService et NotificationService : ils appartiennent
  deja au module processus (decision C.3 de MM.0), ce ne sont PAS des
  couplages a casser.

VARIABLES D'ENVIRONNEMENT (TROIS, pas deux) :
  $env:DB_URL="jdbc:postgresql://localhost:5432/afb_dotations_telephoniques_mm"
  $env:DB_PASSWORD="admin"
  $env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"
Oublier DB_URL fait retomber SILENCIEUSEMENT sur la base du projet
d'origine.

METHODE DE TRAVAIL :
- UN couplage a la fois. Tu montres le diff, j'approuve, tu continues.
- mvn test complet apres chaque couplage extrait, avec les TROIS
  variables ci-dessus. Reference : >= 205 tests, 0 echec.
- Si tu dois modifier une regle metier pour faire passer le refactor,
  ARRETE-TOI et explique-moi -- c'est probablement une erreur de
  conception de l'API, pas un ajustement legitime.
- Les regles RG-01, RG-02, RG-04, RG-05, RG-06, RG-07, RG-08, RG-11
  et RG-12 doivent se comporter EXACTEMENT pareil apres qu'avant.

PREMIERE ACTION : la verification d'espace de travail ci-dessus, puis
etape 2, cartographie des usages.
```

## 3. Étape 2. Cartographier avant de toucher

```
Avant tout refactoring, produis-moi un tableau exhaustif de CHAQUE
appel, dans ProcessusMensuelService, aux quatre repositories
etrangers (Beneficiaire, FonctionEligible, GrilleTarifaire,
Utilisateur).

| Ligne | Appel exact | Donnee reellement utilisee ensuite | Regle metier concernee |

La colonne "donnee reellement utilisee" est la plus importante : si
le service appelle findById() mais n'utilise que le champ `grade`,
l'API du module Beneficiaire doit exposer le grade, pas l'entite
entiere. C'est ce qui determine la forme des API a creer.

Ne code rien a cette etape. Montre-moi seulement le tableau.
```

## 4. Étape 3. Concevoir les API de modules

```
A partir du tableau de l'etape 2, propose-moi les API publiques
minimales que chaque module doit exposer. Pour chacune :

- nom de l'interface et son package
- signature exacte de chaque methode
- type de retour : un DTO dedie, JAMAIS l'entite JPA
  (CLAUDE.md section 9 et section 18 point 4)

Contraintes :
- Expose le MINIMUM necessaire. Une API qui retourne l'entite complete
  recree le couplage qu'on essaie de casser.
- Le module `referentiel` doit exposer UNE seule methode de resolution
  de grille, qui remplacera resoudreGrillePourFonction() -- elle sera
  aussi utilisee en MM.3 pour les deux autres implementations
  existantes. Concois-la en consequence : elle doit pouvoir retourner
  soit la grille, soit un motif d'exclusion (les 4 constantes lignes
  64-67 du service actuel).
- Le module `utilisateurs` doit exposer de quoi trouver les
  destinataires de notification par role, sans exposer l'entite
  Utilisateur.

Montre-moi les interfaces AVANT de les implementer. On valide la
forme des API ensemble, puis tu codes.
```

**Point de conception le plus délicat.** `resoudreGrillePourFonction()` retourne aujourd'hui un `record ResolutionGrille(GrilleTarifaire grilleActive, String motifExclusion)` — il transporte une entité JPA du module `referentiel`. L'API publique ne peut pas faire ça. Elle doit retourner un DTO contenant le montant et, le cas échéant, le motif d'exclusion. Trois des quatre motifs (`MOTIF_FONCTION_INCONNUE`, `MOTIF_FONCTION_DESACTIVEE`, `MOTIF_GRILLE_INTROUVABLE`) relèvent du référentiel ; le quatrième (`MOTIF_GRADE_NON_ELIGIBLE`) dépend du grade du bénéficiaire — **à quel module appartient-il ?** Question à poser explicitement pendant l'étape 3, elle conditionne la signature.

## 5. Étapes 4 à 7. Extraire, un couplage à la fois

Ordre imposé, du plus simple au plus risqué :

| Étape | Couplage | Lignes concernées | Risque |
|---|---|---|---|
| 4 | `UtilisateurRepository` | 354, 413, 558 | **Faible** — usage limité aux notifications, aucune règle métier RG en jeu |
| 5 | `BeneficiaireRepository` — lecture de liste | 107, 198 | Moyen — touche le déclenchement (RG-12) et le détail du processus |
| 6 | `BeneficiaireRepository` — lecture du grade | 623 | **Élevé** — RG-02 sur les ajustements |
| 7 | `FonctionEligibleRepository` + `GrilleTarifaireRepository` | 680, 689, via 112 et 607 | **Le plus élevé** — c'est RG-04, le cœur du calcul de montant |

Prompt à répéter pour chaque étape :

```
Extrais maintenant le couplage <N>, et LUI SEUL.

1. Implemente l'API du module concerne (validee a l'etape 3).
2. Remplace les appels dans ProcessusMensuelService.
3. Retire le repository etranger de la liste des dependances injectees.
4. Lance la suite complete :
     $env:DB_URL="jdbc:postgresql://localhost:5432/afb_dotations_telephoniques_mm"
     $env:DB_PASSWORD="admin"
     $env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"
     cd backend ; .\mvnw.cmd test
5. Montre-moi le diff complet et le resultat des tests.

Si un test de ProcessusMensuelServiceTest echoue (63 tests dans ce
fichier), ne le "repare" pas en modifiant le test -- montre-moi
l'echec, on determine ensemble si c'est le refactor qui est faux ou
le test qui teste un detail d'implementation devenu obsolete.
```

**Pourquoi cet ordre.** `ProcessusMensuelServiceTest` contient **63 tests** — c'est le plus gros fichier de test du projet. En commençant par le couplage le moins couvert par des règles métier (les notifications), on valide la mécanique d'extraction et la forme des API avant d'attaquer RG-04.

## 6. Étape 8. Vérification règle par règle

```
Verification finale de non-regression metier. Pour CHACUNE des regles
ci-dessous, montre-moi le test qui la couvre et confirme qu'il passe :

- RG-04 : montant lu depuis grille_tarifaire ACTIVE + dateFin IS NULL,
          jamais en dur
- RG-05 : sequence ARH -> CRH -> DRH, aucun saut
- RG-06 : un seul PDF par processus, compteur de signatures 1 -> 3
- RG-07 : motif obligatoire non vide sur tout retour
- RG-08 : separation des taches, 403 si meme acteur
- RG-11 : lignes valides traitees meme si d'autres sont rejetees
          (ajustements independants, PATCH /processus/{id})
- RG-12 : unicite mois/annee du processus mensuel
- RG-01 / RG-02 : reverification d'eligibilite lors d'un ajustement
          de fonction retenue (ligne 607 et suivantes du fichier
          d'origine)

Si une regle n'a PAS de test dedie, dis-le-moi explicitement plutot
que de conclure qu'elle fonctionne.
```

## 7. Étape 9. Mesurer le progrès

```
Relance ModularityTests et compare au tableau de violations produit
en MM.1 :

| Famille | Violations en MM.1 | Violations maintenant | Ecart |

Les violations de famille (a) -- celles imputables a
ProcessusMensuelService -- doivent toutes avoir disparu. Si l'une
subsiste, explique-moi pourquoi.
```

## 8. Critères de validation

| Élément | Statut attendu |
|---|---|
| `ProcessusMensuelService` n'injecte plus `BeneficiaireRepository` | Vérifié |
| `ProcessusMensuelService` n'injecte plus `FonctionEligibleRepository` | Vérifié |
| `ProcessusMensuelService` n'injecte plus `GrilleTarifaireRepository` | Vérifié |
| `ProcessusMensuelService` n'injecte plus `UtilisateurRepository` | Vérifié |
| Nombre de dépendances injectées passé de 16 à 12 au maximum | Vérifié |
| Aucune API publique de module ne retourne une entité JPA | Vérifié |
| Une seule méthode de résolution de grille existe dans le module `referentiel` | Vérifié |
| RG-01, RG-02, RG-04, RG-05, RG-06, RG-07, RG-08, RG-11, RG-12 : test dédié identifié et vert | Vérifié |
| Règles sans test dédié explicitement signalées | Fait |
| Suite complète ≥ 205 tests, 0 échec, 0 erreur | Vérifié |
| Les 63 tests de `ProcessusMensuelServiceTest` passent | Vérifié |
| Aucun contrat API modifié | Vérifié |
| Aucune migration Flyway ajoutée | Vérifié |
| Violations `ModularityTests` de famille (a) toutes résorbées | Vérifié |

## Commit

```bash
git add .
git commit -m "mm.2: ProcessusMensuelService reduit a un orchestrateur, 4 repositories etrangers supprimes"
```

---

**Fin du Sprint MM.2** — *en attente de validation avant MM.3*