# SPRINT MM.12

## Workflow des grilles à trois acteurs, et resynchronisation des montants

*Module Dotations Téléphoniques Mensuelles — modules `referentiel` et `processus`*

| | |
|---|---|
| **Objet** | Insérer le CRH dans le workflow de validation des grilles (ARH→CRH→DRH), et resynchroniser les montants obsolètes à la validation ARH d'un processus |
| **Livrable** | Workflow grilles à 3 étapes, resynchronisation non silencieuse des montants |
| **Durée** | Deux jours |
| **Prérequis** | MM.11 validé (décision B de M.0 déjà actée : **variante B2-resync**) |
| **Origine** | `M.4_workflow_grilles_et_montants.md` |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Décision sur RG-08 et le cycle de modules (étape 2) | Opus 4.8 ou Sonnet 5 | **High** |
| Workflow grilles — backend (étapes 3-4) | Opus 4.8 ou Sonnet 5 | **High** |
| Workflow grilles — frontend (étape 5) | Sonnet 5 | Medium à High |
| Resynchronisation des montants (étapes 6-7) | Opus 4.8 ou Sonnet 5 | **High** |

**Le sprint le plus sensible du lot.** Il modifie RG-10 (cycle de vie d'une grille), touche RG-04 (source du montant), pose une question sur RG-08, et agit sur un montant qui alimente le PDF signé puis la comptabilité. High sur tout le sprint.

## 0. Nouvelle session : rappel Graphify

```
/graphify . --update
```

---

## 1. Contexte

### 1.1 Workflow des grilles à trois acteurs

Aujourd'hui une grille suit **ARH → DRH** (RG-10 : l'ARH crée et soumet, la DRH valide ou rejette). Le métier veut insérer le **CRH** entre les deux, sur le modèle du workflow du processus mensuel (ARH→CRH→DRH, construit au Sprint 5).

Comme la DRH aujourd'hui, le CRH doit pouvoir **valider ou rejeter avec motif**. Le motif affiché reste le **dernier saisi**, quel que soit l'acteur qui a rejeté — mécanisme déjà en place pour le processus mensuel et pour l'affichage du motif de rejet des grilles.

### 1.2 Resynchronisation des montants — décision B actée

Un processus validé par l'ARH peut porter un montant obsolète si la grille tarifaire d'une fonction a changé entre le déclenchement et la validation. L'anomalie ne ressort aujourd'hui qu'au rejet CRH/DRH.

**Décision B de M.0, actée — variante « B2-resync », ni B1 ni B2 ni B3 tels quels :**

> À la validation ARH, tout montant obsolète est **resynchronisé automatiquement** sur la grille ACTIVE (RG-04), **mais de façon NON silencieuse** : l'ARH voit un récapitulatif clair des lignes mises à jour (ancien montant → nouveau montant) **avant** de confirmer la validation.
>
> **Motif** : bloquer (B1) forcerait à refaire tout le workflow de validation d'une grille déjà validée par la DRH juste pour resynchroniser — de la lourdeur sans gain. B3 était écarté car silencieux. Cette variante **corrige ET montre**, sans faux choix « continuer avec un montant périmé » qui n'a aucun sens métier.
>
> **L'audit doit tracer le delta de resynchronisation.**

**Bonne nouvelle architecturale sur ce volet** : la comparaison montant stocké ↔ `GrilleTarifaireApi.resoudrePourFonction()` utilise la dépendance `processus → referentiel :: api` **déjà déclarée** depuis MM.2. Aucun nouveau couplage.

---

## 2. ⚠️ 1bis. LE RISQUE ARCHITECTURAL DE CE SPRINT — à lire avant tout

Le guide d'origine `M.4` posait, à son étape 2, cette question :

> « Séparation des tâches (RG-08) : doit-elle s'appliquer au workflow des grilles comme elle s'applique au processus mensuel ? Le métier ne l'a pas précisé — signale-le comme question, ne tranche pas seul. »

Dans l'ancienne architecture en couches, c'était une **pure question métier**. En version modulaire, **répondre « oui » a une conséquence structurelle immédiate** :

| Élément nécessaire à RG-08 sur les grilles | Module propriétaire |
|---|---|
| `GrilleTarifaireService` (workflow des grilles) | **`referentiel`** |
| `SeparationTachesService` (implémentation de RG-08) | **`processus`** |
| Entité `EtapeWorkflow` (trace des étapes validées) | **`processus`** |
| `EtapeWorkflowRepository` | **`processus`** |

Or `processus` dépend **déjà** de `referentiel :: api` (résolution de grille, MM.2).

**Faire dépendre `referentiel` de `processus` créerait un DEUXIÈME cycle de modules.** `ModularityTests` échouerait : son filtre ne couvre **que** le cycle connu `beneficiaires` ↔ `referentiel` (assumé par la décision G-2 en MM.8), et il est précisément conçu pour échouer sur toute violation **nouvelle ou différente**.

Ce n'est donc pas une objection théorique : le test cassera.

---

## 3. Étape 1. Ouvrir la session

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
1. Lis CLAUDE.md dans son integralite, en particulier RG-04, RG-08,
   RG-10, et la section 8 (groupe Grilles tarifaires).
2. Lis la logique de workflow du processus mensuel dans
   processus/service/ProcessusMensuelService.java (branches
   validerBrancheArh/Crh/Drh) -- elle sert de MODELE pour le workflow
   des grilles.
3. Lis docs/chantier-ajout-metier-mm/PLAN_AJOUTS_METIER_MM.md
4. Lis docs/chantier-ajout-metier-mm/MM.12_workflow_grilles_et_montants.md
   EN ENTIER -- SURTOUT la section 1bis (risque de second cycle de
   modules).
5. Lance /graphify . --update

CONTEXTE : Sprint MM.12, le plus sensible du lot. Deux volets : workflow
des grilles a 3 acteurs (module referentiel), et resynchronisation des
montants a la validation ARH (module processus).

DECISION DEJA ACTEE, a appliquer sans la rediscuter :
  Decision B de M.0, variante B2-RESYNC : a la validation ARH, tout
  montant obsolete est resynchronise automatiquement sur la grille
  ACTIVE, mais de facon NON SILENCIEUSE -- l'ARH voit un recapitulatif
  (ancien montant -> nouveau montant) AVANT de confirmer. L'audit trace
  le delta.

ALERTE ARCHITECTURALE : appliquer RG-08 au workflow des grilles
creerait un SECOND cycle de modules (referentiel -> processus), que
ModularityTests rejettera. C'est l'objet de l'etape 2 -- traite-la
AVANT tout code.

VARIABLES D'ENVIRONNEMENT (TROIS, pas deux) :
  $env:DB_URL="jdbc:postgresql://localhost:5432/afb_dotations_telephoniques_mm"
  $env:DB_PASSWORD="admin"
  $env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"

METHODE DE TRAVAIL :
- Un fichier a la fois. Tu montres, j'approuve, tu continues.
- Reference de non-regression : total de MM.11, 0 echec.
- Lance ModularityTests apres CHAQUE etape de ce sprint, pas seulement
  a la fin. C'est le sprint ou il risque le plus de casser.

PREMIERE ACTION : la verification d'espace de travail, puis l'etape 2
(conception). NE CODE RIEN avant que j'aie arbitre.
```

---

## 4. Étape 2. Conception — trois points, aucun tranché seul

```
Traite ces trois points avec moi AVANT toute ligne de code.

POINT 1 -- Sequence de statuts de la grille.

Le cycle actuel de StatutGrilleEnum est :
  BROUILLON -> EN_ATTENTE_DRH -> ACTIVE / REJETEE

Avec le CRH insere, propose la nouvelle sequence (probablement un
EN_ATTENTE_CRH avant EN_ATTENTE_DRH). Points a traiter dans ta
proposition :
  - CLAUDE.md section 6 fige 5 enumerations. Ajouter une valeur a
    StatutGrilleEnum n'en cree pas une 6e -- c'est acceptable -- mais
    signale-le explicitement.
  - Les grilles DEJA en base au statut EN_ATTENTE_DRH : que
    deviennent-elles ? Restent-elles en attente DRH (le CRH ayant
    implicitement valide), ou doivent-elles repasser par le CRH ?
    C'est une question de MIGRATION DE DONNEES, pas seulement de code.
    Pose-la-moi.
  - Le contrat API (docs/reference/contrats_api_dotations_v3.md)
    documente GET /grilles-tarifaires/en-attente-drh, reserve DRH. Il
    faudra un equivalent CRH. Propose le chemin, ne l'invente pas seul.

POINT 2 -- RG-08 sur le workflow des grilles : LE POINT CRITIQUE.

Le metier n'a pas precise si la separation des taches doit s'appliquer
aux grilles. Mais en version modulaire, ce n'est plus une simple
question metier : voir section 1bis de ce guide.

Presente-moi TROIS options, avec pour chacune l'impact sur
ModularityTests :

  W-1) RG-08 NON appliquee au workflow des grilles.
       Aucun nouveau couplage, ModularityTests inchange. Mais un meme
       acteur pourrait creer une grille en tant qu'ARH puis la valider
       en tant que CRH s'il porte les deux roles -- a evaluer avec le
       metier : est-ce un risque reel dans l'organisation d'Afriland,
       ou un cas qui ne se produit pas (les roles etant distincts par
       personne) ?

  W-2) RG-08 appliquee, en DUPLIQUANT le mecanisme dans referentiel.
       Une trace d'etapes propre au referentiel (nouvelle entite, ou
       champs sur GrilleTarifaire : id_validateur_crh, date_validation_crh
       en plus des id_createur/id_validateur existants). Pas de cycle
       de modules. Mais duplication de la logique RG-08, avec risque de
       divergence future entre les deux implementations.

  W-3) RG-08 appliquee en reutilisant SeparationTachesService.
       Le plus DRY, mais cree le SECOND CYCLE de modules
       referentiel -> processus. ModularityTests echouera. Exigerait
       soit d'etendre son filtre (ce qui affaiblirait la protection
       anti-regression, contraire a l'esprit de MM.5), soit de deplacer
       SeparationTachesService dans un module partage -- ce qui rouvre
       le decoupage acte en MM.0.

Recommande-en une avec ton motif. J'arbitre. NE CODE PAS AVANT.

Note : GrilleTarifaire porte deja id_createur et id_validateur
(CLAUDE.md section 4). L'option W-2 s'appuierait naturellement dessus.
Verifie l'etat reel de l'entite avant de proposer.

POINT 3 -- Motif de rejet, quel que soit l'acteur.

Confirme que le motif affiche reste le DERNIER saisi, que le rejet
vienne du CRH ou de la DRH -- meme mecanisme que celui deja construit
pour le processus mensuel (motifRetour/origineRetour) et pour
l'affichage du motif de rejet des grilles (motifRejet, Sprint 6F.7).
Verifie dans le code comment motifRejet est aujourd'hui stocke et
affiche, et dis-moi si le mecanisme actuel suffit ou s'il faut aussi
tracer QUEL acteur a rejete (equivalent d'origineRetour).
```

---

## 5. Étapes 3 et 4. Workflow grilles — backend

```
Une fois la sequence de statuts et l'option RG-08 validees, implemente
dans referentiel/service/GrilleTarifaireService.java.

Insere l'etape CRH entre la soumission ARH et la validation DRH, en
t'inspirant de la STRUCTURE des branches de
ProcessusMensuelService.valider() (validerBrancheArh / validerBrancheCrh
/ validerBrancheDrh) -- meme forme, meme lisibilite, meme discipline de
garde de statut.

- Le CRH valide (la grille passe a l'etape DRH) ou rejette (avec motif).
- La DRH valide (grille ACTIVE) ou rejette (avec motif).
- Applique RG-08 selon l'option retenue a l'etape 2 (W-1, W-2 ou W-3).
- RG-10 preservee : a la validation DRH, l'ancienne grille ACTIVE voit
  toujours sa date_fin renseignee et la nouvelle passe a ACTIVE. Le
  systeme empeche toujours deux grilles ACTIVE sans date_fin pour la
  meme fonction (contrainte d'unicite partielle du script V1).
- Audit (RG-09) a chaque transition, avec delta avant/apres.

Controleur referentiel/controller/GrilleTarifaireController.java :
@PreAuthorize ajustes pour le CRH, nouveaux endpoints selon la decision
du POINT 1.

Migration Flyway si l'option W-2 ou la reprise des grilles existantes
l'exige -- NE MODIFIE JAMAIS un script deja applique.

Mets a jour docs/reference/contrats_api_dotations_v3.md avec les
nouveaux endpoints et la nouvelle sequence de statuts : ce document est
la reference des 34 endpoints, il ne doit pas devenir faux.

TESTS : cycle complet ARH -> CRH -> DRH, rejet CRH, rejet DRH, RG-08
selon l'option retenue, non-regression sur RG-10 (unicite de la grille
ACTIVE).

LANCE ModularityTests apres cette etape. Si l'option W-3 a ete retenue
et que le test casse, ARRETE-TOI et reviens vers moi -- ne modifie pas
le filtre de ModularityTests sans mon accord explicite.
```

---

## 6. Étape 5. Workflow grilles — frontend

```
Adapte les ecrans de grilles.

- frontend/src/pages/grilles/GrillesListPage.jsx : refleter le nouveau
  statut EN_ATTENTE_CRH dans le filtre et l'affichage. ATTENTION : ce
  fichier a deja ete touche par MM.9 (filtres portes par l'URL) --
  verifie son etat reel, ne repars pas de la version d'origine.
- frontend/src/utils/statutGrille.js : ajouter le libelle et le variant
  de badge du nouveau statut (le projet centralise deja les libelles de
  statut dans ces utilitaires -- ne les ecris pas en dur dans les pages).
- Une page/action de validation CRH sur le modele de celle du DRH
  (frontend/src/pages/dashboard/GrillesTarifairesValider.jsx et
  frontend/src/pages/grilles/ValiderGrilleModal.jsx). Le CRH voit les
  grilles EN_ATTENTE_CRH avec valider/rejeter ; la DRH voit les
  EN_ATTENTE_DRH comme avant.
- Reutilise le composant de motif de rejet deja construit
  (frontend/src/components/ui/VoirMotifModal.jsx) plutot que d'en
  ecrire un nouveau.
- frontend/src/components/layout/Sidebar.jsx : si le CRH gagne un ecran
  de validation de grilles, son entree de navigation doit apparaitre --
  et UNIQUEMENT pour le CRH. Rappel de l'audit 6F.9 : les roles de
  NAV_LINKS doivent etre STRICTEMENT identiques a ceux du
  ProtectedRoute cible. Verifie les deux.

Montre les fichiers un par un.
```

---

## 7. Étapes 6 et 7. Resynchronisation des montants

### 7.1 Backend

```
Dans processus/service/ProcessusMensuelService.java, branche ARH de
valider() (validerBrancheArh), applique la variante B2-RESYNC actee.

Comportement attendu :
1. Pour chaque ligne INCLUSE (inclusDansEtat = true) du processus,
   comparer montantApplique au montant que donnerait la grille ACTIVE
   actuelle de fonctionRetenue.
   => Utilise GrilleTarifaireApi.resoudrePourFonction() -- dependance
      processus -> referentiel::api DEJA declaree, aucun nouveau
      couplage. NE REECRIS PAS une resolution de montant : elle est
      unique dans le projet depuis MM.3 (couplage C2).
2. Toute ligne en ecart est RESYNCHRONISEE sur le montant courant.
3. La resynchronisation n'est PAS silencieuse : la reponse de
   validation doit porter le recapitulatif des lignes mises a jour
   (idBeneficiaire, matricule, nomPrenoms, fonctionRetenue,
   ancienMontant, nouveauMontant).
4. Audit (RG-09) : le delta de resynchronisation doit etre trace dans
   audit_log au format {"avant": {...}, "apres": {...}} -- exigence
   explicite de la decision B.

QUESTION A ME POSER AVANT DE CODER, NE TRANCHE PAS SEUL :
la decision B dit que l'ARH voit le recapitulatif "AVANT de confirmer
la validation". Deux interpretations :
  - EN DEUX TEMPS : un premier appel detecte et RETOURNE les ecarts
    sans rien modifier ; l'ARH confirme ; un second appel resynchronise
    et valide. Plus fidele au "avant de confirmer", mais exige un
    nouvel endpoint ou un parametre de confirmation.
  - EN UN TEMPS : la validation resynchronise ET retourne le
    recapitulatif dans sa reponse ; le frontend l'affiche apres coup.
    Plus simple, mais l'ARH voit le recapitulatif APRES la validation,
    pas avant.
Presente les deux, attends ma reponse.

Cas limite a traiter : une ligne dont la fonction n'a PLUS de grille
ACTIVE (resoudrePourFonction retourne un motif d'exclusion, pas un
montant). Que faire ? Exclure la ligne ? Bloquer la validation ?
POSE-MOI LA QUESTION -- ce cas n'est pas couvert par la decision B.

TESTS : montant a jour -> validation normale sans recapitulatif ;
montant obsolete -> resynchronise, recapitulatif retourne, delta
tracé en audit ; fonction sans grille ACTIVE -> comportement retenu.
```

### 7.2 Frontend

```
Dans frontend/src/pages/workflow/ProcessusDetailPage.jsx, adapte
l'action de validation ARH selon l'interpretation retenue (deux temps
ou un temps) :
- afficher clairement les lignes resynchronisees, avec ancien montant
  -> nouveau montant
- libelle explicite : l'ARH doit comprendre que des montants ont ete
  mis a jour automatiquement sur la grille en vigueur, et lesquels

ATTENTION : ce fichier a deja ete touche par MM.9 (LienRetour depuis
location.state?.retour). Verifie son etat reel avant d'editer.

Si l'interpretation "en deux temps" est retenue, reutilise
ConfirmDialog (frontend/src/components/ui/ConfirmDialog.jsx) pour
l'etape de confirmation, plutot que d'ecrire un nouveau composant.
```

---

## 8. Étape 8. Vérification

```
BACKEND :
  cd backend ; .\mvnw.cmd test
Attendu : total de MM.11 + les nouveaux tests, 0 echec.

MODULARITE -- LE POINT CRITIQUE DE CE SPRINT :
  ModularityTests VERT, avec TOUJOURS UNE SEULE exception filtree
  (le cycle beneficiaires <-> referentiel, decision G-2 de MM.8).
  Si un SECOND cycle apparait, le sprint n'est pas valide.

FRONTEND :
  cd frontend ; npx oxlint ; npm run build

VERIFICATION MANUELLE (liste a donner a l'utilisateur) :
1. Creer une grille en tant qu'ARH -> statut EN_ATTENTE_CRH.
2. Se connecter en CRH -> la grille apparait dans son ecran de
   validation. Valider -> statut EN_ATTENTE_DRH.
3. Se connecter en DRH -> valider -> grille ACTIVE, ancienne grille
   date_fin renseignee (RG-10).
4. Refaire le cycle avec un rejet CRH -> motif affiche.
5. Refaire avec un rejet DRH -> motif affiche, et c'est bien le DERNIER
   motif saisi.
6. Si RG-08 retenue sur les grilles : tenter de valider en CRH avec le
   MEME compte que celui qui a cree en ARH -> 403.
7. Verifier la SIDEBAR : le CRH voit son entree de validation de
   grilles, l'ARH ne la voit PAS, et reciproquement (rappel audit 6F.9).
8. RESYNCHRONISATION : declencher un processus, modifier la grille
   d'une fonction concernee (cycle complet ARH->CRH->DRH pour
   l'activer), puis valider le processus en ARH -> verifier que le
   recapitulatif des montants resynchronises s'affiche avec les bons
   ancien/nouveau montants.
9. Verifier EN BASE que audit_log contient le delta de
   resynchronisation au format {"avant":..., "apres":...}.
```

---

## 9. Critères de validation

| Élément | Statut attendu |
|---|---|
| **Séquence de statuts** de la grille tranchée avec l'utilisateur | Fait |
| Sort des grilles déjà `EN_ATTENTE_DRH` en base tranché (migration de données) | Fait |
| **Option RG-08 (W-1 / W-2 / W-3) tranchée avec l'utilisateur** | Fait |
| **`ModularityTests` vert, avec TOUJOURS UNE SEULE exception filtrée** — aucun second cycle | Vérifié |
| Filtre de `ModularityTests` **non modifié** sans accord explicite | Vérifié |
| Workflow grilles ARH→CRH→DRH fonctionnel de bout en bout | Vérifié |
| CRH peut valider **et** rejeter avec motif | Vérifié |
| Dernier motif de rejet affiché, quel que soit l'acteur | Vérifié |
| RG-10 préservée : une seule grille ACTIVE sans `date_fin` par fonction | Vérifié par test |
| `contrats_api_dotations_v3.md` mis à jour (nouveaux endpoints, nouveaux statuts) | Fait |
| `statutGrille.js` : libellés centralisés, non écrits en dur dans les pages | Vérifié |
| Sidebar : rôles **strictement identiques** à ceux du `ProtectedRoute` cible | Vérifié |
| **Interprétation « avant de confirmer » (deux temps / un temps) tranchée** | Fait |
| Cas d'une fonction sans grille ACTIVE à la resynchronisation tranché | Fait |
| Montants obsolètes resynchronisés sur la grille ACTIVE (RG-04) | Vérifié |
| Resynchronisation **non silencieuse** : récapitulatif ancien → nouveau visible par l'ARH | Vérifié |
| **Delta de resynchronisation tracé dans `audit_log`** (RG-09, exigence de la décision B) | Vérifié en base |
| Résolution de montant **réutilisée**, jamais réécrite (unicité acquise en MM.3) | Vérifié |
| Migration Flyway créée si nécessaire, **aucune existante modifiée** | Vérifié |
| Suite backend : ≥ total MM.11, 0 échec | Vérifié |

---

## Commit

```bash
git add .
git commit -m "mm.12: workflow grilles a trois acteurs et resynchronisation non silencieuse des montants"
```

---

**Fin du Sprint MM.12** — *en attente de validation avant MM.13*
