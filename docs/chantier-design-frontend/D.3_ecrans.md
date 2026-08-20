# SPRINT D.3

## Les écrans — hiérarchie, densité, états

*Module Dotations Téléphoniques Mensuelles — Frontend*

| | |
|---|---|
| **Objet** | Porter les 29 écrans au niveau de finition des composants, par groupes fonctionnels |
| **Livrable** | Écrans révisés : hiérarchie visuelle, états vides, états de chargement, cohérence |
| **Durée** | Deux jours |
| **Prérequis** | D.2 validé — composants et layout révisés |

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Critique par groupe (étape 2) | Sonnet 5 | Medium à High |
| Écrans de workflow (étape 3) | Sonnet 5 ou Opus 4.8 | **High** |
| Écrans de liste (étape 4) | Sonnet 5 | Medium |
| Écrans de formulaire (étape 5) | Sonnet 5 | Medium |
| Écrans de reporting (étape 6) | Sonnet 5 | Medium |
| Écrans d'authentification et bords (étape 7) | Sonnet 5 | Medium |

Les écrans de workflow méritent High : ce sont ceux où un ARH, un CRH ou une DRH engagent un paiement réel. Une hiérarchie visuelle ambiguë y a un coût.

## 0. Nouvelle session : rappel Graphify

```
py -3.14 -m graphify update .
```

---

## 1. Contexte

### 1.1 Traiter par groupe, jamais écran par écran isolément

29 écrans, c'est trop pour un passage linéaire — et surtout, la cohérence se joue **entre** écrans d'un même groupe. Un utilisateur qui passe de la liste des processus à celle des grilles doit retrouver les mêmes repères.

### 1.2 Les 6 groupes

| Groupe | Écrans | Enjeu dominant |
|---|---|---|
| **Workflow** | `ProcessusListPage`, `ProcessusDetailPage`, `DeclencherProcessusPage`, `AjusterLignesModal`, `RetournerProcessusModal` | Lisibilité de l'état d'avancement, poids visuel des actions engageantes |
| **Listes** | `Beneficiaires`, `GrillesListPage`, `UtilisateursListPage`, `FonctionsEligiblesListPage` | Densité, filtres, états vides |
| **Formulaires** | `CreerGrillePage`, `CreerUtilisateurPage`, `CreerFonctionPage`, `ModifierGrilleModal`, `ModifierFonctionModal`, `ModifierBeneficiaireModal`, `ValiderGrilleModal` | Groupement des champs, erreurs, actions |
| **Reporting** | `DashboardPage`, `HistoriquePage`, `AuditPage`, `HistoriqueGrillePage` | Hiérarchie de l'information, lecture en balayage |
| **Enrôlement** | `VerifierMatriculePage`, `ConfirmerEnrolementPage`, `ImporterBeneficiairesPage` | Parcours guidé, retour d'information |
| **Bords** | `Login`, `AccesInterdit`, `Admin`, `GrillesTarifairesValider` | Première impression, états d'exception |

### 1.3 Un défaut connu, hérité de l'audit 6F.9

`AccesInterdit.jsx` est un `<div>Accès interdit</div>` nu — sans mise en forme, sans lien de retour, hors charte. Repéré comme écart **E5** au Sprint 6F.9, jugé cosmétique et reporté. **Ce chantier est l'endroit où le corriger.**

### 1.4 Ce qui est figé

- Les rôles de `ProtectedRoute` et de `NAV_LINKS` — verrou de l'audit 6F.9.
- Les couleurs de marque, la sidebar, le logo — charte §15.
- **Tout comportement fonctionnel** : appels API, paramètres d'URL, règles métier. Ce sprint change l'apparence, pas ce que fait l'application.

Attention particulière : les écrans de liste portent depuis MM.9 la **persistance des filtres par query params** (`useSearchParams`, `definirParametre`, `construireRetour`) et la propagation de `state.retour` vers `LienRetour`. **Ne casse pas cette mécanique en réorganisant le JSX.**

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
1. Lis CLAUDE.md SECTION 15 (charte) et SECTION 8 (roles par endpoint).
2. Lis DESIGN.md (garde-fou de D.0).
3. Lis docs/chantier-design-frontend/D.3_ecrans.md
4. Lance /graphify . --update

CONTEXTE : Sprint D.3, les 29 ecrans, traites par GROUPE fonctionnel.

INTERDITS ABSOLUS :
  - modifier un role dans ProtectedRoute ou NAV_LINKS (audit 6F.9)
  - modifier un appel API, un parametre de requete, une regle metier
  - casser la persistance des filtres par query params installee en
    MM.9 (useSearchParams, definirParametre, construireRetour,
    propagation de state.retour vers LienRetour)
  - toucher aux couleurs de marque, a la sidebar, au logo

METHODE : un GROUPE a la fois, et dans chaque groupe un ECRAN a la
fois. Tu montres le diff, j'approuve, tu continues. Jamais de passage
global.

REGLE DE PRUDENCE : avant d'editer un ecran, LIS-LE en entier. La
plupart ont deja ete touches par MM.9 (filtres URL) et certains par
MM.11 ou MM.12. Ne repars jamais d'une version supposee.

BACKEND INTERDIT : aucun fichier sous backend/.

PREMIERE ACTION : la verification d'espace de travail, puis l'etape 2.
```

---

## 3. Étape 2. Critique par groupe, avant toute modification

```
Pour CHACUN des 6 groupes, dans l'ordre du guide :

1. Lance la critique Impeccable sur les fichiers du groupe :

    /impeccable critique <chemins des fichiers du groupe>

2. Produis-moi une fiche par groupe :

   | Ecran | Etat vide ? | Etat chargement ? | Etat erreur ? | Hierarchie claire ? | Cartes imbriquees ? |

   Rempli par LECTURE du code.

3. Signale separement :
   - les incoherences ENTRE ecrans d'un meme groupe (deux listes qui
     placent leurs filtres differemment, deux formulaires qui
     n'alignent pas leurs actions au meme endroit...)
   - toute proposition de l'outil touchant a la CHARTE -> tu me la
     signales, tu ne l'appliques pas

NE MODIFIE RIEN A CETTE ETAPE. Montre-moi les 6 fiches, on decide
ensemble par quoi commencer et ce qu'on laisse tel quel.
```

---

## 4. Étape 3. Groupe Workflow

Le plus sensible : ces écrans engagent des paiements.

```
Ecrans : ProcessusListPage, ProcessusDetailPage, DeclencherProcessusPage,
AjusterLignesModal, RetournerProcessusModal.

POINTS A TRAITER :

  a) PROCESSUSDETAILPAGE -- la TimelineWorkflow (les 3 pastilles
     ARH/CRH/DRH). C'est l'element le plus informatif de
     l'application : il dit ou en est un paiement.
     Verifie qu'on distingue INSTANTANEMENT : etape franchie, etape
     courante, etape a venir, et etape en alerte (statut RETOURNE).
     Le code utilise deja des couleurs distinctes -- verifie les
     CONTRASTES et si l'information passe SANS la couleur seule (un
     daltonien doit comprendre). Ajoute une forme ou une icone
     distinctive si necessaire.

  b) POIDS VISUEL DES ACTIONS. Sur ProcessusDetailPage cohabitent
     "Telecharger le PDF", "Ajuster les lignes", "Voir le motif de
     retour", "Retourner" et "Valider".
     "Valider" engage un paiement. "Retourner" renvoie le dossier.
     Verifie que leur poids visuel reflete leur portee -- et que
     "Retourner" (destructive) ne se confond pas avec "Valider"
     (primaire), les deux etant rouges dans la charte.
     C'EST UN VRAI RISQUE : signale-le-moi avec ta proposition si tu
     le confirmes.

  c) AJUSTERLIGNESMODAL -- retouche en MM.9 (toggle de selection
     groupee + filtre nom/matricule). LIS-LE avant d'editer.
     Verifie que le toggle et le filtre sont visuellement clairs, et
     que l'utilisateur comprend que le filtre n'affecte QUE
     l'affichage, jamais la soumission. C'est une subtilite reelle du
     comportement -- une mention discrete peut eviter une erreur.

  d) DECLENCHERPROCESSUSPAGE -- touche en MM.11 (restriction de
     periode + mode rattrapage). Verifie que le mode rattrapage est
     visuellement DISTINCT d'un declenchement normal : c'est une
     operation differente sur un mois deja paye.

  e) ETATS DE CHARGEMENT : EnTeteSquelette existe deja sur
     ProcessusDetailPage. Verifie la coherence avec les autres ecrans
     du groupe.
```

---

## 5. Étape 4. Groupe Listes

```
Ecrans : Beneficiaires, GrillesListPage, UtilisateursListPage,
FonctionsEligiblesListPage.

ATTENTION -- tous ont ete touches en MM.9 (filtres portes par l'URL).
LIS chaque fichier avant d'editer. Ne casse ni useSearchParams, ni
definirParametre, ni la propagation de state.retour.

POINTS A TRAITER :

  a) COHERENCE DE LA BARRE DE FILTRES entre les 4 ecrans : meme
     position, meme espacement, meme comportement. Beneficiaires en a
     4 (fonction, recherche, unite, statut) avec des largeurs ajustees
     en MM.9 -- prends-le comme reference et aligne les autres si
     l'ecart est visible.

  b) ETAT VIDE, deux cas a DISTINGUER :
       - aucune donnee du tout ("aucun beneficiaire enrole")
       - aucune donnee POUR CE FILTRE ("aucun resultat pour cette
         recherche") + un moyen d'effacer les filtres
     Les confondre laisse l'utilisateur croire que la base est vide.

  c) PAGINATION (Beneficiaires, pagine cote serveur) : position,
     lisibilite, etat desactive des fleches en bord de plage.

  d) ACTIONS DE LIGNE : verifie leur lisibilite et leur zone
     cliquable. Rappel : certaines sont conditionnees par role et
     statut (ex. GrillesListPage n'affiche "modifier" que pour l'ARH
     sur une grille en attente). NE TOUCHE PAS a ces conditions --
     c'est du controle d'acces, pas du style.
```

---

## 6. Étape 5. Groupe Formulaires

```
Ecrans : CreerGrillePage, CreerUtilisateurPage, CreerFonctionPage,
ModifierGrilleModal, ModifierFonctionModal, ModifierBeneficiaireModal,
ValiderGrilleModal.

POINTS A TRAITER :

  a) COHERENCE DE LA POSITION DES ACTIONS. Verifie que "Annuler" et
     l'action principale sont au meme endroit et dans le meme ordre
     partout. Un utilisateur qui apprend un formulaire doit connaitre
     les six autres.

  b) GROUPEMENT DES CHAMPS et rythme vertical. CreerUtilisateurPage a
     6 champs -- verifie qu'ils respirent sans s'etaler.

  c) INDICATION DES CHAMPS OBLIGATOIRES. RetournerProcessusModal
     utilise deja un asterisque rouge sur son libelle. Verifie si
     c'est coherent partout, ou si certains formulaires n'indiquent
     rien.

  d) MESSAGES D'ERREUR : position, couleur, et surtout distinction
     avec le rouge de marque (point souleve en D.2).

  e) ETAT DE SOUMISSION : plusieurs pages changent le libelle du
     bouton ("Creation en cours..."). Si D.2 a introduit une variante
     de chargement sur Button, applique-la ici de facon coherente.

  f) MODALES : verifie que les corrections d'accessibilite de D.2
     (Echap, piege de focus) s'appliquent bien aux 4 modales de
     formulaire.

NE TOUCHE PAS aux schemas zod, aux appels apiClient, ni aux
conditions de role.
```

---

## 7. Étape 6. Groupe Reporting

```
Ecrans : DashboardPage, HistoriquePage, AuditPage, HistoriqueGrillePage.

POINTS A TRAITER :

  a) DASHBOARDPAGE -- c'est la page d'atterrissage de l'ARH et de la
     DRH apres connexion. C'est la PREMIERE IMPRESSION quotidienne de
     l'application.
     Verifie la hierarchie : qu'est-ce qu'on doit voir en premier ?
     Les cartes de synthese (CarteSynthese) doivent avoir un ordre de
     lecture evident. CarteSquelette existe deja pour le chargement.
     Verifie aussi les CHIFFRES : alignement, chiffres tabulaires,
     unite (FCFA) lisible sans ambiguite.

  b) AUDITPAGE -- journal d'audit, reserve DRH. Beaucoup de lignes,
     beaucoup de colonnes, filtres multiples.
     Enjeu : la DENSITE. C'est un ecran de consultation en balayage.
     Verifie la lisibilite des dates, des actions, et du detail JSON
     s'il est affiche.

  c) HISTORIQUEPAGE et HISTORIQUEGRILLEPAGE : lecture chronologique.
     Verifie que l'ordre (plus recent d'abord) est visuellement
     evident, et que les montants s'alignent.

  d) COHERENCE des formats de date entre les 4 ecrans (le projet a
     des utilitaires dans utils/formatters.js -- verifie qu'ils sont
     utilises partout plutot que du formatage inline).
```

---

## 8. Étape 7. Groupe Enrôlement et Bords

```
ENROLEMENT : VerifierMatriculePage, ConfirmerEnrolementPage,
ImporterBeneficiairesPage.

  a) Le parcours en 2 temps (verifier puis confirmer) doit se LIRE
     comme un parcours : l'utilisateur doit savoir ou il en est.
  b) VerifierMatriculePage affiche un resultat d'eligibilite
     (eligible / non eligible). Verifie que le refus est CLAIR et
     explique -- un employe non eligible doit comprendre pourquoi.
  c) ImporterBeneficiairesPage : le rapport d'import (inseres,
     rejetes, erreurs par ligne) est l'element cle. Verifie qu'un
     import partiellement rejete est lisible ligne par ligne.
     RG-11 : les lignes valides sont inserees meme si d'autres sont
     rejetees -- l'ecran doit rendre ca evident, sinon l'utilisateur
     croit a un echec total.

BORDS : Login, AccesInterdit, Admin, GrillesTarifairesValider.

  d) LOGIN -- premiere impression de l'application. Verifie le
     centrage, le poids du logo, la lisibilite du message d'erreur.
     La charte s'applique pleinement ici.

  e) ACCESINTERDIT -- aujourd'hui un <div>Acces interdit</div> NU.
     C'est l'ecart E5 de l'audit 6F.9, reporte a ce chantier.
     Refais-le a la charte : mise en page coherente, message
     explicite, et un LIEN DE RETOUR vers une page autorisee.
     ATTENTION : le lien de retour depend du role de l'utilisateur
     connecte (un CRH n'a pas acces a /dashboard depuis la decision
     du 2026-07-30, il atterrit sur /processus -- voir ROUTE_PAR_ROLE
     dans Login.jsx). Propose-moi comment tu determines la
     destination, ne l'invente pas.

  f) ADMIN et GRILLESTARIFAIRESVALIDER : verifie la coherence avec
     les autres ecrans de leur nature.
```

---

## 9. Étape 8. Vérification

```
1. Detecteur, sur l'ensemble :

    npx impeccable detect frontend/src

   Compare a .impeccable/baseline.json (D.0) et a la mesure de D.2.
   Montre-moi la progression.

2. Build :

    cd frontend ; npx oxlint ; npm run build

3. VERROUS -- controle critique :

    git diff frontend/src/router/
    git diff frontend/src/components/layout/Sidebar.jsx

   Confirme-moi que les ROLES sont inchanges partout (ProtectedRoute
   et NAV_LINKS). Verrou de l'audit 6F.9.

4. NON-REGRESSION MM.9 -- controle critique :
   Confirme que sur les 4 pages de liste, useSearchParams,
   definirParametre et la propagation de state.retour sont INTACTS.
   Montre-moi les lignes concernees.

5. Backend intact :

    git status --short -- backend
   Doit etre VIDE.
```

---

## 10. Vérification visuelle — à faire par l'utilisateur

**C'est le sprint où la validation humaine compte le plus.** Parcours complet, avec le backend lancé :

1. **Connexion en ARH** (`1847`/`Test1234`) → le tableau de bord doit se lire immédiatement.
2. **Cycle de workflow** : déclencher un processus, ajuster des lignes, valider. Puis CRH, puis DRH. À chaque écran, la timeline doit dire clairement où on en est.
3. **Test daltonien** : sur `ProcessusDetailPage`, l'état des 3 étapes est-il compréhensible **sans** la couleur ? (Un filtre de simulation de daltonisme existe dans les DevTools de Chrome, onglet Rendering.)
4. **Confusion Valider / Retourner** : sur un processus en attente CRH, les deux boutons sont-ils clairement distincts ?
5. **États vides** : filtre une liste sur quelque chose d'inexistant → message clair distinguant « rien du tout » de « rien pour ce filtre ».
6. **Non-régression MM.9** : pose des filtres, entre dans un détail, reviens par le lien de retour → les filtres sont toujours là. Recharge la page → toujours là.
7. **Import Excel partiellement rejeté** : le rapport doit montrer clairement que certaines lignes sont passées et d'autres non.
8. **Accès interdit** : connecte-toi en CRH et va sur `/dashboard` → l'écran doit maintenant être présentable et proposer un retour vers `/processus`.
9. **Sidebar et rouge de marque** : inchangés sur tous les écrans.

---

## 11. Critères de validation

| Élément | Statut attendu |
|---|---|
| 6 fiches de critique produites **avant** toute modification | Fait |
| Traitement **par groupe**, un écran à la fois, jamais de passage global | Vérifié |
| Timeline du workflow lisible **sans la couleur seule** | Vérifié en simulation daltonisme |
| Distinction visuelle **Valider / Retourner** traitée ou signalée | Fait |
| États vides distinguant « aucune donnée » de « aucun résultat pour ce filtre » | Vérifié à l'écran |
| Cohérence de la barre de filtres entre les 4 listes | Vérifié |
| Position et ordre des actions cohérents entre les 7 formulaires | Vérifié |
| Champs obligatoires indiqués de façon cohérente | Vérifié |
| Chiffres et montants alignés, chiffres tabulaires, unité lisible | Vérifié |
| Formats de date issus de `utils/formatters.js`, pas de formatage inline | Vérifié |
| Rapport d'import : RG-11 rendue évidente (lignes valides insérées malgré des rejets) | Vérifié à l'écran |
| **`AccesInterdit` refait à la charte** avec lien de retour dépendant du rôle (écart E5 de 6F.9) | Vérifié |
| Destination du lien de retour d'`AccesInterdit` validée **avec l'utilisateur** | Fait |
| **Rôles de `ProtectedRoute` et `NAV_LINKS` strictement inchangés** | Vérifié sur `git diff` |
| **Persistance des filtres MM.9 intacte** sur les 4 listes | Vérifié sur code + à l'écran |
| Aucun appel API, paramètre de requête ou règle métier modifié | Vérifié |
| Aucune condition de rôle d'action de ligne modifiée | Vérifié |
| *Findings* du détecteur en baisse par rapport à D.2 | Vérifié |
| `npx oxlint` et `npm run build` OK | Vérifié |
| **Aucun fichier sous `backend/` modifié** | Vérifié |

---

## Commit

```bash
git add .
git commit -m "d.3: ecrans -- hierarchie, etats vides, coherence par groupe, AccesInterdit a la charte"
```

---

**Fin du Sprint D.3** — *en attente de validation avant D.4*
