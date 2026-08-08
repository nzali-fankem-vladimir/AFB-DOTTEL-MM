# SPRINT MM.11

## Période de déclenchement et rattrapage d'un mois passé

*Module Dotations Téléphoniques Mensuelles — module `processus`*

| | |
|---|---|
| **Objet** | Restreindre les périodes déclenchables (courant + antérieur, jamais futur) et permettre le rattrapage d'un mois passé pour les bénéficiaires non payés |
| **Livrable** | Contrôle de période sur `declencher()`, mode rattrapage, **évolution encadrée de RG-12** |
| **Durée** | Une journée et demie |
| **Prérequis** | MM.10 validé (décision A de M.0 déjà actée : **A2**) |
| **Origine** | `M.3_periode_declenchement.md` |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Restriction de période (étapes 2-3) | Sonnet 5 | Medium |
| Conception du rattrapage et de RG-12 (étape 4) | Opus 4.8 ou Sonnet 5 | **High** |
| Implémentation du rattrapage (étape 5) | Sonnet 5 ou Opus 4.8 | **High** |

Le volet rattrapage est le plus délicat : il touche `declencher()` **et fait évoluer RG-12**, une règle métier figée dans `CLAUDE.md`.

## 0. Nouvelle session : rappel Graphify

```
/graphify . --update
```

---

## 1. Contexte

Deux règles nouvelles sur le déclenchement du cycle mensuel. **Bonne nouvelle architecturale** : tout se passe dans le module `processus` — `ProcessusMensuelService`, les entités `ProcessusMensuel` et `LigneEtatMensuel`, et leurs repositories y vivent tous. Aucune frontière de module franchie, aucune API inter-modules à étendre.

### 1.1 Restriction de période

Aujourd'hui `POST /processus/declencher` accepte n'importe quel mois/année, la seule contrainte étant RG-12 (unicité). Le métier veut :

- mois **courant** et tout mois/année **antérieur** : autorisés (pour rattraper un incident)
- mois/année **futur** : interdit
- si la période choisie est antérieure au mois courant : afficher une **confirmation** avant de déclencher

Cette règle **remplace** la piste « RG-13 » évoquée précédemment (qui proposait « mois courant ou mois précédent seulement »). La décision métier est plus large : **pas de limite basse, seulement une limite haute**.

### 1.2 Rattrapage d'un mois passé

Le métier veut redéclencher un mois déjà traité, mais **uniquement pour les bénéficiaires qui n'ont pas reçu la dotation ce mois-là** (réclamation externe).

**Affichage : décision A de M.0, actée — A2 : tout afficher, les non-payés pré-cochés.** L'ARH voit donc l'ensemble des bénéficiaires, avec les non-payés déjà cochés, et garde la main pour ajuster.

### 1.3 ⚠️ RG-12 doit ÉVOLUER, pas être contournée

**Point acté explicitement le 2026-08-03, à ne pas réinterpréter :**

> La règle d'unicité mois/année (RG-12) a été écrite **avant** que le cas du rattrapage n'existe. Elle est donc à **faire évoluer, pas à contourner** : distinguer un processus de rattrapage d'un processus normal, **plutôt que supprimer l'unicité** — qui reste utile contre un vrai doublon accidentel.

Concrètement : après ce sprint, la contrainte doit toujours empêcher deux processus **normaux** sur la même période, tout en autorisant un ou plusieurs processus de **rattrapage** sur une période déjà traitée.

Texte actuel de RG-12 (`CLAUDE.md` section 7), à faire évoluer :

> « Un seul processus mensuel peut exister pour une combinaison mois/année donnée. Contrainte `UNIQUE(mois_paiement, annee_paiement)` en base (script V1). Toute tentative de créer un second processus pour la même période retourne 409 Conflict. »

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
1. Lis CLAUDE.md dans son integralite, en particulier RG-12 (section 7)
   et la section 8 (endpoint POST /processus/declencher).
2. Lis docs/chantier-ajout-metier-mm/PLAN_AJOUTS_METIER_MM.md
3. Lis docs/chantier-ajout-metier-mm/MM.11_periode_declenchement.md
   EN ENTIER -- surtout la section 1.3 sur RG-12.
4. Lance /graphify . --update

CONTEXTE : Sprint MM.11. Deux volets : restriction de periode (simple)
et rattrapage d'un mois passe (delicat, fait evoluer RG-12).

DEUX DECISIONS DEJA ACTEES, a appliquer sans les rediscuter :
  - Decision A de M.0 : A2 -- au rattrapage, TOUT afficher avec les
    non-payes PRE-COCHES.
  - RG-12 doit EVOLUER, pas etre contournee : distinguer un processus
    de rattrapage d'un processus normal, PLUTOT QUE supprimer
    l'unicite (qui reste utile contre un vrai doublon accidentel).

VARIABLES D'ENVIRONNEMENT (TROIS, pas deux) :
  $env:DB_URL="jdbc:postgresql://localhost:5432/afb_dotations_telephoniques_mm"
  $env:DB_PASSWORD="admin"
  $env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"

METHODE DE TRAVAIL :
- Un fichier a la fois. Tu montres, j'approuve, tu continues.
- Reference de non-regression : total de MM.10, 0 echec.
- Tout se passe dans le module processus : ModularityTests ne devrait
  pas bouger. Si une violation apparait, ARRETE-TOI.

PREMIERE ACTION : la verification d'espace de travail, puis l'etape 2
(restriction de periode) -- plus simple et independante du rattrapage.
```

---

## 3. Étape 2. Restriction de période — backend

```
Dans processus/service/ProcessusMensuelService.java, methode
declencher(), APRES le controle d'unicite RG-12 existant, ajoute la
verification de periode :

- la periode demandee (mois/annee) ne doit JAMAIS etre POSTERIEURE au
  mois courant (YearMonth.now()) -> rejet
- une periode egale au mois courant, ou anterieure, est autorisee

Utilise java.time.YearMonth : il gere nativement le franchissement
d'annee (decembre 2026 vs janvier 2027), contrairement a une
comparaison mois puis annee ecrite a la main.

Cree une exception dediee dans processus/exception/ (les exceptions
sont reparties par module depuis la decision C de MM.0) -- par exemple
PeriodeProcessusFutureException -- mappee en HTTP 400 dans
security/GlobalExceptionHandler.java.

400 et NON 409 : le 409 est reserve aux conflits d'unicite dans ce
projet (RG-03, RG-12). Une periode future est une donnee invalide, pas
un conflit.

PIEGE A VERIFIER AVANT DE COMMITTER : cherche les tests existants de
declencher() qui codent un mois/annee EN DUR. Certains utilisent des
periodes comme 7/2026, 9/2026, 10/2026, 11/2026, 12/2026, voire 2027
(vu dans les noms de fichiers PDF de test). Ceux dont la periode est
FUTURE par rapport a la date reelle d'execution vont casser des que
cette verification existe.
=> Derive-les de YearMonth.now() dans le MEME changement, sinon la
suite de tests cassera un jour au hasard sans rapport avec le code
modifie. Montre-moi la liste des tests concernes.

Montre le service, la nouvelle exception, et le handler.
```

---

## 4. Étape 3. Restriction de période — frontend

```
Dans frontend/src/pages/workflow/DeclencherProcessusPage.jsx :

1. Empeche STRUCTURELLEMENT la selection d'une periode future : le
   selecteur ne propose que le mois courant et les mois/annees
   anterieurs.
2. Si la periode choisie est anterieure au mois courant, affiche une
   confirmation AVANT l'envoi : "La periode selectionnee est
   anterieure au mois actuel. Voulez-vous continuer ?"
   Reutilise le composant ConfirmDialog existant
   (frontend/src/components/ui/ConfirmDialog.jsx) plutot que d'en
   ecrire un nouveau.
3. Gere la reponse 400 du backend (periode future) comme filet de
   securite, meme si l'UI l'empeche deja -- meme principe que les
   autres pages : le backend reste la source de verite.

ATTENTION : ce fichier a deja ete touche par MM.9 (persistance des
filtres, LienRetour, propagation de retourListe). Verifie son etat reel
avant d'editer, ne repars pas de la version d'origine.
```

---

## 5. Étape 4. Rattrapage — conception avant tout code

**Arrêt obligatoire.** Trois points à traiter avec l'utilisateur avant la moindre ligne.

```
Traite ces trois points avec moi AVANT de coder le rattrapage.

POINT 1 -- Comment distinguer un rattrapage d'un processus normal ?

RG-12 doit EVOLUER, pas etre contournee (rappel : decision actee).
L'unicite mois/annee doit continuer d'empecher deux processus NORMAUX
sur la meme periode, tout en autorisant un rattrapage sur une periode
deja traitee.

Presente-moi au moins deux modelisations, avec leurs implications sur
la contrainte UNIQUE(mois_paiement, annee_paiement) du script V1 :

  R-1) Un champ discriminant sur ProcessusMensuel (par exemple un
       booleen `rattrapage`, ou un type/enum). La contrainte UNIQUE
       en base doit alors devenir PARTIELLE (unique sur mois+annee
       uniquement quand ce n'est pas un rattrapage) -- ce qui exige
       une migration Flyway pour remplacer la contrainte existante.
       Precedent dans ce projet : grille_tarifaire porte deja une
       contrainte d'unicite PARTIELLE (unique sur id_fonction_eligible
       WHERE statut_validation = 'ACTIVE' AND date_fin IS NULL) --
       le pattern existe donc deja, ce n'est pas une nouveaute.

  R-2) Reouverture / extension du processus existant du meme mois,
       sans nouveau ProcessusMensuel. Pas de migration de contrainte,
       mais il faut alors decider ce que devient le processus original
       (deja CLOTURE, avec un PDF signe 3 fois et un evenement Kafka
       deja publie) -- ce qui touche RG-06 et la chaine comptable.

Recommande-en une, avec le motif. J'arbitre.

POINT 2 -- Comment determiner "ce beneficiaire n'a pas recu la dotation
pour le mois M" a partir des donnees existantes ?

La piste attendue : jointure LigneEtatMensuel <-> ProcessusMensuel
CLOTURE du meme mois/annee, avec inclusDansEtat = true signifiant
"dotation recue".
CONFIRME que cette jointure est faisable avec les repositories
existants du module processus (LigneEtatMensuelRepository,
ProcessusMensuelRepository) -- et si une nouvelle methode de repository
est necessaire, montre-la-moi.

Attention au cas limite : un beneficiaire dont la ligne existe avec
inclusDansEtat = FALSE (exclu par l'ARH volontairement) est-il
"non paye" au sens du rattrapage, ou "volontairement exclu, donc a ne
pas rattraper" ? POSE-MOI LA QUESTION, ne tranche pas.

POINT 3 -- Impact sur RG-12 dans CLAUDE.md.

RG-12 est une regle metier figee dans CLAUDE.md section 7. Ce sprint la
fait evoluer. PROPOSE-MOI une reecriture du texte de RG-12 refletant la
distinction rattrapage / normal, mais NE MODIFIE PAS CLAUDE.md sans mon
accord explicite : c'est le document normatif du projet.
```

---

## 6. Étape 5. Rattrapage — implémentation

```
Une fois la modelisation validee a l'etape 4, implemente.

BACKEND :
- Le declenchement en mode rattrapage sur un mois deja traite liste
  TOUS les beneficiaires actifs, avec les NON-PAYES PRE-COCHES
  (decision A2 de M.0 -- pas seulement les non-payes, tout le monde
  avec pre-cochage).
- L'integrite du processus ORIGINAL ne doit jamais etre alteree :
  ni son statut, ni ses lignes, ni sa piece jointe, ni son evenement
  Kafka deja publie.
- Migration Flyway si la modelisation R-1 est retenue (contrainte
  d'unicite partielle). NE MODIFIE JAMAIS un script deja applique.
- Audit (RG-09) : un declenchement en rattrapage doit etre tracable
  comme tel dans audit_log, distinct d'un declenchement normal.

FRONTEND (DeclencherProcessusPage.jsx) :
- Un moyen CLAIR de distinguer un declenchement normal d'un rattrapage,
  avec un libelle explicite pour l'ARH.
- Proposition : quand l'ARH selectionne un mois deja traite, l'UI
  detecte le cas et bascule sur le mode rattrapage avec un message
  explicite. Montre-moi ta proposition d'ergonomie avant de coder --
  c'est un ecran que l'ARH utilise chaque mois.

TESTS a ajouter (donnees camerounaises, CLAUDE.md section 9) :
- periode future rejetee (400)
- periode anterieure acceptee
- periode courante acceptee
- rattrapage : les non-payes sont bien pre-coches, les deja-payes
  presents mais non coches
- rattrapage : le processus original n'est PAS altere (verifier statut,
  lignes, piece jointe inchanges)
- rattrapage : la contrainte d'unicite empeche toujours deux processus
  NORMAUX sur la meme periode (non-regression RG-12)
```

---

## 7. Étape 6. Vérification

```
BACKEND :
  cd backend ; .\mvnw.cmd test
Attendu : total de MM.10 + les nouveaux tests, 0 echec.

MODULARITE :
  ModularityTests VERT. Tout ce sprint est intra-module processus --
  aucune nouvelle violation attendue.

MIGRATION :
  Si une migration a ete creee, demarre le backend et confirme que
  Flyway l'applique sans erreur de checksum sur les precedentes.

FRONTEND :
  cd frontend ; npx oxlint ; npm run build

VERIFICATION MANUELLE (liste a donner a l'utilisateur) :
1. Tenter de selectionner un mois futur -> impossible dans le
   selecteur.
2. Forcer un mois futur via un appel API direct (curl/Postman) ->
   400 avec un message explicite.
3. Selectionner un mois anterieur -> message de confirmation affiche
   avant declenchement.
4. Selectionner un mois DEJA TRAITE -> le mode rattrapage est propose,
   avec un libelle clair.
5. Declencher un rattrapage -> verifier que les non-payes sont
   pre-coches et les deja-payes decoches.
6. Verifier EN BASE que le processus original du meme mois est
   totalement inchange (statut, lignes, piece jointe).
7. Tenter de declencher deux processus NORMAUX sur la meme periode ->
   toujours 409 (RG-12 preservee).
```

---

## 8. Critères de validation

| Élément | Statut attendu |
|---|---|
| Période **future** rejetée (400, pas 409) | Vérifié |
| Période **courante** et **antérieure** acceptées | Vérifié |
| `YearMonth` utilisé (franchissement d'année géré) | Vérifié |
| Tests à période codée en dur dérivés de `YearMonth.now()` | Vérifié |
| Sélecteur frontend n'autorisant **structurellement** pas de période future | Vérifié |
| Message de confirmation si période antérieure au mois courant | Vérifié |
| 400 du backend géré côté frontend comme filet de sécurité | Vérifié |
| **Modélisation du rattrapage (R-1 / R-2) tranchée avec l'utilisateur** | Fait |
| **RG-12 fait évoluer, pas contournée** — deux processus normaux sur la même période toujours refusés | Vérifié par test |
| Réécriture de RG-12 **proposée** pour `CLAUDE.md`, non appliquée sans accord | Fait |
| Cas du bénéficiaire volontairement exclu (`inclusDansEtat = false`) tranché avec l'utilisateur | Fait |
| Rattrapage : tout affiché, non-payés **pré-cochés** (décision A2) | Vérifié |
| Processus original **jamais altéré** par un rattrapage | Vérifié en base |
| Déclenchement en rattrapage tracé distinctement dans `audit_log` (RG-09) | Vérifié en base |
| Migration Flyway créée si nécessaire, **aucune existante modifiée** | Vérifié |
| Ergonomie du mode rattrapage validée avec l'utilisateur avant codage | Fait |
| `ModularityTests` toujours vert | Vérifié |
| Suite backend : ≥ total MM.10, 0 échec | Vérifié |

---

## Commit

```bash
git add .
git commit -m "mm.11: restriction de periode de declenchement et rattrapage d'un mois passe, RG-12 fait evoluer"
```

---

**Fin du Sprint MM.11** — *en attente de validation avant MM.12*
