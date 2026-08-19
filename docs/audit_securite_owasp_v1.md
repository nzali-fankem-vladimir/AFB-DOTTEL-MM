# RAPPORT D'AUDIT DE SÉCURITÉ OWASP

Module : Digitalisation des Dotations Téléphoniques Mensuelles — Frontend

*Projet AFRILAND HORIZON 2030 — Module INTRA*

| **Référence** | **AFB_AUDIT_DOTTEL_V1_2026** |
| --- | --- |
| Version | 1.0 |
| Date | 30 juillet 2026 |
| Sprint | 6F.9 — dernier sous-sprint touchant au code avant le Sprint 7 (recette) |
| Périmètre | Frontend React construit aux Sprints 6F.1 à 6F.8, et par extension la configuration backend qui le conditionne (CORS, secrets, transport, messages d'erreur) |
| Méthode | Revue manuelle systématique du code réel, catégorie OWASP par catégorie. Aucune supposition : chaque point est vérifié dans les fichiers. |
| Base de comparaison | `CLAUDE.md` (référence normative) et `docs/reference/contrats_api_dotations_v3.md` V3.3 |

---

## 0. Synthèse

**16 points vérifiés. 9 écarts relevés, dont 4 à corriger. 7 correctifs appliqués, 5 écarts explicitement reportés.**

Le socle de sécurité du frontend est sain : le token JWT n'est jamais persisté ni journalisé, aucune primitive d'injection HTML n'est employée, et le contrôle d'accès par rôle est aligné au rôle près sur la matrice de `CLAUDE.md` section 8.

Les écarts significatifs ne portaient pas sur le code applicatif mais sur la **configuration de production**, jamais exercée jusqu'ici puisque le module n'a tourné qu'en développement : secret JWT non résolvable en profil `prod`, origine CORS figée sur `localhost`, et build frontend qui aurait embarqué une URL d'API en HTTP clair.

| Catégorie OWASP | Points | Conforme | Écart mineur | Écart corrigé |
| --- | --- | --- | --- | --- |
| A01 — Contrôle d'accès | 4 | 3 | 3 | 1 |
| A03 — Injection | 3 | 3 | 0 | 0 |
| A05 — Mauvaise configuration | 5 | 2 | 2 | 3 |
| A06 — Composants vulnérables | 2 | 1 | 0 | 1 |
| A07 — Authentification | 2 | 2 | 0 | 0 |
| A09 — Journalisation | 1 | 1 | 1 | 0 |

---

## 1. Tableau récapitulatif

| # | Point vérifié | OWASP | Statut | Action prise |
| --- | --- | --- | --- | --- |
| 1 | Stockage du token JWT | A01/A07 | **Conforme** | Aucune |
| 2 | Token jamais journalisé en console | A07/A09 | **Conforme** | Aucune |
| 3 | Token jamais exposé dans l'URL | A01 | **Conforme** | Aucune |
| 4 | Token jamais dans un message d'erreur | A07 | **Conforme** | Aucune |
| 5 | Déconnexion efface le token | A07 | **Conforme** | Aucune |
| 6 | Absence de `dangerouslySetInnerHTML` | A03 | **Conforme** | Aucune |
| 7 | Échappement des entrées utilisateur | A03 | **Conforme** | Aucune |
| 8 | Encodage des paramètres de requête | A03 | **Conforme** | Aucune |
| 9 | Rôles des routes alignés sur le backend | A01 | **Écart à corriger** | **E1 corrigé** |
| 10 | Aucune donnée chargée avant contrôle du rôle | A01 | **Conforme** | Aucune |
| 11 | Liens sidebar cohérents avec les rôles | A01 | **Conforme** | Aucune |
| 12 | Aucun détail technique dans les erreurs | A05/A09 | **Conforme** | **E6 corrigé** (préfixe inerte) |
| 13 | Vulnérabilités des dépendances | A06 | **Écart à corriger** | **V1 corrigé**, V2/V3 reportés |
| 14 | Source maps désactivées en production | A05 | **Conforme** | Aucune |
| 15 | HTTPS en production | A05 | **Écart à corriger** | **E7c corrigé** |
| 16 | CORS non permissif et secrets externalisés | A05 | **Écart à corriger** | **E7b, E7d, E7e, E8 corrigés** |

---

## 2. Détail par catégorie

### 2.1 A01/A07 — Stockage et exposition du token

**Conforme sur les 5 points.**

Le token est conservé **en mémoire seule**, dans une variable de module de `frontend/src/api/apiClient.js`, poussée par `AuthProviderLocal` à chaque connexion. Ni `localStorage`, ni `sessionStorage`, ni cookie. La décision, prise au Sprint 6F.1, est commentée aux trois points du code concernés, avec sa contrepartie assumée : déconnexion à chaque rechargement de page, faute de mécanisme de *refresh token* côté backend.

Vérifications complémentaires :

- **Aucune occurrence** de `console.log`, `console.error`, `console.warn`, `console.debug` ni `debugger` dans l'intégralité de `frontend/src`. Le point « pas de log sensible en production » est donc satisfait par construction, et non par une configuration de build qu'il faudrait maintenir.
- Le token transite exclusivement par l'en-tête `Authorization: Bearer`. Aucun paramètre de requête ne le porte.
- Les blocs `catch` n'exploitent que `err.response?.status` et `err.response?.data?.erreur`. Jamais `err.message`, jamais l'objet d'erreur brut — le token, présent dans `err.config.headers`, ne peut donc pas fuiter par ce canal.
- `logout()` remet à `null` l'utilisateur en mémoire **et** le token de l'`apiClient`, puis `AuthContext` provoque un `window.location.href = '/login'`, c'est-à-dire un rechargement complet qui purge toute la mémoire JavaScript. La purge locale est inconditionnelle : l'échec de l'appel réseau `POST /auth/logout` ne l'empêche pas.

Seul usage de `localStorage` dans le frontend : la préférence d'affichage repliée/dépliée de la barre latérale. Donnée de confort, non sensible.

### 2.2 A03 — Injection et échappement

**Conforme sur les 3 points.**

`dangerouslySetInnerHTML` : **zéro occurrence**. De même pour `innerHTML`, `outerHTML`, `document.write`, `insertAdjacentHTML`, `eval` et `new Function`. Aucune justification n'est donc requise.

Tout affichage de donnée passe par le rendu JSX standard, échappé par React.

Les valeurs librement saisies par l'utilisateur (matricule, filtres, année) sont transmises via l'option `params` d'axios, qui les encode. Les interpolations dans les chemins d'URL (`/beneficiaires/${id}`) ne portent que des identifiants et des codes **issus des réponses du serveur**, jamais une saisie brute.

`window.location.href` n'est utilisé qu'avec le littéral `'/login'` : aucune redirection pilotée par une valeur externe, donc pas de vulnérabilité de type *open redirect*.

### 2.3 A01 — Contrôle d'accès

**Alignement des routes.** Les 14 groupes de routes protégées ont été comparés un par un aux rôles déclarés dans `CLAUDE.md` section 8, en tenant compte de **tous** les endpoints réellement appelés par chaque page.

Résultat central : **aucune route du frontend n'est plus permissive que le backend.** Il n'existe donc aucun écart d'élévation de privilège. Les seuls décalages relevés sont des restrictions, où le frontend est plus strict que ce que l'API autoriserait.

**Chargement de données avant contrôle du rôle.** `ProtectedRoute` retourne `<Navigate>` **au lieu** de `<Outlet />` lorsque le rôle ne correspond pas. Les éléments de route enfants ne sont jamais montés, donc aucun `useEffect` de chargement ne se déclenche. La propriété est structurelle, pas conditionnelle. Vérifié également qu'`AppLayout` et `Sidebar` n'émettent aucun appel API.

**Liens de la barre latérale.** Les 11 entrées de `NAV_LINKS` portent des rôles **strictement identiques** à ceux du `ProtectedRoute` de leur page cible. La vérification demandée par le guide a été généralisée à tous les écrans construits depuis le Sprint 6F.1, et non limitée au tableau de bord et à l'audit :

| Rôle | Liens visibles |
| --- | --- |
| EMPLOYE | Enrôlement |
| ARH | Tableau de bord, Bénéficiaires, Importer bénéficiaires, Processus mensuel, Grilles tarifaires |
| CRH | Processus mensuel *(uniquement)* |
| DRH | Tableau de bord, Processus mensuel, Grilles tarifaires (validation), Historique annuel, Journal d'audit |
| ADMIN | Grilles tarifaires, Administration, Fonctions éligibles |

Les deux exigences explicites du guide sont satisfaites : **un ARH ne voit aucun lien vers Administration ni vers Audit**, et **un CRH ne voit aucun lien de gestion**.

### 2.4 A05/A09 — Erreurs et fuite d'information

**Conforme.** `GlobalExceptionHandler` intercepte `Exception` en dernier ressort et retourne un message fixe, *« Une erreur interne est survenue »*, en journalisant la trace **côté serveur uniquement**. Aucune trace d'exécution, aucun nom de classe Java, aucune requête SQL ne franchit la frontière HTTP.

Les 30 gestionnaires spécifiques renvoient un champ `erreur` alimenté par des exceptions métier maison, dont les messages sont fonctionnels et rédigés en français. `FichierImportInvalideException` va plus loin : son message est **écrasé** par un libellé fixe, de sorte que la cause technique remontée par Apache POI n'est jamais exposée.

Côté client, aucun `catch` n'affiche l'objet d'erreur : les pages traduisent le code HTTP en message métier, avec un libellé de repli écrit en dur.

### 2.5 A05/A06 — Dépendances et build de production

**`npm audit` : 3 vulnérabilités hautes, 0 critique, 0 moyenne, 0 basse.**

Chacune a été évaluée sur son **exploitabilité réelle dans ce projet**, et non sur sa seule sévérité déclarée.

| Paquet | Version | Avis | Décision |
| --- | --- | --- | --- |
| `postcss` | 8.5.16 → **8.5.25** | GHSA-r28c-9q8g-f849 — traversée de chemin via `sourceMappingURL` | **Corrigé.** Correctif non cassant. Risque réel faible (faille de temps de build, tout le CSS est first-party), mais le correctif est gratuit. |
| `react-router` | 7.18.1 | GHSA-qwww-vcr4-c8h2 — contournement CSRF en **mode RSC** | **Reporté au Sprint 7.** |
| `react-router-dom` | 7.18.1 | dépend de `react-router` | **Reporté au Sprint 7.** |

**Justification du report de `react-router`.** L'avis vise exclusivement le **mode RSC** (React Server Components). Ce module est une application monopage strictement cliente : `BrowserRouter`, build Vite statique, aucun serveur React, aucune *server action*. Le chemin de code vulnérable n'est pas atteignable. Le seul correctif proposé par npm est une **rétrogradation en 7.11.0**, déclarée cassante, qui remettrait en cause le routage construit sur huit sous-sprints — un risque de régression certain contre un gain de sécurité nul. À réévaluer à la publication d'un correctif dans la ligne 7.19+.

**Source maps de production : conformes.** `vite.config.js` ne définit pas `build.sourcemap`, Vite applique donc son défaut `false`. Vérifié empiriquement sur un build réel : `dist/` ne contient **aucun** fichier `.map` et **aucune** directive `sourceMappingURL`.

### 2.6 A05 — CORS, transport et secrets

**CORS : aucun wildcard.** `CorsConfig` déclare une origine unique et explicite, des méthodes et en-têtes en liste fermée, et une exposition limitée à `Authorization` et `Content-Disposition`. `allowCredentials(true)` rendrait de toute façon `*` illégal côté Spring. L'origine est désormais injectée par variable d'environnement plutôt que figée.

**Transport.** Un build de production embarque maintenant une URL d'API en HTTPS, et l'absence de cette variable fait **échouer le build** au lieu de produire un bundle en HTTP clair.

**Secrets.** La propriété réellement lue par `JwtUtil` et `SecurityConfig` est `dottel.security.jwt-secret`. Elle est désormais déclarée **une seule fois**, dans le fichier de configuration de base, sans aucune valeur de repli. Vérifié empiriquement : un démarrage sans `DOTTEL_JWT_SECRET` échoue avec un message explicite désignant la variable manquante, plutôt que de retomber sur une valeur committée.

### 2.7 A03/UX — Validation côté client

**Rappel de principe, posé par le guide et confirmé par cet audit : la validation côté client est un confort d'ergonomie, jamais une garantie de sécurité.** La garantie est le *Bean Validation* du backend, présent et correct sur l'intégralité des DTO d'entrée (`@NotBlank`, `@NotNull`, `@Positive`, `@Min(1)`/`@Max(12)` sur le mois de paiement).

Cinq formulaires portaient simultanément l'attribut `noValidate` sur leur balise `<form>` et des attributs `required` sur leurs champs. `noValidate` désactivant la validation native HTML5, ces `required` ne se déclenchaient jamais : le formulaire vide partait au backend, qui répondait 400, et l'interface affichait son message générique de repli au lieu d'un message par champ. Sécurité intacte, ergonomie dégradée, et intention du code trompeuse pour un futur lecteur. Corrigé.

Les formulaires validés par zod (`Login`, `VerifierMatriculePage`, `ModifierBeneficiaireModal`) ainsi que `RetournerProcessusModal` et `DeclencherProcessusPage` **conservent** `noValidate` : c'est le motif correct lorsque la validation est portée par react-hook-form ou par un contrôle manuel.

---

## 3. Correctifs appliqués

| # | Écart | Fichiers | Correctif |
| --- | --- | --- | --- |
| **E1** | Un CRH qui se connectait atterrissait sur `/acces-interdit` : `ROUTE_PAR_ROLE` le dirigeait encore vers `/dashboard`, devenu réservé ARH/DRH par la décision du 2026-07-30. Régression non détectée au Sprint 6F.8. | `frontend/src/pages/auth/Login.jsx` | CRH redirigé vers `/processus`, sa seule page accessible et son unique entrée de barre latérale. Invariant documenté en commentaire. |
| **E6** | `include-stacktrace` et `include-message` déclarés sous `spring.web.error`, préfixe qu'aucune `@ConfigurationProperties` de Spring Boot ne lie. Les deux lignes étaient **inertes**. Aucune fuite réelle (les défauts sont déjà `never` et `GlobalExceptionHandler` intercepte tout), mais fausse impression de garantie explicite. | `application-prod.yml` | Corrigé en `server.error.*`, complété par `include-exception: false` et `include-binding-errors: never`. |
| **E7b** | `dottel.security.jwt-secret` n'était défini **que** dans le profil `dev`. En profil `prod`, la propriété n'existait pas, et la variable injectée partout ailleurs (`DOTTEL_JWT_SECRET`) ne correspond pas au nom que la liaison relâchée de Spring attend pour cette propriété (`DOTTEL_SECURITY_JWT_SECRET`). Le module **ne pouvait pas démarrer en production**. | `application.yml`, `application-dev.yml`, `backend/Dockerfile` | Propriété déclarée une seule fois au niveau de base, en `${DOTTEL_JWT_SECRET}` — nom aligné sur le `Secret` Kubernetes et le `Dockerfile`. Bloc `ENV` vide du `Dockerfile` remplacé par un contrat documenté : une valeur vide **résolvait** le placeholder et produisait une `WeakKeyException` opaque au lieu d'une erreur de configuration claire. |
| **E7d** | `app.jwt.secret` et `app.jwt.expiration-hours` : configuration **morte**, lue par aucune classe (l'expiration est une constante de `JwtUtil`). Le repli `changeme-in-production-32-chars-minimum` était un secret littéral committé, contraire à `CLAUDE.md` section 18 point 10. | `application.yml` | Bloc supprimé. |
| **E7e** | Secret de repli en clair dans `application-dev.yml`, même remarque. | `application-dev.yml` | Repli supprimé. Le secret vient exclusivement de l'environnement, en développement comme en production. |
| **E7c** | Les deux fichiers censés porter `VITE_API_BASE_URL` s'appelaient `.env.development.md` et `.env.example.txt` — extensions que **Vite ne charge jamais**. Aucun `.env.production` n'existait. Un build de production aurait embarqué en dur `http://localhost:8080/api`, **en clair**. | `frontend/.env.*`, `frontend/src/api/apiClient.js` | Fichiers renommés en noms chargés par Vite, `.env.production` créé en HTTPS, et repli `localhost` confiné à `import.meta.env.DEV` : en production, une variable absente lève une erreur explicite au lieu de livrer un bundle en HTTP. |
| **E8** | `app.cors.allowed-origins` figé à `http://localhost:3000` dans le fichier de base, jamais surchargé en production ni externalisé. Pas une faille d'ouverture, mais une configuration inexploitable en production. | `application.yml`, `k8s/configmap.yaml` | Passé en `${APP_CORS_ALLOWED_ORIGINS:http://localhost:3000}` et ajouté au ConfigMap Kubernetes. |
| **V1** | `postcss` 8.5.16 vulnérable. | `frontend/package-lock.json` | `npm audit fix` — 8.5.25. |

---

## 4. Écarts reportés, avec justification

Reporter n'est pas ignorer : chaque point ci-dessous a été analysé, et le report est un choix motivé, pas un oubli.

| # | Écart | Sévérité | Justification du report |
| --- | --- | --- | --- |
| **V2/V3** | `react-router` 7.18.1 — contournement CSRF en mode RSC | Haute (déclarée) / **nulle** (réelle) | Chemin de code non atteignable dans une SPA cliente. Le seul correctif est une rétrogradation cassante en 7.11.0. À réévaluer à la sortie d'un correctif 7.19+. **Sprint 7.** |
| **E2** | La DRH n'a aucun accès à l'écran Bénéficiaires ni à son export, pourtant autorisés par `CLAUDE.md` section 8 | Mineure | Restriction, pas élévation de privilège. La page porte des actions réservées à l'ARH ; l'ouvrir à la DRH exposerait des boutons inutilisables. **Décision fonctionnelle, à trancher avec le métier.** — **TRANCHÉ ET CORRIGÉ au Sprint MM.14 (2026-08-19)**, voir 4.1 ci-dessous. |
| **E3** | L'ADMIN est exclu de l'écran de création de grille, pourtant autorisé par la section 8 | Mineure | Même raisonnement. **Décision fonctionnelle.** — **TRANCHÉ ET CORRIGÉ au Sprint MM.14 (2026-08-19)**, voir 4.1 ci-dessous. |
| **E4** | La route de repli `*` renvoie vers `/login` même pour un utilisateur authentifié | Mineure | Aucune conséquence de sécurité : le token reste en mémoire et les routes protégées restent protégées. Gêne d'ergonomie sur une URL inconnue. — **CORRIGÉ au Sprint MM.14.** |
| **E5** | `AccesInterdit` est un `<div>` nu, hors charte visuelle de la section 15, sans lien de retour | Cosmétique | Aucune conséquence de sécurité. — **CORRIGÉ au Sprint MM.14.** |
| **E7a** | `/actuator/health` expose son détail sans authentification | Mineure | **Exigé** par `CLAUDE.md` section 14 pour les sondes de vivacité et de disponibilité Kubernetes. Le restreindre casserait les probes. À arbitrer avec la DSI si l'exposition doit être limitée au réseau du cluster. |

**Observation résiduelle, hors périmètre du sprint.** `application-dev.yml` conserve `password: ${DB_PASSWORD:postgres}`, soit un mot de passe de repli en clair. Il s'agit du défaut notoire de PostgreSQL, en développement uniquement, et le sujet n'était pas dans le périmètre arbitré — signalé ici pour mémoire. — **CORRIGÉ au Sprint MM.14** : le repli est supprimé, le profil `dev` porte désormais la même écriture que `prod` (`${DB_PASSWORD}` sans valeur par défaut). `DB_USER` garde le sien : `postgres` est le nom de compte par défaut de PostgreSQL, pas un secret. **Comportement vérifié empiriquement sans `DB_PASSWORD`** : le démarrage échoue franchement (Flyway ne peut pas ouvrir la connexion), mais sur une erreur d'authentification PostgreSQL et **non** sur un `Could not resolve placeholder`. Ce n'est pas un défaut du correctif, c'est une propriété de Spring : `spring.datasource.password` est liée par `@ConfigurationProperties`, dont le résolveur de placeholders est tolérant (`ignoreUnresolvablePlaceholders`), là où `@Value` lève une erreur nommant la variable — d'où le message explicite obtenu jadis sur `DOTTEL_JWT_SECRET`, lu par `@Value` dans `JwtUtil`. L'objectif de l'observation est atteint : plus aucun secret en clair, et aucun démarrage silencieux sur une valeur committée.

**Dépendances déclarées mais inutilisées.** `jwt-decode` et `react-hot-toast` figurent dans `package.json` sans aucun usage dans `src`. Sans effet sur le bundle livré (élimination du code mort), mais surface de dépendance inutile : à retirer au Sprint 7 si l'usage ne se concrétise pas. — **RÉÉVALUÉ au Sprint MM.14** : `jwt-decode` **est désormais utilisé** (`AuthProviderKeycloak.js`, décodage des claims du jeton Keycloak depuis MM.7) — cette moitié de l'observation est périmée. Seul `react-hot-toast` reste orphelin ; **décision du 2026-08-19 : le conserver sans l'intégrer**, `CLAUDE.md` section 2 l'ayant explicitement validé pour un usage futur, et son intégration relevant du chantier design frontend (D.2/D.3), pas d'un sprint de dette technique.

---

### 4.1 Résolution des écarts E2 à E5 — Sprint MM.14, 2026-08-19

**E2 — la DRH et l'écran Bénéficiaires. Décision : ouvrir en lecture seule.**
La DRH valide en dernier l'état mensuel ; il est légitime qu'elle puisse
vérifier la fiche de référence d'un bénéficiaire sans passer par l'ARH.
`CLAUDE.md` section 8 le prévoyait déjà et le backend l'implémentait
(`GET /beneficiaires` et `/export` en `hasAnyRole('ARH','DRH')`) — seul l'écran
l'en excluait : la DRH détenait un droit qu'elle ne pouvait pas exercer.
Corrections : rôle `DRH` ajouté au `ProtectedRoute` de `/beneficiaires` et à
l'entrée de menu correspondante ; les quatre actions d'écriture (modifier,
désactiver, réactiver, importer) restent conditionnées au rôle `ARH`, la colonne
« Actions » disparaissant entièrement pour la DRH plutôt que d'afficher des
boutons qui échoueraient en 403 ; `GET /beneficiaires/unites-rattachement`
ouvert à la DRH, faute de quoi le filtre « Unité de rattachement » de son écran
resterait en 403. `/beneficiaires/import` demeure ARH seul.

**E3 — l'ADMIN et la création de grille. Décision : garder l'ARH seul, et
aligner la documentation et le backend sur ce comportement.**
Dans ce module, l'ADMIN gère les comptes et le référentiel des fonctions, pas
les montants. Créer une grille, c'est fixer un montant de dotation qui part
aussitôt dans le circuit ARH → CRH → DRH (RG-10, workflow à trois acteurs du
Sprint MM.12) — circuit dont l'ADMIN n'est acteur d'aucune étape ; l'y laisser
injecter un montant contredirait l'esprit de RG-08. Le frontend appliquait déjà
cette restriction proprement (bouton « Créer une grille » conditionné à l'ARH,
donc aucun bouton mort) : c'étaient le `@PreAuthorize` et la documentation qui
étaient en retard. Corrections : `ADMIN` retiré de `POST /grilles-tarifaires` et
`PATCH /grilles-tarifaires/{id}` dans le contrôleur, dans `CLAUDE.md` section 8
et dans `contrats_api_dotations_v3.md` (V3.7).

> **Point vérifié à la demande de l'utilisateur, et qui aurait pu tout
> invalider.** L'écran ADMIN de création d'une fonction éligible porte un champ
> « Montant de la dotation » : ce montant crée-t-il une grille via l'endpoint
> qu'on vient de fermer ? **Non.** `POST /fonctions-eligibles` est servi par
> `FonctionEligibleService.creer()`, qui écrit la fonction **et** sa grille
> initiale directement dans `grilleTarifaireRepository`, à l'intérieur du module
> `referentiel` — sans jamais passer par `GrilleTarifaireController`. Les deux
> chemins sont indépendants : **l'ADMIN conserve la création complète d'une
> fonction éligible avec son montant.** Ce qu'il perd, c'est uniquement le droit
> de *remplacer* le montant d'une fonction existante en injectant une grille
> dans le circuit de validation — ce que l'interface ne lui permettait déjà pas.
> À noter, sans y toucher (hors périmètre MM.14) : cette grille initiale naît en
> `ACTIVE` sans validation DRH, exception à RG-10 assumée et décidée avec le
> métier au Sprint 6F.7bis.

**E4 — route de repli.** Le `*` renvoyait tout le monde vers `/login`, si bien
qu'une faute de frappe dans l'URL donnait à un agent connecté l'impression
d'avoir été déconnecté. Remplacé par `RepliRoute`, qui arbitre explicitement :
`/login` si non authentifié, `/introuvable` sinon. Un unique `*` décide, plutôt
que deux branches `*` concurrentes (racine et imbriquée sous `AppLayout`) qui
obtiendraient le même score de spécificité dans React Router et s'arbitreraient
sur l'ordre de déclaration — fragile à la première réorganisation du routeur.
Nouvelle page `PageIntrouvable`, rendue dans `AppLayout` : barre latérale
conservée, donc la navigation reste possible.

**E5 — `AccesInterdit`.** Réécrite dans la charte (`PageHeader`, `Card`, icône
`ShieldAlert` sur les tokens `primary-*` du chantier D.1), avec un message qui
nomme le rôle de l'utilisateur et un bouton « Retour à l'accueil ». La table
`ROUTE_PAR_ROLE`, jusque-là privée dans `CallbackKeycloak.jsx`, est extraite
dans `src/router/routeParRole.js` : trois écrans en ont désormais besoin, une
seule source évite qu'un rôle soit corrigé à un endroit et pas aux autres.

**E7a — non modifié, comme exigé.** `/actuator/health` conserve
`show-details: always` et son `permitAll` : les sondes de vivacité et de
disponibilité Kubernetes en dépendent (`CLAUDE.md` section 14). Le restreindre
demande un arbitrage DSI (isolation réseau du endpoint au cluster), pas une
décision de sprint. `SecurityConfig.java` n'a reçu aucune modification au
Sprint MM.14.

---

## 5. Vérifications de non-régression

| Contrôle | Résultat |
| --- | --- |
| `mvn test` (backend) | **205 tests, 0 échec, 0 erreur — BUILD SUCCESS** |
| `npx oxlint` (frontend) | 0 erreur (4 avertissements préexistants, sans rapport avec l'audit) |
| `npm run build` (frontend) | Succès — 1991 modules |
| Fichiers `.map` dans `dist/` | **Aucun** |
| Directive `sourceMappingURL` dans le bundle | **Aucune** |
| URL d'API compilée dans le bundle | `https://dottel.afrilandfirstbank.cm/api` — **aucune trace de `localhost:8080`** |
| Démarrage backend **sans** `DOTTEL_JWT_SECRET` (isolé, `DB_PASSWORD` fourni) | **Échec attendu** : `Could not resolve placeholder 'DOTTEL_JWT_SECRET'` — reproduit une seconde fois lors de la validation visuelle du 2026-07-31, avec `DB_PASSWORD` correctement fourni pour isoler la variable testée |
| Démarrage backend **avec** les variables | Démarrage nominal |

**Précision issue de la validation visuelle.** Un premier essai de ce test, sans `DB_PASSWORD` ni `DOTTEL_JWT_SECRET`, a échoué sur l'authentification PostgreSQL (`FATAL: authentification par mot de passe échouée`) avant même d'atteindre le bean qui lit `dottel.security.jwt-secret` — `application-dev.yml` conserve un repli en clair sur `DB_PASSWORD` (`${DB_PASSWORD:postgres}`, non traité dans ce sprint, voir section 4). Le test a été refait en isolant la variable : `DB_PASSWORD` fourni, `DOTTEL_JWT_SECRET` volontairement omis. Résultat : l'échec attendu sur le placeholder JWT, confirmant que E7e fonctionne comme prévu.

### 5.1 Test manuel des appels API

Exécuté sur une instance de test isolée, démarrée avec la configuration corrigée.

| # | Cas | Attendu | Obtenu |
| --- | --- | --- | --- |
| 1 | Connexion CRH (matricule 2093) | 200, rôle `CRH` | ✅ 200 — `role: "CRH"`, ESSAMA Marie Claire. Le rôle qui déclenche la redirection corrigée E1 est bien celui attendu. |
| 2 | Connexion ARH (matricule 1847) | 200, rôle `ARH` | ✅ 200 — `role: "ARH"`, MBARGA Jean Paul |
| 3 | Mot de passe erroné | 401, message métier | ✅ 401 — *« Matricule ou mot de passe incorrect »*. Aucune trace d'exécution, aucun nom de classe. |
| 4 | Matricule inexistant | 401, **message identique** au cas 3 | ✅ 401 — message **strictement identique**. Pas d'énumération de comptes possible. |
| 5 | Endpoint protégé sans token | 401 | ✅ 401, corps vide |
| 6 | Corps de requête vide | 400, erreurs par champ | ✅ 400 — `{"erreur":"Données invalides","champs":{...}}`, messages fonctionnels uniquement |
| 7 | Préflight CORS depuis `http://localhost:3000` | Autorisé, origine exacte | ✅ 200 — `Access-Control-Allow-Origin: http://localhost:3000`. **Aucun wildcard** : l'origine exacte est renvoyée. |
| 8 | Préflight CORS depuis `https://evil.example.com` | Refusé | ✅ **403**, et **aucun** en-tête `Access-Control-Allow-Origin` renvoyé |
| 9 | Token CRH sur `GET /admin/utilisateurs` (réservé ADMIN) | 403 | ✅ 403 — *« Rôle non autorisé pour cette opération »*. Confirme que le contrôle d'accès du frontend est une **défense en profondeur**, pas la seule barrière. |

---

## 6. Conclusion

Le frontend est **apte à passer en recette (Sprint 7)** du point de vue de la sécurité.

Aucune vulnérabilité exploitable n'a été trouvée dans le code applicatif. La régression la plus concrète, un rôle CRH sans page d'atterrissage valide, était un défaut de contrôle d'accès fonctionnel plutôt qu'une faille. Les écarts les plus lourds concernaient une configuration de production qui n'avait jamais été exercée, et auraient empêché le module de démarrer ou l'auraient fait communiquer en HTTP clair.

Trois points restent à arbitrer hors de cet audit : les décisions fonctionnelles E2 et E3 avec le responsable métier, et l'exposition de `/actuator/health` avec la DSI.