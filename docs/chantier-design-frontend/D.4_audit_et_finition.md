# SPRINT D.4

## Audit final — accessibilité, responsive, performance, clôture

*Module Dotations Téléphoniques Mensuelles — Frontend*

| | |
|---|---|
| **Objet** | Vérifier l'ensemble du frontend contre des critères objectifs, corriger le reste, et clore le chantier par un récapitulatif |
| **Livrable** | Détecteur au plus bas, audit d'accessibilité, `RECAPITULATIF_DESIGN.md` |
| **Durée** | Une journée |
| **Prérequis** | D.3 validé |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Audit technique croisé (étape 2) | Sonnet 5 | **High** |
| Accessibilité (étape 3) | Sonnet 5 ou Opus 4.8 | **High** |
| Responsive (étape 4) | Sonnet 5 | Medium |
| Performance (étape 5) | Sonnet 5 | Medium |
| Polish et récapitulatif (étapes 6-7) | Sonnet 5 | Medium |

## 0. Nouvelle session : rappel Graphify

```
/graphify .
```

Cartographie **complète** : c'est la photo finale du frontend après quatre sprints de retouches.

---

## 1. Contexte

D.1 à D.3 ont travaillé par zones. Ce sprint vérifie l'ensemble contre des critères **objectifs et mesurables** — pas contre du goût.

### 1.1 Trois outils complémentaires, à croiser

| Outil | Ce qu'il vérifie | Nature |
|---|---|---|
| `npx impeccable detect` | 59 règles déterministes : anti-patterns d'IA, qualité de design | Sans IA, reproductible |
| `/impeccable audit` | Accessibilité, performance, responsive | Avec IA, contextuel |
| **`web-design-guidelines`** (skill Vercel déjà installé avant ce chantier) | Conformité aux *Web Interface Guidelines* | Avec IA, référentiel externe |

Les croiser a une vraie valeur : les trois ne signalent pas les mêmes choses, et un défaut vu par deux outils différents mérite d'être traité en priorité.

### 1.2 Ce que ce sprint ne fait pas

Il ne **redessine** rien. Si un problème de fond apparaît, il est signalé et arbitré — pas corrigé en catimini dans un sprint de clôture.

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
1. Lis CLAUDE.md SECTION 15 (charte).
2. Lis DESIGN.md (garde-fou de D.0).
3. Lis docs/chantier-design-frontend/D.4_audit_et_finition.md
4. Lance /graphify .  (cartographie COMPLETE, pas --update)

CONTEXTE : Sprint D.4, cloture du chantier design. Audit croise,
corrections, recapitulatif.

REGLE DE CE SPRINT : si un audit revele un probleme de FOND
(reorganisation d'un ecran, changement de parcours), tu le SIGNALES,
tu ne le corriges pas. Un sprint de cloture ne redessine pas.

INTERDITS INCHANGES : roles de ProtectedRoute et NAV_LINKS (audit
6F.9), couleurs de marque, sidebar, logo, appels API, regles metier,
persistance des filtres MM.9. Aucun fichier sous backend/.

PREMIERE ACTION : la verification d'espace de travail, puis l'etape 2.
```

---

## 3. Étape 2. Audit technique croisé

```
Lance les TROIS outils et croise leurs resultats.

1. Detecteur deterministe, sur tout le frontend :

    npx impeccable detect frontend/src

   Compare a .impeccable/baseline.json enregistre en D.0.
   Donne-moi un tableau : regle | findings en D.0 | findings
   maintenant | ecart.

2. Audit Impeccable :

    /impeccable audit frontend/src

3. Skill Vercel deja installe :

    Utilise web-design-guidelines pour une revue d'interface du
    frontend.

4. CROISE les trois. Produis-moi UNE liste unique, classee :

     (P1) signale par 2 outils ou plus -> traiter en priorite
     (P2) signale par 1 outil, correction locale -> traiter
     (P3) signale par 1 outil, mais c'est une decision de CHARTE ->
          ne pas traiter, documenter le motif
     (P4) probleme de FOND (reorganisation) -> SIGNALER, ne pas
          corriger dans ce sprint

   Pour chaque entree : le fichier, la ligne, et ce qui est propose.

NE CORRIGE RIEN A CETTE ETAPE. Montre-moi la liste croisee, on decide
ensemble de l'ordre.
```

---

## 4. Étape 3. Accessibilité

L'enjeu le plus concret du sprint : ce sont des postes de travail bancaires, utilisés toute la journée, parfois au clavier uniquement.

```
Verifie ces points sur l'ensemble du frontend. Pour chacun, dis-moi
ce que tu as REELLEMENT trouve, pas ce qui devrait etre.

  a) CONTRASTES. Verifie le ratio de chaque combinaison
     texte/fond reellement utilisee. Cible : 4.5:1 pour le texte
     courant, 3:1 pour le texte large et les elements d'interface.
     Points a controler en priorite :
       - texte blanc sur #E30613 (boutons primaires)
       - texte sur chaque variant de Badge (les statuts)
       - texte neutral-400/500 sur fond blanc (texte secondaire)
       - texte sur la sidebar neutral-950
     SI un contraste imposé par la CHARTE echoue, SIGNALE-LE-MOI
     avec la mesure exacte -- ne modifie pas une couleur de marque.
     Il existe des solutions qui respectent la charte (augmenter la
     graisse, la taille, ou choisir une autre nuance de l'echelle).

  b) NAVIGATION CLAVIER complete. Parcours reel : connexion,
     navigation laterale, une liste avec filtres, un formulaire, une
     modale. Verifie qu'aucun piege ne bloque, que l'ordre de
     tabulation suit l'ordre visuel, et que le focus est TOUJOURS
     visible.

  c) SEMANTIQUE. Hierarchie des titres (pas de niveau saute -- le
     detecteur a une regle dessus), <table> correctement structuree,
     <button> vs <a> utilises a bon escient, landmarks (<nav>,
     <main>, <aside>).

  d) LECTEUR D'ECRAN. Verifie que les icones seules (lucide-react)
     ont un texte accessible. Cas concret : les boutons d'action de
     ligne dans DataTable, et les icones de la sidebar en mode
     replie.

  e) FORMULAIRES : chaque champ a un <label> associe, les erreurs
     sont reliees au champ (aria-describedby), les champs invalides
     portent aria-invalid.

  f) lang="fr" est bien en place (corrige en D.1). Verifie qu'aucun
     texte anglais residuel ne subsiste dans l'interface.

Traite les corrections une par une, en me montrant chaque diff.
```

---

## 5. Étape 4. Responsive

```
DOTTEL est un outil de poste de travail : le responsive n'est pas la
priorite d'un usage mobile, mais l'application doit rester utilisable
sur un ecran etroit ou une fenetre reduite.

Verifie a ces largeurs : 1920, 1440, 1280, 1024, 768.

  a) SIDEBAR : elle a deja un mode replie (w-20 / w-64) pilote
     manuellement. En dessous de 1024, faut-il un repli automatique ?
     PROPOSE, ne tranche pas -- c'est un changement de comportement.

  b) TABLEAUX : DataTable est le point de tension. Que se passe-t-il
     a 1024 avec 8 colonnes ? Debordement horizontal maitrise
     (conteneur en overflow-x-auto) ou mise en page cassee ?
     La regle : le tableau defile dans SON conteneur, la page ne
     defile jamais horizontalement.

  c) BARRES DE FILTRES : Beneficiaires en a 4 cote a cote avec des
     largeurs fixes (w-48, w-52, w-40). Verifie leur comportement
     quand la place manque -- passage a la ligne propre plutot
     qu'ecrasement.

  d) MODALES : max-w-md / max-w-lg avec p-4 sur le conteneur. Verifie
     sur ecran etroit et avec un contenu long (la modale doit
     defiler, pas deborder).

  e) TABLEAU DE BORD : les cartes de synthese doivent se reorganiser
     proprement.
```

---

## 6. Étape 5. Performance

```
Mesure d'abord, optimise ensuite -- et seulement si la mesure le
justifie.

  a) TAILLE DU BUNDLE :

        cd frontend ; npm run build

     Le build signalait deja "Some chunks are larger than 500 kB"
     AVANT ce chantier (environ 505 Ko / 153 Ko gzip). Compare
     l'etat actuel. Les polices ajoutees en D.1 s'ajoutent au poids
     livre.
     Si l'augmentation depasse ~150 Ko, signale-le.

  b) DEPENDANCES INUTILISEES. L'audit 6F.9 avait releve que
     jwt-decode et react-hot-toast sont declarees dans package.json
     mais utilisees NULLE PART dans src/.
     Verifie si c'est toujours vrai. Si oui, PROPOSE leur retrait --
     ne les supprime pas seul, ce sont des dependances declarees.

  c) DECOUPAGE DU CODE. 29 pages chargees en un seul paquet. Le
     chargement paresseux par route (React.lazy + Suspense) est
     l'optimisation la plus evidente.
     ATTENTION : c'est un changement structurel du routage, et
     AppRouter porte les roles de ProtectedRoute -- verrou de l'audit
     6F.9. PROPOSE-LE avec son cout et son risque, ne l'implemente
     pas sans mon accord explicite.

  d) POLICES : verifie que font-display: swap est bien pose et
     qu'aucun fichier de police inutilise n'est embarque.

Ne fais AUCUNE optimisation qui ne soit pas justifiee par une mesure
que tu me montres.
```

---

## 7. Étape 6. Polish final

```
    /impeccable polish frontend/src

Passage final de coherence. RAPPEL DU CADRE : polish ne redessine
pas. Toute proposition touchant a la charte est signalee, jamais
appliquee.

Traite ensuite les entrees P1 et P2 restantes de la liste croisee de
l'etape 2, une par une.

Les entrees P3 (decisions de charte) doivent etre ajoutees aux
ignores du detecteur avec leur motif, s'il en reste :

    npx impeccable ignores add-value <regle> <valeur> --reason "charte AFB, CLAUDE.md section 15"

Les entrees P4 (problemes de fond) sont listees pour le
recapitulatif, PAS corrigees.
```

---

## 8. Étape 7. Récapitulatif de fin de chantier

```
Produis docs/chantier-design-frontend/RECAPITULATIF_DESIGN.md,
au format des recapitulatifs deja produits dans ce projet
(voir docs/audit_securite_owasp_v1.md comme reference de style).

Contenu :

1. TABLEAU AVANT / APRES, avec des mesures reelles :
   | Indicateur | Avant (D.0) | Apres |
   - findings du detecteur, total et par regle
   - police : pile systeme / la police retenue
   - lang de <html> : en / fr
   - titre du document
   - contrastes en echec : nombre avant/apres
   - taille du bundle
   - composants avec focus visible : X/18
   - ecrans avec etat vide traite : X/29

2. DECISIONS PRISES pendant le chantier, avec leur sprint :
   - police retenue et pourquoi (D.1)
   - titre du document (D.1)
   - traitement de la confusion rouge marque / rouge erreur (D.2)
   - ajout eventuel de @radix-ui/react-dialog (D.2)
   - destination du lien de retour d'AccesInterdit (D.3)
   - toute proposition d'outil REFUSEE au nom de la charte

3. REGLES DE DETECTION IGNOREES, avec le motif de chacune. C'est la
   liste la plus importante pour un futur intervenant : elle dit ce
   qui est un choix assume et non un oubli.

4. POINTS LAISSES OUVERTS (les P4), et pourquoi ils n'ont pas ete
   traites.

5. CE QUI RESTE FIGE : rappel de la charte, pour que le prochain qui
   lance une commande Impeccable sache immediatement ce qu'il ne doit
   pas toucher.
```

---

## 9. Étape 8. Vérification finale

```
1. Detecteur :

    npx impeccable detect frontend/src

   Montre-moi la comparaison complete avec baseline.json (D.0).

2. Build :

    cd frontend ; npx oxlint ; npm run build

3. VERROUS -- controle critique final :

    git diff frontend/src/router/
    git diff frontend/src/components/layout/Sidebar.jsx

   Confirme que les ROLES sont inchanges depuis le debut du chantier
   (ProtectedRoute et NAV_LINKS). Verrou de l'audit 6F.9.

4. CHARTE -- controle critique final :
   Confirme que dans frontend/src/index.css, les tokens
   --color-primary-* et --color-neutral-* sont STRICTEMENT identiques
   a leur valeur d'avant le chantier. Montre-moi le diff de ce
   fichier depuis le premier commit du chantier.

5. Backend intact :

    git status --short -- backend
   Doit etre VIDE sur toute la duree du chantier.

6. Propose-moi une mise a jour de CLAUDE.md section 15 pour y
   consigner la police retenue (la section dit aujourd'hui seulement
   "typographie sans-serif sobre", sans nommer de police -- c'est
   desormais une decision prise).
   MONTRE-MOI la proposition, NE MODIFIE PAS CLAUDE.md sans mon
   accord : c'est le document normatif du projet.
```

---

## 10. Vérification visuelle — à faire par l'utilisateur

1. **Parcours complet au clavier uniquement**, sans toucher la souris : connexion → sidebar → une liste → un formulaire → une modale → validation. Aucun blocage, focus toujours visible.
2. **Simulation daltonisme** (DevTools Chrome → Rendering → Emulate vision deficiencies) sur `ProcessusDetailPage` et sur les badges de statut.
3. **Redimensionnement progressif** de la fenêtre de 1920 à 1024 sur une liste dense : la page ne doit **jamais** défiler horizontalement.
4. **Zoom navigateur à 150 %** sur un écran de formulaire : rien ne doit se chevaucher.
5. **Hors ligne** : recharge avec le réseau coupé — la police s'affiche toujours.
6. **Comparaison avant/après** : ouvre le projet d'origine (`afb-dottel`) côte à côte et compare le même écran. La charte doit être **identiquement respectée** dans les deux, seule la finition doit différer.

---

## 11. Critères de validation

| Élément | Statut attendu |
|---|---|
| Les 3 outils lancés et leurs résultats **croisés** en une liste unique P1-P4 | Fait |
| Entrées P1 et P2 traitées | Vérifié |
| Entrées P3 ajoutées aux ignores **avec motif citant la charte** | Vérifié |
| Entrées P4 (problèmes de fond) **signalées, non corrigées** | Fait |
| Contrastes mesurés sur toutes les combinaisons réellement utilisées | Fait |
| Contraste imposé par la charte en échec : **signalé avec mesure**, couleur de marque non modifiée | Fait |
| Navigation clavier complète sans piège, focus toujours visible | Vérifié au clavier |
| Hiérarchie de titres sans niveau sauté | Vérifié |
| Icônes seules pourvues d'un texte accessible | Vérifié |
| Formulaires : `label`, `aria-describedby`, `aria-invalid` | Vérifié |
| Aucun texte anglais résiduel dans l'interface | Vérifié |
| Aucune page ne défile **horizontalement** à 1024 px | Vérifié à l'écran |
| Repli automatique de la sidebar : **proposé**, non imposé | Fait |
| Taille du bundle mesurée, augmentation signalée si > ~150 Ko | Fait |
| `jwt-decode` / `react-hot-toast` : retrait **proposé**, non exécuté seul | Fait |
| Découpage par route : **proposé avec son risque**, non implémenté sans accord | Fait |
| *Findings* du détecteur au plus bas, comparaison complète avec `baseline.json` | Fait |
| **Rôles `ProtectedRoute` et `NAV_LINKS` inchangés sur toute la durée du chantier** | Vérifié sur `git diff` |
| **Tokens `--color-primary-*` et `--color-neutral-*` strictement inchangés** | Vérifié sur `git diff` |
| Filigrane toujours limité à la zone de contenu | Vérifié à l'écran |
| `RECAPITULATIF_DESIGN.md` produit avec mesures réelles | Fait |
| Mise à jour de `CLAUDE.md` §15 (police retenue) **proposée**, non appliquée sans accord | Fait |
| `npx oxlint` et `npm run build` OK | Vérifié |
| **Aucun fichier sous `backend/` modifié sur toute la durée du chantier** | Vérifié |

---

## Commit

```bash
git add .
git commit -m "d.4: audit croise, accessibilite, responsive, cloture du chantier design"
```

---

**Fin du Sprint D.4 — Fin du chantier design frontend**

*La charte graphique de `CLAUDE.md` section 15 reste la référence. Toute commande Impeccable lancée ultérieurement doit lire `DESIGN.md` avant de proposer quoi que ce soit.*
