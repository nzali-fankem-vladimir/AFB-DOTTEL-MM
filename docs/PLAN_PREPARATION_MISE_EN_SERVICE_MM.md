# PLAN — PRÉPARATION À LA MISE EN SERVICE

## Nettoyage, dette technique et bascule vers les ressources réelles (EHR, Keycloak, Outlook, Kafka, infrastructure)

*Module Dotations Téléphoniques Mensuelles — version monolithe modulaire (`afb-dottel-mm`)*

| | |
|---|---|
| **Objet** | Recenser, sans rien inventer au-delà de ce que le code et les guides existants montrent déjà, tout ce qui reste à faire avant un serveur de test puis une mise en production : dette technique confirmée dans le code, et bascule de chaque ressource simulée (EHR, Keycloak, Outlook, Kafka, infrastructure) vers sa version réelle |
| **Livrable** | Ce document, en 6 sous-sprints (MM.14 à MM.19), directement démarrables ou en attente d'un input DSI explicite |
| **Cible** | `afb-dottel-mm` — reste la copie de travail, jamais le dépôt d'origine |
| **Prérequis** | Chantier MM.0 → MM.13 clos (voir `docs/chantier-ajout-metier-mm/`, dernier commit `mm.13`) |
| **Origine** | Audit sécurité `docs/audit_securite_owasp_v1.md`, `docs/monolithe-modulaire/RECAPITULATIF_CHANTIER.md`, `docs/monolithe-modulaire/MM.7_bascule_realm_dsi.md`, `PLAN_AJOUTS_METIER_MM.md` §6, et vérification directe du code au 2026-08-09/10 |

---

## 0. Pourquoi ce document existe

Le lot d'ajouts métier (MM.8 à MM.13) est clos. `CLAUDE.md` section 16 prévoit
ensuite un **Sprint 7 : Tests d'intégration, recette, corrections, README** —
mais ce sprint suppose implicitement que les ressources externes (EHR,
Keycloak, Outlook, Kafka côté comptabilité) sont branchées. Elles ne le sont
pas : tout le développement depuis M.0 tourne sur des **simulations locales**
(`EhrIntegrationServiceStub`, Keycloak local `docker-compose.yml`, SMTP
désactivé par défaut, schéma Kafka provisoire).

Ce document distingue deux natures de travail, pour ne pas les mélanger :

- **Ce qui est du nettoyage pur**, exécutable dès maintenant, sans attendre
  personne (MM.14).
- **Ce qui dépend d'un input externe** (DSI, comptabilité) : le code qui
  changera est largement anticipable et documenté ici, mais la valeur exacte
  (URL de realm, serveur SMTP, codes unité complets, schéma Kafka validé)
  ne l'est pas (MM.15 à MM.18).

Chaque point cité ci-dessous est **vérifié dans le code ou dans un guide
existant** au moment de la rédaction — jamais une supposition générique de
« bonnes pratiques de mise en production ». Si un point n'est backé par
aucune preuve trouvée dans le projet, il n'est pas dans ce document.

---

## 1. Vue d'ensemble

| Sous-sprint | Contenu | Bloquant sur input externe ? | Risque |
|---|---|---|---|
| **MM.14** | Nettoyage de dette technique confirmée : variable morte, manifests K8s désynchronisés, Swagger cassé, dépendances, écarts UX reportés | **Non** — exécutable immédiatement | Faible à moyen |
| **MM.15** | Bascule Keycloak réel | **Oui** — realm, client, mapping de rôles DSI | Moyen à élevé (5 inconnues non tranchées, voir MM.7) |
| **MM.16** | Intégration EHR réelle | **Oui** — accès EHR, nomenclature complète des codes unité, format d'échange | Élevé (aucun accès EHR à ce jour) |
| **MM.17** | Canal Outlook réel + validation du schéma Kafka | **Oui** — serveur SMTP/Graph DSI ; validation du schéma par la comptabilité | Moyen |
| **MM.18** | Infrastructure de déploiement (namespace, secrets, stockage, image, pipeline) | **Oui** — conventions et arbitrages DSI | Moyen à élevé |
| **MM.19** | Recette finale (Sprint 7 de `CLAUDE.md` §16) | Non, une fois MM.15-18 au moins partiellement faits | Faible |

**Ordre recommandé : MM.14 d'abord** (aucune dépendance externe, réduit le
bruit avant de discuter avec la DSI). **MM.15 à MM.18 peuvent être menés en
parallèle** entre eux — ce sont des ressources indépendantes — mais chacun
est bloqué tant que son propre input DSI n'est pas arrivé. **MM.19 ferme la
marche.**

---

## 2. MM.14 — Nettoyage de dette technique confirmée

| | |
|---|---|
| **Objet** | Corriger ce que le code montre déjà comme faux, mort ou désynchronisé, sans attendre aucune ressource externe |
| **Livrable** | Manifests K8s à jour, variable morte retirée, Swagger réparé (ou diagnostic documenté), écarts UX reportés traités ou explicitement re-reportés |
| **Durée** | Une demi-journée à une journée |
| **Prérequis** | Aucun |

### Configuration recommandée

| Étape | Modèle | Effort |
|---|---|---|
| Diagnostic Swagger (dépendances) | Sonnet 5 ou Opus 4.8 | Medium à High |
| Reste (manifests, variable morte, UX) | Sonnet 5 | Low à Medium |

### Étape 1. Ouvrir la session

```
Tu es mon assistant de developpement pour le projet DOTTEL (Afriland
First Bank).

VERIFICATION D'ESPACE DE TRAVAIL, BLOQUANTE : confirme que le repertoire
se termine par "afb-dottel-mm", que `git log --oneline -1` montre un
commit "mm.13" ou plus recent, et que `git remote -v` est vide.

Lis CLAUDE.md en entier, puis
docs/PLAN_PREPARATION_MISE_EN_SERVICE_MM.md section 2 (MM.14) en entier.

CONTEXTE : ce sprint corrige de la dette technique DEJA CONFIRMEE dans le
code -- il ne touche a aucune ressource externe (EHR, Keycloak, Outlook,
Kafka). Chaque point ci-dessous cite l'endroit exact du code ou du
document qui le prouve.

METHODE : un point a la fois, je valide avant que tu passes au suivant.
```

### Étape 2. Variable d'environnement morte — `DOTTEL_JWT_SECRET`

```
VERIFIE D'ABORD, ne fais rien avant confirmation.

Depuis MM.7, Keycloak est l'unique emetteur de jetons : SecurityConfig
construit son JwtDecoder a partir de
spring.security.oauth2.resourceserver.jwt.issuer-uri (DOTTEL_KEYCLOAK_ISSUER_URI),
pas d'un secret partage. JwtUtil.java n'existe plus.

Confirme par une recherche (grep) que DOTTEL_JWT_SECRET n'apparait plus
que dans des commentaires ou de la configuration morte : backend/Dockerfile
(bloc de commentaire ENV), k8s/secret.yaml, et les guides
docs/chantier-ajout-metier-mm/MM.*.md qui citent encore "trois variables"
en l'incluant.

SI CONFIRME :
- Retire DOTTEL_JWT_SECRET du bloc de commentaire du Dockerfile et de
  k8s/secret.yaml (le Secret Kubernetes ne doit plus la porter).
- NE MODIFIE PAS les guides MM.8 a MM.13 deja executes : ce sont des
  comptes-rendus de sprints clos, pas de la documentation vivante. Note
  plutot ici, dans ce document, que ces guides contiennent une variable
  perimee, pour que quiconque les relit plus tard ne soit pas surpris de
  la voir sans effet.
- Ajoute DOTTEL_KEYCLOAK_ISSUER_URI au k8s/configmap.yaml si elle n'y est
  pas deja (elle n'est pas un secret, c'est une URL) -- voir etape 3.

SI TU TROUVES UN USAGE REEL que je n'ai pas vu, ARRETE-TOI et montre-le
moi : ne retire rien tant que ce n'est pas confirme mort.
```

#### Résultat d'exécution (MM.14, 2026-08-19) — `DOTTEL_JWT_SECRET` confirmée morte

Vérification par `grep` sur l'ensemble du dépôt : **aucun usage réel**. La
variable n'apparaît dans aucun des trois `backend/src/main/resources/application*.yml`,
et aucun `@Value` ni `System.getenv()` du code Java ne la lit.
`backend/src/main/java/com/afriland/dottel/security/` ne contient plus que
`SecurityConfig`, `RoleJwtAuthenticationConverter`, `GlobalExceptionHandler` et
`package-info` — `JwtUtil.java` a bien disparu au Sprint MM.7, et
`SecurityConfig` ne déclare plus de `JwtDecoder` manuel : Spring Boot le
construit depuis `spring.security.oauth2.resourceserver.jwt.issuer-uri`.

Retirée de `backend/Dockerfile` (bloc de commentaire `ENV`) et de
`k8s/secret.yaml`, avec dans chacun un commentaire expliquant *pourquoi* elle
disparaît, afin que son absence ne passe pas pour un oubli.

> **Avertissement pour la relecture des guides antérieurs.** Les guides de
> sprints **clos** citent encore `DOTTEL_JWT_SECRET` dans leur bloc de
> commandes de démarrage (`$env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-..."`),
> souvent introduit par la formule « les trois variables d'environnement » :
> `docs/monolithe-modulaire/MM.0` à `MM.7`, `PLAN_MONOLITHE_MODULAIRE.md`,
> `docs/chantier-ajout-metier-mm/MM.8` à `MM.13`, `PLAN_AJOUTS_METIER_MM.md`,
> et `PLAN_REMEDIATION_AUDIT_CONNEXION.md`. Ces fichiers sont des
> **comptes-rendus de sprints terminés**, pas de la documentation vivante :
> ils ne sont volontairement **pas** modifiés. Définir cette variable
> aujourd'hui est simplement **sans effet** — elle n'est plus lue par rien.
> Ce qu'il faut réellement exporter depuis MM.7, c'est
> **`DOTTEL_KEYCLOAK_ISSUER_URI`** (sans valeur de repli : son absence fait
> échouer le démarrage, y compris `contextLoads`), en plus de `DB_URL`,
> `DB_USER` et `DB_PASSWORD`. Les mentions résiduelles dans
> `KafkaConfig.java`, `application.yml` et `UtilisateurAdminControllerTest.java`
> sont des **commentaires d'analogie** (« même règle que… ») et non des
> lectures de la variable : elles sont conservées telles quelles.

### Étape 3. Manifests Kubernetes désynchronisés des variables réellement lues

```
Compare la liste des variables reellement lues par le backend
(grep -rn '\$\{[A-Z_]*' backend/src/main/resources/*.yml) avec
k8s/configmap.yaml et k8s/secret.yaml. Au moment de la redaction de ce
document, l'ecart constate est :

MANQUANTES DE configmap.yaml : DB_USER, KAFKA_BOOTSTRAP_SERVERS,
DOTTEL_KEYCLOAK_ISSUER_URI, DOTTEL_DOCUMENTS_CHEMIN_STOCKAGE,
DOTTEL_DOCUMENTS_CHAPITRE_DEFAUT, DOTTEL_NOTIFICATIONS_ENABLED,
DOTTEL_NOTIFICATIONS_EXPEDITEUR, DOTTEL_MAIL_HOST, DOTTEL_MAIL_PORT.

MANQUANTES DE secret.yaml : DOTTEL_MAIL_USERNAME, DOTTEL_MAIL_PASSWORD
(secrets), DB_USER (discutable : ni un mot de passe ni une URL, evalue
si ConfigMap ou Secret est le bon choix -- DB_PASSWORD y est deja).

Complete les deux manifests avec des valeurs PLACEHOLDER explicites,
jamais de vraies valeurs (meme regle que le CHANGEME deja present dans
secret.yaml). Pour DOTTEL_KEYCLOAK_ISSUER_URI, DOTTEL_MAIL_HOST et
DOTTEL_NOTIFICATIONS_EXPEDITEUR : marque-les "A CONFIRMER DSI" en
commentaire -- ce sont exactement les points bloquants de MM.15 et MM.17.

Verifie aussi backend/Dockerfile : son bloc de commentaire ENV (qui
documente, sans les declarer en dur, les variables attendues au runtime)
doit lister la meme liste complete, pour rester une reference fiable.
```

#### Résultat d'exécution (MM.14, 2026-08-19) — manifests réalignés

Liste de référence recomptée sur le code : **14 variables** réellement lues par
`backend/src/main/resources/application*.yml`, plus `SPRING_PROFILES_ACTIVE`.
Aucun `@Value` ni `System.getenv()` du code Java ne lit de variable
supplémentaire. Les manifests couvrent désormais exactement ces 15 clés,
**11 au ConfigMap + 4 au Secret**, sans doublon entre les deux (les deux sont
injectés par `envFrom` dans le même conteneur, une clé en double y serait un
piège silencieux).

| Ajouté à `configmap.yaml` | Ajouté à `secret.yaml` |
|---|---|
| `DOTTEL_KEYCLOAK_ISSUER_URI` *(A CONFIRMER DSI)* | `DB_USER` |
| `KAFKA_BOOTSTRAP_SERVERS` | `DOTTEL_MAIL_USERNAME` |
| `DOTTEL_DOCUMENTS_CHEMIN_STOCKAGE` | `DOTTEL_MAIL_PASSWORD` |
| `DOTTEL_DOCUMENTS_CHAPITRE_DEFAUT` | *(`DOTTEL_JWT_SECRET` retirée)* |
| `DOTTEL_NOTIFICATIONS_ENABLED` | |
| `DOTTEL_NOTIFICATIONS_EXPEDITEUR` *(A CONFIRMER DSI)* | |
| `DOTTEL_MAIL_HOST` *(A CONFIRMER DSI)* | |
| `DOTTEL_MAIL_PORT` | |

**`DB_USER` : Secret, pas ConfigMap.** Le document laissait le choix ouvert.
Retenu : le Secret. C'est la moitié d'un couple d'identification, indissociable
de `DB_PASSWORD` qui s'y trouve déjà, et `CLAUDE.md` section 14 le range
explicitement parmi les valeurs sensibles à injecter par Secret. `DB_URL` reste
au ConfigMap : une URL de service interne au cluster n'authentifie personne. Le
raisonnement est écrit dans `secret.yaml` même, pour qu'il ne soit pas à
redécouvrir.

Les trois points bloqués sur la DSI portent le commentaire `A CONFIRMER DSI`
avec le sous-sprint concerné (MM.15 pour le realm, MM.17 pour le canal mail).
Toutes les valeurs sont des placeholders explicites ; les quatre entrées du
Secret restent le `CHANGEME` encodé en base64, vérifié par décodage.

Le bloc de commentaire `ENV` de `backend/Dockerfile` a été réécrit sur la même
liste, ventilé ConfigMap / Secret, avec la raison pour laquelle ces variables
ne sont **pas** déclarées en `ENV` — une valeur vide *résoudrait* le placeholder
Spring au lieu de le laisser échouer, transformant une erreur de configuration
explicite en panne opaque.

### Étape 4. Swagger/springdoc cassé

```
Verifie l'etat actuel : demarre le backend et appelle
GET /api/swagger-ui.html et GET /api/v3/api-docs. Un etat de reference
existe (memoire de session anterieure) : springdoc-openapi-starter-webmvc-ui
2.6.0 est incompatible avec Spring Boot 4.1.0 (NoSuchMethodError sur
ControllerAdviceBean), et un bump vers 3.1.0 avait ete teste puis annule
(73 erreurs de test, conflit ByteBuddy/Mockito).

NE RETENTE PAS un simple bump de version. Lance
`.\mvnw.cmd dependency:tree` et identifie precisement quelle dependance
tire quelle version de ByteBuddy en conflit avec Mockito. Propose soit
une exclusion Maven ciblee, soit une version de springdoc plus recente
que 3.1.0 si une est sortie depuis, en verifiant DANS L'ARBRE que le
conflit ne se reproduit pas avant de l'appliquer.

Si aucune solution propre n'est trouvee dans le temps imparti,
documente l'etat exact (version tentee, erreur precise, arbre de
dependance en cause) plutot que de laisser un correctif fragile. Swagger
n'est pas bloquant pour l'API elle-meme (verifie que les endpoints
fonctionnent hors Swagger), donc un report documente est acceptable --
mais un etat "j'ai laisse en l'etat sans savoir pourquoi" ne l'est pas.
```

#### Résultat d'exécution (MM.14, 2026-08-19) — Swagger RÉPARÉ, la note antérieure était périmée

**État constaté avant correction**, backend démarré, springdoc 2.6.0 :
`GET /api/v3/api-docs` renvoyait **HTTP 500**. `GET /api/swagger-ui.html`
redirigeait bien vers `/api/swagger-ui/index.html`, mais la page était inerte,
sa spécification étant en erreur. Les endpoints de l'API, eux, répondaient
normalement — Swagger n'était bien pas bloquant.

**Diagnostic par l'arbre de dépendances, avant toute modification.** Comparaison
`mvn dependency:tree` entre 2.6.0 et 3.1.0 :

| | springdoc 2.6.0 | springdoc 3.1.0 |
|---|---|---|
| `net.bytebuddy:byte-buddy` | 1.18.10 | **1.18.10** |
| `net.bytebuddy:byte-buddy-agent` | 1.18.10 | **1.18.10** |
| `org.mockito:mockito-core` | 5.23.0 | **5.23.0** |

**Aucun conflit ByteBuddy/Mockito.** Le diff complet des deux arbres tient en
quatre lignes : `swagger-core-jakarta` 2.2.22 → 2.2.52, `org.webjars:swagger-ui`
5.17.14 → 5.32.11, et l'ajout de `webjars-locator-lite:1.1.3`. Rien d'autre ne
bouge. Élément décisif : **`springdoc-openapi:3.1.0` a pour parent
`spring-boot-starter-parent:4.1.0`**, exactement la version du projet — c'est la
ligne alignée sur Spring Boot 4. Vérifié aussi sur Maven Central : 3.1.0 est la
dernière version publiée, il n'existe rien de plus récent à essayer.

La note de session antérieure (« bump 3.1.0 → 73 erreurs Mockito/ByteBuddy »)
**ne se reproduit pas** et doit être considérée comme périmée : elle décrivait
vraisemblablement un état antérieur de l'arbre, avant une montée de version de
Spring Boot ou de Maven.

**Correctif appliqué** : `springdoc-openapi-starter-webmvc-ui` 2.6.0 → **3.1.0**,
version unique modifiée dans `pom.xml`, sans aucune exclusion Maven.

**État vérifié après correction**, backend redémarré :

| Contrôle | Résultat |
|---|---|
| `GET /api/v3/api-docs` | **HTTP 200** — OpenAPI 3.1.0, **37 chemins, 42 opérations, 50 schémas** |
| `GET /api/swagger-ui.html` | **302 → `/api/swagger-ui/index.html`, HTTP 200**, Swagger UI 5.32.11 servie |
| Suite de tests backend | **282 tests, 0 échec, 0 erreur — BUILD SUCCESS** |
| dont `DottelApplicationTests` | contexte Spring complet chargé (12,9 s), springdoc auto-configuré compris |
| dont `ModularityTests` | frontières Spring Modulith intactes |

Les 42 opérations exposées correspondent exactement au décompte de
`contrats_api_dotations_v3.md`. **Aucun report n'est donc nécessaire sur ce
point : il est clos, pas documenté-en-attente.**

#### Complément (MM.14, 2026-08-19, après retour utilisateur) — Swagger utilisable, pas seulement en ligne

La réparation ci-dessus rendait `/api/v3/api-docs` valide et Swagger UI
servie, mais un test manuel a révélé un problème pratique non couvert par le
diagnostic de dépendances : **aucun bean OpenAPI n'existait**, donc
`components.securitySchemes` était vide et `info` portait les valeurs par
défaut de springdoc (`"OpenAPI definition"` / `"v0"`). Conséquence concrète,
reproduite : cliquer "Try it out" sur `POST /auth/logout` sans jeton renvoyait
401 avec le corps « Undocumented » — pas un bug de l'endpoint, mais l'absence
totale de bouton « Authorize » dans Swagger UI pour y joindre un jeton, et
l'absence de toute réponse 401/403 déclarée dans la spécification.

**Corrigé par un unique bean, `com.afriland.dottel.config.OpenApiConfig`** :
- Schéma de sécurité `bearerAuth` (HTTP Bearer JWT) déclaré globalement, ce
  qui fait apparaître le bouton « Authorize » dans Swagger UI et le cadenas
  sur chaque endpoint protégé.
- `info.title`/`version`/`description` alignés sur
  `contrats_api_dotations_v3.md` (V3.7) au lieu des valeurs par défaut.
- Un `GlobalOperationCustomizer` ajoute une réponse **401** et **403**
  documentée à chacune des 42 opérations — les deux codes que le modèle de
  sécurité peut produire uniformément (`SecurityConfig.anyRequest()
  .authenticated()` + `@PreAuthorize` par endpoint). Les codes métier
  (400/404/409, propres à chaque endpoint) restent en prose dans le contrat,
  pas généralisés ici : ce serait inexact pour les endpoints qui ne les
  produisent jamais.

**Nettoyage associé, `SecurityConfig.java`** : le matcher
`.requestMatchers("/auth/login").permitAll()` était inerte — cet endpoint
n'existe plus depuis MM.7 (`AuthController` ne déclare plus que
`/auth/logout`). Retiré, avec un commentaire expliquant pourquoi.

**Vérifié après correctif** : `GET /api/v3/api-docs` déclare
`securitySchemes.bearerAuth` et `security: [{bearerAuth: []}]` ; les 42
opérations portent toutes une réponse 401 et 403 documentée ;
`POST /auth/logout` sans jeton → 401 (comportement correct, désormais
documenté) ; `POST /auth/logout` avec un jeton valide → 200. Suite backend
toujours à **282 tests, 0 échec**.

**Non-sujet, pour mémoire** : l'absence de `POST /auth/login` dans Swagger
n'est pas un manque — cet endpoint a été supprimé au Sprint MM.7 (Keycloak
émet le jeton par redirection, hors de cette API), `AuthController` ne le
déclare plus, et `contrats_api_dotations_v3.md` v3.6 documente déjà ce
retrait.

### Étape 5. Dépendances frontend

```
Deux points a verifier, PAS a supposer :

1. `npm audit` (frontend/) : au moment de la redaction, une vulnerabilite
   haute sur `nanoid` (transitive, via une dependance de build), corrigee
   par `npm audit fix` sans rupture attendue. Verifie l'etat courant
   (l'ecosysteme evolue) et applique le correctif si le meme constat tient.

2. `react-hot-toast` : present dans package.json, VALIDE explicitement
   comme dependance par CLAUDE.md section 2, mais AUCUN usage trouve dans
   frontend/src au moment de la redaction (verifie par toi-meme, ne fais
   pas confiance a cette note perimee). Si toujours inutilise :
   NE LE RETIRE PAS unilateralement, CLAUDE.md l'a explicitement valide
   pour un usage futur (notifications utilisateur). Signale-le-moi et
   attends ma decision -- integrer maintenant les notifications qu'il est
   cense porter, ou le retirer si l'usage est definitivement abandonne.
```

#### Résultat d'exécution (MM.14, 2026-08-19) — dépendances frontend

**1. `npm audit` — même constat, correctif appliqué.** L'état courant confirme
la note : une seule vulnérabilité, **haute**, sur `nanoid < 3.3.18`
(GHSA-2v37-7h3g-55p8, boucle infinie d'un générateur personnalisé appelé avec
`size` nul), transitive et en portée `dev` uniquement. `npm audit fix` la
résout par une montée **3.3.16 → 3.3.18** : trois lignes changées dans
`package-lock.json`, aucune dans `package.json`. Après correctif :
**0 vulnérabilité**, `npm run build` et `npx oxlint` toujours au vert.

**2. `react-hot-toast` — toujours inutilisé, conservé sur décision.** Vérifié
directement : **zéro import** dans `frontend/src`. Le retour utilisateur passe
aujourd'hui par un composant maison `components/ui/Alert.jsx`, posé en ligne
dans une vingtaine de pages. **Décision de l'utilisateur, 2026-08-19 : le
laisser en place sans l'intégrer** — `CLAUDE.md` section 2 l'a explicitement
validé pour un usage futur, il n'a aucun effet sur le bundle livré
(élimination du code mort), et l'intégration des notifications relève du
chantier design frontend (D.2/D.3), pas d'un sprint de dette technique. **Aucune
suppression unilatérale.**

**Correction d'une note périmée au passage.** L'audit citait `jwt-decode` comme
inutilisé au même titre que `react-hot-toast`. C'est faux depuis MM.7 : il est
bien utilisé par `src/auth/AuthProviderKeycloak.js` pour décoder les claims du
jeton Keycloak. `docs/audit_securite_owasp_v1.md` section 4 a été corrigé.

### Étape 6. Écarts UX/sécurité déjà identifiés par l'audit, jamais traités

```
docs/audit_securite_owasp_v1.md section 4 liste des ecarts VOLONTAIREMENT
reportes, chacun avec sa justification. Ne les retraite pas tous en
silence -- certains sont des decisions METIER, pas des bugs :

- E2/E3 (acces DRH aux Beneficiaires, acces ADMIN a la creation de
  grille) : DECISIONS FONCTIONNELLES, pose-moi la question, ne tranche
  pas seul.
- E4 (route de repli "*" renvoie vers /login meme authentifie),
  E5 (AccesInterdit hors charte visuelle, sans lien de retour) :
  corrections cosmetiques a faible risque, tu peux les traiter
  directement.
- E7a (/actuator/health expose son detail sans authentification) :
  EXIGE par les sondes Kubernetes (CLAUDE.md section 14) -- ne le
  restreins pas sans arbitrage DSI (isolation reseau du endpoint au
  cluster, plutot que suppression de show-details).

Verifie aussi l'observation residuelle de l'audit : application-dev.yml
porte encore un mot de passe de repli en clair (`${DB_PASSWORD:postgres}`).
Developpement uniquement, faible priorite, mais corrige-le si c'est
gratuit (retirer le repli, exiger la variable comme en profil prod).
```

#### Résultat d'exécution (MM.14, 2026-08-19) — écarts UX/sécurité

Le détail complet des décisions et des corrections est écrit dans
**`docs/audit_securite_owasp_v1.md` section 4.1**, à côté des écarts qu'il
résout. Synthèse :

| Écart | Traitement |
|---|---|
| **E2** — DRH et écran Bénéficiaires | Question posée. **Décision utilisateur : ouvrir à la DRH en lecture seule.** Route et entrée de menu ouvertes à `ARH, DRH` ; les quatre actions d'écriture restent conditionnées à `ARH` (la colonne « Actions » disparaît pour la DRH) ; `GET /beneficiaires/unites-rattachement` ouvert à la DRH pour que son filtre fonctionne. `/beneficiaires/import` reste ARH seul. |
| **E3** — ADMIN et création de grille | Question posée. **Décision utilisateur : garder l'ARH seul et aligner doc + backend.** `ADMIN` retiré de `POST` et `PATCH /grilles-tarifaires` (contrôleur, `CLAUDE.md` section 8, contrats API V3.7). Vérifié à la demande de l'utilisateur : la création d'une fonction éligible par l'ADMIN passe par `FonctionEligibleService`, pas par cet endpoint — **elle n'est pas affectée**. |
| **E4** — route de repli `*` | **Corrigé directement.** Nouveau `RepliRoute` : `/login` si non authentifié, nouvelle page `PageIntrouvable` (dans la charte, sous `AppLayout`) si authentifié. |
| **E5** — `AccesInterdit` | **Corrigé directement.** Page réécrite dans la charte, avec le rôle nommé et un bouton de retour vers l'accueil du rôle. `ROUTE_PAR_ROLE` extraite dans `src/router/routeParRole.js`, source unique pour les trois écrans qui en dépendent. |
| **E7a** — `/actuator/health` | **Non modifié, comme exigé.** `show-details: always` et `permitAll` intacts, `SecurityConfig.java` sans aucune modification au sprint. Reste un arbitrage DSI (isolation réseau plutôt que suppression du détail). |
| Repli `${DB_PASSWORD:postgres}` | **Corrigé** — c'était effectivement gratuit. `application-dev.yml` exige désormais `DB_PASSWORD`, même écriture qu'en profil `prod`. `DB_USER` garde son repli : `postgres` est le nom de compte par défaut de PostgreSQL, pas un secret. **Nuance vérifiée sur le tas** : sans la variable, l'échec au démarrage est franc mais se présente en erreur d'authentification PostgreSQL, pas en `Could not resolve placeholder` — `@ConfigurationProperties` tolère les placeholders non résolus là où `@Value` les refuse. Détail et conséquence dans `docs/audit_securite_owasp_v1.md` section 4. |

### Critères de validation

| Élément | Statut attendu |
|---|---|
| `DOTTEL_JWT_SECRET` confirmée morte ou usage réel signalé | Fait |
| Retirée du Dockerfile et de `k8s/secret.yaml` si confirmée morte | Vérifié |
| `k8s/configmap.yaml` et `k8s/secret.yaml` alignés sur les variables réellement lues | Vérifié |
| Points bloqués sur DSI marqués explicitement dans les manifests | Vérifié |
| État de Swagger diagnostiqué avec l'arbre de dépendances, corrigé ou documenté | Fait |
| `npm audit` réévalué, correctifs non cassants appliqués | Fait |
| `react-hot-toast` : question posée, pas de suppression unilatérale | Fait |
| E2/E3 : question posée au métier, pas tranché seul | Fait |
| E4/E5 corrigés | Vérifié |
| E7a : non modifié sans arbitrage DSI | Vérifié |
| Suite backend et frontend toujours au vert après ce sprint | Vérifié |

### Commit

```bash
git add .
git commit -m "mm.14: nettoyage dette technique -- variable morte, manifests k8s, ecarts audit"
```

---

## 3. MM.15 — Bascule vers le realm Keycloak réel

| | |
|---|---|
| **Objet** | Remplacer le Keycloak local et provisoire par le realm réel de la DSI |
| **Livrable** | Variables d'environnement basculées, points d'incertitude de `MM.7_bascule_realm_dsi.md` tranchés avec la DSI, code ajusté si l'un d'eux l'exige |
| **Durée** | Dépend entièrement de la réactivité DSI ; le travail de code lui-même est court (quelques heures) une fois les réponses obtenues |
| **Prérequis** | **Bloquant** : réponses DSI aux 5 points de la section 2 de `docs/monolithe-modulaire/MM.7_bascule_realm_dsi.md` |
| **Origine** | `docs/monolithe-modulaire/MM.7_bascule_realm_dsi.md` (déjà rédigé, ne pas dupliquer — ce sous-sprint l'exécute) |

### Ce qui est déjà documenté, à ne pas refaire

`MM.7_bascule_realm_dsi.md` liste déjà :
1. Les 4 variables purement de configuration (`DOTTEL_KEYCLOAK_ISSUER_URI`
   backend, `VITE_KEYCLOAK_URL`/`VITE_KEYCLOAK_REALM`/`VITE_KEYCLOAK_CLIENT_ID`
   frontend).
2. Les 5 hypothèses prises pour le realm local, à vérifier avant bascule
   (forme du claim de rôle, claim `matricule`, forme exacte du claim
   `email`, configuration du client côté realm réel, mode de fédération AD).

**Ce document n'y ajoute rien de neuf** — ce sous-sprint est le moment où on
l'exécute, une fois les réponses obtenues.

### À demander à la DSI avant de démarrer

- URL du realm réel, nom du realm, nom du client à créer côté Keycloak DSI.
- Réponse aux 5 points d'incertitude listés ci-dessus.
- URL de callback de production (`https://<domaine-prod>/auth/callback`) à
  faire enregistrer côté client Keycloak par la DSI.

### Étape 1. Ouvrir la session (une fois les réponses DSI en main)

```
Tu es mon assistant de developpement pour le projet DOTTEL.

VERIFICATION D'ESPACE DE TRAVAIL, BLOQUANTE (voir modele des sprints MM.*).

Lis docs/monolithe-modulaire/MM.7_bascule_realm_dsi.md EN ENTIER -- c'est
le document de reference, deja redige, pour ce sprint.

Voici les reponses obtenues de la DSI aux 5 points de sa section 2 :
[COLLER ICI LES REPONSES DSI]

Applique les changements de configuration (etape 1 du guide MM.7). Pour
chaque point ou la reponse DSI diverge de l'hypothese prise localement
(RoleJwtAuthenticationConverter, claim matricule, resolution par email
dans AuthenticatedUserService), ARRETE-TOI et montre-moi le changement de
code necessaire avant de l'appliquer -- ce sont des points qui n'etaient
PAS de simples variables, le guide le dit explicitement.
```

### Point à trancher pendant ce sprint : `motDePasseHash`

```
MM.7_keycloak_provisoire.md section 3.3 a explicitement reporte ce point :
"Le rendre nullable ou le supprimer est reporte a un futur sprint, une
fois le realm reel confirme." Ce sprint EST ce futur sprint.

Une fois le realm reel bascule et l'authentification locale BCrypt
definitivement hors d'usage, pose-moi la question : rendre la colonne
motDePasseHash nullable, ou la supprimer avec une migration Flyway
V8. Ne tranche pas seul -- CLAUDE.md section 4 dit explicitement de ne
pas modifier le schema sans accord explicite.
```

### Critères de validation

| Élément | Statut attendu |
|---|---|
| Les 5 points d'incertitude de MM.7 tranchés avec la DSI | Fait, réponses consignées |
| Variables Keycloak basculées vers le realm réel (backend + frontend) | Vérifié |
| Code ajusté uniquement sur les points où la réponse DSI diverge de l'hypothèse locale | Vérifié |
| `motDePasseHash` : décision prise avec l'utilisateur | Fait |
| Cycle de connexion testé pour chaque rôle contre le realm réel | Vérifié |
| Realm local (`docker-compose.yml`, `keycloak/realm-dottel-dev.json`) conservé pour le développement, pas supprimé | Vérifié |

### Commit

```bash
git add .
git commit -m "mm.15: bascule vers le realm keycloak reel de la dsi"
```

---

## 4. MM.16 — Intégration EHR réelle

| | |
|---|---|
| **Objet** | Remplacer `EhrIntegrationServiceStub` par une implémentation branchée sur l'EHR réel de la banque |
| **Livrable** | Nouvelle implémentation de `EhrIntegrationService`, table de correspondance codes unité complète, table de correspondance libellés EHR ↔ `fonction_eligible.code` |
| **Durée** | Dépend du format d'échange fourni par la DSI (API REST ? export périodique ? base partagée ?) — inconnu à ce jour |
| **Prérequis** | **Bloquant** : accès technique à l'EHR, documentation de son format d'échange, liste complète des codes unité |
| **Origine** | `CLAUDE.md` section 12, `EhrIntegrationServiceStub.java`, `PLAN_AJOUTS_METIER_MM.md` §6 |

### Ce que le stub actuel prouve qu'il faut reproduire

`EhrIntegrationService` (interface, `beneficiaires/service/`) n'expose que
deux méthodes — la future implémentation doit satisfaire le **même contrat**,
rien de plus :

```java
Optional<EmployeEhrDto> rechercherEmploye(String matricule);
List<UniteRattachementDto> listerUnitesRattachement();
```

`EmployeEhrDto` porte : nom, prénom, fonction (à faire correspondre à un
`fonction_eligible.code`), grade, unité de rattachement, **code unité (4
chiffres)**, **code agence (5 chiffres)**, numéro de compte courant,
chapitre. Chacun de ces champs doit avoir une source confirmée côté EHR réel
avant de coder quoi que ce soit.

### État confirmé vs provisoire (à ne pas confondre)

| Donnée | État |
|---|---|
| Codes agence (25, à 5 chiffres) | **Confirmés par le métier**, intégrés en MM.10 — pas d'action ici sauf si la liste réelle en contient d'autres (le métier a signalé qu'il en existe au-delà de 25) |
| Code unité `DSI = 4060` | **Seul code unité confirmé** à ce jour |
| Tous les autres codes unité | **Provisoires** (données de stub), à remplacer intégralement |
| Correspondance libellé EHR ↔ `fonction_eligible.code` | Aucune confirmée — le stub utilise directement les codes internes, l'EHR réel utilisera vraisemblablement sa propre nomenclature |

### À demander à la DSI avant de démarrer

- Nature de l'accès : API REST synchrone, export de fichier périodique,
  base de données partagée en lecture ?
- Authentification de cet accès (encore un secret à externaliser en
  variable d'environnement, jamais en dur — même règle que le reste).
- Liste complète des codes unité (4 chiffres).
- Nomenclature exacte des libellés de fonction/poste utilisés par l'EHR,
  pour construire la table de correspondance vers `fonction_eligible.code`.
- Confirmation qu'au-delà des 25 codes agence connus, la liste est bien
  complète, ou obtention de la liste complète.

### Étape 1. Ouvrir la session (une fois l'accès EHR et sa documentation en main)

```
Tu es mon assistant de developpement pour le projet DOTTEL.

VERIFICATION D'ESPACE DE TRAVAIL, BLOQUANTE.

Lis CLAUDE.md section 12, puis
backend/src/main/java/com/afriland/dottel/beneficiaires/service/EhrIntegrationService.java
et EhrIntegrationServiceStub.java EN ENTIER -- c'est le contrat exact a
respecter, ne l'etends pas et ne le reduis pas sans m'en parler.

Voici la documentation d'acces a l'EHR reel fournie par la DSI :
[COLLER ICI : nature de l'acces, authentification, format d'echange]

Voici la liste complete des codes unite : [COLLER ICI]
Voici la nomenclature des libelles de fonction EHR : [COLLER ICI]

Propose une nouvelle implementation de EhrIntegrationService (nom a
convenir, ex. EhrIntegrationServiceReel), qui respecte le contrat de
l'interface EXACTEMENT. Ne supprime pas EhrIntegrationServiceStub : garde-le
disponible pour les tests et le developpement local, actif par une
configuration explicite (meme principe que
NotificationServiceStub/NotificationServiceSmtp, MM.13).

Tout parametre d'acces (URL, cle, identifiants) va en variable
d'environnement, jamais en dur (CLAUDE.md section 17.6/18.10).
```

### Critères de validation

| Élément | Statut attendu |
|---|---|
| Contrat `EhrIntegrationService` respecté à l'identique | Vérifié |
| `EhrIntegrationServiceStub` conservé, activable pour le développement | Vérifié |
| Table de correspondance codes unité complète, plus aucune valeur provisoire | Fait |
| Table de correspondance libellés EHR ↔ `fonction_eligible.code` documentée | Fait |
| Accès EHR authentifié via variables d'environnement | Vérifié |
| Cas d'un matricule inconnu de l'EHR géré (RG-01 : refus NON ELIGIBLE) | Vérifié par test |
| Tests d'intégration contre l'EHR réel (ou un environnement de recette EHR) | Fait |

### Commit

```bash
git add .
git commit -m "mm.16: integration ehr reelle"
```

---

## 5. MM.17 — Canal Outlook réel et validation du schéma Kafka

| | |
|---|---|
| **Objet** | Basculer les notifications vers un vrai serveur mail, et faire valider par la comptabilité le schéma provisoire de l'événement Kafka enrichi |
| **Livrable** | `dottel.mail.*` pointant vers un serveur réel, schéma Kafka confirmé ou ajusté selon retour comptabilité |
| **Durée** | Le code est déjà prêt (MM.13) — ce sprint n'est que de la configuration et de la validation, sauf si la comptabilité demande un ajustement de schéma |
| **Prérequis** | **Bloquant** : paramètres SMTP/Graph réels (DSI), validation formelle du schéma par la comptabilité |
| **Origine** | `MM.13_notifications_et_kafka.md` (décision D de M.0 : schéma « à valider avec la comptabilité ») |

### Ce qui est déjà prêt, à ne pas refaire

MM.13 a livré l'abstraction complète : `NotificationService`/
`NotificationServiceStub`/`NotificationServiceSmtp` (module `notifications`),
`dottel.notifications.enabled` désactivé par défaut, tous les paramètres déjà
externalisés (`DOTTEL_MAIL_HOST`, `DOTTEL_MAIL_PORT`, `DOTTEL_MAIL_USERNAME`,
`DOTTEL_MAIL_PASSWORD`, `DOTTEL_NOTIFICATIONS_EXPEDITEUR`). **Ce sous-sprint
ne code rien de nouveau côté notifications** — sauf si la DSI impose l'API
Microsoft Graph plutôt que SMTP, ce qui redeviendrait un vrai sous-sprint de
code (voir la question posée et tranchée en MM.13 : SMTP a été choisi
justement pour éviter cette éventualité).

Côté Kafka, `EvenementClotureDto` porte déjà le schéma enrichi, marqué en
commentaire « provisoire, à valider avec la comptabilité » — exactement la
décision D de M.0. Ce sous-sprint est le moment de cette validation.

### À demander avant de démarrer

- DSI : serveur SMTP réel (host, port), identifiants de service, adresse
  d'expédition réelle. Ou confirmation qu'un canal Microsoft Graph est
  imposé à la place.
- Comptabilité : validation formelle du schéma JSON documenté dans
  `docs/reference/contrats_api_dotations_v3.md` section « Événement Kafka de
  clôture ». En particulier : les noms de champs conviennent-ils tels
  quels, le format de `dateCloture` (ISO 8601 local, sans fuseau) est-il
  exploitable côté comptabilité, `KAFKA_BOOTSTRAP_SERVERS` de l'environnement
  cible.

### Étape 1. Bascule Outlook (si SMTP confirmé, cas simple)

```
Tu es mon assistant de developpement pour le projet DOTTEL.

VERIFICATION D'ESPACE DE TRAVAIL, BLOQUANTE.

La DSI a fourni ces parametres SMTP reels :
[COLLER ICI : host, port, identifiants, adresse d'expedition]

Verifie que la configuration existante (application.yml, dottel.mail.*)
les accepte sans modification de code -- c'est cense etre le cas depuis
MM.13, le but de ce sprint est de le CONFIRMER, pas de recoder. Si un
ajustement de code s'avere necessaire (ex. authentification OAuth2 au
lieu d'un mot de passe SMTP classique), ARRETE-TOI et signale-le : ce
serait un ecart par rapport a la conception validee en MM.13, pas une
simple bascule de configuration.

Demarre le backend avec DOTTEL_NOTIFICATIONS_ENABLED=true et les vraies
variables SMTP, declenche un cycle de validation, et CONFIRME reellement
la reception d'un email (pas seulement l'absence d'erreur cote serveur).
```

### Étape 2. Si Microsoft Graph est imposé à la place de SMTP

```
Ce cas ROUVRE une decision prise en MM.13 (SMTP retenu plutot que Graph,
pour eviter un point DSI Azure supplementaire). Si la DSI impose
neanmoins Graph :

Implemente une nouvelle classe derriere l'abstraction NotificationService
existante (module notifications/service/), sur le meme modele que
NotificationServiceSmtp -- meme interface, meme comportement
transactionnel (AFTER_COMMIT + try/catch, decisions N-1/N-2 de MM.13, ne
pas les rouvrir). Conserve NotificationServiceSmtp et
NotificationServiceStub : ne les supprime pas, meme si Graph devient
l'implementation active. Tout parametre Azure (tenant, client id, secret,
scope) en variable d'environnement.
```

### Étape 3. Validation du schéma Kafka avec la comptabilité

```
Presente a la comptabilite le payload documente dans
docs/reference/contrats_api_dotations_v3.md, section "Evenement Kafka de
cloture". Rapporte-moi leur retour.

SI le schema est valide tel quel : retire la mention "PROVISOIRE, a
valider avec la comptabilite" du DTO (EvenementClotureDto,
processus/api/) et de la documentation -- le schema devient le contrat
reel.

SI la comptabilite demande des changements : POSE-MOI LA QUESTION avant
de modifier EvenementClotureDto ou LigneClotureDto. Rappelle que
fonctionRetenue et montantApplique restent obligatoirement les valeurs
FIGEES de LigneEtatMensuel (jamais recalculees), c'est une regle metier
etablie, pas un simple detail de schema -- si la comptabilite demande une
valeur recalculee, c'est un changement de regle metier a faire trancher
explicitement, pas a accepter silencieusement.
```

### Critères de validation

| Élément | Statut attendu |
|---|---|
| Email réel reçu par un destinataire réel lors d'un cycle de test | Vérifié |
| Paramètres SMTP/Graph en variables d'environnement, jamais en dur | Vérifié |
| Si Graph imposé : abstraction `NotificationService` toujours préservée | Vérifié |
| Schéma Kafka validé par la comptabilité, ou ajustements tranchés avec l'utilisateur | Fait |
| Mention « provisoire » retirée seulement après validation effective | Vérifié |
| `KAFKA_BOOTSTRAP_SERVERS` de l'environnement cible confirmé | Fait |

### Commit

```bash
git add .
git commit -m "mm.17: canal outlook reel et schema kafka valide par la comptabilite"
```

---

## 6. MM.18 — Infrastructure de déploiement

| | |
|---|---|
| **Objet** | Faire passer les manifests Kubernetes et le pipeline d'image de l'état « placeholder » à l'état « déployable » |
| **Livrable** | Namespace et conventions de nommage confirmés, secrets gérés par le mécanisme retenu par la DSI, stockage persistant pour les PDF, pipeline de build/push d'image |
| **Durée** | Dépend des arbitrages DSI, en grande partie hors du contrôle du développement applicatif |
| **Prérequis** | **Bloquant** sur plusieurs points DSI distincts, détaillés ci-dessous |
| **Origine** | `CLAUDE.md` section 14, `k8s/*.yaml` (notes « à confirmer avec la DSI » déjà présentes dans chaque fichier), constat direct : aucun pipeline CI/CD n'existe dans le dépôt à ce jour |

### Points bloquants, un par un

1. **Namespace et conventions de nommage.** Chaque fichier sous `k8s/`
   porte déjà la note « à confirmer avec la DSI, aucun namespace n'est fixé
   volontairement ». Rien à coder tant que la réponse n'est pas là — ajouter
   `metadata.namespace` partout une fois connu.

2. **Stockage des PDF non persistant.** `dottel.documents.chemin-stockage`
   pointe sur un chemin local au pod (`./documents/` par défaut). `CLAUDE.md`
   section 14 le documente déjà comme une limitation temporaire : un
   redémarrage de pod perd les documents générés. Il faut soit un
   `PersistentVolumeClaim` Kubernetes, soit un stockage objet (S3-compatible
   ou équivalent DSI) — à trancher avec la DSI, puis implémenter le
   changement d'écriture dans `DocumentService` si un stockage objet est
   retenu (l'écriture locale ne suffira plus).

3. **Gestion des secrets réels.** `k8s/secret.yaml` porte un avertissement
   explicite : les valeurs `CHANGEME` en base64 ne doivent **jamais** être
   les vraies valeurs committées. Le mécanisme réel (Vault, Sealed Secrets,
   ou autre) est à la main de la DSI, hors du contrôle de ce dépôt.

4. **Image et registre.** `k8s/deployment.yaml` référence
   `harbor.afrilandfirstbank.cm/baobab/dottel-backend:latest`, un
   placeholder explicite. Aucun pipeline CI/CD n'existe dans le dépôt
   (vérifié : pas de `.github/workflows/`, `.gitlab-ci.yml`, ni `Jenkinsfile`).
   Il faut soit que la DSI dispose déjà d'un pipeline standard pour les
   modules BAOBAB (à documenter, pas à réinventer ici), soit en écrire un
   minimal (build image, push Harbor, tag par commit).

5. **Origine CORS et URLs réelles.** `APP_CORS_ALLOWED_ORIGINS` et
   `VITE_API_BASE_URL` de production portent déjà des valeurs plausibles
   (`https://dottel.afrilandfirstbank.cm`) mais **non confirmées** — à
   valider avec la DSI avant le premier déploiement réel.

### Étape 1. Ouvrir la session (au fur et à mesure que les réponses DSI arrivent)

```
Tu es mon assistant de developpement pour le projet DOTTEL.

VERIFICATION D'ESPACE DE TRAVAIL, BLOQUANTE.

Lis CLAUDE.md section 14 en entier, et les 4 fichiers sous k8s/ EN ENTIER
(chacun porte deja des notes "a confirmer DSI" -- ne les recree pas,
complete-les).

Voici les reponses DSI obtenues a ce stade (peut etre partiel) :
- Namespace et conventions de nommage : [COLLER OU "pas encore recu"]
- Mecanisme de stockage persistant retenu : [COLLER OU "pas encore recu"]
- Mecanisme de gestion des secrets retenu : [COLLER OU "pas encore recu"]
- Pipeline CI/CD existant a reutiliser, ou a ecrire : [COLLER OU "pas encore recu"]
- Origine CORS et URL API de production confirmees : [COLLER OU "pas encore recu"]

Traite UNIQUEMENT les points dont la reponse est arrivee. Pour chaque
point encore "pas encore recu", ne devine rien -- laisse le placeholder
existant et son commentaire "a confirmer DSI" en l'etat.
```

### Critères de validation

| Élément | Statut attendu |
|---|---|
| Namespace appliqué partout une fois confirmé | Vérifié |
| Stockage des PDF persistant (ou décision explicite de le reporter, documentée) | Fait |
| Aucune vraie valeur de secret committée à aucun moment | Vérifié |
| Image buildable et poussée vers Harbor selon un mécanisme documenté | Fait |
| CORS/URLs de production confirmées avant tout déploiement réel | Fait |
| `kubectl apply --dry-run` (ou équivalent) valide sur les 4 manifests | Vérifié |

### Commit

```bash
git add .
git commit -m "mm.18: infrastructure de deploiement -- namespace, stockage, secrets, pipeline"
```

---

## 7. MM.19 — Recette finale (Sprint 7 de `CLAUDE.md`)

| | |
|---|---|
| **Objet** | Le « Sprint 7 » déjà annoncé par `CLAUDE.md` section 16 : tests d'intégration, recette, corrections, README |
| **Livrable** | Suite de tests bout en bout contre les ressources réelles branchées, README complet, dernières corrections de recette |
| **Durée** | Variable selon ce que la recette remonte |
| **Prérequis** | MM.14 fait ; MM.15 à MM.18 au moins partiellement faits (la recette peut démarrer dès qu'une ressource réelle est branchée, sans attendre les quatre) |

### Ce qui est déjà confirmé manquant

Le `README.md` racine fait **11 lignes** au moment de la rédaction : le nom
du projet, une phrase de description, et une note sur Keycloak. Aucune
instruction de démarrage, aucune liste de variables d'environnement, aucune
procédure de build/déploiement. `CLAUDE.md` section 16 range explicitement
la rédaction du README dans ce sprint — ce n'est pas un oubli à combler
avant, c'est le bon moment.

### Étape 1. Ouvrir la session

```
Tu es mon assistant de developpement pour le projet DOTTEL.

VERIFICATION D'ESPACE DE TRAVAIL, BLOQUANTE.

Lis CLAUDE.md en entier, ce document (docs/PLAN_PREPARATION_MISE_EN_SERVICE_MM.md)
en entier, et l'etat d'avancement reel de MM.14 a MM.18 (quels sont faits,
lesquels restent bloques sur un input DSI qui n'est toujours pas arrive).

CONTEXTE : ce sprint correspond au "Sprint 7" deja annonce par CLAUDE.md
section 16. Il ne redecouvre rien de nouveau -- il verifie que tout ce
qui a ete construit depuis M.0 fonctionne ensemble, avec autant de
ressources reelles que possible a ce stade, et documente ce qui reste
simule en attendant le reste.

PREMIERE ACTION : dresser un etat des lieux honnete -- quelles ressources
sont reelles a ce jour (Keycloak ? EHR ? Outlook ? Kafka comptabilite ?),
lesquelles restent simulees, et adapter le perimetre de la recette en
consequence. Ne pas faire semblant qu'une ressource est reelle si elle ne
l'est pas.
```

### Étape 2. Rédaction du README

```
Redige un README.md racine complet : presentation du module, stack
technique (CLAUDE.md section 2), instructions de demarrage local
(backend + frontend + docker-compose pour Kafka/Keycloak), liste
COMPLETE des variables d'environnement attendues avec leur role (reprends
la liste consolidee de MM.14 etape 3), etat des ressources reelles vs
simulees a la date de redaction, et pointeurs vers les guides
docs/monolithe-modulaire/ et docs/chantier-ajout-metier-mm/ pour qui veut
l'historique complet.

Ne duplique pas le contenu de CLAUDE.md -- renvoie vers lui pour les
regles metier et les conventions, le README est un point d'entree
operationnel, pas une seconde specification.
```

### Critères de validation

| Élément | Statut attendu |
|---|---|
| État des lieux honnête des ressources réelles vs simulées, daté | Fait |
| Tests d'intégration bout en bout sur chaque ressource réellement branchée | Vérifié |
| README complet (démarrage, variables d'environnement, état des ressources) | Fait |
| Aucune régression sur la suite de tests existante | Vérifié |
| Dernières corrections de recette tracées (quoi, pourquoi, où) | Fait |

### Commit

```bash
git add .
git commit -m "mm.19: recette finale et readme -- sprint 7"
```

---

## 8. Récapitulatif — ce qu'il faut demander, en une seule liste

À transmettre tel quel à la DSI et à la comptabilité, pour éviter les
allers-retours dispersés entre MM.15 et MM.18 :

**À la DSI :**
1. URL, nom de realm et nom de client Keycloak réels (MM.15).
2. Réponses aux 5 points d'incertitude de `MM.7_bascule_realm_dsi.md` §2
   (forme du claim de rôle, claim `matricule`, forme du claim `email`,
   configuration du client, mode de fédération AD) (MM.15).
3. Accès technique et documentation de l'EHR réel, plus la liste complète
   des codes unité (MM.16).
4. Confirmation ou complément de la liste des 25 codes agence (MM.16).
5. Paramètres du serveur mail réel (SMTP ou, si imposé, Microsoft Graph)
   (MM.17).
6. Namespace Kubernetes et conventions de nommage (MM.18).
7. Mécanisme de gestion des secrets retenu (MM.18).
8. Mécanisme de stockage persistant pour les documents générés (MM.18).
9. Pipeline CI/CD existant à réutiliser, sinon accord pour en écrire un
   minimal (MM.18).
10. Confirmation des URLs réelles de production (API, frontend, CORS)
    (MM.18).

**À la comptabilité :**
1. Validation du schéma de l'événement Kafka enrichi (`docs/reference/
   contrats_api_dotations_v3.md`, section « Événement Kafka de clôture »)
   (MM.17).
2. Confirmation que `KAFKA_BOOTSTRAP_SERVERS` de l'environnement cible
   (test puis production) est connu et joignable depuis le module DOTTEL
   (MM.17).

**Décisions internes, pas DSI, à trancher avec le métier ou le
responsable produit :**
1. Accès DRH à l'écran Bénéficiaires, accès ADMIN à la création de grille
   (E2/E3 de l'audit sécurité) (MM.14).
2. Devenir de `react-hot-toast` : l'intégrer réellement ou le retirer
   (MM.14).
3. `motDePasseHash` : nullable ou supprimé, une fois le realm réel
   confirmé (MM.15).

---

**Fin du document.** Chaque sous-sprint ci-dessus reste un guide
indépendant : MM.14 peut démarrer immédiatement, MM.15 à MM.18 dès que leur
input externe respectif arrive, MM.19 une fois qu'au moins l'un d'eux est
fait.