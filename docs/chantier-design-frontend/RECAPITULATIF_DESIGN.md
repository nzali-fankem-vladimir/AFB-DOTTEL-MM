# RÉCAPITULATIF DE FIN DE CHANTIER — DESIGN FRONTEND

Module : Digitalisation des Dotations Téléphoniques Mensuelles — Frontend

*Projet AFRILAND HORIZON 2030 — Module INTRA*

| **Référence** | **AFB_DESIGN_DOTTEL_D0_D4_2026** |
| --- | --- |
| Version | 1.0 |
| Date | 20 août 2026 |
| Chantier | D.0 à D.4 — finition visuelle et accessibilité du frontend |
| Périmètre | `frontend/src/` uniquement : 19 composants partagés, 30 pages, `index.css`. Aucun fichier sous `backend/`. |
| Méthode | Outils déterministes (`npx impeccable detect`), audit contextuel (`/impeccable audit`), référentiel externe (`web-design-guidelines`), et **vérification en navigateur réel** sur les cinq rôles authentifiés. |
| Base normative | `CLAUDE.md` section 15 (charte visuelle) et `DESIGN.md` (garde-fou du chantier) |

---

## 0. Synthèse

**Le chantier n'a modifié aucune couleur de marque, aucun rôle et aucun fichier backend.** Ces trois verrous sont vérifiés par `git diff` en fin de sprint D.4 et le restent sur toute la durée du chantier.

Le socle visuel était sain dès le départ : le détecteur déterministe rendait **zéro finding** en D.0 comme en D.4, y compris relancé avec `--no-config` (aucun défaut masqué par un *ignore*). Le vrai gisement n'était donc pas l'esthétique mais **l'accessibilité**, invisible pour ce détecteur : hiérarchie de titres, noms accessibles, contrastes des nuances secondaires, respect du réglage système de mouvement réduit.

L'écart le plus significatif du chantier tient en un chiffre : **`aria-invalid` et `aria-describedby` apparaissaient zéro fois dans 55 fichiers**. Un agent utilisant un lecteur d'écran entendait le libellé d'un champ refusé, jamais la raison du refus — sur des écrans qui engagent des paiements réels.

| Domaine | Constat d'entrée | État de sortie |
| --- | --- | --- |
| Détecteur déterministe | 0 finding | 0 finding *(maintenu)* |
| Détecteur navigateur | 2 findings | 2 findings *(assumés, motivés au § 3)* |
| Contrastes en échec | 6 combinaisons | **0** |
| Hiérarchie de titres | `h2` sauté sur 29 écrans | conforme |
| Champs de formulaire reliés à leur erreur | 0 / 3 | **3 / 3** |
| Icônes décoratives masquées aux lecteurs d'écran | 15 / 76 | **76 / 76** |
| Contrôles sans nom accessible | 8 | **0** |
| Modales sans limite de hauteur | 7 / 9 | **0 / 9** |
| `prefers-reduced-motion` | ignoré | honoré |

---

## 1. Tableau avant / après — mesures réelles

| Indicateur | Avant (D.0) | Après (D.4) | Mesure |
| --- | --- | --- | --- |
| **Findings du détecteur — analyse statique** | 0 | 0 | `npx impeccable detect frontend/src` contre `.impeccable/baseline.json` |
| **Findings du détecteur — scan navigateur** | 2 | 2 | contre `.impeccable/baseline-navigateur.json` |
| ├ `flat-type-hierarchy` | 1 (« 14px, 16px, 18px, ratio 1.3:1 ») | 1 (« 14px, 16px, 20px, ratio 1.4:1 ») | ratio amélioré, règle non levée — voir § 4 |
| └ `layout-transition` | 1 (« transition: width ») | 1 | conservé sur décision — voir § 2 et § 3 |
| **Police** | pile système (`ui-sans-serif, system-ui`) | **Source Sans 3** embarquée en woff2 | 4 graisses, 62,61 kB |
| **`lang` de `<html>`** | `en` | **`fr`** | vérifié en rendu navigateur |
| **Titre du document** | *(générique)* | **« Dotations Téléphoniques Mensuelles »** | vérifié en rendu navigateur |
| **Contrastes en échec** | **6** | **0** | calcul WCAG 2.1 sur toutes les combinaisons réellement employées |
| **Taille du bundle** | 565,22 kB livrés | **642,64 kB livrés** (+77,42 kB) | deux `npm run build` réels, mêmes dépendances — détail § 5 |
| ├ JS | 521,72 kB (158,49 gzip) | 545,35 kB (163,42 gzip) | |
| ├ CSS | 24,64 kB (5,78 gzip) | 30,00 kB (6,74 gzip) | |
| └ Polices + emblème | 0 | 67,29 kB | ajoutés en D.1 |
| **Composants au focus visible** | — | **19 / 19** | 9 composants portent une déclaration de focus ; les 10 autres n'ont aucun élément focalisable propre |
| **Écrans à l'état vide traité** | — | **11 / 11 listes** | 9 `messageVide` personnalisés + 2 tableaux à message contextuel propre |
| **Écrans à l'erreur de chargement traitée** | — | **11** | `erreurChargement` distinct de l'état vide |

### 1.1 Contrastes — le détail des six échecs corrigés

| Combinaison | Avant | Après | Seuil |
| --- | --- | --- | --- |
| `text-neutral-500` sur blanc (20 occurrences) | 3,45:1 ❌ | `neutral-600` → **5,17:1** ✅ | 4,5 |
| `text-neutral-500` sur `neutral-50` | 3,17:1 ❌ | `neutral-600` → **4,75:1** ✅ | 4,5 |
| `text-neutral-400` en texte sur blanc (3 occurrences) | 2,30:1 ❌ | `neutral-600` → **5,17:1** ✅ | 4,5 |
| `placeholder:text-neutral-400` | 2,30:1 ❌ | `neutral-600` → **5,17:1** ✅ | 4,5 |
| Bordure de contrôle `neutral-300` | 1,67:1 ❌ | `neutral-500` → **3,45:1** ✅ | 3,0 (WCAG 1.4.11) |
| En-tête de tableau `AjusterLignesModal` | divergent | aligné sur `neutral-700` → **6,87:1** ✅ | 4,5 |

### 1.2 Contrastes imposés par la charte — aucun en échec

Point le plus important de l'audit d'accessibilité : **le rouge institutionnel `#E30613` passe le seuil AA**, il n'a donc jamais fallu envisager de le contourner.

| Combinaison imposée | Ratio | Verdict |
| --- | --- | --- |
| Texte blanc sur `#E30613` (bouton primaire, entrée de navigation active) | **4,88:1** | ✅ AA |
| Texte blanc sur `primary-700` (action destructive) | 8,48:1 | ✅ |
| Texte blanc sur la sidebar `neutral-950` | 18,88:1 | ✅ |
| `neutral-300` (lien inactif) sur la sidebar | 11,29:1 | ✅ |
| `neutral-400` (rôle, bouton Réduire) sur la sidebar | 8,22:1 | ✅ |
| `primary-300` (Déconnexion) sur `neutral-900` | 7,33:1 | ✅ |
| Les 5 variantes de `Badge` | 4,84 à 7,70:1 | ✅ toutes |

---

## 2. Décisions prises pendant le chantier

| # | Décision | Sprint | Motif |
| --- | --- | --- | --- |
| 1 | **Police : Source Sans 3** (Adobe, SIL OFL 1.1), embarquée en woff2, jamais chargée depuis un CDN | D.1 | Retenue sur trois mesures faites sur les fichiers réels : chiffres à chasse fixe nativement (les colonnes de montants FCFA s'alignent sans classe utilitaire), 18,3 % d'em de dégagement au-dessus des accents capitales (les en-têtes de `DataTable` sont en capitales accentuées), couverture complète des diacritiques françaises. Embarquée parce qu'un poste bancaire interne peut n'avoir aucun accès sortant. |
| 2 | **Titre du document : « Dotations Téléphoniques Mensuelles »** | D.1 | Le document portait un titre générique. Le nom métier du module est celui que l'agent voit dans son onglet et dans ses favoris. |
| 3 | **Quatre graisses seulement** (400/500/600/700) | D.1 | Comptage des occurrences réelles : 37 × `font-medium`, 17 × `font-semibold`, 14 × `<strong>`, 3 × `font-bold`. Aucune graisse embarquée « au cas où ». |
| 4 | **Rouge de marque / rouge d'erreur : séparation par la couleur du texte** | D.2 | Le rouge `#E30613` reste réservé à l'action primaire. Un message d'erreur écrit dans ce rouge se confondait avec les boutons primaires. Le texte d'erreur est désormais en `neutral-800`, **seule l'icône porte le rouge**. |
| 5 | **Couleurs sémantiques hors palette de marque** (`emerald`, `amber`, `sky` sur `Badge`, `Alert`, timeline) | D.2 | Conséquence de la décision 4. Employer le rouge de marque pour le succès et l'alerte rendrait le statut d'un paiement illisible. Voir § 3, entrée P3-2. |
| 6 | **`@radix-ui/react-dialog` : NON ajouté** | D.2 | Le piège de focus a été écrit à la main (`useFocusTrap`, 47 lignes) plutôt que d'ajouter une dépendance. `DESIGN.md` interdit toute migration de bibliothèque sur ce chantier. Les 9 modales partagent ce hook. |
| 7 | **Lien de retour d'`AccesInterdit`** | D.3 | Voir le récapitulatif D.3. |
| 8 | **`CardTitle` : `h3` → `h2`** | D.4 | `PageHeader` pose le `h1` de chaque écran ; la carte suivante enchaînait sur `h3`, sautant un niveau sur les 29 écrans. Aucun impact visuel : la taille et la graisse restent portées par les classes explicites. Vérifié en rendu réel — la modale annonce bien un `H2`. |
| 8bis | **Lien d'évitement : essayé, mesuré, puis RETIRÉ** | D.4 | Seule correction du sprint à avoir été annulée. Le gain (9 → 2 tabulations par écran) a été jugé disproportionné au regard d'un module utilisé à la souris, et l'élément déroutant à l'usage. Voir § 4, entrée P4-8, pour l'écart WCAG assumé. |
| 9 | **Bordures de contrôle : `neutral-300` → `neutral-500`** | D.4 | **Arbitré avec le métier.** Dans les 12 formulaires, le champ blanc est posé sur une `Card` blanche : la bordure est le seul repère qui dit où l'on saisit. `#C8C8C8` donnait 1,67:1 pour un seuil de 3:1. `#8A8A8A` donne 3,45:1 et reste un gris de l'échelle déjà déclarée, donc « bordure grise » au sens de `DESIGN.md`, dont la section LIBRE couvre explicitement les contrastes d'accessibilité. |
| 10 | **Transition de largeur de la sidebar : CONSERVÉE, encadrée par `motion-reduce`** | D.4 | **Arbitré avec le métier.** Signalée par deux outils. La barre *pousse* le contenu (`flex`) : un `transform` la ferait glisser par-dessus en laissant un vide. Le coût est borné — 200 ms, au clic sur « Réduire » uniquement, jamais au défilement ni à la frappe. `motion-reduce:transition-none` traite le vrai risque, celui des personnes sensibles au mouvement. |
| 11 | **`autoComplete="off"` et `spellCheck={false}` par défaut sur `Input`** | D.4 | Aucun champ de DOTTEL ne décrit la personne qui saisit : un ARH tape le matricule d'un tiers, un ADMIN crée le compte de quelqu'un d'autre. La saisie semi-automatique proposerait les coordonnées du saisisseur sur la fiche d'un collègue. Le correcteur souligne par ailleurs en rouge les matricules et codes de fonction, qui ne sont pas des mots. Surchargeable au cas par cas. |
| 12 | **Découpage par route (`React.lazy`) : NON implémenté** | D.4 | Gain mesuré et réel (voir § 4), mais `AppRouter.jsx` porte les rôles de `ProtectedRoute`, verrou de l'audit 6F.9. Un sprint de clôture ne restructure pas. |

### 2.1 Propositions d'outil refusées au nom de la charte

| Proposition | Origine | Motif du refus |
| --- | --- | --- |
| « Title Case » sur les titres et libellés de boutons | `web-design-guidelines` | Règle typographique anglophone. Le français ne capitalise pas chaque mot. `PRODUCT.md` fixe une voix sobre et française. |
| `touch-action: manipulation`, cibles tactiles de 44 px | `web-design-guidelines` | DOTTEL est un poste de travail bancaire au clavier et à la souris (`PRODUCT.md`, section Users). |
| Placeholders terminés par `…` | `web-design-guidelines` | Les placeholders du projet sont des **exemples** préfixés « Ex. » (« Ex. Douala Bonanjo »), pas des instructions. Les deux champs de recherche, eux, portent déjà l'ellipse. |
| Avertir avant de quitter un formulaire non enregistré | `web-design-guidelines` | Ajout de fonctionnalité. `DESIGN.md`, section HORS PÉRIMÈTRE : « ce chantier polit l'existant, il ne l'étend pas ». |
| Mode sombre | détecteur / conventions générales | `DESIGN.md` FIGÉ : « Pas de mode sombre : il n'est pas demandé et sortirait de la charte. » |

---

## 3. Règles de détection ignorées

**C'est la section la plus importante pour un futur intervenant : elle dit ce qui est un choix assumé et non un oubli.**

| Règle | Valeur ignorée | Motif enregistré |
| --- | --- | --- |
| `layout-transition` | `transition: width` | Sidebar : la barre **pousse** le contenu (`flex`), un `transform` la ferait glisser par-dessus en laissant un vide. Coût borné : 200 ms, au clic sur « Réduire » uniquement. Arbitré avec le métier au Sprint D.4 ; `motion-reduce:transition-none` couvre les personnes sensibles au mouvement. |

Enregistré dans `.impeccable/config.json` (portée partagée, versionnée) via :

```bash
npx impeccable ignores add-value layout-transition "transition: width" --reason "…"
```

**Limite à connaître :** cet *ignore* ne filtre **pas** le scan navigateur (`impeccable detect http://localhost:3000`). Les *ignores* du détecteur s'appliquent à l'analyse de fichiers, pas aux scans d'URL. Le finding continuera donc de s'afficher lors d'un scan navigateur — c'est attendu, pas une régression.

**Aucune autre règle n'est ignorée.** En particulier, `overused-font` n'a jamais été ignorée : c'était le défaut F3 du plan, corrigé en D.1 par l'adoption de Source Sans 3.

---

## 4. Points laissés ouverts

| # | Point | Mesure | Pourquoi il n'a pas été traité |
| --- | --- | --- | --- |
| **P4-1** | **Découpage par route** (`React.lazy` + `Suspense`) | Premier chargement : **158,15 → 61,78 kB gzip, soit −63 %**. Mesuré le 20/08/2026 en bac à sable, sur 23 pages différées et 53 fragments. Le plus gros fragment sorti est `zod` (86,55 kB), chargé au premier formulaire seulement. | Changement structurel de `AppRouter.jsx`, qui porte les rôles de `ProtectedRoute` — verrou de l'audit 6F.9. Un sprint de clôture ne restructure pas le routage. Mérite un sprint dédié où les 23 routes sont revérifiées une à une. |
| **P4-2** | **Repli automatique de la sidebar sous 1024 px** | À 1024 px, la barre dépliée occupe 256 px et laisse 702 px de contenu ; le tableau des bénéficiaires y mesure 763 px et défile donc dans son cadre. | Changement de **comportement**, pas de finition. Entre de plus en conflit avec la préférence mémorisée dans `localStorage` (`dottel-sidebar-reduite`) : il faudrait trancher si le repli automatique écrase ou non un dépliage explicite de l'utilisateur. |
| **P4-3** | **Retrait de `react-hot-toast`** | 0 occurrence dans `src/`. Gain de poids : **0 kB** — la dépendance est déjà éliminée par le *tree-shaking*, le gain serait de propreté. | Dépendance déclarée. Elle figure de plus dans `CLAUDE.md` section 2 comme dépendance validée du projet : la retirer suppose d'amender ce document normatif, pas seulement `package.json`. |
| **P4-4** | `flat-type-hierarchy` sur la page de connexion | Tailles relevées : 14 px (bouton), 16 px (hérité du `body` sur des conteneurs sans texte propre), 20 px (titre). Le pas 16→14 reste sous 1,25. | La correction de D.4 a porté le titre de 18 à 20 px (ratio 1,3 → 1,4) sans lever la règle. Le 16 px n'appartient à aucun texte visible : le supprimer supposerait de fixer le `body` à 14 px, ce qui serait absurde. **Délibérément non ignoré** : un *ignore* porterait sur le snippet exact (« Sizes: 14px, 16px, 20px »), deviendrait obsolète en silence à la moindre évolution typographique et masquerait une régression future. Un finding visible et documenté est plus sûr. |
| **P4-5** | Coche blanche sur `emerald-500` (timeline, étape franchie) | 2,54:1 pour un seuil de 3:1. `emerald-600` donnerait 3,77:1. | Touche la palette sémantique arrêtée en D.2 (décision 5). **Atténuation en place** : depuis D.3, l'état d'étape ne repose pas sur la couleur seule — la forme le porte (coche / chiffre / triangle d'alerte) et un libellé `sr-only` le double pour les lecteurs d'écran. |
| **P4-6** | Apostrophes typographiques | 22 apostrophes courbes (`’`) contre 337 droites (`'`) dans `frontend/src`. | Incohérence typographique réelle mais mineure. Une reprise globale toucherait 337 emplacements dont beaucoup sont des **délimiteurs de chaîne JavaScript** : risque de casse disproportionné au gain. À traiter, si souhaité, par une passe ciblée sur les seules chaînes affichées. |
| **P4-7** | `jwt-decode` — **constat périmé, aucune action** | 2 occurrences réelles : `AuthProviderKeycloak.js:1` et `:69`. | L'audit 6F.9 la déclarait « utilisée nulle part ». C'était vrai à l'époque : le commit `66550f0` (MM.7, 04/08/2026) l'a introduite après l'audit. **À conserver.** Consigné ici pour qu'un futur intervenant ne la retire pas sur la foi de l'audit 6F.9. |
| **P4-8** | **Lien d'évitement (« Aller au contenu principal ») — essayé puis RETIRÉ** | Gain mesuré, rôle DRH : atteindre le premier contrôle du contenu demande **9 tabulations** sans le lien, **2 actions** avec (Entrée + un Tab), et cela à chaque changement d'écran. | **Retiré sur décision du métier au Sprint D.4.** Le gain de 7 tabulations ne bénéficie qu'aux utilisateurs naviguant au clavier seul ; dans l'usage réel du module, la navigation se fait à la souris, et un utilisateur qui clique dans la barre latérale conserve le focus sur le lien cliqué. L'élément a par ailleurs été jugé déroutant : il apparaît en superposition au-dessus du logo AFB au premier Tab. **Écart assumé au critère WCAG 2.4.1 « Contournement de blocs » (niveau A)** — le seul critère de ce niveau non satisfait par le module. Un commentaire dans `AppLayout.jsx` le rappelle pour qu'il ne soit pas réintroduit par inadvertance. *(Vérifié au passage : le fragment `#contenu-principal` ne subsistait pas dans l'URL après navigation, il n'y avait donc pas d'effet de bord sur les URL de filtres de MM.9.)* |

---

## 5. Détail des mesures de performance

Les deux versions ont été construites réellement, avec les mêmes dépendances (`package.json` strictement identique entre `abbd3bb^` et `HEAD`).

| Élément livré | Avant chantier | Après D.4 | Écart |
| --- | --- | --- | --- |
| `index-*.js` | 521,72 kB (158,49 gzip) | 545,35 kB (163,42 gzip) | +23,63 kB (+4,93 gzip) |
| `index-*.css` | 24,64 kB (5,78 gzip) | 30,00 kB (6,74 gzip) | +5,36 kB (+0,96 gzip) |
| Polices woff2 (4 graisses) | — | 62,61 kB | +62,61 kB |
| Emblème du logo (sidebar repliée) | — | 4,68 kB | +4,68 kB |
| **Total** | **565,22 kB** | **642,64 kB** | **+77,42 kB** |

**+77 kB, sous le seuil de ~150 kB fixé par le guide D.4.** L'essentiel de l'augmentation est la police embarquée, contrepartie assumée de la décision 1 (fonctionnement sans accès sortant).

`font-display: swap` est posé sur les quatre déclarations `@font-face`. Quatre fichiers woff2 présents, quatre émis par le build : **aucun fichier de police inutilisé**.

---

## 6. Vérification en navigateur réel

Contrairement aux sprints précédents, D.4 ne s'est pas arrêté à la lecture du code. Les cinq rôles ont été parcourus dans un navigateur piloté, authentifiés par le vrai flux Keycloak (Authorization Code + PKCE).

| Contrôle | Résultat |
| --- | --- |
| Défilement horizontal de la page, 4 écrans × 5 largeurs (1920 / 1440 / 1280 / 1024 / 768) | **Aucun**, sur les 20 combinaisons |
| Tableau dense à 1024 px | 763 px de contenu dans un cadre de 702 px → **défile dans son cadre**, la page ne bouge pas |
| Parcours clavier sur une liste dense (35 tabulations) | **0 focus invisible, 0 contrôle sans nom accessible**, ordre de tabulation conforme à l'ordre visuel |
| Modale `ConfirmDialog` | `aria-modal=true`, `aria-labelledby` → **`H2`**, `overscroll-behavior: contain`, focus initial correct, piège de focus étanche sur 6 tabulations, Échap ferme et **rend le focus au déclencheur** |
| Sidebar repliée (80 px) | Les 5 liens et les 2 boutons portent tous un nom accessible |
| Non-régression fonctionnelle | 5 rôles, 17 écrans, **aucune erreur console, aucune exception** |

Un défaut n'a été trouvé que par ce parcours réel : le bouton de compte était annoncé « MBARGAARH », le nom et le rôle étant deux `<span>` adjacents sans séparation lisible. Corrigé par un `aria-label` explicite.

---

## 7. Ce qui reste figé

**Avant toute commande Impeccable ultérieure, lire `DESIGN.md`.** Rappel de ce qui n'est pas négociable, par ordre de sensibilité :

1. **Couleur de marque `#E30613`** (`--color-primary-500`). Aucune alternative, aucune nuance de remplacement, aucun dégradé, aucune « version plus moderne », aucune désaturation. Les 22 tokens `--color-primary-*` et `--color-neutral-*` de `frontend/src/index.css` sont **strictement inchangés** depuis avant le chantier — vérifié par `git diff` en clôture de D.4.
2. **Rôles de `ProtectedRoute` et de `NAV_LINKS`** — verrou de l'audit 6F.9. Le bloc `NAV_LINKS` est **strictement identique** depuis le début du chantier design ; `frontend/src/router/` n'a reçu aucune modification. Les rôles de `NAV_LINKS` doivent rester alignés au rôle près sur ceux du `ProtectedRoute` cible.
3. **Navigation latérale verticale**, fond sombre `neutral-950`, texte et icônes blancs. Jamais un header horizontal, jamais une navigation escamotable qui supprimerait la colonne.
4. **Logo AFB** en haut de la barre de navigation. Sa position et sa présence ne se discutent pas.
5. **Fonds** : contenu principal en blanc ou `#F5F5F5`. Pas de fond coloré, pas de mode sombre.
6. **Filigrane** limité à la zone de contenu (`.fond-filigrane` sur `<main>` dans `AppLayout.jsx`). Jamais sur la sidebar, jamais sur les cartes ni les en-têtes.
7. **Registre sobre.** DOTTEL est un outil bancaire interne. Toute suggestion visant à le rendre « plus audacieux, plus mémorable, plus différenciant » est hors périmètre par construction.
8. **Aucun fichier sous `backend/`.** Vérifié sur toute la durée du chantier design : `git diff 703d389 -- backend` est vide.

---

**Fin du chantier design frontend — D.0 à D.4.**

*La charte graphique de `CLAUDE.md` section 15 reste la référence. En cas de contradiction entre une suggestion d'outil et cette section, c'est la section 15 qui gagne, sans exception et sans arbitrage.*
