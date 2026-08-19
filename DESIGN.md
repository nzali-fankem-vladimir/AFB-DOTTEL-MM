---
# Frontmatter lu par le detecteur Impeccable (npx impeccable detect).
# Les couleurs declarees ici sont la SEULE palette autorisee : toute autre
# couleur trouvee dans le code est signalee comme hors charte.
#
# Deliberement ABSENTS de ce frontmatter :
#   - rounded : la charte AFB n'impose aucune echelle de rayons. A declarer
#     en D.2, une fois l'echelle des composants arretee.
#
# typography : la FAMILLE est arretee en D.1 (Source Sans 3, cf. ci-dessous).
#     Les TAILLES ne sont volontairement pas declarees : la hierarchie
#     typographique appartient a D.2/D.3, la figer ici serait premature.
typography:
  body:
    fontFamily: "Source Sans 3, ui-sans-serif, system-ui, sans-serif"
colors:
  primary-50: "#fef1f1"
  primary-100: "#fddada"
  primary-200: "#fbb0b0"
  primary-300: "#f67d7d"
  primary-400: "#ed4141"
  primary-500: "#e30613"
  primary-600: "#c10510"
  primary-700: "#9e040d"
  primary-800: "#7c030a"
  primary-900: "#5a0207"
  primary-950: "#3d0105"
  neutral-50: "#f5f5f5"
  neutral-100: "#ebebeb"
  neutral-200: "#d8d8d8"
  neutral-300: "#c8c8c8"
  neutral-400: "#ababab"
  neutral-500: "#8a8a8a"
  neutral-600: "#6d6d6d"
  neutral-700: "#4f4f4f"
  neutral-800: "#3a3a3a"
  neutral-900: "#1c1c1c"
  neutral-950: "#111111"
  white: "#ffffff"
  black: "#000000"
---

# DESIGN — Module DOTTEL, Afriland First Bank

Ce fichier est le garde-fou de charte du chantier de design frontend.
Il a deux lecteurs : les commandes Impeccable et `frontend-design`, qui
doivent s'y conformer, et le détecteur, qui applique le frontmatter
ci-dessus.

**Source d'autorité : `CLAUDE.md` section 15 (charte visuelle frontend).**
En cas de contradiction entre une suggestion d'outil et cette section,
c'est la section 15 qui gagne, sans exception et sans arbitrage.

---

## FIGÉ — ne jamais proposer de modifier

Ces éléments relèvent de l'identité visuelle d'Afriland First Bank. Ils
ne sont pas des choix de design ouverts à la discussion : ce sont des
contraintes reçues. Aucune commande ne doit proposer de les changer, ni
les modifier en silence. Toute proposition qui y touche doit être
refusée et signalée à l'utilisateur.

**Couleur de marque : `#E30613`** — rouge institutionnel Afriland First
Bank (`--color-primary-500`). Utilisée pour l'élément actif de la
navigation et les boutons primaires. Cette valeur est imposée par
l'identité visuelle de la banque. Aucune alternative, aucune nuance de
remplacement, aucun dégradé, aucune « version plus moderne », aucune
désaturation au motif qu'elle serait trop vive.

**Navigation** : latérale **verticale**, fond sombre (`neutral-950`),
texte et icônes blancs. Jamais un header horizontal, jamais une barre
supérieure de remplacement, jamais une navigation escamotable qui
supprimerait la colonne sur poste de travail.

**Fonds** : contenu principal en blanc ou gris très clair
(`#F5F5F5` = `neutral-50`). Jamais de fond coloré, jamais de fond sombre
en dehors de la sidebar. Pas de mode sombre : il n'est pas demandé et
sortirait de la charte.

**Boutons** : primaires en `#E30613`, secondaires en gris ou en contour.

**Tableaux** : en-tête gris clair, texte sombre, bordures discrètes.

**Formulaires** : champs blancs, bordure grise, sobres.

**Logo AFB** en haut de la barre de navigation. Sa position et sa
présence ne se discutent pas.

**Filigrane** : motif discret sur la zone de contenu uniquement
(`.fond-filigrane`, défini dans `frontend/src/index.css`, appliqué dans
`AppLayout.jsx`). Jamais sur la sidebar, jamais sur les cartes ou les
en-têtes, qui restent opaques par-dessus.

**Registre général** : sobriété imposée. DOTTEL est un outil bancaire
interne. Pas de dégradés décoratifs, pas d'illustrations d'ambiance, pas
de ton enjoué, pas d'effets spectaculaires. Une suggestion du type
« rendre l'interface plus audacieuse / plus mémorable / plus
différenciante » est hors périmètre par construction.

---

## LIBRE — terrain de jeu autorisé

La charte contraint l'**identité**, pas le **soin**. Tout ce qui suit
est ouvert et constitue le vrai périmètre du chantier D.1 à D.4 :

- **Choix de la police — TRANCHÉ en D.1 : Source Sans 3** (Adobe,
  SIL OFL 1.1), embarquée en woff2 dans `frontend/src/assets/fonts/`,
  déclarée via `--font-sans` dans `@theme`. Retenue sur trois mesures
  faites sur les fichiers réels : chiffres à chasse fixe nativement
  (alignement des colonnes de montants FCFA garanti sans classe CSS),
  18,3 % d'em de dégagement au-dessus des accents capitales (les
  en-têtes de `DataTable` sont en capitales accentuées), et couverture
  complète des diacritiques français. Quatre graisses seulement
  (400/500/600/700), celles réellement employées. Ce choix est
  désormais **acquis** : il ne se rediscute pas en D.2-D.4. Ce qui
  reste ouvert, ce sont les tailles et les graisses par rôle.
- **Rythme vertical, espacements, densité.**
- **Hiérarchie typographique** : tailles, graisses, contrastes de
  niveau à l'intérieur d'une page.
- **États** : vide, chargement, erreur, succès.
- **Micro-interactions et transitions.**
- **Focus visibles et contrastes d'accessibilité.**
- **Comportement responsive.**
- **Cohérence entre les 29 écrans.**

**Règle de tranchage** : une proposition qui change une couleur de
marque, la forme de la navigation ou la place du logo relève du FIGÉ →
refus. Une proposition qui change un espacement, un état, une
transition ou un contraste de lisibilité relève du LIBRE → recevable.

---

## TOKENS EXISTANTS — réutiliser, ne pas recréer

Définis dans `frontend/src/index.css` via `@theme` de Tailwind 4 :

- `--color-primary-50` à `--color-primary-950` — échelle du rouge AFB,
  construite autour de `#E30613` en `primary-500`.
- `--color-neutral-50` à `--color-neutral-950` — échelle des gris, de
  `#F5F5F5` (fond de contenu) à `#111111` (fond de navigation).

Ces deux échelles sont reprises dans le frontmatter ci-dessus et
constituent la palette autorisée. Toute nouvelle couleur doit être
justifiée et ajoutée comme token dans `@theme` **et** dans ce
frontmatter — jamais en hexadécimal en dur dans un composant.

Ne pas recréer une échelle parallèle, ne pas introduire de couleurs
sémantiques (succès / alerte / info) sans les faire passer par cette
procédure.

---

## PILE TECHNIQUE — ne pas migrer

React 19, Vite, Tailwind CSS 4 (configuration CSS via `@theme`, **pas**
de `tailwind.config.js`), Radix UI (`Checkbox`, `Label`), lucide-react
(icônes), class-variance-authority + tailwind-merge (variantes),
react-hook-form + zod (formulaires).

Aucune migration de bibliothèque n'est au programme de ce chantier.

---

## HORS PÉRIMÈTRE — ne jamais toucher

- Les routes et les rôles (`ProtectedRoute`, `NAV_LINKS`) : verrouillés
  par l'audit 6F.9. Les rôles de `NAV_LINKS` doivent rester strictement
  identiques à ceux du `ProtectedRoute` cible.
- Les appels API et la forme des payloads.
- Les règles métier (RG-01 à RG-12 de `CLAUDE.md`).
- Le backend : aucun fichier sous `backend/`.
- L'ajout de fonctionnalités. Ce chantier polit l'existant, il ne
  l'étend pas.
