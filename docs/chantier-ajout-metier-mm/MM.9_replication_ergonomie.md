# SPRINT MM.9

## Réplication de M.1 — ergonomie du modal d'ajustement et persistance des filtres

*Module Dotations Téléphoniques Mensuelles — Frontend + module `beneficiaires`*

| | |
|---|---|
| **Objet** | Répliquer sur la version modulaire les changements de M.1, **déjà implémentés, testés et validés** sur le projet d'origine |
| **Livrable** | 12 fichiers frontend + 4 fichiers du module `beneficiaires`, à l'identique du comportement validé |
| **Durée** | Une demi-journée à une journée |
| **Prérequis** | MM.8 validé |
| **Source de vérité** | `SPRINT_M1_REALISE.md` du dépôt d'origine — document de traçabilité technique exhaustif |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Réplication frontend (étapes 2-4) | Sonnet 5 | **Low** |
| Réplication backend module `beneficiaires` (étape 5) | Sonnet 5 | Low à Medium |
| Vérification (étape 6) | Sonnet 5 | Medium |

**Sprint volontairement à effort bas.** Ce n'est pas de la conception : le comportement est déjà arbitré, implémenté et validé manuellement par le responsable projet sur la version d'origine. L'effort High serait du gaspillage — la seule difficulté est la rigueur de la transposition.

## 0. Nouvelle session : rappel Graphify

```
/graphify . --update
```

---

## 1. Contexte

M.1 a été **entièrement réalisé et validé** sur le projet d'origine (version en couches techniques). Son document de traçabilité, `SPRINT_M1_REALISE.md`, décrit fichier par fichier ce qui a changé et pourquoi — y compris les décisions prises en séance qui s'écartent du guide initial.

Ce sprint le **réplique**, il ne le reconçoit pas.

### 1.1 Pourquoi la réplication est simple ici

| Volet | Impact modulaire |
|---|---|
| **Frontend** (12 fichiers) | Le chantier MM n'a **jamais touché** `frontend/`. L'arborescence est identique dans les deux versions → **copie à l'identique**, aucun chemin à adapter. |
| **Backend** (4 fichiers) | Les 3 fichiers de production touchés (`BeneficiaireSpecifications`, `BeneficiaireService`, `BeneficiaireController`) appartiennent **tous les trois au module `beneficiaires`** → aucun franchissement de frontière, aucune API inter-modules à étendre, aucune déclaration `allowedDependencies` à ajouter. |

C'est le sprint le moins risqué du lot. Il sert aussi à roder la méthode de réplication sur la copie modulaire avant les sprints à vrai contenu de conception (MM.10 à MM.13).

### 1.2 Les seuls chemins à adapter — 4 fichiers backend

| Fichier | Chemin d'origine | Chemin modulaire |
|---|---|---|
| `BeneficiaireSpecifications.java` | `backend/…/dottel/repository/` | `backend/…/dottel/beneficiaires/repository/` |
| `BeneficiaireService.java` | `backend/…/dottel/service/` | `backend/…/dottel/beneficiaires/service/` |
| `BeneficiaireController.java` | `backend/…/dottel/controller/` | `backend/…/dottel/beneficiaires/controller/` |
| `BeneficiaireServiceTest.java` | `backend/…/test/…/dottel/service/` | `backend/…/test/…/dottel/beneficiaires/service/` |

**Tous les fichiers frontend gardent leur chemin exact.**

### 1.3 Périmètre réel, plus large que le guide M.1 d'origine

Trois écarts au guide initial, tous décidés en séance et **validés** — à répliquer tels quels :

1. **4 pages, pas 3.** `UtilisateursListPage.jsx` figurait dans la décision C de M.0 (« 4 pages à navigation sortante ») mais pas dans le texte du guide M.1. Traité sur confirmation explicite.
2. **Toggle unique, pas deux boutons.** Le modal d'ajustement a un seul bouton dynamique (« Tout sélectionner » / « Tout désélectionner ») au lieu de deux boutons séparés.
3. **Ajout backend hors périmètre initial** : filtre « Nom ou matricule » sur `Beneficiaires.jsx`, nécessitant un nouveau paramètre de recherche sur `GET /beneficiaires` — parce que cette liste est paginée **côté serveur** (20/page), donc un filtre purement client aurait été trompeur.

---

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
1. Lis CLAUDE.md dans son integralite, en particulier la section 15
   (charte visuelle frontend).
2. Lis docs/chantier-ajout-metier-mm/PLAN_AJOUTS_METIER_MM.md
3. Lis docs/chantier-ajout-metier-mm/MM.9_replication_ergonomie.md
4. Lis le document de tracabilite source, qui est la VERITE de
   reference pour ce sprint :
   "D:\stage afriland\formation spécialisée DSI\projet de gestion des absences\implementation\afb-dottel\docs\chantier d'ajout metier\SPRINT_M1_REALISE.md"
   Tu as le droit de LIRE dans le depot d'origine. Tu n'as JAMAIS le
   droit d'y ECRIRE.
5. Lance /graphify . --update

CONTEXTE : Sprint MM.9. Replication d'un sprint DEJA VALIDE (M.1) sur
la version modulaire. Ce n'est pas de la conception : le comportement
est arbitre, teste et approuve. Tu transposes, tu ne reinventes pas.

VARIABLES D'ENVIRONNEMENT (TROIS, pas deux) :
  $env:DB_URL="jdbc:postgresql://localhost:5432/afb_dotations_telephoniques_mm"
  $env:DB_PASSWORD="admin"
  $env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"

METHODE DE TRAVAIL :
- Un volet a la fois (frontend puis backend), mvn test apres le volet
  backend.
- Reference de non-regression : > 226 tests apres MM.8, 0 echec.
  Ce sprint ajoute 1 test (voir etape 5).
- Si tu constates un ECART entre ce que decrit SPRINT_M1_REALISE.md et
  ce que tu trouves reellement dans la copie modulaire, ARRETE-TOI et
  signale-le : cela voudrait dire que le chantier MM a modifie quelque
  chose que la replication doit prendre en compte.

PREMIERE ACTION : la verification d'espace de travail, puis l'etape 2.
```

---

## 3. Étape 2. Volet 1 — modal d'ajustement

Fichier : `frontend/src/pages/workflow/AjusterLignesModal.jsx` *(chemin identique dans les deux versions)*

```
Replique le volet 1 de SPRINT_M1_REALISE.md (section 2).

Trois changements :

1. TOGGLE UNIQUE de selection groupee (un seul bouton dynamique, PAS
   deux boutons separes -- decision actee en seance) :
   - etat derive `toutesIncluses` = true si toutes les lignes
     ACTUELLEMENT VISIBLES (apres filtre) sont incluses
   - fonction `definirInclusionGlobale(inclusDansEtat)` qui bascule
     l'inclusion de toutes les lignes de `lignesFiltrees` -- et NON
     de `lignes`. C'EST LE POINT IMPORTANT : le toggle n'agit que sur
     ce qui est visible.
   - libelle : "Tout deselectionner" si toutesIncluses, sinon "Tout
     selectionner" ; onClick appelle definirInclusionGlobale(!toutesIncluses)
   - place dans le CardHeader, a cote du titre
     (flex-row items-center justify-between)

2. FILTRE nom/matricule :
   - etat `recherche` (useState('')), champ Input avec placeholder
     "Rechercher par matricule ou nom…", positionne entre le header et
     le tableau
   - `lignesFiltrees` = useMemo filtrant `lignes` sur
     matricule.toLowerCase().includes(terme) OU
     nomPrenoms.toLowerCase().includes(terme) -- une seule barre,
     recherche OR
   - le tableau boucle sur `lignesFiltrees`, avec une ligne d'etat vide
     ("Aucun beneficiaire ne correspond a la recherche.") si vide

3. INVARIANT A NE PAS CASSER : soumettre() continue d'iterer sur
   `lignes` (l'ensemble COMPLET, non filtre). Le filtre n'affecte
   JAMAIS la soumission, uniquement l'affichage et la portee du toggle.

Comportement garanti a verifier : filtrer par un nom partiel, cliquer
"Tout deselectionner" -> seules les lignes visibles apres filtre sont
affectees ; les lignes masquees gardent leur etat d'inclusion anterieur.
```

---

## 4. Étapes 3 et 4. Volet 2 — persistance des filtres par l'URL

Décision **C1** de M.0 : filtres portés par les query params de l'URL.

### 4.1 Utilitaire partagé à créer

```
Cree frontend/src/utils/searchParams.js avec deux fonctions :

  definirParametre(searchParams, cle, valeur)
      set ou delete une cle de URLSearchParams (chaine vide => delete)

  construireRetour(chemin, searchParams)
      "chemin?filtres", sans "?" final si searchParams est vide

Utilise-les sur TOUTES les pages ci-dessous plutot que de dupliquer la
logique inline. C'est le pattern deja etabli dans ce projet : des que
2+ pages partagent un besoin identique, remonter en utilitaire partage
(meme raisonnement que pour le composant LienRetour).
```

### 4.2 Principe appliqué à chaque page de liste

```
Pour CHAQUE page de liste :

1. Les filtres ne sont plus des useState locaux : ils sont LUS depuis
   useSearchParams() (searchParams.get('cle') ?? '') et ECRITS via
   setSearchParams((precedent) => definirParametre(precedent, 'cle', valeur)).
2. Chaque navigation sortante passe
   state: { retour: construireRetour('/chemin-liste', searchParams) }
   a navigate(...).
3. La page cible lit location.state?.retour ?? '/valeur-par-defaut' et
   l'utilise comme `to` du composant LienRetour
   (frontend/src/components/ui/LienRetour.jsx -- NON MODIFIE, seul son
   usage change).
4. Si la page cible redirige apres une action reussie (ex. creation),
   elle redirige vers retourListe au lieu d'un chemin fixe.
```

### 4.3 Les 4 groupes — tous chemins identiques à l'origine

| Groupe | Fichiers | Filtres portés par l'URL |
|---|---|---|
| **Processus mensuel** | `pages/workflow/ProcessusListPage.jsx`<br>`pages/workflow/ProcessusDetailPage.jsx`<br>`pages/workflow/DeclencherProcessusPage.jsx` | `statut`, `annee`. Propage `retour` vers `/processus/declencher` et `/processus/{id}`. Sur `DeclencherProcessusPage`, le bouton « Voir le détail du processus » propage aussi `retourListe`. |
| **Grilles tarifaires** | `pages/grilles/GrillesListPage.jsx`<br>`pages/grilles/HistoriqueGrillePage.jsx`<br>`pages/grilles/CreerGrillePage.jsx` | `fonction`, `statut`. Propage vers `/grilles-tarifaires/creer` et `/grilles-tarifaires/historique/{code}`. Redirection post-création → `retourListe`. |
| **Administration utilisateurs** | `pages/admin/UtilisateursListPage.jsx`<br>`pages/admin/CreerUtilisateurPage.jsx` | `role`, `actif`. Propage vers `/admin/utilisateurs/creer`. Redirection post-création → `retourListe`. **4ᵉ page, actée par M.0 décision C.** |
| **Bénéficiaires** | `pages/dashboard/Beneficiaires.jsx`<br>`pages/enrolement/ImporterBeneficiairesPage.jsx` | `fonction`, `recherche`, `unite` (debounce 300 ms), `actif`, `page`. Ajout d'un `LienRetour` vers `/beneficiaires` sur la page d'import (**absent avant**). |

### 4.4 Deux pièges documentés, à ne pas re-découvrir

```
PIEGE 1 -- remise a zero de la pagination.
L'effet qui reinitialise `page` a 0 quand un filtre change DOIT
utiliser une garde `premierRendu` (useRef(true)) pour NE PAS
s'executer au premier montage. Sans cette garde, recharger une URL
sur une page > 0 avec des filtres actifs ramenerait systematiquement
a la page 0 -- exactement le contraire de l'objectif de persistance.

PIEGE 2 -- limite assumee sur le groupe Beneficiaires.
Beneficiaires.jsx n'a AUCUNE navigation de page contextuelle vers
ImporterBeneficiairesPage.jsx : l'acces a l'import passe par le lien
generique de la Sidebar, pas un bouton de la liste. Il n'y a donc pas
de point d'interception pour passer state.retour -- et l'edition d'un
beneficiaire se fait par MODALE (ModifierBeneficiaireModal.jsx), jamais
par navigation de page.
Le benefice reel de ce groupe est donc la persistance au RECHARGEMENT
de page et via les boutons precedent/suivant du navigateur, pas le
retour depuis un ecran de detail. Limite deja signalee et ACCEPTEE
telle quelle -- ne cherche pas a la "corriger".
```

---

## 5. Étape 5. Volet 3 — filtre nom/matricule côté serveur

Ajout backend, hors périmètre du guide M.1 initial, demandé et validé en séance.

```
Quatre fichiers, TOUS dans le module beneficiaires -- aucun
franchissement de frontiere de module, aucune API inter-modules a
etendre, aucune declaration allowedDependencies a ajouter.

1. beneficiaires/repository/BeneficiaireSpecifications.java
   avecFiltres(...) gagne un parametre `recherche` : LIKE insensible a
   la casse sur matricule OU nomPrenoms (cb.or(...)), meme semantique
   que le filtre du modal du volet 1.

2. beneficiaires/service/BeneficiaireService.java
   rechercher(fonction, uniteRattachement, actif, pageable)
   devient
   rechercher(fonction, recherche, uniteRattachement, actif, pageable)

3. beneficiaires/controller/BeneficiaireController.java
   GET /beneficiaires accepte un nouveau
   @RequestParam(required = false) String recherche

4. beneficiaires/service/BeneficiaireServiceTest.java (dossier de test)
   Mettre a jour les 4 appels existants (nouvel argument null) et
   ajouter le test
   rechercher_filtreParRecherche_retourneParNomOuMatricule

VERIFICATION IMPORTANTE avant de coder : confirme que rechercher()
n'est appelee QUE par BeneficiaireController (donc intra-module). Si
un autre module l'appelle -- ce qui ne devrait pas etre le cas, elle
ne fait pas partie de BeneficiaireApi -- ARRETE-TOI et signale-le.

Cote frontend, dans Beneficiaires.jsx (meme fichier que 4.3) :
- nouveau champ controle `rechercheSaisie` avec debounce 300 ms (meme
  pattern que `uniteSaisie`), repercute dans le parametre d'URL
  `recherche`
- positionne ENTRE les filtres "Fonction" et "Unite de rattachement"
  (placement demande explicitement ; ne remplace PAS le filtre Unite)
- largeurs pour un alignement propre : Fonction w-56 -> w-48, nouveau
  champ "Nom ou matricule" w-52, Unite w-56 -> w-48, Statut inchange
  w-40
- requete GET /beneficiaires : nouveau param
  recherche: recherche || undefined
```

**Note de contrat API** : `GET /beneficiaires` n'est pas documenté avec le détail de ses query params dans `CLAUDE.md` section 8 (seule la route y figure). Aucune mise à jour de contrat nécessaire — mais à garder en tête si `CLAUDE.md` est un jour enrichi.

---

## 6. Étape 6. Vérification

```
BACKEND :
  cd backend ; .\mvnw.cmd test
Attendu : le total de MM.8 + 1 (nouveau test
rechercher_filtreParRecherche_retourneParNomOuMatricule), 0 echec.

MODULARITE :
  ModularityTests doit rester VERT. Aucun de ces changements n'est
  inter-modules -- si une violation apparait, quelque chose a ete mal
  place, ARRETE-TOI.

FRONTEND :
  cd frontend ; npx oxlint ; npm run build
Attendu : build reussi. Aucun test frontend automatise n'existe dans
ce projet (pas de suite Vitest/Jest configuree).

VERIFICATION MANUELLE (a faire par l'utilisateur, liste a lui donner) :
1. Modal d'ajustement : filtrer par un nom partiel, cliquer "Tout
   deselectionner" -> seules les lignes visibles sont affectees.
2. Modal d'ajustement : filtrer, deselectionner, EFFACER le filtre ->
   les lignes precedemment masquees ont bien garde leur etat.
3. Sur chacune des 4 pages de liste : poser des filtres, RECHARGER la
   page (F5) -> les filtres sont toujours la.
4. Sur les 3 groupes a navigation sortante : poser des filtres, entrer
   dans un detail, revenir par le LienRetour -> les filtres sont
   toujours la.
5. Beneficiaires : filtrer par "Nom ou matricule", verifier que le
   resultat porte bien sur TOUTE la base et pas seulement la page
   courante (chercher un nom qui n'est pas sur la page 1).
6. Beneficiaires : aller en page 2 avec des filtres, recharger ->
   toujours page 2 (piege 1 de la section 4.4).
```

---

## 7. Critères de validation

| Élément | Statut attendu |
|---|---|
| **Volet 1** — toggle unique dynamique, portée limitée aux lignes visibles | Vérifié |
| Volet 1 — `soumettre()` itère toujours sur l'ensemble **complet** | Vérifié |
| Volet 1 — état vide affiché si le filtre ne ramène rien | Vérifié |
| **Volet 2** — `frontend/src/utils/searchParams.js` créé et utilisé partout (aucune duplication inline) | Vérifié |
| Volet 2 — filtres persistés par l'URL sur les **4** pages de liste | Vérifié |
| Volet 2 — `LienRetour` reçoit `location.state?.retour` sur les pages cibles, composant **non modifié** | Vérifié |
| Volet 2 — garde `premierRendu` en place (piège 1) | Vérifié |
| Volet 2 — `LienRetour` ajouté sur `ImporterBeneficiairesPage.jsx` | Vérifié |
| **Volet 3** — les 4 fichiers backend sont bien tous dans le module `beneficiaires` | Vérifié |
| Volet 3 — recherche serveur porte sur toute la base, pas la page courante | Vérifié manuellement |
| `ModularityTests` toujours vert, aucune nouvelle violation | Vérifié |
| Suite backend : total MM.8 **+1**, 0 échec | Vérifié |
| `oxlint` et `npm run build` OK | Vérifié |
| Aucune migration Flyway ajoutée | Vérifié |
| Écart éventuel avec `SPRINT_M1_REALISE.md` signalé, non improvisé | Fait |

---

## Commit

```bash
git add .
git commit -m "mm.9: replication de M.1 -- ergonomie du modal d'ajustement et persistance des filtres par l'URL"
```

---

**Fin du Sprint MM.9** — *en attente de validation avant MM.10*
