# SPRINT D.1

## Fondations — coquille HTML, typographie, tokens

*Module Dotations Téléphoniques Mensuelles — Frontend*

| | |
|---|---|
| **Objet** | Corriger les trois défauts de fondation, choisir et intégrer une police, compléter les tokens de design |
| **Livrable** | `index.html` corrigé, police déclarée dans `@theme`, tokens de typographie et d'espacement |
| **Durée** | Une journée |
| **Prérequis** | D.0 validé — `DESIGN.md` écrit, baseline du détecteur enregistrée |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Correction de la coquille HTML (étape 2) | Sonnet 5 | **Low** |
| Choix de la police (étape 3) | Sonnet 5 ou Opus 4.8 | **High** |
| Intégration et tokens (étapes 4-5) | Sonnet 5 | Medium |

L'effort High sur l'étape 3 se justifie : **la police est le choix le plus visible et le plus durable du chantier.** Il touche les 29 écrans d'un coup, et revenir dessus plus tard veut dire revalider visuellement toute l'application.

## 0. Nouvelle session : rappel Graphify

```
/graphify . --update
```

---

## 1. Contexte

Trois défauts de fondation, tous vérifiés dans le code. Aucun n'est cosmétique : ils sont en amont de tout le reste, c'est pourquoi ce sprint passe avant les composants et les écrans.

### 1.1 F1 — `lang="en"` sur une application française

`frontend/index.html` déclare `<html lang="en">`. Toute l'interface est en français.

**Ce n'est pas un détail de propreté, c'est un bug d'accessibilité réel.** Un lecteur d'écran s'appuie sur cet attribut pour choisir sa prononciation : il lira « Bénéficiaires » avec les règles phonétiques de l'anglais. Cela affecte aussi la césure automatique et les correcteurs orthographiques du navigateur.

### 1.2 F2 — titre d'échafaudage

`<title>frontend</title>` — la valeur générée par Vite à la création du projet, jamais remplacée. C'est ce que voient l'onglet du navigateur, l'historique et les favoris.

### 1.3 F3 — aucune police déclarée

Vérification faite sur `frontend/src/index.css`, `frontend/index.html` et tout `frontend/src/` : **aucune `font-family`, aucun `@font-face`, aucun `--font-sans` dans le bloc `@theme`**.

Conséquence : Tailwind applique sa pile par défaut (`ui-sans-serif, system-ui, …`). L'application n'a donc pas le même visage sur Windows, macOS et Linux — et sur un poste bancaire Windows, elle hérite de Segoe UI par défaut.

La charte (`CLAUDE.md` §15) dit seulement « typographie sans-serif sobre ». Elle **ne nomme aucune police** : le choix est donc ouvert, et c'est l'amélioration la plus rentable de tout le chantier.

### 1.4 Ce qui est déjà en place et ne doit pas être défait

`frontend/src/index.css` définit déjà, via `@theme` de Tailwind 4 :
- `--color-primary-50` à `--color-primary-950` — l'échelle du rouge AFB, `primary-500` = `#e30613`
- `--color-neutral-50` à `--color-neutral-950` — l'échelle des gris, `neutral-50` = `#f5f5f5`
- la classe `.fond-filigrane`

**Ces valeurs sont figées par la charte.** Ce sprint les complète, il ne les remplace pas.

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
1. Lis CLAUDE.md, SECTION 15 (charte visuelle) en priorite.
2. Lis docs/chantier-design-frontend/PLAN_DESIGN_FRONTEND.md section 0
   (FIGE / NEGOCIABLE).
3. Lis DESIGN.md ecrit en D.0 -- c'est ton garde-fou.
4. Lis docs/chantier-design-frontend/D.1_fondations_et_tokens.md
5. Lance /graphify . --update

CONTEXTE : Sprint D.1, fondations. Trois defauts a corriger (lang,
title, police absente) puis completion des tokens.

RAPPEL DE CHARTE : le rouge #E30613, la sidebar sombre, les fonds
blanc/F5F5F5 et la place du logo sont FIGES. Ce sprint n'y touche
pas. Il complete la typographie et les espacements, qui eux sont
libres.

BACKEND INTERDIT : aucun fichier sous backend/ ne doit changer.

PREMIERE ACTION : la verification d'espace de travail, puis l'etape 2
(la plus simple, pour se caler).
```

---

## 3. Étape 2. Corriger la coquille HTML

```
Dans frontend/index.html, trois corrections :

1. lang="en" -> lang="fr"
   Toute l'interface est en francais. C'est un correctif
   d'accessibilite, pas de style.

2. <title>frontend</title> -> un titre reel.
   Propose-m'en un, ne l'invente pas seul. Contraintes : il apparait
   dans l'onglet, l'historique et les favoris ; il doit identifier
   l'application ET la banque. Pistes a me soumettre, avec ton avis :
     - "Dotations Telephoniques -- Afriland First Bank"
     - "DOTTEL -- Afriland First Bank"
     - autre chose que tu proposes
   Verifie ce qu'affiche deja l'ecran de connexion (Login.jsx
   affiche "Dotations Telephoniques Mensuelles" dans son CardTitle)
   pour rester coherent.

3. Verifie le favicon : href="/favicon.svg". Regarde s'il s'agit du
   logo AFB ou du logo Vite par defaut. Si c'est celui de Vite,
   SIGNALE-LE-MOI -- le remplacer touche a la charte (place et
   identite du logo), donc je dois trancher. Ne le remplace pas seul.

4. Ajoute une meta description en francais.

Montre-moi le fichier complet apres modification.
```

---

## 4. Étape 3. Choisir la police — arrêt obligatoire

**Ne code rien avant validation.** C'est le choix le plus visible du chantier.

```
Propose-moi 3 polices sans-serif, avec pour chacune : son caractere,
pourquoi elle convient a un outil bancaire interne dense, et sa
licence.

CONTRAINTES A RESPECTER, toutes verifiables :

  a) Sobre. La charte impose la sobriete : pas de police a fort
     caractere, pas de geometrique excentrique.

  b) Excellente lisibilite en francais ACCENTUE. L'application est
     pleine de "Bénéficiaires", "Éligibilité", "Ngaoundéré",
     "Sangmélima". Une police dont les diacritiques sont mal dessinees
     ou trop serrees est disqualifiee.

  c) Chiffres tabulaires disponibles. C'est un critere DUR, pas un
     confort : l'application affiche des colonnes de montants en FCFA
     (formatMontantFCFA) dans DataTable. Sans chiffres a chasse fixe,
     les colonnes de montants ne s'alignent pas verticalement et
     deviennent penibles a comparer -- exactement ce qu'un controleur
     fait toute la journee.

  d) Licence libre d'usage commercial interne, hebergeable en local.
     ATTENTION : ne propose PAS de chargement depuis Google Fonts en
     CDN. Un poste bancaire interne peut ne pas avoir d'acces sortant,
     et une police qui ne charge pas fait retomber sur la pile
     systeme sans prevenir. La police doit etre EMBARQUEE dans le
     bundle (fichiers woff2 dans src/assets/ ou public/).

  e) Poids raisonnable. Limite-toi aux graisses reellement utilisees :
     regarde dans le code quelles classes font-* sont employees
     (font-medium, font-semibold, font-bold) et n'embarque que
     celles-la, en woff2.

INTERDIT EXPLICITEMENT : Inter, Arial, Helvetica, et la pile systeme.
Impeccable les classe en anti-pattern, et c'est justement le defaut
F3 qu'on corrige.

Montre-moi les 3 options. Je choisis. NE CODE RIEN AVANT.
```

**Repère pour la discussion.** Trois familles conviennent typiquement à ce profil (outil dense, français accentué, chiffres tabulaires) : les grotesques neutres type Source Sans / IBM Plex Sans, les humanistes type Public Sans / Libre Franklin, et les polices conçues pour l'interface type Figtree / Manrope. L'assistant doit argumenter, pas piocher.

---

## 5. Étape 4. Intégrer la police

```
Une fois la police choisie :

1. Telecharge les fichiers woff2 des graisses retenues UNIQUEMENT.
   Place-les dans frontend/src/assets/fonts/ (coherent avec
   src/assets/ ou vivent deja le logo et le filigrane).

2. Declare-les en @font-face dans frontend/src/index.css, avec
   font-display: swap.

3. Ajoute la police au bloc @theme EXISTANT, sans toucher aux tokens
   de couleur :

       --font-sans: "<Police>", ui-sans-serif, system-ui, sans-serif;

   Garde la pile de repli : si un fichier de police manque, l'interface
   reste lisible.

4. VERIFIE que Tailwind 4 prend bien --font-sans depuis @theme et que
   font-sans s'applique par defaut au body. Si ce n'est pas
   automatique dans cette version, ajoute la regle explicitement --
   mais VERIFIE-LE plutot que de le supposer.

5. Active les chiffres tabulaires LA OU C'EST UTILE, pas partout :
   uniquement sur les colonnes de montants et de dates dans DataTable.
   Utilise font-variant-numeric: tabular-nums (classe Tailwind
   tabular-nums). Sur du texte courant, les chiffres tabulaires sont
   moins agreables -- ne les mets pas globalement.

Montre-moi index.css complet apres modification, et confirme que les
tokens de couleur sont INCHANGES.
```

---

## 6. Étape 5. Compléter les tokens

```
L'echelle de couleur est complete. Ce qui manque, ce sont les tokens
NON couleur -- ceux qui font la difference entre une interface
coherente et une interface qui derive ecran par ecran.

1. Analyse d'abord l'existant : releve dans frontend/src/ les valeurs
   reellement utilisees pour les rayons de bordure (rounded-*), les
   ombres (shadow-*), et les espacements recurrents. Montre-moi la
   distribution AVANT de proposer quoi que ce soit -- si le projet
   utilise deja 3 rayons de facon coherente, il n'y a rien a
   inventer.

2. Sur cette base, propose les tokens manquants dans @theme :
     - rayons de bordure (si l'usage est disperse)
     - ombres, en restant SOBRE : la charte parle de "bordures
       discretes". Une ombre marquee contredirait la charte.
     - echelle typographique si les tailles derivent

3. REGLE : n'ajoute un token que si l'analyse du point 1 montre une
   VRAIE dispersion. Creer des tokens que personne n'utilise, c'est
   de la ceremonie, pas du design system.

4. INTERDIT : toucher aux tokens --color-primary-* et --color-neutral-*.
   Ils sont figes par la charte.

Montre-moi ta proposition avec, pour chaque token, le nombre
d'occurrences qui le justifie.
```

---

## 7. Étape 6. Vérification

```
1. Detecteur -- comparaison avec la baseline de D.0 :

    npx impeccable detect frontend/src

   La regle "police surutilisee" doit avoir DISPARU. Le total doit
   avoir diminue. Montre-moi l'ecart avec .impeccable/baseline.json.

2. Build :

    cd frontend ; npx oxlint ; npm run build

   Verifie AUSSI la taille du bundle : les fichiers de police
   s'ajoutent au poids livre. Si l'augmentation depasse ~150 Ko,
   signale-le -- on a peut-etre embarque trop de graisses.

3. Backend intact :

    git status --short -- backend

   Doit etre VIDE.

4. Montre-moi le diff complet de index.html et index.css.
```

---

## 8. Vérification visuelle — à faire par l'utilisateur

Lance le backend (3 variables d'environnement) et le frontend, puis :

1. **Onglet du navigateur** — le titre est le nouveau, plus « frontend ».
2. **Connexion** (`1847` / `Test1234`) — la nouvelle police s'applique, l'interface ne « saute » pas au chargement (`font-display: swap` bien posé).
3. **Liste des bénéficiaires** — regarde spécifiquement la colonne des montants : les chiffres doivent s'aligner **verticalement** d'une ligne à l'autre.
4. **Un écran avec beaucoup d'accents** — page Bénéficiaires avec les unités camerounaises (« Ngaoundéré », « Sangmélima »). Les accents doivent être nets, ni collés ni coupés.
5. **Sidebar** — vérifie que le rouge de l'élément actif est **inchangé**, et que le logo AFB est toujours en haut.
6. **Mode hors ligne** — coupe le réseau et recharge : la police doit toujours s'afficher (preuve qu'elle est bien embarquée et non chargée depuis un CDN).

---

## 9. Critères de validation

| Élément | Statut attendu |
|---|---|
| `lang="fr"` sur `<html>` | Vérifié |
| `<title>` réel, validé **avec l'utilisateur**, cohérent avec l'écran de connexion | Fait |
| Favicon Vite éventuel **signalé**, non remplacé sans accord (charte) | Fait |
| Meta description en français ajoutée | Vérifié |
| Police choisie **avec l'utilisateur** parmi 3 options argumentées | Fait |
| Police **embarquée** en woff2, **jamais** chargée depuis un CDN | Vérifié hors ligne |
| Graisses embarquées limitées à celles réellement utilisées dans le code | Vérifié |
| `--font-sans` ajouté au bloc `@theme` **existant**, pile de repli conservée | Vérifié |
| Chiffres tabulaires actifs **sur les colonnes de montants**, pas globalement | Vérifié visuellement |
| Tokens `--color-primary-*` et `--color-neutral-*` **strictement inchangés** | Vérifié sur diff |
| Nouveaux tokens justifiés par une **analyse d'occurrences**, pas inventés | Fait |
| Règle « police surutilisée » disparue du détecteur | Vérifié |
| Total des *findings* en **baisse** par rapport à `baseline.json` | Vérifié |
| Augmentation du bundle mesurée et signalée | Fait |
| `npx oxlint` et `npm run build` OK | Vérifié |
| **Aucun fichier sous `backend/` modifié** | Vérifié |
| Rouge AFB, sidebar et logo visuellement inchangés | Vérifié à l'écran |

---

## Commit

```bash
git add .
git commit -m "d.1: fondations frontend -- lang fr, titre reel, police embarquee, tokens completes"
```

---

**Fin du Sprint D.1** — *en attente de validation avant D.2*
