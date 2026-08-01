# SPRINT MM.7

## Keycloak local provisoire, en remplacement de la simulation JWT

*Module Dotations Téléphoniques Mensuelles — Backend et Frontend*

| | |
|---|---|
| **Objet** | Remplacer la simulation JWT symétrique par un Keycloak **local et provisoire**, pour préparer la bascule vers le realm AFB réel |
| **Livrable** | Keycloak local dans `docker-compose.yml`, `SecurityConfig` en mode `issuer-uri`, `AuthProviderKeycloak` côté frontend |
| **Durée** | Deux à trois journées |
| **Prérequis** | Les **3 questions ouvertes** de la section 3 tranchées avec le responsable projet |

---

## ⚠️ 0. Cadre et limites — à lire avant tout

**Ce sprint installe un Keycloak LOCAL, dans `docker-compose.yml`, sur le poste de développement. Il ne s'agit en aucun cas d'une demande d'ouverture réseau vers un realm externe, ni d'une anticipation d'infrastructure non validée.**

Cette précaution découle directement de `CLAUDE.md` :

- **Section 13** : « En développement : simuler Keycloak avec une instance locale ou avec l'authentification locale BCrypt. » Un Keycloak local en conteneur est donc **explicitement prévu par le cahier des charges**, ce n'est pas une extension de périmètre.
- **Section 14** : « Service registry / API Gateway : aucun pour le moment […] ne pas anticiper cette architecture dans le code actuel. »
- **Section 18, point 18** : « Introduire un service registry ou une gateway non demandée par la DSI » figure dans les erreurs à ne jamais commettre.

Le realm réel de la banque reste **hors périmètre et non confirmé à ce jour**. L'objectif de MM.7 est uniquement que, le jour où la DSI fournit ce realm, la bascule se limite idéalement à un changement de variables d'environnement.

Le Keycloak local rejoint le broker Kafka déjà présent dans `docker-compose.yml` depuis le Sprint 5.3 (`apache/kafka:3.8.0`, conteneur `dottel-kafka`, port 9092) — même logique : une dépendance d'infrastructure simulée localement, jamais un service distant.

---

## Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Arbitrage des 3 questions ouvertes (section 3) | Opus 4.8 ou Sonnet 5 | **High** |
| Keycloak local et realm de développement (étapes 2-3) | Sonnet 5 | Medium |
| Bascule backend `SecurityConfig` (étapes 4-5) | Sonnet 5 | **High** |
| Mapping des rôles et de l'identité (étape 6) | Sonnet 5 | **High** |
| `AuthProviderKeycloak` frontend (étapes 7-8) | Sonnet 5 | Medium à High |
| Vérification de bout en bout (étape 9) | Sonnet 5 | High |

L'effort High sur les étapes 4 à 6 se justifie par un point précis : **c'est le seul sprint du chantier qui touche à qui peut entrer dans l'application**. Une erreur de mapping de rôles ne se voit pas à la lecture et transforme un CRH en ARH.

## 0. Nouvelle session : rappel Graphify

```
/graphify . --update
```

---

## 1. Contexte — état réel du mécanisme d'authentification

### 1.1 Backend : l'application est son propre émetteur de jetons

| Élément | Fichier et ligne | Constat vérifié |
|---|---|---|
| Émission du jeton | [`AuthService.authentifier()`](../../backend/src/main/java/com/afriland/dottel/service/AuthService.java) lignes 25-54 | Matricule + BCrypt contre la table `utilisateurs`, puis appel à `JwtUtil` |
| Fabrication | [`JwtUtil.genererToken()`](../../backend/src/main/java/com/afriland/dottel/security/JwtUtil.java) ligne 26 | `subject` = **matricule**, un seul claim `role`, `issuedAt`, `expiration` |
| Algorithme | `JwtUtil` ligne 18 | `Jwts.SIG.HS384` — **symétrique** |
| Durée de validité | `JwtUtil` ligne 16 | `EXPIRATION_MS = 8 * 60 * 60 * 1000`, **codée en dur** |
| Clé | `JwtUtil` ligne 22 | `@Value("${dottel.security.jwt-secret}")`, sans repli depuis le Sprint 6F.9 |
| Validation | [`SecurityConfig.jwtDecoder()`](../../backend/src/main/java/com/afriland/dottel/security/SecurityConfig.java) lignes 64-69 | `NimbusJwtDecoder.withSecretKey(new SecretKeySpec(jwtSecret, "HmacSHA384")).macAlgorithm(MacAlgorithm.HS384)` |
| Rôles | [`RoleJwtAuthenticationConverter`](../../backend/src/main/java/com/afriland/dottel/security/RoleJwtAuthenticationConverter.java) lignes 16-22 | Lit le claim `role`, produit **une seule** autorité `ROLE_<role>` |
| Identité applicative | [`AuthenticatedUserService`](../../backend/src/main/java/com/afriland/dottel/service/AuthenticatedUserService.java) lignes 16-22 | `SecurityContextHolder…getAuthentication().getName()` → **matricule** → `findByMatricule()` |

**Trois conséquences structurantes :**

1. **Aucun `issuer`, aucun `issuer-uri`, aucun JWKS.** L'application signe et vérifie avec la même clé secrète : elle est à la fois fournisseur d'identité et serveur de ressources. Passer à Keycloak rend `AuthService.authentifier()` et `JwtUtil.genererToken()` **obsolètes pour l'émission**.

2. **Il n'existe qu'UN SEUL bean `JwtDecoder` et UNE SEULE `SecurityFilterChain`** (`SecurityConfig` lignes 36-69). Ce point est déterminant pour la question de portée (section 2).

3. **Le pont entre le jeton et la base repose entièrement sur le `subject` = matricule.** Tout changement d'identifiant porté par le jeton casse `AuthenticatedUserService`, donc l'audit (RG-09) et toutes les vérifications de séparation des tâches (RG-08).

### 1.2 Entité `Utilisateur` : aucun champ d'identité externe

Champs réels de [`Utilisateur.java`](../../backend/src/main/java/com/afriland/dottel/model/entity/Utilisateur.java) :

| Champ | Contrainte | Pertinence pour Keycloak |
|---|---|---|
| `id` | PK auto | — |
| `matricule` | **UNIQUE, NOT NULL** | Clé d'identité actuelle, portée par le `subject` du jeton |
| `nom`, `prenom` | NOT NULL | Redondants avec les claims Keycloak |
| `email` | **UNIQUE, NOT NULL** | **Seul candidat existant** pour un rattachement à une identité AD |
| `role` | NOT NULL, `RoleEnum` | À faire correspondre aux rôles Keycloak |
| `motDePasseHash` | **NOT NULL** | **Devient sans objet** si Keycloak porte l'authentification — contrainte de schéma bloquante |
| `actif` | NOT NULL | Doublon potentiel avec l'état d'activation Keycloak |
| `dateCreation` | NOT NULL | — |
| `idBeneficiaire` | nullable, FK vers `beneficiaires` | **Déclaré mais écrit nulle part** dans tout le code (voir section 3.1) |

**Constat vérifié : il n'existe aucun champ pouvant porter un identifiant Active Directory** (`sAMAccountName`, `userPrincipalName`, `preferred_username` ou équivalent). C'est un manque, pas un oubli de lecture.

### 1.3 Frontend : une abstraction réelle, mais incomplète

[`AuthProvider.js`](../../frontend/src/auth/AuthProvider.js) définit bien un contrat à trois méthodes : `login(matricule, motDePasse)`, `logout()`, `utilisateurCourant()`. [`AuthProviderLocal.js`](../../frontend/src/auth/AuthProviderLocal.js) l'implémente et pousse le jeton dans `apiClient` via `setAuthToken()`.

**Deux réserves, vérifiées dans le code :**

**a) `AuthContext.jsx` est câblé au singleton concret.** [`AuthContext.jsx`](../../frontend/src/contexts/AuthContext.jsx) ligne 2 fait `import { authProviderLocal } from '../auth/AuthProviderLocal';` et l'appelle lignes 18 et 25. Le commentaire du fichier annonce « le jour d'une vraie intégration Keycloak, seule cette dépendance changera » — c'est exact, mais cela signifie quand même **modifier `AuthContext.jsx`**. L'hypothèse « aucune modification d'`AuthContext`» est donc **infirmée** : la modification est minime (un import et deux références, ou l'injection du provider en propriété), mais elle existe.

**b) La forme même du contrat suppose un mot de passe.** `login(matricule, motDePasse)` implique que l'application voit le mot de passe de l'utilisateur. Le flux standard de Keycloak est une **redirection** (Authorization Code + PKCE) où l'application ne voit jamais le mot de passe et reçoit un code à échanger. Le contrat actuel ne colle donc qu'au *Direct Access Grant*. C'est l'objet de la **question ouverte n° 2**.

Point favorable en revanche : le jeton est déjà conservé **en mémoire seule** ([`apiClient.js`](../../frontend/src/api/apiClient.js) ligne 11, décision du Sprint 6F.1, confirmée par l'audit 6F.9). Cette propriété est conservable avec Keycloak.

---

## 2. Portée : quels rôles le Keycloak provisoire couvre-t-il ?

### 2.1 L'information métier apportée par le responsable projet

Dans la réalité d'Afriland, les **4 rôles internes** (ARH, CRH, DRH, ADMIN) se connectent avec leur **compte Microsoft Active Directory** (identifiants Windows), **pas** avec leur matricule. Le matricule sert uniquement au parcours d'enrôlement, comme clé de recherche dans l'EHR.

### 2.2 Ce que le code fait aujourd'hui, en contradiction avec cela

Le parcours EMPLOYE utilise **exactement le même mécanisme** que les 4 rôles internes :

- Même endpoint `/auth/login`, même `AuthService`, même `JwtUtil`
- [`EnrolementController`](../../backend/src/main/java/com/afriland/dottel/controller/EnrolementController.java) lignes 27 et 33 : `@PreAuthorize("hasRole('EMPLOYE')")` — un compte utilisateur est donc **obligatoire**
- `V3__insertion_utilisateurs_test.sql` crée bien un utilisateur `2201` / NKOLO Sylvie avec le rôle `EMPLOYE` et un mot de passe BCrypt

Il n'existe **pas** deux chemins d'authentification dans le code : il n'y en a qu'un.

### 2.3 Fédération Keycloak ↔ Active Directory — ✅ **CONFIRMÉE le 2026-07-31**

**Confirmation apportée par le responsable projet, qui exerce à la banque, à partir du fonctionnement observé sur la plateforme BAOBAB :**

- Le realm Keycloak d'Afriland **est relié à l'Active Directory**.
- Sur BAOBAB, un clic sur « Se connecter » **redirige vers la page de login Keycloak**.
- Les identifiants saisis sont **les identifiants Windows habituels**, les mêmes que pour ouvrir sa session.

Cette confirmation vaut **observation de terrain**, pas document officiel. Elle est suffisamment précise pour orienter la conception — elle a permis de trancher la question 2 en faveur de PKCE — mais elle ne remplace pas les **paramètres techniques du realm**, qui restent à obtenir de la DSI : URL du realm, nom du client, format des rôles, et surtout format de l'identifiant utilisateur (voir question 3).

**Pour mémoire, état de la documentation projet :**

> **Recherche exhaustive effectuée le 2026-07-31 sur l'intégralité du dépôt** (`CLAUDE.md`, `docs/`, guides de sprint, code source, manifests) pour les termes *Active Directory*, *LDAP*, *SAML*, *Kerberos*, *annuaire* : **zéro occurrence**. Les seuls résultats pour « Windows » concernent `mvnw` et un commentaire d'encodage CP1252 dans `DocumentService`.

Ce fonctionnement, bien que réel et quotidien sur BAOBAB, **n'est écrit nulle part dans le projet** — d'où l'intérêt de le consigner ici.

### 2.4 Décision de portée — ✅ **TRANCHÉE le 2026-07-31 : P-2**

> ## ✅ Décision actée — P-2, Keycloak pour les 5 rôles, EMPLOYE compris
>
> **Le responsable projet a tranché directement**, sans attendre de confirmation externe : si les rôles internes (ARH/CRH/DRH/ADMIN) s'authentifient via leur compte AD, il n'y a aucune raison métier de penser qu'un employé lambda venant s'enrôler en serait dépourvu — tous les employés Afriland ont un compte AD. Séparer l'EMPLOYE du reste n'aurait donc apporté que de la complexité technique (double émetteur JWT) sans bénéfice métier réel.
>
> **Conséquences concrètes :**
>
> - **Un seul émetteur, une seule `SecurityFilterChain`.** Pas d'`AuthenticationManagerResolver` à construire, pas de double `JwtDecoder`.
> - **`AuthService.authentifier()` et `JwtUtil.genererToken()` deviennent morts** et seront supprimés en MM.7 étape 5 — avec leurs tests associés (`AuthServiceTest`, 4 tests ; `JwtUtilTest`, 3 tests). C'est le **seul cas légitime du chantier** où le nombre total de tests peut descendre sous 205, à condition de le signaler explicitement au moment de l'exécution.
> - **`POST /auth/login` disparaît** en tant qu'endpoint applicatif : la connexion devient une redirection Keycloak (cohérent avec la décision F-2/PKCE de la question 2), pour les 5 rôles sans exception.
> - **Le realm local provisoire doit créer les 5 rôles** (`EMPLOYE`, `ARH`, `CRH`, `DRH`, `ADMIN`), pas seulement les 4 rôles internes — impact direct sur l'étape 3 (realm de développement).
>
> **Ce qui ne change pas :** `EnrolementController`/`EnrolementService` restent inchangés dans leur logique métier — seul le mécanisme d'authentification en amont change, pas la vérification `@PreAuthorize("hasRole('EMPLOYE')")` ni le comportement acté à la question 1 (E-3).

L'intuition initiale « le Keycloak provisoire ne couvre que les 4 rôles internes, l'EMPLOYE reste sur matricule » semblait cohérente sur le plan métier, mais avait une conséquence technique lourde : elle est documentée ci-dessous pour traçabilité, avant d'être écartée au profit de P-2.

`SecurityConfig` ne déclare **qu'un seul bean `JwtDecoder`** et **une seule `SecurityFilterChain`**. Faire coexister deux modes d'authentification suppose donc :

| Option de portée | Ce que ça implique concrètement |
|---|---|
| **P-1 — Keycloak pour les 4 rôles internes uniquement** | L'application doit accepter **deux émetteurs simultanés** : Keycloak (asymétrique, JWKS) pour ARH/CRH/DRH/ADMIN, et HS384 local pour EMPLOYE. Exige un `AuthenticationManagerResolver` ou deux `SecurityFilterChain` distinctes discriminées par route. `AuthService` et `JwtUtil` **survivent**, uniquement pour l'EMPLOYE. **Plus proche du réel, mais ajoute de la complexité au lieu d'en retirer.** |
| **P-2 — Keycloak pour les 5 rôles, EMPLOYE compris** | Un seul émetteur, une seule chaîne, `AuthService`/`JwtUtil` supprimés. Techniquement le plus simple et le plus propre. **Mais s'éloigne du fonctionnement réel** si l'employé lambda ne doit pas passer par l'AD pour s'enrôler. |
| **P-3 — Keycloak pour les 4 rôles, parcours EMPLOYE rendu public** ⛔ **NON RECOMMANDÉE** | L'enrôlement devient un parcours non authentifié (`permitAll` sur `/enrolement/**`). Supprime le besoin de double émetteur. **Documentée uniquement pour tracer qu'elle a été envisagée et écartée** — voir l'encadré ci-dessous. |

> ### ⛔ Pourquoi P-3 n'est pas à égalité avec P-1 et P-2
>
> P-1 et P-2 sont un **arbitrage neutre** entre fidélité au fonctionnement réel et simplicité technique : les deux préservent l'authentification sur l'ensemble des parcours, elles ne diffèrent que par l'émetteur du jeton.
>
> **P-3 est d'une autre nature : elle supprime une barrière d'authentification pour éviter une complexité d'architecture.** Ce n'est pas un compromis entre deux qualités équivalentes, c'est un recul de sécurité échangé contre du confort d'implémentation. À ce titre elle ne peut pas figurer comme un troisième choix parmi d'autres.
>
> **Trois raisons concrètes, vérifiables dans le code :**
>
> 1. **Elle aggrave un trou déjà documenté, elle n'en crée pas un hypothétique.** La question ouverte n° 1 (§3.1) établit que tout EMPLOYE authentifié peut déjà enrôler n'importe quel matricule connu de l'EHR. Aujourd'hui cette faiblesse est au moins bornée par « il faut un compte EMPLOYE actif et son mot de passe ». P-3 retire cette dernière borne : **n'importe qui atteignant le réseau pourrait enrôler n'importe quel matricule.**
>
> 2. **L'enrôlement n'est pas une consultation, c'est une écriture qui alimente une chaîne de paiement.** `EnrolementService.confirmer()` crée une ligne `beneficiaires` avec `actif = true`. Or `ProcessusMensuelService.declencher()` sélectionne ses lignes via `beneficiaireRepository.findByActifTrue()` (ligne 107). Un endpoint non authentifié insérerait donc directement des bénéficiaires dans l'état mensuel, jusqu'au fichier transmis à la comptabilité.
>
> 3. **Elle contredit frontalement le cahier des charges.** `CLAUDE.md` section 8 attribue explicitement le rôle `EMPLOYE` aux deux endpoints d'enrôlement, et la section 18 point 9 interdit de « créer un endpoint métier sans `@PreAuthorize` ». P-3 reviendrait à retirer un `@PreAuthorize` existant et fonctionnel.
>
> **Si P-3 devait malgré tout être retenue, elle exigerait une justification métier écrite et une compensation explicite** (par exemple une limitation de débit, une restriction réseau, ou un contrôle de possession du matricule) — et non le simple constat qu'elle est plus simple à implémenter.

L'arbitrage entre P-1 et P-2 est tranché ci-dessus : **P-2 retenue**.

---

## 3. LES TROIS QUESTIONS — toutes tranchées le 2026-07-31

| # | Question | État |
|---|---|---|
| **1** | L'enrôlement ne vérifie pas le matricule du porteur du jeton | ✅ **Tranchée** — E-3, comportement voulu, à documenter |
| **2** | Direct Access Grant ou Authorization Code + PKCE | ✅ **Tranchée** — F-2 (PKCE), fédération AD confirmée |
| **3** | Clé d'identité de `Utilisateur` | ✅ **Tranchée** — I-2, résolution par `email` |

Leurs décisions figurent dans les encadrés ci-dessous ; le raisonnement d'origine est conservé en repli pour traçabilité.

**Les 3 questions ET la décision de portée (§2.4, P-2) sont toutes tranchées. La section 4 (mise en œuvre) peut démarrer.**

### 3.1 Question 1 — ~~L'enrôlement ne vérifie pas que le matricule est celui de l'utilisateur connecté~~ ✅ **TRANCHÉE le 2026-07-31**

> ## ✅ Décision actée — comportement voulu, à documenter
>
> **Le responsable projet a confirmé que ce comportement est intentionnel** : un collègue connecté doit pouvoir aider un autre collègue à s'enrôler via la page d'enrôlement individuel. Il ne s'agit donc **pas d'un défaut à corriger**, mais d'un choix fonctionnel à écrire noir sur blanc.
>
> **Option retenue : E-3 — statu quo, documenté.**
>
> **Aucune modification de code n'est requise** sur `EnrolementController` ni sur `EnrolementService`. Le champ `Utilisateur.idBeneficiaire` reste inutilisé.
>
> **Ce qui reste à faire (documentation seulement, hors MM.7) :**
> - Ajouter une note dans `docs/reference/contrats_api_dotations_v3.md` §2, précisant que `POST /enrolement/confirmer` accepte volontairement un matricule différent de celui du porteur du jeton.
> - Mentionner ce choix dans un futur complément de `CLAUDE.md`, pour qu'un audit ultérieur ne le re-signale pas comme une anomalie.
>
> **Conséquence sur la portée (§2.4) : cette décision ne réhabilite pas P-3.** Le comportement acté suppose toujours un utilisateur **authentifié** ; c'est l'authentification qui trace *qui* a enrôlé *qui* dans `audit_log` (`EnrolementService` ligne 114). P-3 supprimerait cette traçabilité en même temps que l'authentification. Les trois raisons de l'encadré §2.4 restent valables.
>
> **À ne pas confondre avec l'import Excel.** `POST /beneficiaires/import` est un parcours distinct, réservé à l'**ARH** (analyste RH), et n'est pas concerné par cette question.

<details>
<summary>Constat d'origine, conservé pour traçabilité</summary>

**Constat vérifié, indépendant de Keycloak.**

| Emplacement | Constat |
|---|---|
| [`EnrolementController.verifier()`](../../backend/src/main/java/com/afriland/dottel/controller/EnrolementController.java) ligne 28 | Le matricule vient de `@RequestParam String matricule` |
| [`EnrolementService.confirmer()`](../../backend/src/main/java/com/afriland/dottel/service/EnrolementService.java) ligne 69 | `String matricule = requeteConfirmation.getMatricule()` — **du corps de la requête** |
| `EnrolementService` ligne 114 | `authenticatedUserService.utilisateurCourant()` n'est appelé **que pour le journal d'audit** |
| `Utilisateur.idBeneficiaire` | Déclaré ligne 59 et présent dans `V1__creation_tables.sql` avec une FK, mais **écrit nulle part dans le code** — la recherche ne retourne que sa déclaration et le DDL |

**Conséquence : tout utilisateur authentifié avec le rôle EMPLOYE peut enrôler n'importe quel matricule connu de l'EHR, pas seulement le sien.** Le backend ne compare jamais le matricule soumis à celui porté par le jeton.

Ce défaut est antérieur au chantier (présent depuis le Sprint 2) et n'a pas été détecté par l'audit du Sprint 6F.9, dont le périmètre était le frontend.

**Pourquoi cette question appartient à MM.7 :** le champ `idBeneficiaire`, aujourd'hui mort, est vraisemblablement le lien prévu à l'origine entre un compte utilisateur et son bénéficiaire. Keycloak ne corrige pas ce trou tout seul, mais il rend la question du lien identité ↔ matricule incontournable.

| Option | Implications |
|---|---|
| **E-1 — Contrôle strict** | `EnrolementService` compare le matricule soumis à celui de l'utilisateur authentifié, et refuse (403) sinon. Ferme le trou. **Mais** casse le cas d'usage éventuel où un gestionnaire enrôlerait pour le compte d'un tiers — à confirmer avec le métier. |
| **E-2 — Renseigner `idBeneficiaire`** | À la confirmation, le compte utilisateur est lié au bénéficiaire créé. Donne du sens au champ existant, permet un contrôle ultérieur. **Ne ferme pas le trou à lui seul.** |
| **E-3 — Statu quo, documenté** | Le comportement est assumé et écrit noir sur blanc comme accepté. **À n'envisager que si le métier confirme que l'enrôlement pour un tiers est voulu.** |
| **E-4 — Reporter hors MM.7** | Traité dans un sprint dédié. **Risque : la question du mapping d'identité ressurgira de toute façon pendant MM.7.** |

</details>

### 3.2 Question 2 — ~~Direct Access Grant ou Authorization Code + PKCE ?~~ ✅ **TRANCHÉE le 2026-07-31**

> ## ✅ Décision actée — F-2, Authorization Code + PKCE
>
> **La question 2 est résolue par la confirmation apportée sur la section 2.3.** Le responsable projet, qui exerce à la banque, confirme le fonctionnement observé sur la plateforme BAOBAB :
>
> - Le realm Keycloak d'Afriland **est bien relié à l'Active Directory**.
> - Sur BAOBAB, un clic sur « Se connecter » **redirige vers la page de login Keycloak**.
> - Les identifiants utilisés y sont **les identifiants Windows habituels**, les mêmes que pour ouvrir sa session.
>
> C'est la description exacte d'un flux **Authorization Code par redirection**. **F-1 (Direct Access Grant) est donc écartée** : elle suppose que l'application collecte elle-même le mot de passe, ce qui est incompatible avec ce fonctionnement.
>
> **Conséquences concrètes pour l'implémentation :**
>
> 1. Le contrat `AuthProvider.js` — `login(matricule, motDePasse)` — **ne convient plus**. Une redirection n'a pas cette forme. Le contrat doit être élargi (par exemple `login()` sans argument, déclenchant la redirection, plus une méthode de traitement du retour).
> 2. `Login.jsx` : le formulaire matricule + mot de passe est **remplacé par un bouton de redirection**, pour s'aligner sur l'expérience BAOBAB.
> 3. Le routage doit gérer l'**URL de retour** de Keycloak (callback).
> 4. Le travail frontend est **nettement plus important** qu'avec F-1 — c'est le prix de la fidélité au fonctionnement réel.
>
> **Bénéfice majeur** : l'étape provisoire ressemblera au fonctionnement cible. Le jour où la DSI fournit le realm réel, seule l'URL du realm changera — pas le flux.
>
> **La section 2.3 n'est plus une hypothèse** : la fédération Keycloak ↔ Active Directory est confirmée par observation directe sur BAOBAB. Reste à obtenir de la DSI les paramètres du realm (URL, nom du client, mapping des rôles), pas à valider le principe.

### 3.3 Question 3 — ~~Quelle clé d'identité pour `Utilisateur` ?~~ ✅ **TRANCHÉE le 2026-07-31**

> ## ✅ Décision actée — I-2, résolution par `email`
>
> **Confirmation apportée par le responsable projet, à partir de sa propre identité AD :**
>
> ```
> Nom complet AFB : NZALI FANKEM Vladimir
> Identifiant AD  : vladimir_nzali
> Email pro       : vladimir_nzali@afrilandfirstbank.com
>                    └──────┬──────┘
>                       identique à l'identifiant AD
> ```
>
> **L'identifiant AD est exactement la partie locale de l'email professionnel** (avant le `@`). La convention observée est `prenom_nom` (tiret bas), avec troncature au premier élément si le nom de famille est composé (« NZALI FANKEM Vladimir » → `vladimir_nzali`, pas `vladimir_nzali_fankem`).
>
> **Option retenue : I-2 — résolution par `email`.**
>
> - Aucune migration Flyway : `email` est déjà `UNIQUE NOT NULL` sur `Utilisateur`.
> - `AuthenticatedUserService` passe de `findByMatricule(SecurityContextHolder…getName())` à une résolution basée sur le claim `email` du jeton (ou reconstruite à partir de `preferred_username` + domaine, selon ce qu'expose réellement le realm — voir inconnue 2 ci-dessous).
> - L'option I-1 (ajouter un champ `identifiantAd`) devient inutile : la donnée est entièrement déductible de l'email déjà présent.
>
> **Deux inconnues restent, à lever pendant l'implémentation, pas bloquantes pour le realm LOCAL provisoire :**
>
> 1. **Prénoms composés.** L'exemple confirmé tronque le second *nom de famille* (« Fankem »). Le comportement sur un **prénom** composé (« Jean Paul MBARGA ») n'est pas connu : `jeanpaul_mbarga` ou `jean_paul_mbarga` ? À vérifier avec un cas réel ou avec la DSI.
> 2. **Forme exacte du claim.** Le realm peut exposer `preferred_username` comme `vladimir_nzali` seul, ou comme l'email complet `vladimir_nzali@afrilandfirstbank.com`. Change la ligne de résolution (comparaison directe à l'email, ou reconstruction `identifiant + "@afrilandfirstbank.com"`).
>
> **Contrainte `motDePasseHash` — tranchée dans la foulée.** Puisque Keycloak porte désormais l'authentification, ce champ **NOT NULL** n'a plus d'usage. Pour le realm local provisoire, il est conservé tel quel (les utilisateurs de test gardent un hash BCrypt inerte) — **aucune migration n'est nécessaire pour ce sprint**. Le rendre nullable ou le supprimer est reporté à un futur sprint, une fois le realm réel confirmé.
>
> **⚠️ Écart trouvé dans les données de test, corrigé dans ce commit.** `V3__insertion_utilisateurs_test.sql` utilisait un **point** (`jeanpaul.mbarga@…`) au lieu du **tiret bas** de la convention réelle (`jeanpaul_mbarga@…`). Corrigé pour que la résolution par email fonctionne dès le realm local — voir `V3__insertion_utilisateurs_test.sql`.
>
> **Point additionnel signalé, non corrigé ici :** les URL placeholder commitées au Sprint 6F.9 (`k8s/configmap.yaml`, `frontend/.env.production`) utilisent le domaine `.cm`, alors que l'email confirmé est en `.com`. Ces valeurs étaient explicitement marquées « à confirmer avec la DSI » — l'écart est noté pour correction ultérieure, hors périmètre de MM.7.

---

## 4. Mise en œuvre

> **Cette section ne doit pas être exécutée tant que les 3 questions de la section 3 et la décision de portée de la section 2.4 ne sont pas tranchées par écrit.**

### 4.1 Étape 1. Ouvrir la session

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
1. Lis CLAUDE.md dans son integralite, en particulier les sections 13
   (authentification Keycloak) et 14 (deploiement, perimetre DSI).
2. Lis docs/monolithe-modulaire/MM.7_keycloak_provisoire.md EN ENTIER.
3. Confirme-moi que les TROIS questions ouvertes de la section 3 ET
   la decision de portee de la section 2.4 sont tranchees par ecrit.
   Si l'une d'elles est encore ouverte, ARRETE-TOI et dis-le-moi --
   ce sprint ne peut pas demarrer sans.
4. Lance /graphify . --update

CONTEXTE : Sprint MM.7. Remplacer la simulation JWT symetrique
(HS384, cle partagee) par un Keycloak LOCAL et PROVISOIRE declare
dans docker-compose.yml. Le realm reel de la DSI reste hors perimetre
et non confirme.

VARIABLES D'ENVIRONNEMENT (TROIS, pas deux) :
  $env:DB_URL="jdbc:postgresql://localhost:5432/afb_dotations_telephoniques_mm"
  $env:DB_PASSWORD="admin"
  $env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"
Oublier DB_URL fait retomber SILENCIEUSEMENT sur la base du projet
d'origine.

METHODE DE TRAVAIL :
- Une etape a la fois. Tu montres le diff, j'approuve, tu continues.
- mvn test complet apres chaque etape. Reference : >= 205 tests.
- Ce sprint touche a QUI PEUT ENTRER dans l'application. Si tu es
  tente d'assouplir une verification pour "faire marcher" quelque
  chose, ARRETE-TOI et explique-moi.

PREMIERE ACTION : la verification d'espace de travail, puis la
confirmation que les 3 questions + la portee sont tranchees.
```

### 4.2 Étape 2. Keycloak local dans `docker-compose.yml`

```
Ajoute un service Keycloak a docker-compose.yml, a cote du service
kafka existant (apache/kafka:3.8.0, conteneur dottel-kafka, port
9092, present depuis le Sprint 5.3).

Contraintes :
- Image officielle Keycloak, mode developpement (start-dev).
- Port distinct de 8080 (deja pris par le backend) et de 9092.
  Propose 8081 ou 8180, dis-moi lequel et pourquoi.
- Identifiants admin de la console : via variables d'environnement,
  JAMAIS en dur dans le fichier -- meme regle que DB_PASSWORD et
  DOTTEL_JWT_SECRET (CLAUDE.md sections 17.6 et 18.10, et correctif
  E7d/E7e de l'audit 6F.9).
- Un commentaire en tete du service rappelant que ce Keycloak est
  LOCAL et PROVISOIRE, et que le realm reel de la DSI n'est pas
  confirme.

Montre-moi le fichier avant de le lancer.
```

### 4.3 Étape 3. Realm de développement

```
Cree le realm local. Deux approches, propose-moi la tienne :
  - realm exporte en JSON et monte dans le conteneur (versionnable,
    reproductible)
  - configuration manuelle via la console (plus rapide, non
    reproductible)

Le realm doit contenir :
- Un client pour le frontend, configure en Authorization Code + PKCE
  (decision F-2 de la question 2, actee).
- Les CINQ roles de RoleEnum (EMPLOYE, ARH, CRH, DRH, ADMIN) --
  decision de portee P-2 actee section 2.4, Keycloak couvre les
  5 roles sans exception.
- Les 5 utilisateurs de test recrees a l'identique de
  V3__insertion_utilisateurs_test.sql (1847 ARH, 2093 CRH, 1562 DRH,
  2201 EMPLOYE, 1734 ADMIN), sous leur email corrige au format
  prenom_nom@afrilandfirstbank.com (decision I-2, question 3 actee).
  L'identifiant Keycloak de connexion est la partie locale de cet
  email (ex. jeanpaul_mbarga).
```

### 4.4 Étape 4. Bascule de `SecurityConfig`

```
Fais passer SecurityConfig de la cle symetrique a la validation par
issuer.

ETAT ACTUEL (verifie) : SecurityConfig lignes 64-69 declare
  NimbusJwtDecoder.withSecretKey(new SecretKeySpec(jwtSecret, "HmacSHA384"))
          .macAlgorithm(MacAlgorithm.HS384)

CIBLE : validation par issuer-uri et JWKS, avec l'URL du realm en
VARIABLE D'ENVIRONNEMENT -- c'est le point qui rendra la bascule vers
le realm DSI reel triviale.

1. Remplace le bean jwtDecoder() par une configuration issuer-uri.
2. L'URL du realm doit venir d'une variable d'environnement, sans
   valeur de repli codee en dur (meme regle que DOTTEL_JWT_SECRET).
3. Decision de portee P-2 actee (section 2.4) : Keycloak devient
   l'UNIQUE emetteur, pour les 5 roles sans exception. UN SEUL bean
   JwtDecoder, UNE SEULE SecurityFilterChain suffisent -- pas
   d'AuthenticationManagerResolver ni de double chaine a construire.
4. NE TOUCHE PAS a csrf.disable(), SessionCreationPolicy.STATELESS,
   ni au permitAll sur /actuator/health sans me le signaler -- ce
   sont des decisions validees par l'audit 6F.9. Le permitAll sur
   /auth/login, lui, DISPARAIT avec l'endpoint (voir etape 5).

Montre-moi le diff complet.
```

### 4.5 Étape 5. Suppression de `AuthService` et `JwtUtil`

```
Decision de portee P-2 actee : Keycloak couvre les 5 roles, EMPLOYE
compris. AuthService.authentifier() et JwtUtil.genererToken()
deviennent MORTS.

1. Propose-moi leur suppression (AuthService, JwtUtil, et
   AuthController si POST /auth/login disparait entierement -- a
   confirmer selon ce que devient l'endpoint : soit supprime, soit
   transforme en simple redirection vers Keycloak).
2. Signale-moi TOUT ce qui casse -- notamment AuthServiceTest
   (4 tests) et JwtUtilTest (3 tests), qui deviennent obsoletes.
3. Le nombre de tests va DESCENDRE sous 205 : c'est le seul cas
   legitime du chantier ou c'est acceptable. Signale-le explicitement
   et donne-moi le nouveau total.

POST /auth/logout est aujourd'hui stateless et sans liste noire
(contrat API V3.3 section 1). Avec Keycloak, la deconnexion peut
aussi invalider la session cote realm. Signale-moi si le
comportement change, ne le modifie pas silencieusement.
```

### 4.6 Étape 6. Mapping des rôles et de l'identité

```
Etape la plus sensible du sprint.

1. ROLES. RoleJwtAuthenticationConverter lit aujourd'hui un claim
   "role" (singulier, chaine simple) et produit UNE autorite
   ROLE_<role>. Keycloak, lui, expose ses roles dans realm_access.roles
   (un TABLEAU) ou resource_access.<client>.roles.
   Adapte le converter. ATTENTION : un utilisateur Keycloak peut
   porter PLUSIEURS roles, alors que Utilisateur.role est un champ
   UNIQUE en base. Comment resoudre un utilisateur portant a la fois
   ARH et DRH ? POSE-MOI LA QUESTION, ne choisis pas seul -- cela
   toucherait RG-08 (separation des taches).

2. IDENTITE. AuthenticatedUserService resout aujourd'hui par
   findByMatricule(SecurityContextHolder...getName()). Applique la
   decision I-1/I-2/I-3 de la question ouverte 3.
   Verifie ensuite que TOUT ce qui depend de cette resolution
   fonctionne encore : l'audit (RG-09, id_utilisateur NOT NULL avec
   FK), la separation des taches (RG-08), et le champ id_createur des
   processus et grilles.

Montre-moi le mapping propose sous forme de tableau
(claim Keycloak -> champ DOTTEL) AVANT de coder.
```

### 4.7 Étapes 7 et 8. `AuthProviderKeycloak` côté frontend

```
Cree frontend/src/auth/AuthProviderKeycloak.js implementant le
contrat de AuthProvider.js.

RAPPEL DU CONSTAT VERIFIE : AuthContext.jsx ligne 2 importe
DIRECTEMENT le singleton authProviderLocal et l'appelle lignes 18 et
25. Il faudra donc bien TOUCHER AuthContext.jsx, contrairement a ce
que laisse penser son commentaire d'en-tete.

Propose-moi comment rendre le provider interchangeable proprement :
  - selection par variable d'environnement Vite (VITE_AUTH_PROVIDER) ?
  - injection en propriete de AuthContextProvider ?
  Montre-moi les deux, je choisis.

CONTRAINTES A PRESERVER (validees par l'audit 6F.9, ne pas regresser) :
- Le jeton reste EN MEMOIRE SEULE, jamais dans localStorage ni
  sessionStorage (apiClient.js ligne 11).
- Aucun console.log : le frontend en compte ZERO aujourd'hui.
- Le jeton ne doit jamais apparaitre dans une URL ni dans un message
  d'erreur affiche.
- La deconnexion doit purger le jeton de facon inconditionnelle.

Si la decision F-2 (PKCE) a ete retenue, signale-moi tout ce qui doit
changer dans Login.jsx et dans le routage -- une redirection n'a pas
la meme forme qu'un formulaire.
```

### 4.8 Étape 9. Vérification de bout en bout

```
Verification complete.

1. mvn test avec les TROIS variables d'environnement. Donne-moi le
   nombre de tests et signale tout ecart par rapport a 205.

2. Demarrage reel : docker compose up -d (Kafka + Keycloak), puis le
   backend. Verifie que le contexte Spring demarre avec la nouvelle
   configuration de securite.

3. Parcours reel par role, dans le navigateur, pour les CINQ roles
   (EMPLOYE, ARH, CRH, DRH, ADMIN -- decision de portee P-2, tous
   passent desormais par Keycloak) :
   - connexion via redirection Keycloak
   - atterrissage sur la bonne page (rappel : le CRH atterrit sur
     /processus depuis le correctif E1 de l'audit 6F.9)
   - acces a une page autorisee
   - acces refuse a une page non autorisee
   - EMPLOYE en particulier : verifie que /enrolement/verifier et
     /enrolement/confirmer fonctionnent toujours avec un jeton
     Keycloak (le mecanisme d'emission a change, la verification
     @PreAuthorize("hasRole('EMPLOYE')") non)

4. Verifie en base que audit_log continue d'etre alimente
   correctement : id_utilisateur renseigne, adresse_ip NON NULLE,
   detail_json au format {"avant":..., "apres":...} (RG-09).

Montre-moi les resultats reels, pas une conclusion.
```

### 4.9 Étape 10. Documenter la bascule vers le realm DSI

```
Produis une courte section dans le README ou dans un document dedie
listant EXACTEMENT ce qu'il faudra changer le jour ou la DSI fournit
le realm reel.

L'objectif annonce du sprint est que cette liste se limite a des
VARIABLES D'ENVIRONNEMENT. Confirme si c'est reellement le cas apres
implementation, ou liste honnetement ce qui necessitera du code en
plus (par exemple : mapping de roles different, claim d'identite
different, flux F-1 impossible avec une federation Kerberos).

Ne pretends pas que la bascule sera triviale si ton implementation
montre le contraire.
```

---

## 5. Critères de validation

| Élément | Statut attendu |
|---|---|
| Question 1 (enrôlement) tranchée — **E-3 acté le 2026-07-31** | ✅ Fait |
| Question 2 (flux) tranchée — **F-2 / PKCE acté le 2026-07-31** | ✅ Fait |
| Question 3 (clé d'identité `Utilisateur`) tranchée — **I-2 / `email` acté le 2026-07-31** | ✅ Fait |
| Décision de portée (section 2.4) tranchée — **P-2 acté le 2026-07-31** | ✅ Fait |
| Comportement d'enrôlement pour un tiers documenté dans le contrat API (suite de E-3) | Fait |
| Keycloak local déclaré dans `docker-compose.yml`, port distinct de 8080 et 9092 | Vérifié |
| Aucun identifiant Keycloak en dur dans `docker-compose.yml` | Vérifié |
| URL du realm injectée par variable d'environnement, sans repli codé en dur | Vérifié |
| `SecurityConfig` valide les jetons par `issuer-uri` / JWKS | Vérifié |
| Mapping rôles Keycloak → `RoleEnum` documenté sous forme de tableau | Fait |
| Cas d'un utilisateur Keycloak portant plusieurs rôles : question posée, non tranchée seul | Fait |
| `AuthenticatedUserService` résout l'utilisateur par `email` (décision I-2) | Vérifié |
| RG-08 (séparation des tâches) fonctionne toujours après changement d'identité | Vérifié |
| RG-09 : `audit_log.id_utilisateur` renseigné, `adresse_ip` **non nulle** | Vérifié en base |
| `AuthProviderKeycloak` implémente le contrat `AuthProvider` existant | Vérifié |
| Modification de `AuthContext.jsx` limitée au strict nécessaire et documentée | Vérifié |
| Jeton toujours **en mémoire seule** — aucun `localStorage`/`sessionStorage` | Vérifié |
| Toujours **zéro** `console.log` dans `frontend/src` | Vérifié |
| Parcours réel testé pour les **5 rôles** (P-2 : Keycloak couvre EMPLOYE compris) | Vérifié |
| `AuthService` et `JwtUtil` supprimés, `AuthServiceTest`/`JwtUtilTest` retirés en conséquence | Vérifié |
| Écart du nombre de tests par rapport à 205 (suppression légitime) signalé et expliqué | Fait |
| Aucun contrat API modifié sur les 34 endpoints, hors `POST /auth/login` (disparu ou transformé en redirection, P-2) | Vérifié |
| Liste honnête de ce qui restera à faire pour le realm DSI réel | Fait |
| Caractère **local et provisoire** rappelé dans `docker-compose.yml` et le README | Vérifié |

---

## 6. Ce que ce sprint ne fait pas

- **Aucune connexion à un realm externe.** Keycloak tourne en conteneur local, comme Kafka depuis le Sprint 5.3.
- **Aucune demande d'ouverture réseau**, aucune infrastructure à provisionner côté DSI.
- **Aucune anticipation de gateway ni de service registry** (`CLAUDE.md` sections 14 et 18, point 18).
- **Aucune modification du frontend au-delà** de `AuthProviderKeycloak`, du câblage minimal dans `AuthContext.jsx`, et de `Login.jsx` si la décision F-2 est retenue.
- **Aucune migration Flyway**, sauf si la décision I-1 ou le traitement de `motDePasseHash` l'impose — auquel cas c'est une **exception explicitement assumée** au périmètre posé en `PLAN_MONOLITHE_MODULAIRE.md` §5.

## Commit

```bash
git add .
git commit -m "mm.7: keycloak local provisoire en remplacement de la simulation jwt symetrique"
```

---

**Fin du Sprint MM.7**

*Le realm AFB réel reste hors périmètre et non confirmé par la DSI à ce jour. L'hypothèse d'une fédération Active Directory (section 2.3) n'est documentée nulle part dans le projet et doit être validée auprès de la DSI avant toute décision définitive.*
