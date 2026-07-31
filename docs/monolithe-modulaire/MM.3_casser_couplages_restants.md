# SPRINT MM.3

## Casser les quatre couplages inter-modules restants

*Module Dotations Téléphoniques Mensuelles — Backend*

| | |
|---|---|
| **Objet** | Éliminer les couplages C2 à C5 identifiés dans le plan maître : résolution de grille en triple, mutation croisée du référentiel, lectures directes de `DocumentService` et de `ReportingService` |
| **Livrable** | Aucun service n'accède plus au repository d'un autre module, hors `AuditService` (traité en MM.4) |
| **Durée** | Une à deux journées |
| **Prérequis** | MM.2 terminé, suite ≥ 205 au vert, violations de famille (a) résorbées |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| C2 — unification de la résolution de grille (étape 2) | Sonnet 5 | **High** |
| C3 — mutation croisée `FonctionEligibleService` (étape 3) | Sonnet 5 | **High** |
| C4 — `DocumentService` (étape 4) | Sonnet 5 | Medium |
| C5 — `ReportingService` (étape 5) | Sonnet 5 | Medium |

C2 et C3 touchent RG-04 et RG-01 ; C4 et C5 sont de la lecture pure, moins risquée.

## 0. Nouvelle session : rappel Graphify

```
/graphify . --update
```

## 1. Contexte

MM.2 a traité le plus gros foyer. Restent quatre couplages, indépendants les uns des autres — ils peuvent donc être traités dans l'ordre, chacun avec son propre commit si tu préfères découper.

## 2. Étape 1. Ouvrir la session

```
Tu es mon assistant de developpement pour le projet DOTTEL (Afriland
First Bank).

AVANT TOUT :
1. Lis CLAUDE.md dans son integralite (sections 7 et 17 en priorite).
2. Lis docs/monolithe-modulaire/PLAN_MONOLITHE_MODULAIRE.md
3. Lis docs/monolithe-modulaire/MM.0_cadrage.md (decoupage acte)
4. Lance /graphify . --update

CONTEXTE : Sprint MM.3. Quatre couplages independants a casser :
C2 (resolution de grille en triple), C3 (FonctionEligibleService qui
mute des Beneficiaire), C4 (DocumentService), C5 (ReportingService).

CE SPRINT NE TOUCHE PAS :
- AuditService (MM.4)
- Le schema de base de donnees
- Les contrats API

METHODE DE TRAVAIL :
- UN couplage a la fois, dans l'ordre C2, C3, C4, C5.
- mvn test complet apres chaque couplage. Reference : >= 205 tests.
- Tu montres le diff, j'approuve, tu continues.

PREMIERE ACTION : etape 2, couplage C2.
```

## 3. Étape 2. C2 — Trois implémentations d'une même résolution

### Constat factuel

Le parcours `code fonction → FonctionEligible → GrilleTarifaire ACTIVE avec dateFin IS NULL` (c'est-à-dire **RG-04**) est implémenté **trois fois**, différemment :

| # | Emplacement | Forme | Ce qu'il retourne |
|---|---|---|---|
| 1 | `BeneficiaireService.resoudreMontantCourant(String codeFonction)` — ligne 151, **publique** | Méthode publique du service | `Integer` montant, ou `null` si pas de grille active |
| 2 | `ProcessusMensuelService.resoudreGrillePourFonction(String codeFonction)` — ligne 679, **privée** | Méthode privée + `record ResolutionGrille` ligne 698 | La grille, **ou** un motif d'exclusion parmi 4 constantes |
| 3 | `EnrolementService` — lignes 91 à 96, **en ligne** | Code inline dans `confirmer()` | Rien : vérifie l'existence et lève `GrilleTarifaireIntrouvableException` sinon |

**Ce n'est donc pas une méthode dupliquée deux fois, mais trois comportements distincts pour une même règle.** L'implémentation 1 est aussi appelée depuis un autre service : `BeneficiaireExportService` ligne 52 (`beneficiaireService.resoudreMontantCourant(...)`).

Si MM.2 a bien été fait, l'implémentation 2 a déjà migré vers l'API du module `referentiel`. Ce sprint traite les deux autres.

### Prompt

```
Couplage C2 : unifier la resolution de grille tarifaire.

ETAT ATTENDU APRES MM.2 : le module referentiel expose deja une
methode publique de resolution. Confirme-le-moi d'abord -- si ce
n'est pas le cas, on a un probleme d'enchainement entre MM.2 et MM.3.

1. Fais pointer BeneficiaireService.resoudreMontantCourant() vers
   l'API du module referentiel, au lieu de refaire la resolution
   lui-meme. Attention au contrat existant : il retourne null quand
   aucune grille ACTIVE n'existe (documente dans
   docs/reference/contrats_api_dotations_v3.md : "montantCourant vaut
   null si aucune grille ACTIVE n'existe"). Ce comportement doit etre
   PRESERVE a l'identique.

2. Fais de meme pour le code inline d'EnrolementService (lignes 91-96
   du fichier d'origine). Attention : il leve aujourd'hui
   GrilleTarifaireIntrouvableException, qui produit un 400 via
   GlobalExceptionHandler. Ce code HTTP doit rester 400, pas 500 --
   c'est une correction explicitement documentee au contrat API V3.1.

3. Retire GrilleTarifaireRepository et FonctionEligibleRepository des
   dependances injectees de BeneficiaireService et EnrolementService.

4. Verifie que BeneficiaireExportService (ligne 52) fonctionne
   toujours -- il appelle resoudreMontantCourant() en cascade.

5. mvn test complet, montre-moi le diff et le resultat.

QUESTION A ME POSER SI TU HESITES : faut-il conserver
resoudreMontantCourant() comme methode publique de BeneficiaireService
(simple delegation vers le referentiel), ou faire appeler directement
l'API du referentiel par BeneficiaireExportService ? Ne tranche pas
seul, les deux se defendent.
```

## 4. Étape 3. C3 — `FonctionEligibleService` mute des `Beneficiaire`

### Constat factuel

`backend/src/main/java/com/afriland/dottel/service/FonctionEligibleService.java` injecte **`BeneficiaireRepository`** et l'utilise pour deux opérations d'écriture cross-domaine :

- **Renommage de code** : `beneficiaireRepository.findByFonction(...)` puis `saveAll(...)`. C'est une cascade volontaire, documentée dans `docs/reference/contrats_api_dotations_v3.md` §3bis : « `beneficiaires.fonction` est un `VARCHAR`, pas une FK, donc un renommage cascade explicitement vers les bénéficiaires restants référençant l'ancien code (y compris inactifs) pour éviter une référence orpheline ».
- **Comptage** : `nombreBeneficiairesActifs` exposé par `GET /fonctions-eligibles/toutes` et utilisé comme avertissement avant désactivation.

Il crée aussi directement une `GrilleTarifaire` à la création d'une fonction — mais si la décision B de MM.0 a retenu **B1** (un seul module `referentiel`), ce couplage-là est devenu **interne au module**, donc légitime. À vérifier avant de le toucher inutilement.

### Prompt

```
Couplage C3 : FonctionEligibleService ne doit plus muter directement
des entites Beneficiaire.

Deux operations sont concernees, elles n'ont pas la meme nature :

A) LECTURE -- le comptage nombreBeneficiairesActifs.
   Simple : le module beneficiaires expose une methode de comptage
   par code fonction. Aucune ambiguite.

B) ECRITURE -- la cascade de renommage de code.
   Deux options, PROPOSE-LES-MOI sans trancher :

   B-1) Appel d'API publique : le module beneficiaires expose une
        methode "renommerFonction(ancienCode, nouveauCode)". Simple,
        synchrone, reste dans la meme transaction -- donc l'atomicite
        actuelle est preservee telle quelle.

   B-2) Evenement applicatif FonctionRenommeeEvent : le module
        referentiel publie, le module beneficiaires ecoute.
        Decouplage plus fort, mais ATTENTION : si l'ecoute devient
        asynchrone, la cascade n'est plus atomique avec le renommage.
        Une fonction pourrait etre renommee sans que les beneficiaires
        suivent, laissant exactement la reference orpheline que le
        contrat API dit vouloir eviter.

   Mon avis : B-1 est plus sur ici, parce que le contrat API decrit
   explicitement la cascade comme faisant partie de l'operation. Mais
   MONTRE-MOI les deux et attends ma decision.

CONTRAINTE A NE PAS PERDRE DE VUE : le renommage n'est autorise que
si au plus 1 beneficiaire actif est rattache (409 sinon), et les
lignes ligne_etat_mensuel ne sont JAMAIS modifiees par ce renommage
(instantane historique volontairement fige). Verifie que ces deux
regles survivent au refactor.

Si la decision B de MM.0 a retenu un module referentiel unique,
NE TOUCHE PAS a la creation de GrilleTarifaire par
FonctionEligibleService : c'est devenu du couplage intra-module,
donc legitime. Confirme-le-moi avant.
```

## 5. Étape 4. C4 — `DocumentService` lit deux domaines étrangers

### Constat factuel

`backend/src/main/java/com/afriland/dottel/service/DocumentService.java` injecte cinq dépendances : `BeneficiaireRepository`, `FonctionEligibleRepository`, `PieceJointeRepository`, `EcartMensuelService`, `SignatureService`.

Les deux premières sont étrangères au module `processus`. Elles servent à construire le PDF de l'état mensuel (nom du bénéficiaire, libellé de fonction, code unité, chapitre).

**Point clé** : `LigneEtatMensuel` porte déjà `fonctionRetenue`, un instantané figé au moment du processus — précisément pour que l'état historique ne bouge pas si le bénéficiaire change de fonction ensuite (`CLAUDE.md` section 4). `DocumentService` qui va relire `Beneficiaire` en direct contourne partiellement cette intention.

### Prompt

```
Couplage C4 : DocumentService ne doit plus lire BeneficiaireRepository
ni FonctionEligibleRepository.

Principe : DocumentService ne va PAS chercher les donnees, on les lui
DONNE. L'appelant (ProcessusMensuelService, module processus) assemble
un DTO complet et le passe en parametre.

1. Recense d'abord precisement QUELLES donnees de Beneficiaire et de
   FonctionEligible sont reellement utilisees dans la generation du
   PDF (nom, code unite, chapitre, libelle de fonction, numero de
   compte ?). Montre-moi la liste avant de coder.

2. Concois un DTO de ligne de document portant exactement ces
   donnees, ni plus ni moins.

3. Fais assembler ce DTO par l'appelant, via les API de modules deja
   creees en MM.2.

4. Retire les deux repositories des dependances de DocumentService.

POINT D'ATTENTION IMPORTANT : LigneEtatMensuel porte deja
fonctionRetenue, un instantane volontairement fige (CLAUDE.md section
4 : "Ces valeurs peuvent differer de celles du beneficiaire si l'ARH
ajuste l'etat mensuel avant validation"). Verifie si le PDF genere
aujourd'hui affiche fonctionRetenue ou la fonction actuelle du
beneficiaire -- si c'est la seconde, c'est peut-etre un BUG existant
qu'on revele au passage. Signale-le-moi, ne le corrige pas
silencieusement.

5. Verifie que RG-06 tient toujours : un seul PDF par processus,
   compteur nombre_signatures de 1 a 3, jamais trois fichiers.
   DocumentServiceTest contient 5 tests, ils doivent tous passer.
```

## 6. Étape 5. C5 — `ReportingService` lit trois domaines étrangers

### Constat factuel

- `ReportingService` injecte `BeneficiaireRepository`, `ProcessusMensuelRepository`, `LigneEtatMensuelRepository`. Il n'appelle aucun autre service.
- `HistoriqueExportService` n'injecte **que** `ReportingService`, dont il appelle `historique(annee)`.

Si la décision C.2 de MM.0 a fait de `reporting` un module sans entité ni repository en propre, alors **les trois repositories lui sont étrangers**. C'est structurel : un module de lecture agrégée a besoin de données qu'il ne possède pas.

### Prompt

```
Couplage C5 : ReportingService lit 3 repositories etrangers.

C'est le couplage le plus structurel du sprint : un module de
reporting a besoin, par nature, de donnees qu'il ne possede pas.

Propose-moi DEUX approches, sans trancher :

R-1) Chaque module proprietaire expose une methode de projection
     dediee au reporting (ex: le module processus expose
     "statistiquesParAnnee(int annee)"). ReportingService devient un
     simple assembleur. Frontieres nettes, mais chaque module doit
     connaitre les besoins du reporting.

R-2) On accepte que reporting soit une couche APPLICATIVE au-dessus
     des modules, autorisee a lire en transverse, et on le declare
     explicitement a Spring Modulith (via la configuration de
     dependances autorisees) plutot que de faire semblant que la
     violation n'existe pas.

Mon avis : R-1 est plus propre architecturalement, R-2 est plus
honnete si le reporting evolue souvent. Mais c'est une decision
d'architecture, pas technique -- MONTRE-MOI les deux et attends.

Dans les deux cas : HistoriqueExportService n'injecte que
ReportingService, il n'a rien a changer.

Verifie ensuite que les 5 tests de ReportingServiceTest et le test
unique de HistoriqueExportServiceTest passent toujours.
```

## 7. Étape 6. Mesurer le progrès

```
Relance ModularityTests et compare aux mesures de MM.1 et MM.2 :

| Famille de violation | MM.1 | MM.2 | MM.3 | Reste a faire |

A ce stade, les seules violations restantes devraient etre celles de
famille (c) -- les 8 appels directs a AuditService, traites en MM.4.

Si une violation de famille (b) subsiste, explique-moi laquelle et
pourquoi. Si une violation de famille (d) est apparue, signale-la
immediatement.
```

## 8. Critères de validation

| Élément | Statut attendu |
|---|---|
| **C2** — une seule implémentation de la résolution de grille, dans le module `referentiel` | Vérifié |
| C2 — `montantCourant` retourne toujours `null` si aucune grille ACTIVE | Vérifié |
| C2 — absence de grille à l'enrôlement retourne toujours **400**, jamais 500 | Vérifié |
| C2 — `BeneficiaireService` et `EnrolementService` n'injectent plus `GrilleTarifaireRepository` ni `FonctionEligibleRepository` | Vérifié |
| **C3** — `FonctionEligibleService` n'injecte plus `BeneficiaireRepository` | Vérifié |
| C3 — le renommage de code cascade toujours vers les bénéficiaires (y compris inactifs) | Vérifié |
| C3 — le renommage reste refusé (409) au-delà d'1 bénéficiaire actif | Vérifié |
| C3 — les lignes `ligne_etat_mensuel` ne sont toujours pas modifiées par un renommage | Vérifié |
| **C4** — `DocumentService` n'injecte plus `BeneficiaireRepository` ni `FonctionEligibleRepository` | Vérifié |
| C4 — RG-06 préservée : un seul PDF, compteur 1→3 | Vérifié |
| C4 — écart éventuel entre `fonctionRetenue` et fonction actuelle signalé, non corrigé silencieusement | Fait |
| **C5** — approche R-1 ou R-2 tranchée explicitement avec l'utilisateur | Fait |
| C5 — `HistoriqueExportService` inchangé | Vérifié |
| Suite complète ≥ 205 tests, 0 échec, 0 erreur | Vérifié |
| Aucun contrat API modifié | Vérifié |
| Aucune migration Flyway ajoutée | Vérifié |
| Seules subsistent les violations de famille (c), liées à `AuditService` | Vérifié |

## Commit

```bash
git add .
git commit -m "mm.3: couplages inter-modules restants casses (grille, referentiel, document, reporting)"
```

---

**Fin du Sprint MM.3** — *en attente de validation avant MM.4*