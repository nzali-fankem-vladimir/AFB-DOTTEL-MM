# SPRINT 6F.2

## Layout global et navigation

*Module Dotations Téléphoniques Mensuelles — Frontend*

| | |
|---|---|
| **Objet** | Structure de page commune : sidebar, en-têtes de page, routage |
| **Livrable** | Sidebar, AppLayout, PageHeader/PageTitle équivalents, configuration react-router-dom |
| **Durée** | Une journée |
| **Prérequis** | Sprint 6F.1 validé |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Sidebar et layout (étapes 2-3) | Sonnet 5 | Medium |
| Routage et en-têtes de page (étapes 4-5) | Sonnet 5 | Medium |

## 0. Nouvelle session : rappel Graphify

```
py -3.14 -m graphify update .
```

## 1. Contexte

Richard construit son layout autour d'un header horizontal (`AppHeader.tsx`, `AppFooter.tsx`). DOTTEL utilise une **sidebar verticale sombre**, déjà actée dans la charte du projet — le pattern de `AppHeader.tsx` (menu déroulant utilisateur, `useRef`/`useEffect` pour fermer au clic extérieur, structure `NAV_LINKS` filtrée par rôle) reste utile à adapter, mais la disposition change entièrement (verticale, pas horizontale).

`PageHeader.tsx`/`PageTitle.tsx` de Richard (bandeau badge + titre + séparateur or) sont directement transposables tels quels, juste recolorés.

## 2. Objectifs

- `Sidebar.jsx` : navigation verticale sombre, liens filtrés par rôle (EMPLOYE, ARH, CRH, DRH, ADMIN)
- `AppLayout.jsx` : structure globale (sidebar + zone de contenu)
- `PageHeader.jsx` (badge + titre, adapté de `PageHeader.tsx`/`PageTitle.tsx` de Richard)
- Configuration des routes avec `react-router-dom`
- Tests visuels manuels

## 3. Rappel des rôles et routes prévues

- EMPLOYE : `/enrolement/verifier`, `/enrolement/confirmer`
- ARH : `/beneficiaires`, `/processus`, `/grilles-tarifaires`
- CRH, DRH : `/processus` (validation), `/reporting` (DRH uniquement pour l'audit)
- ADMIN : `/admin/utilisateurs`, `/grilles-tarifaires`

## 4. Étapes d'implémentation

### Étape 1. Ouvrir une session Claude Code

```
Tu es mon assistant de developpement pour le projet de digitalisation
des dotations telephoniques mensuelles d'Afriland First Bank.

AVANT TOUT : lis CLAUDE.md, en particulier la charte frontend.
Confirme en 3 lignes ce que tu y as trouve.

CONTEXTE : Sprint 6F.2. Les composants UI de base (Sprint 6F.1) sont
termines et commites.

Tu as acces en lecture a
D:\stage afriland\formation specialisee DSI\projet de gestion des
absences\projet richard\absence-module\absences-front\components\layout
pour AppHeader.tsx, AppFooter.tsx, PageHeader.tsx, PageTitle.tsx --
reprends la LOGIQUE (menu utilisateur, filtrage de liens par role,
structure badge+titre), jamais sa palette ni son header horizontal.
DOTTEL utilise une sidebar verticale sombre, deja actee.

METHODE DE TRAVAIL :
- Un fichier a la fois. Tu montres, j'approuve, tu continues.
- Si un choix n'est pas couvert par CLAUDE.md, tu poses la question
  plutot que de supposer.

PREMIERE ACTION : cree Sidebar.jsx : navigation verticale sombre,
logo Afriland en haut (reutilise Logo.jsx du Sprint 6F.1), liens
filtres selon le role de l'utilisateur connecte (structure
NAV_LINKS avec un tableau roles, meme pattern que AppHeader.tsx de
Richard), menu utilisateur en bas de sidebar (nom, role, bouton
deconnexion). Montre le fichier.
```

### Étape 2. AppLayout

```
Cree AppLayout.jsx : structure globale avec Sidebar a gauche, zone
de contenu a droite (scroll independant), utilise <Outlet /> de
react-router-dom pour le contenu des routes enfants. Montre le
fichier.
```

### Étape 3. PageHeader

```
Adapte PageHeader.jsx a partir du pattern de Richard
(PageHeader.tsx/PageTitle.tsx) : bandeau avec sur-titre (badge),
titre principal, separateur decoratif a droite -- recolore en
E30613/or remplace par une teinte coherente avec la charte DOTTEL
(a toi de proposer, montre-moi avant de generaliser si tu hesites
entre plusieurs teintes d'accent). Montre le fichier.
```

### Étape 4. Configuration des routes

```
Installe react-router-dom si absent. Configure les routes de base
(sans les pages elles-memes, qui seront creees aux sous-sprints
suivants) : une route par domaine (/enrolement, /beneficiaires,
/processus, /grilles-tarifaires, /admin, /reporting), toutes
enfants d'AppLayout. Pages provisoires (placeholder simple) pour
chaque route a ce stade. Montre le fichier de configuration des
routes.
```

### Étape 5. Vérification visuelle

```
Lance npm run dev, verifie que la sidebar s'affiche, que la
navigation entre les routes placeholder fonctionne, et qu'aucune
erreur de build ou de console n'apparait.
```

## 5. Critères de validation

| Élément | Statut attendu |
|---|---|
| Sidebar affichée avec liens filtrés par rôle (à vérifier une fois l'auth branchée au 6F.3) | Fait |
| Navigation entre routes placeholder fonctionnelle | Vérifié |
| PageHeader réutilisable sur toutes les pages | Vérifié |
| Aucune dépendance Next.js résiduelle | Vérifié |

## Commit

```bash
git add .
git commit -m "sprint-6F.2: layout global et navigation (sidebar, routage)"
```

---

**Fin du Sprint 6F.2** — *en attente de validation avant le Sprint 6F.3*
