# PLAN — CHANTIER DESIGN FRONTEND

## Élever la finition du frontend sans toucher à la charte graphique

*Module Dotations Téléphoniques Mensuelles — Frontend*

| | |
|---|---|
| **Objet** | Passer d'un frontend fonctionnel à un frontend soigné, en s'appuyant sur les skills Impeccable et `frontend-design` |
| **Livrable** | 5 guides de sous-sprint (D.0 à D.4) |
| **Cible** | Copie `afb-dottel-mm` — **jamais** le dépôt d'origine |
| **Contrainte absolue** | **La charte graphique de `CLAUDE.md` section 15 n'est pas négociable** |
| **Prérequis** | Chantier MM.0 → MM.13 clos |

---

## 0. La contrainte centrale : ce qui est figé, ce qui ne l'est pas

C'est le point qui gouverne tout ce chantier. Impeccable et `frontend-design` sont des outils **opinionés** : livrés à eux-mêmes, ils proposeront de changer des couleurs, des polices, des formes. Il faut leur poser un cadre avant de les lâcher sur le code.

### 0.1 FIGÉ — la charte, jamais discutée

Extrait de `CLAUDE.md` section 15, imposé par l'identité visuelle Afriland First Bank :

| Élément | Valeur imposée |
|---|---|
| Rouge institutionnel | **`#E30613`** (`--color-primary-500`) — élément actif de navigation, boutons primaires |
| Navigation latérale | Verticale, **fond sombre**, texte et icônes blancs |
| Contenu principal | Fond blanc ou gris très clair **`#F5F5F5`** |
| Boutons secondaires | Gris ou contour |
| Tableaux | En-tête gris clair, texte sombre, bordures discrètes |
| Formulaires | Champs blancs, bordure grise, sobres |
| Logo AFB | En haut de la barre de navigation |
| Typographie | Sans-serif **sobre** |

S'y ajoutent deux décisions déjà prises et implémentées : le **filigrane discret** de la zone de contenu (`.fond-filigrane`, jamais sur la sidebar ni les cartes) et l'échelle de tokens déjà définie dans `frontend/src/index.css` (`--color-primary-*`, `--color-neutral-*`).

### 0.2 NÉGOCIABLE — l'espace réel d'amélioration

La charte contraint l'**identité**, pas le **soin**. Tout ce qui suit est libre et constitue le vrai périmètre du chantier :

- **Le choix précis de la police.** La charte dit « sans-serif sobre » — elle ne nomme aucune police. Or aucune n'est déclarée aujourd'hui : le projet retombe sur la pile système par défaut.
- **Rythme vertical, espacements, densité** — aucune contrainte de charte.
- **Hiérarchie typographique** (tailles, graisses, contrastes) à l'intérieur d'une page.
- **États** : vide, chargement, erreur, succès. Peu traités aujourd'hui.
- **Micro-interactions et transitions** — aucune contrainte.
- **Accessibilité** : focus visibles, contrastes, sémantique, lecteurs d'écran.
- **Responsive** : comportement sur écrans étroits.
- **Cohérence** entre les 29 écrans.

**Règle de tranchage** : si une proposition change une **couleur de marque, la forme de la navigation, ou la place du logo** → refus, c'est la charte. Si elle change un **espacement, un état, une transition, un contraste de lisibilité** → c'est le périmètre du chantier.

---

## 1. Constat de départ — état réel vérifié

### 1.1 Ce qui est déjà bon

- **Tokens de couleur déjà centralisés** dans `frontend/src/index.css` via `@theme` de Tailwind 4 — les 11 nuances de `primary` et `neutral` sont définies proprement, sans hexadécimal en dur éparpillé dans les composants.
- **Bibliothèque de composants réelle** : 15 composants dans `components/ui/`, 3 dans `components/layout/`. Le projet ne réinvente pas ses boutons à chaque page.
- **`class-variance-authority` + `tailwind-merge`** déjà en place — l'infrastructure pour des variantes propres existe.
- **Radix UI** pour `Checkbox` et `Label` — primitives accessibles.
- **Squelettes de chargement** déjà présents sur certains écrans (`CarteSquelette`, `EnTeteSquelette`).
- **Zéro `console.log`**, jeton jamais en `localStorage` — hygiène validée par l'audit 6F.9.

### 1.2 Trois défauts concrets, vérifiés

| # | Défaut | Fichier | Nature |
|---|---|---|---|
| **F1** | `<html lang="en">` sur une application **entièrement en français** | `frontend/index.html` | **Bug d'accessibilité réel** : un lecteur d'écran prononcera le français avec les règles de l'anglais |
| **F2** | `<title>frontend</title>` | `frontend/index.html` | Titre d'échafaudage Vite jamais remplacé — visible dans l'onglet du navigateur et les favoris |
| **F3** | **Aucune police déclarée** — ni `font-family`, ni `@font-face`, ni `--font-sans` dans `@theme` | `frontend/src/index.css` | Repli silencieux sur la pile système. C'est précisément l'anti-pattern « system defaults » d'Impeccable, et la charte laisse le choix ouvert. |

### 1.3 Périmètre matériel

| Élément | Nombre |
|---|---|
| Pages (`src/pages/`) | **29** |
| Composants UI (`src/components/ui/`) | 15 |
| Composants de layout | 3 |
| Utilitaires de statut | 3 (`statutGrille`, `statutProcessus`, `statutUtilisateur`) |

---

## 2. Les 5 sous-sprints

| Sprint | Contenu | Risque |
|---|---|---|
| **D.0** | Installation des skills, prérequis, et **garde-fou de charte** (`PRODUCT.md`, `DESIGN.md`, règles de détection ignorées) | Faible — aucun code applicatif |
| **D.1** | Fondations : les 3 défauts F1-F3, choix et intégration de la police, complétion des tokens | Faible à moyen |
| **D.2** | Les 15 composants UI : variantes, états, focus, cohérence | Moyen |
| **D.3** | Les écrans : hiérarchie, densité, états vides et de chargement | Moyen |
| **D.4** | Audit final : accessibilité, responsive, performance, détecteur au vert | Moyen |

### Ordre d'exécution

**D.0 obligatoirement en premier** : sans le garde-fou de charte, les commandes Impeccable proposeront de changer le rouge institutionnel dès le premier passage.

Puis **D.1 avant tout le reste** : choisir la police et compléter les tokens *avant* de toucher aux composants, sinon chaque composant est retouché deux fois.

Ensuite **D.2 avant D.3** : les écrans consomment les composants. Polir un écran avant son composant, c'est corriger le symptôme.

**D.4 en clôture.**

---

## 3. Règles non négociables, valables pour les 5 sprints

### 3.1 Vérification d'espace de travail, au début de chaque session

```powershell
(Get-Location).Path      # doit se terminer par afb-dottel-mm
git log --oneline -1     # ne doit PAS afficher d9be38c
git remote -v            # doit être vide
```

### 3.2 La charte prime sur l'outil

Si une commande Impeccable propose une modification qui touche un élément de la section 0.1, **elle est refusée** — même si l'outil la présente comme une amélioration. L'outil ne connaît pas les contraintes d'identité d'une banque.

Toute proposition de ce type doit être **signalée**, pas appliquée en silence.

### 3.3 Aucun changement de comportement fonctionnel

Ce chantier touche l'apparence et l'ergonomie. Il ne change **jamais** :
- une route ou un rôle (`ProtectedRoute`, `NAV_LINKS`) — verrouillé par l'audit 6F.9
- un appel API ou la forme d'un payload
- une règle métier

Rappel de l'audit 6F.9 : les rôles de `NAV_LINKS` doivent rester **strictement identiques** à ceux du `ProtectedRoute` cible.

### 3.4 Le backend ne bouge pas

Aucun fichier sous `backend/` ne doit être modifié. Si `git status` en montre un, c'est une erreur à signaler.

### 3.5 Vérification à chaque sprint

```powershell
cd frontend
npx oxlint
npm run build
```

Et, dès D.0 installé :

```powershell
npx impeccable detect src/
```

Le build doit rester vert. Le nombre de *findings* du détecteur doit **diminuer** sprint après sprint, jamais augmenter.

### 3.6 Vérification visuelle humaine obligatoire

Aucun test frontend automatisé n'existe dans ce projet (pas de suite Vitest/Jest). **La seule validation réelle est visuelle**, faite par toi dans le navigateur, avec le backend lancé. Chaque sprint se termine par une liste d'écrans à regarder.

---

## 4. Ce que ce chantier ne fait pas

- **Il ne redessine pas l'application.** L'objectif est la finition, pas une refonte.
- **Il ne touche pas à la charte** (section 0.1).
- **Il n'ajoute aucune fonctionnalité.** Un centre de notifications in-app, par exemple, resterait une demande métier distincte.
- **Il ne migre pas de bibliothèque.** Tailwind 4, Radix, lucide-react et CVA restent.

---

**Fin du plan** — *D.0 peut démarrer immédiatement*
