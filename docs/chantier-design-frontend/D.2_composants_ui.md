# SPRINT D.2

## Les composants — variantes, états, focus, cohérence

*Module Dotations Téléphoniques Mensuelles — Frontend*

| | |
|---|---|
| **Objet** | Porter les 18 composants partagés au niveau de finition attendu, avant de toucher aux écrans qui les consomment |
| **Livrable** | `components/ui/` et `components/layout/` révisés, états et focus complets |
| **Durée** | Une journée et demie |
| **Prérequis** | D.1 validé — police et tokens en place |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Inventaire et critique (étape 2) | Sonnet 5 | Medium à High |
| Composants de saisie (étape 3) | Sonnet 5 | **High** |
| Composants d'affichage (étape 4) | Sonnet 5 | Medium |
| `DataTable` (étape 5) | Sonnet 5 ou Opus 4.8 | **High** |
| Layout (étape 6) | Sonnet 5 | Medium |

`DataTable` mérite un traitement à part : c'est le composant le plus vu de l'application — listes de bénéficiaires, de processus, de grilles, d'utilisateurs, journal d'audit, et le tableau des lignes d'état mensuel.

## 0. Nouvelle session : rappel Graphify

```
py -3.14 -m graphify update .
```

---

## 1. Contexte

### 1.1 Pourquoi les composants avant les écrans

Les 29 écrans consomment ces 18 composants. Polir un écran avant son composant revient à corriger le symptôme : le même défaut réapparaîtra sur les 28 autres. Ce sprint traite la cause.

### 1.2 Inventaire réel

**`components/ui/` — 15 composants**

| Composant | Rôle | Enjeu de finition |
|---|---|---|
| `Button` | Boutons, variantes CVA | Focus visible, état désactivé, état de chargement |
| `Input` | Champ texte | Focus, état d'erreur, état désactivé |
| `Textarea` | Zone de texte | Idem, plus redimensionnement |
| `Select` | Liste déroulante | Focus, cohérence avec `Input` |
| `Checkbox` | Case à cocher (Radix) | Déjà accessible — vérifier le focus visible |
| `Label` | Libellé (Radix) | Association au champ, indicateur d'obligatoire |
| `FormField` | Libellé + champ + erreur | Cohérence du message d'erreur |
| `Card` | Conteneur | **Ne pas imbriquer** (anti-pattern Impeccable) |
| `Badge` | Statuts | Contraste du texte sur fond coloré |
| `Alert` | Messages | Hiérarchie des variantes |
| `DataTable` | Tableaux | Le composant le plus exposé — voir étape 5 |
| `ConfirmDialog` | Confirmation | Piège de focus, fermeture au clavier |
| `VoirMotifModal` | Affichage d'un motif | Idem |
| `LienRetour` | Navigation retour | Zone cliquable, cohérence |
| `Logo` | Logo AFB | **Figé par la charte** — ne pas toucher |

**`components/layout/` — 3 composants** : `AppLayout`, `Sidebar`, `PageHeader`.

### 1.3 Ce qui est figé dans ce périmètre

- **`Logo.jsx`** — place et identité du logo, charte §15.
- **`Sidebar.jsx`** — fond sombre, texte blanc, élément actif en `#E30613`. Sa **structure visuelle** est figée ; son ergonomie (focus clavier, transitions, état replié) est libre.
- **Les rôles de `NAV_LINKS`** — verrouillés par l'audit 6F.9, strictement identiques à ceux du `ProtectedRoute` cible. **Ne jamais les modifier.**

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
1. Lis CLAUDE.md SECTION 15 (charte visuelle).
2. Lis DESIGN.md (garde-fou ecrit en D.0).
3. Lis docs/chantier-design-frontend/D.2_composants_ui.md
4. Lance /graphify . --update

CONTEXTE : Sprint D.2, les 18 composants partages.

FIGE, ne touche pas : Logo.jsx, la structure visuelle de Sidebar.jsx
(fond sombre, texte blanc, actif en #E30613), et surtout les roles de
NAV_LINKS -- verrouilles par l'audit 6F.9, ils doivent rester
STRICTEMENT identiques a ceux du ProtectedRoute cible.

LIBRE : focus, etats (vide, chargement, erreur, desactive),
transitions, espacements internes, contrastes de lisibilite.

METHODE : un composant a la fois. Tu montres le diff, j'approuve, tu
continues. Ne fais JAMAIS un passage global sur les 18 composants
d'un coup -- je ne pourrais pas relire.

BACKEND INTERDIT : aucun fichier sous backend/.

PREMIERE ACTION : la verification d'espace de travail, puis l'etape 2.
```

---

## 3. Étape 2. Critique avant modification

```
Avant de toucher a un seul composant, fais un etat des lieux.

1. Lance la critique Impeccable sur le repertoire des composants :

    /impeccable critique frontend/src/components

2. Lance aussi le detecteur cible :

    npx impeccable detect frontend/src/components

3. Produis-moi un TABLEAU, un composant par ligne :

   | Composant | Focus visible ? | Etat desactive ? | Etat erreur ? | Etat chargement ? | Findings detecteur |

   Remplis-le en LISANT le code, pas en supposant. Pour "focus
   visible", cherche reellement les classes focus-visible: ou
   focus: dans chaque fichier.

4. Signale-moi separement :
   - les composants qui ont DEJA tout ce qu'il faut (a ne pas
     toucher -- ne "polis" pas ce qui va bien)
   - les cartes imbriquees dans des cartes, s'il y en a
     (anti-pattern Impeccable explicite)
   - toute proposition de l'outil qui toucherait a la CHARTE : tu me
     la signales, tu ne l'appliques pas

NE MODIFIE RIEN A CETTE ETAPE.
```

---

## 4. Étape 3. Composants de saisie

Ordre : `Button` → `Input` → `Textarea` → `Select` → `Checkbox` → `Label` → `FormField`.

```
Traite les composants de saisie, un par un, dans l'ordre indique.

POUR CHACUN, les points a couvrir :

  a) FOCUS VISIBLE au clavier. C'est le plus important du sprint.
     Un agent bancaire qui navigue au clavier doit toujours savoir ou
     il est. Utilise focus-visible: (pas focus:) pour ne pas afficher
     l'anneau au clic souris.
     L'anneau doit contraster sur fond blanc ET sur fond neutral-50.

  b) ETAT DESACTIVE lisible mais clairement inactif. Verifie le
     contraste : un champ desactive illisible est un probleme
     d'accessibilite, pas un effet de style.

  c) ETAT D'ERREUR coherent. FormField affiche deja error.message en
     text-primary-500. VERIFIE que le rouge d'erreur et le rouge de
     marque ne se confondent pas visuellement -- c'est un vrai
     risque ici, la charte imposant #E30613 pour les actions
     primaires. Si un bouton primaire et un message d'erreur ont la
     meme couleur, l'utilisateur ne distingue plus "action" de
     "probleme". SIGNALE-LE-MOI si tu le constates, avec ta
     proposition.

  d) ETAT DE CHARGEMENT sur Button : plusieurs pages gerent deja
     enCours/isSubmitting en changeant le libelle ("Creation en
     cours..."). Regarde si une variante de Button serait plus
     coherente que du texte conditionnel repete dans 10 pages.
     Propose, ne l'impose pas.

  e) TAILLE DE CIBLE tactile suffisante (le detecteur a une regle
     dessus).

CONTRAINTE : les boutons primaires restent en #E30613, les
secondaires en gris ou contour. Charte, non negociable.

Montre le diff de chaque composant separement.
```

---

## 5. Étape 4. Composants d'affichage

Ordre : `Card` → `Badge` → `Alert` → `LienRetour` → `ConfirmDialog` → `VoirMotifModal`.

```
POINTS SPECIFIQUES a couvrir :

CARD
  Verifie qu'aucune Card n'est imbriquee dans une autre Card
  (anti-pattern Impeccable explicite). Si tu en trouves, SIGNALE-LES
  avec leur emplacement -- la correction se fera en D.3, ecran par
  ecran, pas ici.

BADGE
  Les variants sont utilises pour les statuts via statutGrille.js,
  statutProcessus.js et statutUtilisateur.js.
  VERIFIE LE CONTRASTE du texte sur chaque fond de variant. Le
  detecteur a une regle "texte gris sur fond colore" -- c'est un
  anti-pattern classique et il est probable qu'un variant en souffre.
  Les statuts sont l'information la plus lue de l'application : un
  badge illisible est un vrai defaut fonctionnel.

ALERT
  Verifie la hierarchie entre variants (destructive, warning, etc.) :
  a l'oeil, on doit distinguer immediatement une erreur d'un simple
  avertissement.

CONFIRMDIALOG et VOIRMOTIFMODAL
  Ces deux modales sont construites a la main
  (fixed inset-0 z-50 ... bg-black/40), sans primitive Radix.
  Verifie et corrige si necessaire :
    - fermeture par la touche Echap
    - piege de focus (le clavier ne doit pas sortir de la modale)
    - focus rendu a l'element declencheur a la fermeture
    - role="dialog" et aria-modal="true"
    - aria-labelledby pointant sur le titre
  C'est de l'accessibilite reelle, pas du confort. Si l'ajout de
  @radix-ui/react-dialog te semble justifie plutot que de tout
  reimplementer a la main, PROPOSE-LE-MOI avec le cout -- mais ne
  l'installe pas seul, ce serait une nouvelle dependance.

LIENRETOUR
  Ce composant est desormais utilise avec to={location.state?.retour}
  sur plusieurs pages (sprint MM.9). Verifie sa zone cliquable et son
  focus.
```

---

## 6. Étape 5. `DataTable` — le composant le plus exposé

```
DataTable est utilise par presque tous les ecrans de liste. Traite-le
seul, avec attention.

Points a couvrir :

  a) ETAT VIDE. Que se passe-t-il quand donnees est un tableau vide ?
     Regarde le code reel. Un tableau vide sans message est un cul-de-
     sac pour l'utilisateur : il ne sait pas si ca charge, si le
     filtre est trop restrictif, ou s'il n'y a rien.
     Propose un etat vide avec un message utile -- et si possible
     contextuel (l'appelant passe le message).

  b) ETAT DE CHARGEMENT. Certains ecrans gerent leur propre squelette
     (CarteSquelette, EnTeteSquelette). Y a-t-il une incoherence
     entre les ecrans qui en ont un et ceux qui n'en ont pas ?
     Signale-la ; ne l'uniformise que si je valide.

  c) ALIGNEMENT DES MONTANTS. Les colonnes de montants en FCFA
     doivent etre alignees a DROITE et en chiffres tabulaires (token
     pose en D.1). Aujourd'hui, verifie ce qui est fait reellement.
     C'est le point le plus utile de tout ce sprint pour un
     controleur qui compare des colonnes de chiffres.

  d) EN-TETE. Charte : "en-tete gris clair, texte sombre, bordures
     discretes". Verifie la conformite.

  e) DENSITE. Un tableau de dotations se lit en balayage vertical.
     Verifie que l'espacement des lignes aide ce balayage sans
     etaler inutilement.

  f) LARGEUR DE LIGNE et debordement : que se passe-t-il si
     nomPrenoms est tres long ? Et sur un ecran etroit ?

  g) SEMANTIQUE : <table> avec <thead>/<tbody>, scope="col" sur les
     en-tetes. Un lecteur d'écran doit pouvoir annoncer la colonne.

Montre-moi le diff, puis la liste des ecrans qui consomment DataTable
et qu'il faudra reverifier visuellement.
```

---

## 7. Étape 6. Layout

```
AppLayout, Sidebar, PageHeader.

SIDEBAR -- attention particuliere :
  FIGE : fond sombre, texte et icones blancs, actif en #E30613, logo
  AFB en haut, structure verticale. Charte §15.
  VERROUILLE : les roles de NAV_LINKS. Audit 6F.9. Ne les touche
  sous aucun pretexte.

  LIBRE et a traiter :
    - focus visible sur chaque NavLink au clavier
    - transition de l'etat replie/deplie (deja animee via
      transition-[width]) -- verifie qu'elle reste fluide
    - le menu utilisateur en bas : fermeture au clic exterieur deja
      geree par useEffect ; verifie la fermeture par Echap et le
      focus clavier
    - en mode replie, les titres (title=...) suffisent-ils, ou faut-il
      un vrai tooltip accessible ? Propose, ne tranche pas.

APPLAYOUT
  Verifie le comportement du defilement (la zone de contenu a son
  propre overflow-y-auto) et le filigrane .fond-filigrane -- qui doit
  rester UNIQUEMENT sur la zone de contenu, jamais sur la sidebar ni
  sur les cartes. Charte.

PAGEHEADER
  Utilise sur presque tous les ecrans (surTitre + titre). Verifie la
  hierarchie typographique avec la nouvelle police de D.1, et la
  coherence des marges avec le contenu qui suit.
```

---

## 8. Étape 7. Vérification

```
1. Detecteur -- comparaison :

    npx impeccable detect frontend/src/components

   Compare a la mesure de l'etape 2. Le total doit avoir BAISSE.
   Montre-moi les findings restants et pourquoi ils restent.

2. Build :

    cd frontend ; npx oxlint ; npm run build

3. Backend intact :

    git status --short -- backend
   Doit etre VIDE.

4. Verrou de securite -- CRITIQUE :

    git diff frontend/src/components/layout/Sidebar.jsx

   Confirme-moi que NAV_LINKS est INCHANGE (memes chemins, memes
   roles, meme ordre). C'est le verrou de l'audit 6F.9.
```

---

## 9. Vérification visuelle — à faire par l'utilisateur

1. **Navigation entièrement au clavier**, sans souris : `Tab` depuis l'écran de connexion jusqu'à un bouton d'action. À chaque étape, tu dois **voir** où tu es. C'est le test le plus important du sprint.
2. **Modales** — ouvre `ConfirmDialog` (désactiver un bénéficiaire) : `Échap` la ferme, `Tab` ne sort pas de la modale, et le focus revient au bouton d'origine à la fermeture.
3. **Badges de statut** — parcours la liste des processus et celle des grilles : chaque statut doit être lisible **sans effort**.
4. **Tableau de montants** — liste des bénéficiaires : les montants sont alignés à droite et les chiffres s'empilent verticalement.
5. **Tableau vide** — filtre sur quelque chose qui ne renvoie rien : un message clair s'affiche, pas un tableau nu.
6. **Sidebar** — replie/déplie, vérifie la fluidité ; confirme que le rouge de l'actif et le logo sont **inchangés**.
7. **Formulaire en erreur** — soumets un formulaire invalide : le message d'erreur se distingue-t-il bien d'un bouton primaire ? (point 3c de l'étape 3)

---

## 10. Critères de validation

| Élément | Statut attendu |
|---|---|
| Tableau d'état des lieux produit **par lecture du code**, pas par supposition | Fait |
| Composants déjà corrects **laissés intacts** | Vérifié |
| `focus-visible:` (et non `focus:`) sur tous les éléments interactifs | Vérifié au clavier |
| Anneau de focus contrasté sur fond blanc **et** sur `neutral-50` | Vérifié |
| Confusion possible rouge d'erreur / rouge de marque : **signalée**, traitée avec accord | Fait |
| `ConfirmDialog` et `VoirMotifModal` : Échap, piège de focus, retour du focus, `role="dialog"` | Vérifié au clavier |
| Ajout éventuel de `@radix-ui/react-dialog` **proposé**, jamais installé seul | Fait |
| Contraste des `Badge` vérifié sur **chaque** variant de statut | Vérifié |
| Cartes imbriquées **signalées** (correction reportée à D.3) | Fait |
| `DataTable` : état vide avec message utile | Vérifié |
| `DataTable` : montants **alignés à droite en chiffres tabulaires** | Vérifié à l'écran |
| `DataTable` : `<thead>`/`<tbody>` et `scope="col"` | Vérifié |
| **`NAV_LINKS` strictement inchangé** (verrou audit 6F.9) | Vérifié sur `git diff` |
| `Logo.jsx` non modifié | Vérifié |
| Filigrane toujours limité à la zone de contenu | Vérifié à l'écran |
| Rouge `#E30613` et structure de la sidebar inchangés | Vérifié à l'écran |
| *Findings* du détecteur en **baisse** sur `components/` | Vérifié |
| `npx oxlint` et `npm run build` OK | Vérifié |
| **Aucun fichier sous `backend/` modifié** | Vérifié |

---

## Commit

```bash
git add .
git commit -m "d.2: composants partages -- focus, etats, contrastes, accessibilite des modales"
```

---

**Fin du Sprint D.2** — *en attente de validation avant D.3*
