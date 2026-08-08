# SPRINT MM.10

## Référentiel unité / agence, et formats numériques réels

*Module Dotations Téléphoniques Mensuelles — modules `beneficiaires` et `processus`*

| | |
|---|---|
| **Objet** | Unité de rattachement en liste déroulante EHR avec résolution automatique du code unité, nouvelle colonne code agence, et alignement des formats numériques sur la réalité de la banque |
| **Livrable** | Select unité↔code, colonne `code_agence`, formats `code_unite` (4 chiffres) et `code_agence` (5 chiffres) |
| **Durée** | Une journée et demie |
| **Prérequis** | MM.9 validé |
| **Origine** | `M.2_referentiel_unite_agence.md` |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Analyse préalable du stub EHR (étape 2) | Sonnet 5 | Medium |
| Liste EHR unité↔code (étapes 3-4) | Sonnet 5 | Medium |
| Colonne code agence (étape 5) | Sonnet 5 | **High** |
| Formats numériques (étape 6) | Sonnet 5 | Medium |

L'effort High sur l'étape 5 se justifie : ajouter `code_agence` à `Beneficiaire` touche une migration Flyway, l'enrôlement, l'import Excel, **et l'API inter-modules `BeneficiaireApi`** — c'est le seul volet de ce sprint qui franchit une frontière de module.

## 0. Nouvelle session : rappel Graphify

```
/graphify . --update
```

---

## 1. Contexte

### 1.1 La distinction à ne jamais confondre

Confirmée à la présentation métier, et **reconfirmée explicitement le 2026-08-08** :

| | **Code unité** | **Code agence** |
|---|---|---|
| **Signification** | Unité d'affectation **professionnelle** du bénéficiaire | Agence ou direction où son **compte courant** est domicilié |
| **Format réel** | **4 chiffres** — `XXXX` | **5 chiffres** — `XXXXX` |
| **Exemple confirmé** | DSI = `4060` | `00001` = Agence Retraite / Siège Social (Yaoundé) |
| **État dans le code** | Colonne `code_unite` existe, mais **format non conforme** | **Colonne inexistante** |

**Ce sont deux référentiels différents, avec deux formats et deux significations distincts.** Toute confusion entre les deux est un bug métier, pas un détail cosmétique.

### 1.2 État réel du code, vérifié

`beneficiaires/service/EhrIntegrationServiceStub.java` contient 10 employés de test. Constat :

| Champ | Valeurs actuelles | Conformité |
|---|---|---|
| `matricule` | `1847`, `2093`, `3164`, `4275`, `5386`, `6497`, `7508`, `8619`, `9720`, `1053` | ✅ **Déjà conforme** — 4 chiffres. Rien à changer. |
| `codeUnite` | `DLA-AKW`, `YDE-DC01`, `BFS-CTR`, `GRA-DR01`, `BTA-ANT`, `NGD-AG01`, `DLA-CTL`, `YDE-IG01`, `BUE-AG01` | ❌ **Non conforme** — préfixe ville + suffixe alphanumérique, au lieu de 4 chiffres |
| `codeAgence` | *(champ absent)* | ❌ À créer |
| `numCompteCourant` | `10011847002` … (11 chiffres) | Format non spécifié par le métier — **ne pas y toucher** |
| `chapitre` | `37210100` … (8 chiffres) | Non concerné par ce sprint |

**Aucune validation de format n'existe** sur `code_unite` ni sur `matricule` : ni `@Pattern` sur l'entité `beneficiaires/model/entity/Beneficiaire.java`, ni contrainte en base. C'est un choix à trancher à l'étape 6.

### 1.3 Les 25 codes agence confirmés par le métier

Le métier signale qu'il en existe **d'autres au-delà de ces 25** — cette liste n'est donc pas exhaustive, mais elle est confirmée.

| Code | Agence | Code | Agence |
|---|---|---|---|
| `00001` | Agence Retraite / Siège Social (Yaoundé) | `00014` | Agence de Foumban |
| `00002` | Agence Principale de Douala (Bonanjo) | `00015` | Agence de Nkongsamba |
| `00003` | Agence de Bafoussam | `00016` | Agence de Kumba |
| `00004` | Agence de Garoua | `00017` | Agence de Kousseri |
| `00005` | Agence de Bamenda | `00018` | Agence de Guider |
| `00006` | Agence de Maroua | `00019` | Agence de Yagoua |
| `00007` | Agence de Ngaoundéré | `00020` | Agence d'Akonolinga |
| `00008` | Agence de Bertoua | `00021` | Agence de Mbalmayo |
| `00009` | Agence de Buea | `00022` | Agence de Sangmélima |
| `00010` | Agence d'Ebolowa | `00023` | Agence de Bafang |
| `00011` | Agence de Kribi | `00024` | Agence de Bangangté |
| `00012` | Agence de Limbe | `00025` | Agence de Mbouda |
| `00013` | Agence de Dschang | | |

### 1.4 ⚠️ Ce qui manque encore

**Les codes unité (4 chiffres) ne sont pas fournis**, à l'exception de `DSI = 4060`. Le sprint utilisera donc des valeurs camerounaises réalistes **inventées et marquées provisoires**, à remplacer le jour de l'intégration EHR réelle. C'est explicitement le même statut provisoire que le reste du stub.

**Écart mineur signalé, à confirmer avec le métier** : `M.2` donnait « Hippodrome = `00001` » comme exemple de code agence, alors que la liste confirmée attribue `00001` à « Agence Retraite / Siège Social (Yaoundé) ». La liste étant plus récente et explicite, c'est elle qui fait foi — mais l'écart mérite d'être signalé.

### 1.5 Rappel : l'EHR réel n'est pas branché

Point DSI en attente. Tout ce sprint s'appuie sur `EhrIntegrationServiceStub`. Les formats appliqués rapprochent les données de test de la réalité, mais restent des **données de stub**.

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
1. Lis CLAUDE.md dans son integralite, en particulier la section 4
   (modele de donnees, entite beneficiaires), la section 9 (donnees de
   test camerounaises OBLIGATOIRES) et la section 12 (integration EHR).
2. Lis docs/chantier-ajout-metier-mm/PLAN_AJOUTS_METIER_MM.md
3. Lis docs/chantier-ajout-metier-mm/MM.10_referentiel_unite_agence.md
   EN ENTIER -- surtout la section 1.1 (distinction code unite / code
   agence) et la section 1.3 (les 25 codes agence confirmes).
4. Lance /graphify . --update

CONTEXTE : Sprint MM.10, referentiel unite/agence et formats reels.

RAPPEL METIER CRITIQUE, a ne jamais confondre :
  - code unite  = unite d'affectation professionnelle, 4 CHIFFRES
                  (ex. DSI = 4060)
  - code agence = agence de domiciliation du compte courant,
                  5 CHIFFRES (ex. 00001)
Deux referentiels distincts, deux formats distincts. Si tu te trompes
sur l'un des deux, c'est un bug metier.

VARIABLES D'ENVIRONNEMENT (TROIS, pas deux) :
  $env:DB_URL="jdbc:postgresql://localhost:5432/afb_dotations_telephoniques_mm"
  $env:DB_PASSWORD="admin"
  $env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"

METHODE DE TRAVAIL :
- Un fichier a la fois. Tu montres, j'approuve, tu continues.
- Reference de non-regression : le total de MM.9, 0 echec.
- ModularityTests doit rester vert. L'etape 5 (code agence) touche
  BeneficiaireApi, donc une frontiere de module -- redouble d'attention
  la.

PREMIERE ACTION : la verification d'espace de travail, puis l'etape 2
(analyse prealable). Ne code rien avant d'avoir repondu a l'etape 2.
```

---

## 3. Étape 2. Analyse préalable — ce que le stub EHR expose

```
Avant tout code, analyse :
  beneficiaires/service/EhrIntegrationService.java
  beneficiaires/service/EhrIntegrationServiceStub.java
  beneficiaires/model/dto/ehr/EmployeEhrDto.java

Reponds precisement a :
1. Le stub expose-t-il aujourd'hui une LISTE des unites de
   rattachement avec leur code unite associe ? (Attendu : non -- il
   n'expose qu'une recherche par matricule. Confirme-le.)
2. Sous quelle forme le contrat EhrIntegrationService est-il defini
   (methodes, DTO) ?
3. EhrIntegrationServiceStub est-il package-private depuis MM.5 ? Si
   oui, l'ajout d'une methode au contrat EhrIntegrationService
   suffira-t-il, ou faut-il exposer autre chose ?
4. Y a-t-il deja un controleur exposant des donnees EHR au frontend ?
   (Verifie beneficiaires/controller/ -- EnrolementController expose
   GET /enrolement/verifier, mais est-ce le bon endroit pour une liste
   d'unites ?)

Rapporte ce que tu trouves. Ne code rien a cette etape.
```

---

## 4. Étapes 3 et 4. Liste des unités et résolution du code

### 4.1 Table de correspondance dans le stub

```
Ajoute au stub EHR une table de correspondance unite <-> code unite
(4 chiffres), avec des unites camerounaises realistes (CLAUDE.md
section 9). Le seul code unite CONFIRME par le metier est DSI = 4060 :
utilise-le tel quel, et invente les autres en 4 chiffres.

MARQUE CLAIREMENT EN COMMENTAIRE que tous les codes autres que 4060
sont PROVISOIRES, a remplacer le jour de l'integration EHR reelle.

Coherence a preserver : les unites de la table doivent correspondre
aux uniteRattachement deja presentes dans les 10 employes du stub
(Agence Douala Akwa, Direction Centrale Yaounde, Agence Bafoussam
Centre, Direction Regionale Garoua, Antenne Bertoua, Agence
Ngaoundere, Direction Controle Douala, Inspection Generale Yaounde,
Agence Buea) -- sinon le select proposera des unites que le stub ne
connait pas.

Expose la liste via une nouvelle methode du contrat
EhrIntegrationService, puis un endpoint backend. Pour l'endpoint :
- place-le dans beneficiaires/controller/ (module proprietaire de
  l'EHR depuis la decision C.3 de MM.0)
- role @PreAuthorize aligne sur celui deja utilise pour la modification
  de beneficiaire (ARH)
- chemin coherent avec les endpoints existants -- propose-le-moi avant
  de coder, ne l'invente pas seul

Montre le service puis le controleur.
```

### 4.2 Select unité dans le modal de modification

```
Dans frontend/src/pages/dashboard/ModifierBeneficiaireModal.jsx,
remplace le champ texte libre "unite de rattachement" par une liste
deroulante alimentee par l'endpoint de l'etape 3.

Comportement attendu : quand l'ARH selectionne une unite, le code
unite correspondant est resolu AUTOMATIQUEMENT et affecte au
beneficiaire a la validation du PATCH -- l'ARH ne saisit JAMAIS le
code unite a la main.

QUESTION A ME POSER AVANT DE CODER, NE TRANCHE PAS SEUL : ou se fait
la resolution ?
  - Cote BACKEND a la validation : source unique de verite, le
    frontend n'envoie que le libelle de l'unite. Plus sur.
  - Cote FRONTEND a partir de la liste deja chargee : retour visuel
    immediat du code a l'ARH, mais deux sources possibles de verite.
Presente les deux, attends ma reponse.

Attention : ce modal a deja ete touche par MM.9 (rien sur ce champ,
mais verifie l'etat reel du fichier avant d'editer).
```

---

## 5. Étape 5. Colonne code agence — le volet inter-modules

```
Ajoute code_agence. C'est le volet le plus large de ce sprint : il
touche une migration, l'entite, l'enrolement, l'import Excel, ET une
API inter-modules.

QUESTIONS A ME POSER AVANT TOUT CODE, NE TRANCHE PAS SEUL :

1. ORIGINE de la donnee : le code agence vient-il de l'EHR (comme les
   autres donnees du beneficiaire) ou est-il saisi manuellement par
   l'ARH ?
   - Si EHR : il faut l'ajouter a EmployeEhrDto, au stub, ET au flux
     d'enrolement (EnrolementService).
   - Si saisi : il faut l'ajouter au modal de modification, et decider
     quoi faire des beneficiaires deja enroles (nullable ? valeur par
     defaut ?).

2. IMPORT EXCEL : le format d'import actuel a 7 colonnes
   (N°ORDRE, MATRICULE, NOMS & PRENOMS, FONCTION, UNITE, CODE_UNITE,
   N°COMPTE -- indices 0 a 6 dans BeneficiaireImportService).
   Faut-il une 8e colonne CODE_AGENCE ? Si oui, le contrat d'import
   change et les fichiers Excel existants deviennent invalides --
   decision metier, pas technique.

3. EXPOSITION EN LECTURE : faut-il afficher le code agence dans la
   liste et le detail des beneficiaires ? Dans quels DTO ?

4. NULLABLE ou NOT NULL : les beneficiaires deja enroles n'ont pas de
   code agence. Une colonne NOT NULL exigerait une valeur de repli
   pour l'existant (comme chapitre-defaut l'a fait au Sprint 3.4).

Une fois mes reponses obtenues, implemente dans cet ordre, un fichier
a la fois :

  a) Migration Flyway VN__ajout_code_agence.sql -- NE MODIFIE JAMAIS
     une migration deja appliquee, cree un nouveau script en
     respectant la convention de nommage (CLAUDE.md section 2).
  b) beneficiaires/model/entity/Beneficiaire.java
  c) beneficiaires/model/dto/ehr/EmployeEhrDto.java + le stub (si
     origine EHR)
  d) beneficiaires/service/EnrolementService.java (si origine EHR)
  e) beneficiaires/service/BeneficiaireImportService.java (si 8e
     colonne retenue)
  f) Les DTO de reponse concernes selon la reponse 3

POINT INTER-MODULES A NE PAS MANQUER :
beneficiaires/api/BeneficiaireDocumentDto.java porte deja
(id, nomPrenoms, codeUnite, numCompteCourant, ...) et sert a
DocumentService (module processus) pour generer le PDF.
=> Si le code agence doit apparaitre dans le PDF de l'etat mensuel,
   OU etre transmis a la comptabilite via Kafka (ce que MM.13 prevoit),
   il faut ETENDRE ce DTO d'API.
Signale-le-moi explicitement quand tu y arrives : c'est un changement
de contrat inter-modules, pas un simple ajout de colonne. Verifie
ensuite que ModularityTests reste vert.
```

---

## 6. Étape 6. Formats numériques

```
Aligne les formats sur la realite de la banque.

CE QUI EST DEJA CONFORME -- NE TOUCHE PAS :
  matricule : deja en 4 chiffres partout (stub EHR : 1847, 2093, 3164,
  4275, 5386, 6497, 7508, 8619, 9720, 1053 ; et
  V3__insertion_utilisateurs_test.sql). Format CONFIRME par le metier.
  Aucun changement.

CE QUI DOIT CHANGER :

1. code_unite -> 4 CHIFFRES.
   Aujourd'hui le stub contient "DLA-AKW", "YDE-DC01", "BFS-CTR",
   "GRA-DR01", "BTA-ANT", "NGD-AG01", "DLA-CTL", "YDE-IG01",
   "BUE-AG01" -- non conformes.
   Remplace par les codes 4 chiffres de la table de correspondance
   creee a l'etape 3 (coherence obligatoire entre les deux).

2. code_agence -> 5 CHIFFRES, parmi les 25 codes confirmes listes en
   section 1.3 de ce guide. Attribue a chaque employe du stub un code
   agence COHERENT avec sa ville :
     - Agence Douala Akwa        -> 00002 (Douala Bonanjo, la seule
                                    agence Douala de la liste)
     - Direction Centrale Yaounde -> 00001 (Siege Social Yaounde)
     - Agence Bafoussam Centre    -> 00003
     - Direction Regionale Garoua -> 00004
     - Antenne Bertoua            -> 00008
     - Agence Ngaoundere          -> 00007
     - Direction Controle Douala  -> 00002
     - Inspection Generale Yaounde -> 00001
     - Agence Buea                -> 00009
   Ces attributions sont une PROPOSITION coherente : montre-la-moi
   avant de l'ecrire, je confirme ou j'ajuste.

3. Donnees Flyway de test : si des donnees de reference contiennent des
   codes unite ou agence, applique les memes formats. NE MODIFIE JAMAIS
   un script Flyway deja applique -- cree un nouveau script si
   necessaire.

QUESTION A ME POSER, NE TRANCHE PAS SEUL :
faut-il ajouter une VALIDATION DE FORMAT bloquante (@Pattern sur
l'entite, ou contrainte CHECK en base) ?
  - POUR : les deux formats sont maintenant confirmes par le metier,
    une validation empecherait une saisie ou un import invalide.
  - CONTRE : l'EHR reel n'est pas branche. Si ses formats reels
    divergent (ne serait-ce qu'un code unite a 5 chiffres pour une
    nouvelle direction), une contrainte bloquante casserait
    l'integration le jour du branchement.
Par defaut, le guide M.2 d'origine disait : "ajuster les donnees, PAS
ajouter de contrainte de format bloquante". Confirme ou infirme.

MARQUE TOUT CE VOLET COMME PROVISOIRE en commentaire (donnees de stub,
a remplacer par les vraies donnees EHR le jour de l'integration).
```

---

## 7. Étape 7. Vérification

```
BACKEND :
  cd backend ; .\mvnw.cmd test
Attendu : total de MM.9 + les nouveaux tests, 0 echec.

MODULARITE :
  ModularityTests VERT. Si l'etape 5 a etendu BeneficiaireApi, verifie
  qu'aucune nouvelle violation n'apparait et que les
  allowedDependencies existants suffisent.

MIGRATION :
  Demarre le backend et confirme que Flyway applique bien la nouvelle
  migration sans erreur de checksum sur les precedentes.

FRONTEND :
  cd frontend ; npx oxlint ; npm run build

VERIFICATION MANUELLE (liste a donner a l'utilisateur) :
1. Ouvrir le modal de modification d'un beneficiaire : l'unite est bien
   une liste deroulante, plus un champ libre.
2. Selectionner une unite, valider, verifier EN BASE que code_unite a
   ete resolu automatiquement au bon code 4 chiffres.
3. Verifier en base que les 10 employes du stub portent bien des
   code_unite a 4 chiffres et des code_agence a 5 chiffres.
4. Enroler un nouveau beneficiaire : verifier que code_agence est
   renseigne (si origine EHR retenue).
5. Si 8e colonne d'import retenue : tester un import Excel avec la
   nouvelle colonne.
```

---

## 8. Critères de validation

| Élément | Statut attendu |
|---|---|
| **Distinction code unité / code agence respectée**, jamais confondus | Vérifié |
| Liste unité↔code exposée par le stub EHR, unités cohérentes avec les employés existants | Vérifié |
| Lieu de résolution du code unité (backend / frontend) tranché **avec l'utilisateur** | Fait |
| Unité en liste déroulante, code unité **jamais saisi à la main** par l'ARH | Vérifié |
| Origine du code agence (EHR / saisie) tranchée **avec l'utilisateur** | Fait |
| 8ᵉ colonne d'import Excel : décision tranchée **avec l'utilisateur** | Fait |
| Nullable / NOT NULL du code agence tranché **avec l'utilisateur** | Fait |
| Migration Flyway créée, **aucune migration existante modifiée** | Vérifié |
| Extension éventuelle de `BeneficiaireDocumentDto` signalée comme changement inter-modules | Fait |
| `code_unite` : **4 chiffres** partout dans le stub | Vérifié en base |
| `code_agence` : **5 chiffres**, parmi les 25 codes confirmés | Vérifié en base |
| Attribution code agence ↔ ville validée **avec l'utilisateur** | Fait |
| `matricule` : **inchangé** (déjà conforme) | Vérifié |
| Validation de format bloquante : décision tranchée **avec l'utilisateur** | Fait |
| Caractère **provisoire** des données de stub documenté en commentaire | Vérifié |
| Données camerounaises réalistes (CLAUDE.md §9) | Vérifié |
| `ModularityTests` toujours vert | Vérifié |
| Suite backend : ≥ total MM.9, 0 échec | Vérifié |

---

## Commit

```bash
git add .
git commit -m "mm.10: unite en liste EHR avec code unite 4 chiffres, colonne code agence 5 chiffres, 25 codes agence reels"
```

---

**Fin du Sprint MM.10** — *en attente de validation avant MM.11*
