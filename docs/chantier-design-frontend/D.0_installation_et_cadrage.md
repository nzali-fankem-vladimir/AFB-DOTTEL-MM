# SPRINT D.0

## Installation des skills de design et garde-fou de charte

*Module Dotations Téléphoniques Mensuelles — Frontend*

| | |
|---|---|
| **Objet** | Installer Impeccable et `frontend-design`, puis leur poser le cadre de la charte AFB **avant** de les laisser toucher au code |
| **Livrable** | Skills opérationnels, `PRODUCT.md`, `DESIGN.md`, configuration du détecteur, `.gitignore` |
| **Durée** | Une demi-journée |
| **Prérequis** | Chantier MM clos, `PLAN_DESIGN_FRONTEND.md` lu |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Installation et vérification (étapes 2-4) | Sonnet 5 | Low |
| Rédaction du garde-fou de charte (étapes 5-6) | Sonnet 5 ou Opus 4.8 | **High** |
| Premier passage du détecteur (étape 7) | Sonnet 5 | Medium |

L'effort High sur les étapes 5-6 se justifie : **c'est le seul moment du chantier où l'on décide ce que les outils ont le droit de toucher.** Un `DESIGN.md` bâclé et Impeccable proposera de changer le rouge institutionnel à chaque commande.

## 0. Nouvelle session : rappel Graphify

```
py -3.14 -m graphify update .
```

---

## 1. Contexte

### 1.1 État vérifié le 2026-08-09 : rien n'est installé

Contrôle effectué sur l'ensemble des emplacements possibles :

| Vérification | Résultat |
|---|---|
| `.impeccable/` dans le projet | **Absent** |
| `.claude/skills/` du projet | Contient uniquement `graphify` |
| `~/.claude/skills/` (global) | `composition-patterns`, `deploy-to-vercel`, `graphify`, `react-best-practices`, `react-native-skills`, `react-view-transitions`, `vercel-cli-with-tokens`, `vercel-optimize`, `web-design-guidelines`, `writing-guidelines` |
| Marketplace Claude Code | Aucun enregistré |
| `frontend-design` | **Introuvable** |

Les skills présents globalement viennent de l'écosystème Vercel. **Ni Impeccable ni `frontend-design` ne sont installés** — ce sprint part donc réellement de zéro.

**Note utile** : `web-design-guidelines` (déjà présent) est un skill de revue d'interface. Il n'est pas redondant avec Impeccable — il vérifie la conformité aux *Web Interface Guidelines*, là où Impeccable vise la qualité esthétique et les anti-patterns d'IA. Les deux se complètent, et `web-design-guidelines` sera utilisé en D.4.

### 1.2 Ce que chaque outil apporte, et ce qu'il faut lui interdire

| Outil | Apport | Danger pour ce projet |
|---|---|---|
| **Impeccable** | 23 commandes, 59 règles de détection déterministes, itération visuelle en navigateur | Ses anti-patterns par défaut visent les polices système, les couleurs ternes, les cartes imbriquées. Certains **contredisent directement la charte AFB** (sobriété imposée, rouge unique). |
| **`frontend-design`** (anthropics/skills) | Guidance de design frontend généraliste | Même risque : il propose des directions esthétiques sans connaître les contraintes d'identité d'une banque. |
| **`web-design-guidelines`** (déjà installé) | Revue d'accessibilité et de bonnes pratiques d'interface | Aucun — il vérifie des règles objectives, il ne propose pas d'esthétique. |

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
1. Lis CLAUDE.md dans son integralite, en particulier la SECTION 15
   (charte visuelle frontend) -- c'est la contrainte centrale de tout
   ce chantier.
2. Lis docs/chantier-design-frontend/PLAN_DESIGN_FRONTEND.md, surtout
   sa section 0 (ce qui est FIGE et ce qui est NEGOCIABLE).
3. Lis docs/chantier-design-frontend/D.0_installation_et_cadrage.md
   EN ENTIER.
4. Lance /graphify . --update

CONTEXTE : Sprint D.0. Installer Impeccable et frontend-design, puis
leur poser le cadre de la charte AFB avant tout usage.

REGLE ABSOLUE DE CE CHANTIER : la charte graphique de CLAUDE.md
section 15 n'est PAS negociable. Rouge E30613, sidebar verticale
sombre, fond blanc/F5F5F5, logo AFB en haut de la navigation. Si un
outil propose de changer l'un de ces elements, tu REFUSES et tu me le
SIGNALES -- tu ne l'appliques jamais en silence.

CE SPRINT NE TOUCHE AUCUN FICHIER APPLICATIF. Uniquement de
l'installation et de la configuration. Aucun fichier sous
frontend/src/ ni backend/ ne doit etre modifie.

PREMIERE ACTION : la verification d'espace de travail, puis l'etape 2.
```

---

## 3. Étape 2. Installer Impeccable

```
Installe Impeccable dans le projet.

Depuis la RACINE du projet (afb-dottel-mm, pas frontend/) :

    npx impeccable install

L'installeur va :
  - detecter les harnais presents (il devrait voir ~/.claude)
  - demander si l'installation est projet-locale ou globale
  - proposer d'installer le hook de detection

MES CHOIX A APPLIQUER :
  - Portee : PROJET (--scope=project). Ce chantier est specifique a
    DOTTEL ; une installation globale imposerait Impeccable a tous
    tes autres projets.
  - Fournisseur : claude uniquement (--providers=claude).
  - Hook de detection : OUI. Il fait tourner le detecteur sur chaque
    edition de fichier d'interface et remonte les problemes dans le
    flux -- exactement ce qu'on veut pour ne pas deriver.

Commande non interactive equivalente si tu preferes :

    npx impeccable install --providers=claude --scope=project

APRES INSTALLATION, montre-moi :
  - le contenu de .claude/skills/ (impeccable doit y apparaitre a cote
    de graphify)
  - le contenu de .impeccable/ s'il a ete cree
  - .claude/settings.local.json (le hook y est ecrit ; ce fichier est
    machine-local et gitignore)

ATTENTION : ne laisse PAS l'installeur ecraser le hook graphify deja
present dans .claude/settings.json (hook-guard search / read). Si
l'installeur touche a settings.json plutot qu'a settings.local.json,
ARRETE-TOI et montre-moi le diff avant.
```

**Point de vigilance.** Le projet a déjà des hooks graphify déclarés dans `.claude/settings.json` (`hook-guard search`, `hook-guard read`). L'installeur Impeccable est censé préserver les entrées existantes, mais c'est à vérifier plutôt qu'à supposer.

---

## 4. Étape 3. Installer `frontend-design`

```
Le skill frontend-design vient du depot anthropics/skills. Il n'y a
pas d'installeur npx dedie.

1. Verifie d'abord le contenu reel du depot :

    git clone --depth 1 https://github.com/anthropics/skills.git /tmp/anthropic-skills
    ls /tmp/anthropic-skills

2. Localise le skill frontend-design et montre-moi son SKILL.md AVANT
   de l'installer -- je veux savoir ce qu'il contient et s'il entre en
   conflit avec la charte.

3. Installe-le en PROJET (coherent avec Impeccable) :

    cp -r <chemin>/frontend-design .claude/skills/

4. Nettoie : rm -rf /tmp/anthropic-skills

5. Redemarre la session Claude Code pour que les nouveaux skills
   soient charges, puis confirme-moi qu'ils apparaissent.

SI le skill n'existe pas sous ce nom dans le depot, NE DEVINE PAS :
montre-moi la liste reelle des skills disponibles et demande-moi
lequel correspond a ce que je cherche.
```

---

## 5. Étape 4. Vérifier que tout répond

```
Verifications, une par une :

1. Skills charges :
   Tape /impeccable seul -- la liste des 23 commandes doit s'afficher.
   Verifie aussi que frontend-design est reconnu.

2. Detecteur en ligne de commande (sans IA, sans cle API) :

    npx impeccable detect frontend/src --json > .impeccable/baseline.json
    npx impeccable detect frontend/src

   Montre-moi le nombre total de findings et leur repartition par
   regle. C'EST NOTRE POINT DE REFERENCE : il doit diminuer a chaque
   sprint suivant, jamais augmenter.

   Garde baseline.json de cote -- on comparera en D.4.

3. Confirme que le hook fonctionne : il devrait se declencher a la
   prochaine edition d'un fichier d'interface.

NE CORRIGE AUCUN FINDING dans ce sprint. D.0 installe et mesure,
il ne repare pas.
```

---

## 6. Étape 5. `PRODUCT.md` — le contexte produit

**Ne pas lancer `/impeccable init` sans avoir lu cette section.** La commande pose des questions dont les réponses conditionnent tout le reste du chantier.

```
Lance /impeccable init.

Il va demander si la surface est "brand" (marketing, landing,
portfolio) ou "product" (app UI, dashboard, outil).

REPONSE : product. DOTTEL est un outil metier interne, jamais une
vitrine. Cette reponse oriente tout le reste vers la densite
d'information et la lisibilite, pas vers l'impact visuel.

Il va ensuite ecrire PRODUCT.md. Voici ce qu'il doit contenir --
montre-le-moi avant de valider :

  AUDIENCE : agents internes d'Afriland First Bank, 5 roles
  (EMPLOYE, ARH, CRH, DRH, ADMIN). Usage quotidien a mensuel, sur
  poste de travail bancaire. Ce ne sont pas des utilisateurs grand
  public : ils reviennent, ils connaissent le domaine, ils veulent
  aller vite.

  NATURE : outil metier interne de validation multi-niveaux. Le
  parcours central est un workflow a 3 signatures (ARH puis CRH puis
  DRH) sur un etat mensuel de dotations telephoniques.

  ENJEU : les ecrans engagent des paiements reels. La clarte prime
  sur l'esthetique ; une ambiguite sur un montant ou un statut a un
  cout financier.

  VOIX : sobre, factuelle, en francais. Vouvoiement. Aucun ton
  promotionnel, aucune familiarite.

  ANTI-REFERENCES : tableaux de bord SaaS grand public, degrades,
  illustrations decoratives, ton enjoue. DOTTEL est un outil bancaire
  interne.

  CONTRAINTE NON NEGOCIABLE : charte visuelle imposee, voir DESIGN.md.

Si /impeccable init propose un buildPath (comp-first ou code-first),
reponds CODE-FIRST : on part d'un frontend existant a polir, pas
d'une page a creer de zero.
```

---

## 7. Étape 6. `DESIGN.md` — le garde-fou de charte

**C'est le livrable le plus important de ce sprint.**

```
Ecris DESIGN.md de facon a ce qu'aucune commande Impeccable ne
propose jamais de toucher a la charte. Structure imposee :

  ## FIGE -- ne jamais proposer de modifier

  Couleur de marque : #E30613 (rouge institutionnel Afriland First
  Bank). Utilisee pour l'element actif de navigation et les boutons
  primaires. Cette valeur est imposee par l'identite visuelle de la
  banque. Aucune alternative, aucune nuance de remplacement, aucun
  degrade.

  Navigation : laterale VERTICALE, fond sombre (neutral-950), texte
  et icones blancs. Jamais un header horizontal.

  Fonds : contenu principal en blanc ou gris tres clair (#F5F5F5 =
  neutral-50). Jamais de fond colore, jamais de fond sombre hors
  sidebar.

  Boutons : primaires en #E30613, secondaires en gris ou en contour.

  Tableaux : en-tete gris clair, texte sombre, bordures discretes.

  Formulaires : champs blancs, bordure grise, sobres.

  Logo AFB en haut de la barre de navigation.

  Filigrane : motif discret sur la zone de contenu uniquement
  (.fond-filigrane). Jamais sur la sidebar, jamais sur les cartes.

  ## LIBRE -- terrain de jeu autorise

  Choix de la police (contrainte : sans-serif sobre, lisible en
  francais avec accents, licence compatible usage bancaire interne).
  Rythme vertical et espacements. Hierarchie typographique (tailles,
  graisses). Etats vides, de chargement, d'erreur, de succes.
  Micro-interactions et transitions. Focus visibles et contrastes
  d'accessibilite. Comportement responsive. Coherence entre ecrans.

  ## TOKENS EXISTANTS -- reutiliser, ne pas recreer

  Definis dans frontend/src/index.css via @theme de Tailwind 4 :
  --color-primary-50 a --color-primary-950 (echelle du rouge AFB)
  --color-neutral-50 a --color-neutral-950 (echelle des gris)
  Toute nouvelle couleur doit etre justifiee et ajoutee comme token,
  jamais en hexadecimal en dur dans un composant.

  ## PILE TECHNIQUE -- ne pas migrer

  React 19, Vite, Tailwind CSS 4 (configuration CSS via @theme, PAS
  de tailwind.config.js), Radix UI (Checkbox, Label), lucide-react
  (icones), class-variance-authority + tailwind-merge (variantes),
  react-hook-form + zod (formulaires).

Montre-moi le fichier avant de le valider.
```

---

## 8. Étape 7. Configurer le détecteur

```
Certaines des 59 regles du detecteur vont signaler des choix qui sont
en realite des decisions de charte. Il faut les declarer une fois pour
toutes, sinon chaque passage produira du bruit et on finira par
ignorer le detecteur en entier.

1. Lance d'abord le detecteur et montre-moi la liste COMPLETE des
   regles declenchees, avec leur compte.

2. Pour CHAQUE regle declenchee, classe-la avec moi :
     (a) VRAI defaut -> a corriger dans un sprint D.1 a D.4
     (b) DECISION DE CHARTE -> a ignorer definitivement, avec motif
     (c) DOUTE -> on en discute

   NE CLASSE RIEN EN (b) SANS MON ACCORD. Une regle ignoree a tort,
   c'est un vrai defaut qu'on ne verra plus jamais.

3. Pour les regles classees (b), utilise la configuration prevue :

    npx impeccable ignores add-value <regle> <valeur> --reason "<motif>"

   Le motif doit citer la source : "charte AFB, CLAUDE.md section 15".

CANDIDAT PROBABLE a discuter -- ne le traite pas seul :
la regle sur les polices surutilisees ("overused-font") va signaler
la pile systeme actuelle. Ce n'est PAS une decision de charte a
ignorer : c'est le defaut F3 du plan, un vrai manque, et il sera
corrige en D.1. Ne l'ignore surtout pas.

4. Ajoute le bloc .gitignore fourni par Impeccable (entre les marqueurs
   impeccable-ignore-start / impeccable-ignore-end) au .gitignore du
   PROJET. Verifie qu'il n'entre pas en conflit avec les regles
   existantes.

   Restent VERSIONNES (artefacts partages, ne pas ignorer) :
   .impeccable/config.json, .impeccable/live/config.json,
   .impeccable/design.json, .impeccable/critique/*.md
```

---

## 9. Étape 8. Vérification finale

```
1. AUCUN fichier applicatif modifie :

    git status --short -- frontend/src backend

   Doit etre VIDE. Si quelque chose apparait, c'est une erreur de ce
   sprint : D.0 n'installe et ne configure, il ne code pas.

2. Le frontend construit toujours :

    cd frontend ; npx oxlint ; npm run build

3. Les skills repondent :
   /impeccable seul affiche les 23 commandes.

4. Le point de reference est enregistre :
   .impeccable/baseline.json existe et son contenu m'a ete montre.

5. Montre-moi le diff complet de .gitignore et de
   .claude/settings.json (ce dernier ne devrait PAS avoir change --
   le hook Impeccable va dans settings.local.json).
```

---

## 10. Critères de validation

| Élément | Statut attendu |
|---|---|
| Impeccable installé en **portée projet**, fournisseur `claude` | Vérifié |
| Hook de détection installé, **sans écraser les hooks graphify existants** | Vérifié sur diff |
| `frontend-design` installé, ou absence signalée si introuvable dans le dépôt | Fait |
| `/impeccable` répond et liste ses commandes | Vérifié |
| `PRODUCT.md` écrit, surface = **product**, buildPath = **code-first** | Vérifié |
| **`DESIGN.md` écrit avec les trois sections FIGÉ / LIBRE / TOKENS** | Vérifié |
| `DESIGN.md` cite explicitement `#E30613` comme non négociable | Vérifié |
| Détecteur lancé, **baseline enregistrée** dans `.impeccable/baseline.json` | Fait |
| Chaque règle déclenchée classée (a)/(b)/(c) **avec l'utilisateur** | Fait |
| Aucune règle ignorée sans accord explicite et sans motif écrit | Vérifié |
| Règle « police surutilisée » **non ignorée** — c'est le défaut F3, corrigé en D.1 | Vérifié |
| Bloc `.gitignore` Impeccable ajouté, artefacts partagés restés versionnés | Vérifié |
| **Aucun fichier sous `frontend/src/` ni `backend/` modifié** | Vérifié |
| `npx oxlint` et `npm run build` toujours OK | Vérifié |

---

## Commit

```bash
git add .
git commit -m "d.0: installation impeccable et frontend-design, garde-fou de charte AFB"
```

---

**Fin du Sprint D.0** — *en attente de validation avant D.1*
