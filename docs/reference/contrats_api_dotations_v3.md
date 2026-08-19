# CONTRATS API

Module : Digitalisation des Dotations Téléphoniques Mensuelles

*Projet AFRILAND HORIZON 2030*

| **Référence** | **AFB_API_DOTTEL_V3.7_2026** |
| --- | --- |
| Version | 3.7 |
| Date | Août 2026 |
| Nombre d'endpoints documentés | 42 endpoints réellement exposés, répartis en 8 groupes (+ 1 sous-groupe) — recompté sur le code, `POST /auth/login` supprimé par MM.7 n'est plus compté |
| Version 3.1 | Alignement complet sur l'implémentation réelle : correction des réponses login/PATCH bénéficiaire, ajout de PATCH /processus/{id} (absent de la V3.0), correction de l'erreur RG-04 (400, pas 500), ajout d'un statut d'implémentation par endpoint. |
| Version 3.2 | Ajout de GET /fonctions-eligibles et de PATCH /beneficiaires/{id}/reactiver (Sprint 6F.5), absents de la V3.1 — le premier alimente en frontend les filtres et formulaires liés à la fonction sans liste en dur, le second comble une lacune : aucun endpoint ne permettait de revenir sur une désactivation. |
| Version 3.3 | Sprint 6F.7bis : comble deux écarts face au cahier des charges (section II.1.7). Ajout de POST /grilles-tarifaires/{id}/desactiver (retrait volontaire d'une grille ACTIVE sans remplacement, absent du cycle de vie initial qui ne couvrait que le remplacement automatique). `fonction_eligible` passe d'un référentiel figé par migration Flyway à un référentiel géré par l'application : ajout de GET /fonctions-eligibles/toutes, POST /fonctions-eligibles, PATCH /fonctions-eligibles/{code}, PATCH /fonctions-eligibles/{code}/desactiver et PATCH /fonctions-eligibles/{code}/reactiver. Rôle ADMIN ajouté à GET /fonctions-eligibles (nécessaire au filtre de `GrillesListPage` côté ADMIN, oublié à la V3.2). |
| Version 3.4 | Sprint MM.10 (référentiel unité/agence). Ajout de GET /beneficiaires/unites-rattachement : liste unité↔code_unite exposée par le stub EHR, alimente le select du modal de modification bénéficiaire (l'ARH ne saisit plus le code unité à la main, résolu côté backend à la validation du PATCH). Introduction de la colonne `code_agence` (5 chiffres, agence de domiciliation du compte courant — distincte de `code_unite`, 4 chiffres, unité d'affectation professionnelle), non exposée en lecture dans les DTO bénéficiaire mais portée par le flux d'enrôlement EHR et l'import Excel (8ᵉ colonne CODE_AGENCE). |
| Version 3.5 | Sprint MM.11 (période de déclenchement et rattrapage). POST /processus/declencher refuse désormais toute période postérieure au mois courant (400) et accepte un champ `rattrapage` pour redéclencher un mois déjà clos, réservé aux bénéficiaires non payés (RG-12 évoluée — voir CLAUDE.md section 7 : l'unicité mois/année ne s'applique plus qu'aux processus normaux). `rattrapage` et `idProcessusOriginal` ajoutés aux réponses de GET /processus, GET /processus/{id} et POST /processus/declencher. Ajout de GET /reporting/audit/actions (liste dynamique, dérivée des valeurs réellement présentes en base, pour le filtre "Action" du journal d'audit — évite qu'une liste figée côté frontend se désynchronise à chaque nouvelle action). **Correction de statut au passage** : le Groupe 6 Reporting, marqué "non implémenté" depuis la V3.0, est en réalité construit et consommé par le frontend depuis le Sprint 6F — corrigé ici car GET /reporting/audit/actions vient s'y ajouter. |
| Version 3.6 | Revue de cohérence contre le code réel (Sprint MM.13+). **Corrections majeures de statut**, plusieurs endpoints étaient documentés "Planifié" alors qu'ils sont implémentés et testés depuis plusieurs sprints : `POST /processus/{id}/retourner`, `GET /processus/{id}/piece-jointe`, `GET /pieces-jointes/{id}/download`, et les 4 endpoints du Groupe 8 Administration (`GET`/`POST /admin/utilisateurs`, `PATCH /admin/utilisateurs/{id}/statut`, `PATCH /admin/utilisateurs/{id}/role`). **`POST /auth/login` supprimé** (Sprint MM.7, décision de portée P-2) : Keycloak est désormais l'unique émetteur de jetons, la connexion passe par une redirection frontend directe (Authorization Code + PKCE), ce contrôleur n'émet plus rien. `POST /auth/logout` corrigé (ne retourne plus de corps JSON). Section 0 corrigée : l'authentification n'est plus un HS384 local mais un jeton Keycloak vérifié par JWKS. |

| Version 3.7 | Sprint MM.14 (nettoyage de dette technique). **Aucun endpoint ajouté ni supprimé — 42 endpoints inchangés.** Trois corrections de rôles, issues des écarts E2 et E3 de `docs/audit_securite_owasp_v1.md` section 4, tranchés avec le métier le 2026-08-19 : **ADMIN retiré** de `POST /grilles-tarifaires` et `PATCH /grilles-tarifaires/{id}` (fixer un montant est un acte métier engageant le circuit ARH → CRH → DRH, pas une opération d'administration ; l'ADMIN garde la lecture et `POST /fonctions-eligibles`) ; **DRH ajoutée** à `GET /beneficiaires/unites-rattachement` (l'écran Bénéficiaires est ouvert à la DRH en lecture seule, ce filtre doit suivre). Dans les trois cas, le `@PreAuthorize` du backend a été aligné sur ce contrat dans le même commit. |

Chaque endpoint porte désormais une étiquette de statut :
- **Implémenté** : construit, testé, commité.
- **En cours** : en cours de construction au moment de cette révision, décisions non finalisées.
- **Planifié** : documenté mais non encore développé, sprint cible indiqué.

# 0. Introduction

Ce document définit l'ensemble des contrats API REST entre le frontend React et le backend Spring Boot du module de gestion des dotations téléphoniques mensuelles.

| **Paramètre** | **Valeur** |
| --- | --- |
| URL de base (dev) | http://localhost:8080/api |
| Format | JSON (Content-Type: application/json) |
| Authentification | JWT — Header Authorization: Bearer {token}. **Depuis le Sprint MM.7**, le jeton est émis exclusivement par Keycloak (Authorization Code + PKCE côté frontend) et validé par le backend via JWKS/issuer-uri — plus de signature HS384 locale ni d'émission par ce module (voir `docs/monolithe-modulaire/MM.7_keycloak_provisoire.md`). |
| Encodage | UTF-8 |
| Dates | Format ISO 8601 : YYYY-MM-DD |
| Montants | Entiers en FCFA (jamais de décimales, jamais stockés directement sur un bénéficiaire) |

# 1. Groupe Auth — /auth

### POST /auth/login — **SUPPRIMÉ (Sprint MM.7)**

**N'existe plus.** Documenté par erreur comme "Implémenté" jusqu'à la V3.5.
Keycloak est devenu l'unique émetteur de jetons (décision de portée P-2,
Sprint MM.7) : le frontend redirige directement vers Keycloak
(Authorization Code + PKCE), ce backend n'émet plus aucun jeton et
n'expose donc plus cet endpoint. `AuthController` ne porte désormais que
`logout`. Voir `docs/monolithe-modulaire/MM.7_keycloak_provisoire.md` et
`MM.7_bascule_realm_dsi.md`.

### POST /auth/logout — Implémenté

Terminaison stateless : aucune liste noire de jetons, le token expire naturellement après 8h. Ce choix est documenté explicitement dans le code.

**Rôles autorisés : Authentifié**

Réponse succès : **200, corps vide** (corrigé — la V3.5 documentait à tort
un corps JSON `{ "message": "Déconnexion réussie." }`, qui n'existe pas
dans `AuthController.logout()`). Le token est simplement supprimé côté
client ; cet endpoint n'a plus qu'une valeur symbolique depuis MM.7
(aucune liste noire, aucun état serveur à purger).

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Déconnexion réussie (corps vide). |
| 401 | Token absent ou invalide. |

# 2. Groupe Enrôlement — /enrolement

### GET /enrolement/verifier?matricule={m} — Implémenté

Interroge l'EHR (stub en dev) et vérifie l'éligibilité selon RG-01 et RG-02.

**Rôles autorisés : EMPLOYE**

Réponse succès (éligible) :

```json
{
  "eligible": true,
  "matricule": "2093",
  "nomPrenoms": "Sylvie NKOLO",
  "fonction": "GFC",
  "grade": null,
  "uniteRattachement": "Agence Bastos",
  "codeUnite": "AG001",
  "numCompteCourant": "10001234567"
}
```

Réponse (non éligible, RG-01 ou RG-02) :

```json
{
  "eligible": false,
  "matricule": "2093",
  "nomPrenoms": "Sylvie NKOLO",
  "fonction": "AGENT_GUICHET",
  "grade": null,
  "uniteRattachement": "Agence Bastos",
  "codeUnite": "AG001",
  "numCompteCourant": "10001234567"
}
```

Note : la non-éligibilité n'est pas une erreur HTTP (200 + `eligible:false`). Aucun champ de montant n'est retourné (colonne `beneficiaires.montant_dotation` supprimée dès la V3 ; le montant est calculé dynamiquement depuis `grille_tarifaire` au cycle mensuel, jamais à l'enrôlement).

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Données EHR retournées, éligible ou non (voir champ `eligible`). |
| 404 | Matricule inconnu de l'EHR. |
| 409 | Matricule déjà enrôlé (RG-03). |

### POST /enrolement/confirmer — Implémenté

Enregistre l'employé dans `beneficiaires`. Le montant n'est ni calculé ni stocké ici (RG-04) — seule l'existence d'une grille ACTIVE pour la fonction est vérifiée.

**Rôles autorisés : EMPLOYE**

Corps de la requête :

```json
{
  "matricule": "2093",
  "grade": null
}
```

Note importante : le champ `grade` reste dans le corps de la requête pour compatibilité de format, mais **il n'est jamais exploité en logique métier**. `EnrolementService.confirmer()` utilise exclusivement le grade retourné par l'EHR pour revérifier RG-01/RG-02 — accepter le grade soumis par le client permettrait à un employé de mentir sur son grade pour contourner RG-02. Ne jamais réactiver ce champ sans revoir cette décision de sécurité.

Note de comportement (décision E-3, actée le 2026-07-31, chantier MM.7) :
`POST /enrolement/confirmer` **accepte volontairement** un matricule différent
de celui de l'utilisateur EMPLOYE authentifié porteur du jeton. Ce n'est pas
une faille : un collègue connecté doit pouvoir aider un autre collègue à
s'enrôler via cette page. L'utilisateur authentifié est tracé dans
`audit_log` pour cette action, ce qui permet de savoir *qui* a enrôlé *qui*.
Ce comportement suppose toujours un utilisateur EMPLOYE authentifié — un
appel non authentifié reste refusé.

Réponse succès :

```json
{
  "id": 1,
  "matricule": "2093",
  "nomPrenoms": "Sylvie NKOLO",
  "fonction": "GFC",
  "dateEnrolement": "2026-07-09"
}
```

| **Code HTTP** | **Description** |
| --- | --- |
| 201 | Enrôlement réussi. |
| 400 | Données invalides, ou aucune grille tarifaire ACTIVE pour la fonction (`GrilleTarifaireIntrouvableException`). |
| 403 | Non éligible (RG-01 ou RG-02). |
| 404 | Matricule inconnu de l'EHR. |
| 409 | Déjà enrôlé (RG-03). |

# 3. Groupe Bénéficiaires — /beneficiaires

### GET /beneficiaires — Implémenté

Liste paginée des bénéficiaires, avec filtres optionnels `fonction`, `uniteRattachement`, `actif`.

**Rôles autorisés : ARH, DRH**

Réponse succès :

```json
{
  "contenu": [
    {
      "id": 1,
      "matricule": "2093",
      "nomPrenoms": "Sylvie NKOLO",
      "fonction": "GFC",
      "montantCourant": 40000,
      "uniteRattachement": "Agence Bastos",
      "actif": true
    }
  ],
  "total": 1,
  "page": 0,
  "taille": 20
}
```

`montantCourant` est recalculé à chaque requête depuis `grille_tarifaire` ACTIVE (RG-04), jamais stocké. Vaut `null` si aucune grille ACTIVE n'existe pour la fonction du bénéficiaire.

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Liste retournée. |
| 401 | Non authentifié. |
| 403 | Rôle non autorisé. |

### GET /beneficiaires/unites-rattachement — Implémenté (ajouté au Sprint MM.10)

Liste les unités de rattachement connues du stub EHR, avec leur `code_unite` (4 chiffres) associé. Alimente le select du modal de modification bénéficiaire côté frontend : l'ARH choisit un libellé d'unité, jamais le code — le `code_unite` est résolu côté backend à la validation du `PATCH /beneficiaires/{id}` (source unique de vérité, décision actée avec l'utilisateur au Sprint MM.10).

**Sprint MM.14, écart E2 de l'audit tranché avec le métier** : rôle **DRH
ajouté**. L'écran Bénéficiaires est désormais ouvert à la DRH en lecture seule
(`GET /beneficiaires` et `GET /beneficiaires/export` l'autorisaient déjà, seul
le frontend l'en excluait), et cette liste alimente son filtre « Unité de
rattachement ». Elle n'expose aucune donnée que `GET /beneficiaires` ne montre
déjà à ce rôle ; la laisser en 403 rendrait l'écran boiteux pour la DRH.

**Rôles autorisés : ARH, DRH** (l'ARH par le rôle déjà requis par
`PATCH /beneficiaires/{id}` ; la DRH pour le filtre de son écran en lecture seule)

Réponse succès :

```json
[
  {"uniteRattachement": "DSI", "codeUnite": "4060"},
  {"uniteRattachement": "Agence Douala Akwa", "codeUnite": "1102"}
]
```

Seul `DSI` → `4060` est confirmé par le métier ; les autres codes sont des données de stub provisoires, à remplacer le jour de l'intégration EHR réelle (CLAUDE.md section 12).

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Liste retournée. |
| 403 | Rôle non autorisé. |

### PATCH /beneficiaires/{id} — Implémenté

Modifie fonction/grade/unité d'un bénéficiaire. Toute modification est tracée dans `audit_log` avec delta JSON complet avant/après (RG-09).

**Rôles autorisés : ARH**

Corps de la requête (tous les champs optionnels, mise à jour partielle) :

```json
{
  "fonction": "DA",
  "grade": null
}
```

Réponse succès (structure alignée sur `BeneficiaireResponseDto`, corrigée par rapport à la V3.0 qui ne montrait que deux champs) :

```json
{
  "id": 1,
  "matricule": "2093",
  "nomPrenoms": "Sylvie NKOLO",
  "fonction": "DA",
  "montantCourant": 50000,
  "uniteRattachement": "Agence Bastos",
  "actif": true
}
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Modification enregistrée. |
| 400 | Données invalides, ou unité de rattachement inconnue de la liste EHR (`UniteInconnueException`, ajouté au Sprint MM.10). |
| 403 | Combinaison fonction/grade non éligible après modification (RG-01/RG-02, revérifiée via `EligibiliteService` — code ajouté par rapport à la V3.0, absent à tort). |
| 404 | Bénéficiaire introuvable. |

### DELETE /beneficiaires/{id} — Implémenté

Désactive un bénéficiaire (`actif = false`). Ne supprime jamais la ligne en base. Un bénéficiaire désactivé est exclu des futurs déclenchements de processus mensuel (`findByActifTrue()`), sans effet rétroactif sur les cycles déjà déclenchés.

**Rôles autorisés : ARH**

Réponse succès :

```json
{ "message": "Bénéficiaire désactivé." }
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Désactivation réussie. |
| 404 | Bénéficiaire introuvable. |

### PATCH /beneficiaires/{id}/reactiver — Implémenté (ajouté au Sprint 6F.5, absent de la V3.0/V3.1)

Réactive un bénéficiaire désactivé (`actif = true`). Symétrique de `DELETE /beneficiaires/{id}` : endpoint dédié plutôt qu'extension de `PATCH /beneficiaires/{id}`, pour isoler la revalidation RG-01/RG-02 (la fonction/grade du bénéficiaire a pu devenir non éligible pendant qu'il était inactif — référentiel désactivé, grille tarifaire retirée) sans complexifier `modifier()`.

**Rôles autorisés : ARH**

Réponse succès (structure alignée sur `BeneficiaireResponseDto`) :

```json
{
  "id": 1,
  "matricule": "2093",
  "nomPrenoms": "Sylvie NKOLO",
  "fonction": "GFC",
  "montantCourant": 40000,
  "uniteRattachement": "Agence Bastos",
  "actif": true
}
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Réactivation réussie. |
| 403 | Fonction/grade non éligible à la réactivation (RG-01/RG-02, `NonEligibleException`). |
| 404 | Bénéficiaire introuvable. |

### GET /beneficiaires/export — Implémenté

Exporte au format Excel la liste complète des bénéficiaires actifs. Colonnes exactes (décidées au Sprint 4.3, absentes de la V3.0 qui ne précisait rien) :

`N°ordre, Matricule, Nom et prénoms, Fonction, Unité de rattachement, Code unité, Montant courant (FCFA), N° compte courant`

**Rôles autorisés : ARH, DRH**

Réponse succès : fichier binaire `.xlsx` — `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Fichier Excel retourné. |
| 403 | Rôle non autorisé. |

### POST /beneficiaires/import — Implémenté

Importe des bénéficiaires en masse. Format à 8 colonnes (la colonne MONTANT a été retirée sur décision métier — le montant est dérivé de la grille tarifaire, jamais importé ; colonne CODE_AGENCE ajoutée au Sprint MM.10, `code_agence` étant NOT NULL en base depuis la migration V5) :

`N°ORDRE, MATRICULE, NOMS & PRENOMS, FONCTION, UNITE, CODE_UNITE, N°COMPTE, CODE_AGENCE`

Les corps de contrôle et assimilés (`CORPS_CONTROLE_IG`, `CORPS_CONTROLE_IGA`, `CONTROLEUR_GESTION`, `CONTROLEUR_COMPTABLE`, `COMPTABLE`) sont systématiquement rejetés à l'import — le format n'a pas de colonne GRADE, donc RG-02 ne peut jamais être vérifié via ce canal. Ces cas doivent passer par l'enrôlement individuel.

Une ligne sans CODE_AGENCE est rejetée au même titre qu'un matricule manquant (motif `Champ CODE_AGENCE manquant`), au lieu de faire échouer l'insertion en base.

**Rôles autorisés : ARH**

Réponse succès :

```json
{
  "inseres": 45,
  "rejetes": 3,
  "erreurs": [
    {"ligne": 12, "matricule": "9999", "motif": "Matricule déjà enrôlé"},
    {"ligne": 18, "matricule": "9998", "motif": "Fonction inconnue : STAGIAIRE"},
    {"ligne": 23, "matricule": null, "motif": "Champ MATRICULE manquant"}
  ]
}
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Import traité (RG-11 : lignes valides insérées même si d'autres sont rejetées). |
| 400 | Fichier illisible ou format invalide (`FichierImportInvalideException`). |
| 403 | Rôle non autorisé. |

# 3bis. Groupe Référentiel — /fonctions-eligibles

### GET /fonctions-eligibles — Implémenté (ajouté au Sprint 6F.5, absent de la V3.0)

Liste les fonctions actives de `fonction_eligible`, triées par libellé. Ajouté pour alimenter le filtre et le formulaire de modification de `BeneficiairesListPage` côté frontend — jusqu'ici cette liste n'existait qu'en dur côté client, ce qui obligeait à la resynchroniser manuellement si le référentiel changeait en base.

**Rôles autorisés : ARH, DRH, ADMIN** (ADMIN ajouté au Sprint 6F.7bis — nécessaire au filtre "Fonction" de `GrillesListPage`, resté vide côté ADMIN faute de rôle jusque-là)

Réponse succès :

```json
[
  { "code": "ADG", "libelle": "Administrateur Directeur Général" },
  { "code": "DA", "libelle": "Directeur d'Agence" }
]
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Liste retournée. |
| 401 | Non authentifié. |
| 403 | Rôle non autorisé. |

### GET /fonctions-eligibles/toutes — Implémenté (Sprint 6F.7bis)

Vue ADMIN complète du référentiel (actives et inactives), avec le nombre
de bénéficiaires actifs rattachés à chaque fonction. Pas de pagination :
même décision que pour `GET /grilles-tarifaires` (volume interne limité,
25 fonctions éligibles).

**Rôles autorisés : ADMIN**

Réponse succès :

```json
[
  { "code": "ADG", "libelle": "Administrateur Directeur Général", "actif": true, "nombreBeneficiairesActifs": 2 },
  { "code": "GFC", "libelle": "Gestionnaire de Fonds de Commerce", "actif": false, "nombreBeneficiairesActifs": 0 }
]
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Liste retournée. |
| 403 | Rôle non autorisé. |

### POST /fonctions-eligibles — Implémenté (Sprint 6F.7bis)

Crée une nouvelle fonction éligible. Comble un écart du cahier des
charges (section II.1.7) : `fonction_eligible` n'était alimenté que par
Flyway V2, sans endpoint de création. Décision actée avec le métier : la
grille tarifaire initiale est obligatoire dans le même appel, et passe
directement au statut ACTIVE (création ADMIN hors workflow ARH/DRH de
RG-10 — la fonction est utilisable dès sa création).

**Rôles autorisés : ADMIN**

Corps de la requête :

```json
{
  "code": "CHARGE_INNOVATION",
  "libelle": "Chargé Innovation",
  "montantFcfa": 40000,
  "dateDebut": "2026-08-01"
}
```

Réponse succès :

```json
{
  "code": "CHARGE_INNOVATION", "libelle": "Chargé Innovation",
  "actif": true, "nombreBeneficiairesActifs": 0
}
```

| **Code HTTP** | **Description** |
| --- | --- |
| 201 | Fonction créée avec sa grille initiale ACTIVE. |
| 409 | Ce code de fonction existe déjà. |

### PATCH /fonctions-eligibles/{code} — Implémenté (Sprint 6F.7bis)

Corrige `libelle` et/ou `code` après création (aucun endpoint ne le
permettait jusqu'ici — une faute de frappe imposait de désactiver et
recréer la fonction). `libelle` est toujours modifiable (donnée
d'affichage pure, sans référence ailleurs). `code` ne l'est que si au
plus 1 bénéficiaire actif est rattaché à la fonction (décision actée
avec le métier) : `beneficiaires.fonction` est un `VARCHAR`, pas une FK,
donc un renommage cascade explicitement vers les bénéficiaires restants
référençant l'ancien code (y compris inactifs) pour éviter une référence
orpheline — les lignes `ligne_etat_mensuel` (instantané historique
volontairement figé, section 4 de ce document) ne sont jamais modifiées
par ce renommage.

**Rôles autorisés : ADMIN**

Corps de la requête (les deux champs sont optionnels) :

```json
{ "nouveauCode": "CHARGE_INNOVATION_TECH", "libelle": "Chargé Innovation Technologique" }
```

Réponse succès : même forme que `POST /fonctions-eligibles`.

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Modification enregistrée. |
| 404 | Code fonction inconnu. |
| 409 | Plus d'1 bénéficiaire actif rattaché (renommage de code refusé), ou nouveau code déjà utilisé par une autre fonction. |

### PATCH /fonctions-eligibles/{code}/desactiver — Implémenté (Sprint 6F.7bis)

Désactive une fonction éligible (`actif = false`). Décision actée avec
le métier : l'action est **autorisée même si des bénéficiaires actifs
sont rattachés** — l'avertissement (nombre exact de bénéficiaires
concernés) est affiché côté frontend avant confirmation, et ce nombre
est tracé dans le delta d'audit. RG-01 s'applique immédiatement à tout
nouvel enrôlement/ajustement ; les bénéficiaires déjà rattachés
deviennent non conformes RG-01, à traiter individuellement.

**Rôles autorisés : ADMIN**

Réponse succès : même forme que `POST /fonctions-eligibles`, avec `actif: false`.

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Fonction désactivée. |
| 404 | Code fonction inconnu. |

### PATCH /fonctions-eligibles/{code}/reactiver — Implémenté (Sprint 6F.7bis)

Réactive une fonction éligible (`actif = true`). Sans cet endpoint, une
fonction désactivée par erreur ou temporairement resterait bloquée
définitivement (même trou déjà comblé pour les bénéficiaires au Sprint
6F.5). Pas de revalidation RG-02 : contrairement à la réactivation d'un
bénéficiaire, réactiver une fonction ne concerne aucun grade individuel.
RG-04 reprend son cours naturel selon l'état de la grille tarifaire.

**Rôles autorisés : ADMIN**

Réponse succès : même forme que `POST /fonctions-eligibles`, avec `actif: true`.

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Fonction réactivée. |
| 404 | Code fonction inconnu. |

# 4. Groupe Processus mensuel — /processus

### POST /processus/declencher — Implémenté

Déclenche un nouveau cycle mensuel pour tous les bénéficiaires actifs.

**Rôles autorisés : ARH**

**Sprint MM.11** : deux évolutions.
1. **Restriction de période** — la période demandée ne doit jamais être postérieure au mois courant (`YearMonth`, franchissement d'année géré nativement). Une période antérieure ou égale au mois courant reste toujours acceptée, sans limite basse.
2. **Rattrapage** — champ `rattrapage` (booléen, défaut `false`) dans le corps de la requête. À `true`, redéclenche un mois déjà traité : un processus normal `CLOTURE` doit exister pour la même période (sinon 400), tous les bénéficiaires actifs sont listés, ceux déjà payés dans le processus normal original en sont exclus (motif `"Déjà payé pour cette période"`), les autres sont inclus. Le processus normal original n'est jamais modifié. RG-12 évolue en conséquence : l'unicité mois/année ne s'applique plus qu'aux processus normaux (`rattrapage = false`) — plusieurs rattrapages peuvent coexister sur la même période (voir CLAUDE.md section 7).

Corps de la requête :

```json
{
  "moisPaiement": 7,
  "anneePaiement": 2026,
  "rattrapage": false
}
```

Réponse succès (le champ `beneficiairesExclus` a été ajouté par rapport à la V3.0, qui l'omettait ; `rattrapage` ajouté en V3.5) :

```json
{
  "id": 1,
  "moisPaiement": 7,
  "anneePaiement": 2026,
  "statut": "EN_COURS_ARH",
  "dateCreation": "2026-07-09T08:00:00",
  "nombreBeneficiaires": 48,
  "beneficiairesExclus": [
    {"idBeneficiaire": 12, "matricule": "6497", "motif": "Grille tarifaire introuvable"}
  ],
  "rattrapage": false
}
```

`nombreBeneficiaires` ne compte que les lignes avec `inclus_dans_etat=true` (les exclus n'y figurent pas, mais restent listés dans `beneficiairesExclus` avec leur motif — grille introuvable, fonction désactivée depuis l'enrôlement, ou déjà payé en mode rattrapage).

| **Code HTTP** | **Description** |
| --- | --- |
| 201 | Processus déclenché. |
| 400 | Période future (Sprint MM.11), ou rattrapage demandé sans processus normal `CLOTURE` pour cette période. |
| 409 | Processus **normal** déjà existant pour cette période (RG-12, distincte de RG-03 — corrige la V3.0 qui référençait cette règle de façon ambiguë). Ne bloque jamais un rattrapage. |

### GET /processus — Implémenté

Liste les processus mensuels, tous statuts confondus par défaut, avec
filtres optionnels par statut et par année (ajoutés au Sprint 6F.7 — le
endpoint lui-même existait déjà, sans filtres, depuis le Sprint 5).

**Rôles autorisés : ARH, CRH, DRH**

Paramètres de requête (facultatifs) : `statut`
(`EN_COURS_ARH` | `EN_ATTENTE_CRH` | `EN_ATTENTE_DRH` | `RETOURNE` | `CLOTURE`),
`annee` (ex. `2026`).

Réponse succès :

```json
[
  {
    "id": 14, "moisPaiement": 11, "anneePaiement": 2026,
    "statut": "RETOURNE", "dateCreation": "2026-07-24T20:46:47.624242",
    "rattrapage": false
  }
]
```

`rattrapage` (Sprint MM.11) : distingue un processus normal d'un rattrapage. Alimente la détection frontend qui bascule `DeclencherProcessusPage` en mode rattrapage dès qu'un mois déjà traité par un processus normal `CLOTURE` est sélectionné.

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Liste retournée (vide si aucun processus ne correspond aux filtres). |

### GET /processus/{id} — Implémenté

Détail complet d'un processus avec ses `lignesEtatMensuel[]`. Documenté depuis la V3.0, initialement absent (découvert manquant au Sprint 4.3), construit depuis.

**Rôles autorisés : ARH, CRH, DRH**

Structure de réponse (correction du nom de champ : `inclusDansEtat`, pas `inclusInEtat` comme l'écrivait la V3.0 — aligné sur le nom réel de la colonne et de l'entité) :

```json
{
  "id": 1,
  "moisPaiement": 7,
  "anneePaiement": 2026,
  "statut": "RETOURNE",
  "lignesEtatMensuel": [
    {
      "idBeneficiaire": 1,
      "matricule": "2093",
      "nomPrenoms": "Sylvie NKOLO",
      "fonctionRetenue": "GFC",
      "montantApplique": 40000,
      "inclusDansEtat": true
    }
  ],
  "motifRetour": "Ecart mensuel injustifie sur un beneficiaire",
  "origineRetour": "DRH",
  "rattrapage": false,
  "idProcessusOriginal": null
}
```

`rattrapage` et `idProcessusOriginal` (Sprint MM.11) : pour un rattrapage, `idProcessusOriginal` référence le processus normal `CLOTURE` dont il découle (`null` pour un processus normal).

`motifRetour` et `origineRetour` (Sprint 6F.7, RG-07) : renseignés uniquement
si le processus a déjà été retourné au moins une fois — `null`/`null` sinon
(y compris si le statut courant n'est plus `RETOURNE`, par exemple après une
resoumission suivie d'un nouveau retour à une autre étape). `origineRetour`
vaut `"CRH"` ou `"DRH"` selon l'étape qui a retourné le processus.

**Un processus peut être retourné plusieurs fois au fil de ses
resoumissions (`RETOURNE` n'est pas terminal)** : chaque retour crée une
nouvelle ligne `etape_workflow`, sans écraser les précédentes. Ces deux
champs reflètent toujours le **dernier** retour en date (tri par
`dateAction` décroissant côté service, jamais par `ordreEtape` qui peut se
répéter d'un cycle à l'autre) — jamais un motif obsolète d'un cycle de
retour antérieur.

### PATCH /processus/{id} — Implémenté (US-10, absent de la V3.0)

**Endpoint entièrement absent de la version précédente du contrat, alors qu'il est construit et testé depuis le Sprint 3.3.** Permet à l'ARH d'ajuster l'état mensuel avant validation : exclure/réintégrer un bénéficiaire, modifier la fonction retenue (avec recalcul automatique du montant).

**Rôles autorisés : ARH**

Corps de la requête :

```json
{
  "ajustements": [
    {"idBeneficiaire": 1, "inclusDansEtat": false, "fonctionRetenue": null},
    {"idBeneficiaire": 2, "inclusDansEtat": null, "fonctionRetenue": "DA"}
  ]
}
```

Réponse succès (rapport partiel, pas le détail complet du processus — chaque ajustement de la liste est traité indépendamment, un id invalide ou une fonction sans grille ACTIVE ne bloque jamais les autres, RG-11 appliquée par cohérence) :

```json
{
  "idProcessus": 1,
  "resultats": [
    {"idBeneficiaire": 1, "applique": true, "motifRejet": null},
    {"idBeneficiaire": 2, "applique": false, "motifRejet": "Aucune grille active pour la fonction"}
  ]
}
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Requête traitée (voir `resultats[]` pour le détail par ajustement). |
| 404 | Processus introuvable. |
| 409 | Processus non modifiable (statut différent de `EN_COURS_ARH`). |

# 5. Groupe Workflow — /processus/{id}/...

### GET /processus/{id}/ecarts-montants — Implémenté (Sprint MM.12)

**Premier temps de la variante B2-RESYNC** (décision B de M.0, interprétation
« en deux temps » arbitrée le 2026-08-09).

Détecte, pour chaque ligne **incluse** du processus, l'écart entre le montant
stocké et la grille tarifaire ACTIVE courante (RG-04, résolution via
`GrilleTarifaireApi` — unique point de résolution du projet depuis MM.3).

**Lecture pure : aucune écriture.** L'ARH doit pouvoir consulter le
récapitulatif puis renoncer.

**Rôles autorisés : ARH** (seule sa branche de validation resynchronise).

Réponse succès :

```json
{
  "idProcessus": 12,
  "lignesResynchronisees": [
    {
      "idBeneficiaire": 701, "matricule": "1874", "nomPrenoms": "ESSAMA Jeanne",
      "fonctionRetenue": "GFC", "ancienMontant": 40000, "nouveauMontant": 45000
    }
  ],
  "lignesExclues": [
    {
      "idBeneficiaire": 704, "matricule": "2093", "nomPrenoms": "NKOLO Sylvie",
      "fonctionRetenue": "COMPTABLE", "ancienMontant": 35000,
      "motifExclusion": "Aucune grille tarifaire ACTIVE pour cette fonction",
      "montantGrilleEnAttente": null,
      "etapeGrilleEnAttente": null,
      "dateSoumissionGrilleEnAttente": null
    },
    {
      "idBeneficiaire": 710, "matricule": "4409", "nomPrenoms": "NGUEMA Paul",
      "fonctionRetenue": "JURISTE", "ancienMontant": 37000,
      "motifExclusion": "Aucune grille tarifaire ACTIVE pour cette fonction",
      "montantGrilleEnAttente": 35000,
      "etapeGrilleEnAttente": "DRH",
      "dateSoumissionGrilleEnAttente": "2026-08-05T09:00:00"
    }
  ]
}
```

**Les deux listes sont volontairement distinctes** : leurs conséquences n'ont
rien de comparable — `lignesResynchronisees` = la personne **est** payée, à un
autre montant ; `lignesExclues` = la personne **n'est pas** payée ce mois-ci.
Les clients doivent les présenter en deux blocs séparés et visuellement
distincts, jamais dans une liste indifférenciée (exigence du 2026-08-09).

#### Absence durable / absence transitoire (options P-1 + P-2, 2026-08-09)

Une ligne exclue l'est pour deux raisons de nature différente, que
`motifExclusion` seul confondait. Les trois champs `*GrilleEnAttente` sont le
discriminant — le motif lui-même n'est **pas** reformulé, ses libellés étant
figés par ce contrat.

| Situation | `etapeGrilleEnAttente` | Comportement à la validation |
|---|---|---|
| Aucune grille, ou uniquement des grilles rejetées — **durable** | `null` | La ligne est **exclue** ; le bénéficiaire n'est pas payé ce mois-ci (arbitrage du 2026-08-09). |
| Une grille attend une signature CRH ou DRH — **transitoire** | `"CRH"` ou `"DRH"` | La validation est **REFUSÉE (409)** — voir `POST /processus/{id}/valider`. |

Le blocage n'est pas une confirmation à cocher : c'est une situation à
résoudre. L'ARH a deux sorties, explicitées dans le message d'erreur — faire
aboutir la signature (le bénéficiaire sera payé au montant recalé), ou exclure
délibérément la ligne via `PATCH /processus/{id}` (il ne sera pas payé, mais
c'est alors une décision prise).

Motif : l'argument qui avait fait écarter « bloquer » le 9 août — devoir
relancer un cycle ARH→CRH→DRH complet — ne s'applique pas ici, puisque le cycle
est **déjà en vol**. MM.12 rend d'ailleurs ce cas plus fréquent : en passant de
deux à trois acteurs, il a allongé la fenêtre pendant laquelle une fonction n'a
plus de grille en vigueur.

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Écarts retournés (les deux listes sont vides si tout est à jour). |
| 404 | Processus introuvable. |

### POST /processus/{id}/valider — Implémenté (trois branches)

Endpoint unique pour les trois rôles, la branche étant choisie par le **statut
courant du processus**.

> *Correction de contrat (Sprint MM.12)* : cette entrée décrivait encore
> « seule la branche ARH est implémentée » et RG-08 comme non vérifiée. C'est
> périmé depuis le Sprint 5 — les branches CRH et DRH et la séparation des
> tâches sont en place.

**Rôles autorisés : ARH si `EN_COURS_ARH` ou `RETOURNE`, CRH si `EN_ATTENTE_CRH`, DRH si `EN_ATTENTE_DRH`**

Paramètre de requête (Sprint MM.12) :

| Paramètre | Défaut | Rôle |
| --- | --- | --- |
| `confirmerResynchronisation` | `false` | **Second temps de B2-RESYNC.** Sans lui, une validation ARH portant des montants obsolètes est **refusée (409)** plutôt que resynchronisée en silence — ce serait la variante B3, écartée. Sans effet sur les branches CRH et DRH, qui ne resynchronisent rien. |

Corps de la requête :

```json
{ "commentaire": "État vérifié et conforme." }
```

Réponse succès :

```json
{
  "id": 1,
  "statut": "EN_ATTENTE_CRH",
  "etapeValidee": "VALIDATION_ARH",
  "idPieceJointe": 1,
  "lignesResynchronisees": [],
  "lignesExclues": []
}
```

`lignesResynchronisees` / `lignesExclues` (Sprint MM.12) : récapitulatif de ce
qui a **réellement été appliqué**, même forme que
`GET /processus/{id}/ecarts-montants`. Renseignés uniquement par la branche ARH
(`null` pour les branches CRH et DRH). La resynchronisation est appliquée
**avant** la génération du PDF : le document et l'événement Kafka qui en découle
portent toujours les montants à jour.

Audit (RG-09, exigence explicite de la décision B) : une entrée par ligne —
`RESYNCHRONISATION_MONTANT_LIGNE` et `EXCLUSION_LIGNE_SANS_GRILLE_ACTIVE`, sur
l'entité `ligne_etat_mensuel`, avec delta `{"avant": …, "apres": …}`.

#### Événement Kafka de clôture (branche DRH) — enrichi au Sprint MM.13

La validation DRH clôture le processus et publie un événement sur le topic
`dottel.processus.cloture`, consommé par le module comptable — hors périmètre
DOTTEL, qui reste **producteur uniquement** (`CLAUDE.md` §11 et §17.9). Aucun
endpoint REST n'est concerné : ce n'est pas un contrat HTTP, mais il est
documenté ici faute d'autre emplacement, car c'est le seul contrat sortant du
module.

> **Schéma PROVISOIRE, à valider avec la comptabilité** (décision D de M.0).
> Le contrat exact attendu n'est pas confirmé : DOTTEL en propose un.

Jusqu'à MM.12 le payload ne portait qu'un **montant total agrégé**. MM.13 y
ajoute le **détail par bénéficiaire**, sans retirer aucun champ existant — les
consommateurs de l'ancien format restent valides.

```json
{
  "idProcessus": 11,
  "moisPaiement": 6,
  "anneePaiement": 2026,
  "montantTotal": 361000,
  "dateCloture": "2026-08-09T21:48:58.968",
  "lignes": [
    {
      "codeUnite": "1102",
      "codeAgence": "00004",
      "numCompteCourant": "10014275003",
      "chapitre": "37210130",
      "nomPrenoms": "ATANGANA Sylvie",
      "fonctionRetenue": "CONSEILLER",
      "montantAttribue": 50000
    }
  ]
}
```

| Règle | Détail |
| --- | --- |
| **Bénéficiaires inclus uniquement** | Une entrée par ligne d'état `inclus_dans_etat = true`. Un bénéficiaire exclu n'a pas été payé : une écriture comptable le concernant serait fausse. |
| **Valeurs figées** | `fonctionRetenue` et `montantAttribue` viennent de `ligne_etat_mensuel`, jamais recalculés depuis la grille courante — la comptabilité reçoit ce que la DRH a validé, même si la grille change ensuite. |
| **Montants entiers** | FCFA en `long`, jamais `BigDecimal`. |
| **Chapitre** | Donnée EHR du bénéficiaire, avec repli sur `dottel.documents.chapitre-defaut` — **même** mécanisme que le PDF, pas une seconde logique. |
| **Clé du message** | `idProcessus`, pour que tous les événements d'un même processus restent ordonnés sur la même partition. Envoi *fire-and-forget*. |

Les champs alimentent les deux lignes d'écriture comptable cibles (`CLAUDE.md`
§11), que DOTTEL ne génère pas lui-même :

```
DEBIT  : codeUnite  - 64310090002      - montantAttribue - DOT TEL MM/AAAA
CREDIT : codeAgence - numCompteCourant - montantAttribue - DOT TEL MM/AAAA
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Validation réussie. |
| 403 | RG-05 : rôle incompatible avec l'étape déclenchée par le statut. |
| 403 | RG-08 : l'acteur a déjà validé l'étape précédente du processus. |
| 404 | Processus introuvable. |
| 409 | Statut incompatible avec l'action. |
| 409 | **Sprint MM.12** — des montants sont obsolètes et `confirmerResynchronisation` n'a pas été fourni. Appeler `GET /processus/{id}/ecarts-montants`, présenter le récapitulatif, puis rappeler avec `confirmerResynchronisation=true`. |
| 409 | **Sprint MM.12 (P-2)** — une fonction de l'état mensuel n'a plus de grille en vigueur alors qu'une grille attend une signature CRH ou DRH. **Non contournable par `confirmerResynchronisation`** : le contrôle s'effectue avant. Le message nomme la fonction, le bénéficiaire, l'étage bloquant et la date de soumission, et rappelle les deux sorties (attendre la signature, ou exclure la ligne via `PATCH /processus/{id}`). |

### POST /processus/{id}/retourner — Implémenté (corrigé V3.6, documenté à tort "Planifié" depuis la V3.0)

Retourne le processus à l'ARH pour correction depuis `EN_ATTENTE_CRH` ou
`EN_ATTENTE_DRH`, motif obligatoire (RG-07). Un retour n'est pas terminal :
l'ARH reprend la main via `PATCH /processus/{id}` puis revalide via
`POST /processus/{id}/valider` (branche ARH). Voir aussi `motifRetour` /
`origineRetour` documentés dans `GET /processus/{id}` ci-dessus, qui
reflètent toujours le dernier retour en date.

**Rôles autorisés : CRH, DRH**

Corps de la requête :

```json
{ "motif": "Écart mensuel injustifié sur un bénéficiaire" }
```

Réponse succès :

```json
{ "id": 1, "statut": "RETOURNE", "motif": "Écart mensuel injustifié sur un bénéficiaire" }
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Retour enregistré. |
| 400 | Motif absent ou vide (RG-07). |
| 404 | Processus introuvable. |
| 409 | Statut du processus différent de `EN_ATTENTE_CRH`/`EN_ATTENTE_DRH`. |

### GET /processus/{id}/piece-jointe — Implémenté (corrigé V3.6, documenté à tort "Planifié" depuis la V3.0)

Métadonnées de la pièce jointe unique du processus (pas le fichier lui-même — voir `GET /pieces-jointes/{id}/download` pour le binaire).

**Rôles autorisés : ARH, CRH, DRH**

Réponse succès :

```json
{
  "id": 1,
  "nomFichier": "dotations-telephoniques-7-2026.pdf",
  "dateGenerationInitiale": "2026-07-09T08:00:00",
  "dateDerniereMiseAJour": "2026-07-10T14:30:00",
  "nombreSignatures": 2
}
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Métadonnées retournées. |
| 404 | Processus ou pièce jointe introuvable. |

### GET /pieces-jointes/{id}/download — Implémenté (corrigé V3.6, documenté à tort "Planifié" depuis la V3.0)

Télécharge le PDF de l'état mensuel (`id` = id de la `piece_jointe`, pas du processus).

**Rôles autorisés : ARH, CRH, DRH**

Réponse succès : fichier binaire — `Content-Type: application/pdf`,
`Content-Disposition: attachment; filename="dotations-telephoniques-<mois>-<annee>.pdf"`.

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Fichier PDF retourné. |
| 404 | Pièce jointe introuvable. |

# 6. Groupe Reporting — /reporting

**Correction V3.5** : ce groupe était documenté "non implémenté" depuis la V3.0. En réalité, construit et consommé par le frontend (`DashboardPage`, `HistoriquePage`, `AuditPage`) depuis le Sprint 6F — corrigé ici à l'occasion de l'ajout de GET /reporting/audit/actions (Sprint MM.11).

### GET /reporting/dashboard — Implémenté

Indicateurs agrégés pour la page d'accueil ARH/DRH.

**Rôles autorisés : ARH, DRH**

Réponse succès :

```json
{
  "nombreBeneficiairesActifs": 312,
  "montantTotalMensuel": 14520000,
  "processusEnCours": {"id": 21, "statut": "EN_ATTENTE_CRH"},
  "processusClotureesCetteAnnee": 9
}
```

`processusEnCours` vaut `null` si aucun processus n'est en cours (tous clôturés ou aucun déclenché).

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Indicateurs retournés. |

### GET /reporting/historique — Implémenté

Historique des processus clôturés, agrégé par mois.

**Rôles autorisés : DRH**

Paramètre de requête (facultatif) : `annee` (ex. `2026`).

Réponse succès :

```json
{
  "lignes": [
    {
      "moisPaiement": 7, "anneePaiement": 2026, "statut": "CLOTURE",
      "montantTotal": 1840000, "nombreBeneficiaires": 46
    }
  ]
}
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Historique retourné (liste vide si aucun processus clôturé). |

### GET /reporting/historique/export — Implémenté

Export Excel (`.xlsx`) du même historique, mêmes filtres.

**Rôles autorisés : DRH**

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Classeur Excel retourné (`Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`). |

### GET /reporting/audit — Implémenté

Journal d'audit paginé, avec filtres combinables.

**Rôles autorisés : DRH**

Paramètres de requête (tous facultatifs) : `idUtilisateur`, `action`, `entiteCible`, `dateDebut`, `dateFin` (ISO 8601), `page` (défaut `0`), `taille` (défaut `20`).

Réponse succès :

```json
{
  "contenu": [
    {
      "id": 501, "idUtilisateur": 10, "action": "DECLENCHEMENT_PROCESSUS_RATTRAPAGE",
      "entiteCible": "processus_mensuel", "idEntite": 33,
      "dateAction": "2026-08-09T01:30:00", "adresseIp": null,
      "detailJson": "{\"avant\":null,\"apres\":{\"rattrapage\":true,\"idProcessusOriginal\":30}}"
    }
  ],
  "total": 214,
  "page": 0,
  "taille": 20
}
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Page de résultats retournée. |

### GET /reporting/audit/actions — Implémenté (ajouté au Sprint MM.11)

**Nouvel endpoint.** Alimente dynamiquement le filtre "Action" de `AuditPage` : liste triée des valeurs `action` réellement présentes en base (`SELECT DISTINCT`), et non une liste figée maintenue à la main côté frontend. Les codes d'action sont des chaînes libres déposées par chaque module au fil de `AuditService.enregistrer()` (aucun enum de référence — CLAUDE.md section 6 fixe la liste des énumérations du projet à 5, aucune ne couvre ce cas). Conséquence : une action qui n'a encore jamais eu lieu n'apparaît pas dans la liste (rien à filtrer de toute façon).

**Rôles autorisés : DRH**

Réponse succès :

```json
[
  "AJUSTEMENT_LIGNE_ETAT_MENSUEL",
  "CLOTURE_PROCESSUS",
  "DECLENCHEMENT_PROCESSUS",
  "DECLENCHEMENT_PROCESSUS_RATTRAPAGE",
  "VALIDATION_PROCESSUS_ARH"
]
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Liste retournée (peut être vide si `audit_log` est vide). |

# 7. Groupe Grilles tarifaires — /grilles-tarifaires

**Groupe en cours de construction (Sprint 4bis).**

> **Sprint MM.12 — workflow à trois acteurs.** Le CRH est inséré entre l'ARH et
> la DRH, sur le modèle du workflow du processus mensuel. Le cycle de vie
> `StatutGrilleEnum` devient :
>
> ```
> création ARH ──► EN_ATTENTE_CRH ──(CRH valide)──► EN_ATTENTE_DRH ──(DRH valide)──► ACTIVE
>                        │                                 │
>                   (CRH rejette)                     (DRH rejette)
>                        └──────────► REJETEE ◄────────────┘
> ```
>
> `BROUILLON` reste déclaré mais demeure inatteignable (décision Sprint 4bis.1).
> `REJETEE` est **terminal** : l'ARH crée une nouvelle grille, il n'y a jamais de
> resoumission — donc jamais plus d'un rejet par ligne.
>
> **RG-08 appliquée (option W-2, arbitrée le 2026-08-09)** avec un mécanisme
> propre au module `referentiel` (colonnes `id_decideur_crh` /
> `date_decision_crh`, migration V7), et non par réutilisation de
> `SeparationTachesService` qui aurait créé un second cycle de modules. Le CRH ne
> peut pas être l'ARH créateur ; la DRH ne peut pas être le CRH décideur → 403.
>
> Les grilles déjà `EN_ATTENTE_DRH` avant MM.12 sont **laissées en l'état** (le
> CRH est réputé avoir implicitement validé) ; leur `id_decideur_crh` reste
> `null` et RG-08 est donc inopérante pour cette population transitoire.

### GET /grilles-tarifaires — Implémenté (Sprint 6F.7)

Liste les grilles tarifaires, tous statuts confondus, avec filtres
optionnels par fonction et par statut. Pas de pagination : volume
interne limité (25 fonctions éligibles, quelques grilles par fonction
au fil du temps), même décision que pour `GET /admin/utilisateurs`.

**Rôles autorisés : ARH, ADMIN**

Paramètres de requête (facultatifs) : `fonction` (code), `statut`
(`BROUILLON` | `EN_ATTENTE_CRH` | `EN_ATTENTE_DRH` | `ACTIVE` | `REJETEE`).

Réponse succès :

```json
{
  "contenu": [
    {
      "id": 12, "codeFonction": "GFC", "libelleFonction": "Gestionnaire de Fonds de Commerce",
      "montantFcfa": 40000, "dateDebut": "2026-01-01", "dateFin": null,
      "statutValidation": "ACTIVE", "motifRejet": null, "origineRejet": null
    }
  ]
}
```

`motifRejet` (Sprint 6F.7, RG-10) : renseigné uniquement pour une grille au
statut `REJETEE`, `null` sinon.

`origineRejet` (Sprint MM.12) : `"CRH"` ou `"DRH"` selon l'étage qui a rejeté,
`null` si la grille n'est pas `REJETEE`. **Dérivé côté service, jamais stocké** —
équivalent d'`origineRetour` pour le processus mensuel. La DRH est testée en
premier, car après un rejet DRH les deux couples de décision sont renseignés.
Peut être `null` sur une grille rejetée avant MM.12.

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Liste retournée (vide si aucune grille ne correspond aux filtres). |
| 404 | Code fonction fourni en filtre mais inconnu. |

### POST /grilles-tarifaires — Implémenté

Crée une nouvelle grille tarifaire pour une fonction. La grille passe
directement au premier statut d'attente (soumission automatique, conforme à
US-20 — pas d'étape BROUILLON distincte, décision actée au Sprint 4bis.1 après
contradiction repérée dans une version antérieure de ce contrat).

**Sprint MM.12** : ce premier statut est désormais `EN_ATTENTE_CRH`, plus
`EN_ATTENTE_DRH`.

**Sprint MM.14, écart E2/E3 de l'audit tranché avec le métier** : le rôle
**ADMIN est retiré**. Fixer un montant de dotation n'est pas un acte
d'administration technique — c'est une décision métier qui part aussitôt dans
le circuit ARH → CRH → DRH (RG-10), circuit dont l'ADMIN n'est acteur d'aucune
étape ; l'y laisser injecter un montant contredirait l'esprit de RG-08. Le
frontend appliquait déjà cette restriction (bouton « Créer une grille »
conditionné au rôle ARH) : c'est le `@PreAuthorize` et ce contrat qui étaient
en retard. **L'ADMIN conserve** `GET /grilles-tarifaires`,
`GET /grilles-tarifaires/fonction/{code}` et surtout `POST /fonctions-eligibles`,
qui crée une fonction **neuve** avec sa grille initiale directement ACTIVE —
chemin distinct, interne au module `referentiel`, qui ne passe pas par cet
endpoint.

**Rôles autorisés : ARH**

Corps de la requête :

```json
{
  "codeFonction": "GFC",
  "montantFcfa": 45000,
  "dateDebut": "2026-08-01"
}
```

Réponse succès :

```json
{
  "id": 25, "codeFonction": "GFC",
  "montantFcfa": 45000, "dateDebut": "2026-08-01",
  "statutValidation": "EN_ATTENTE_CRH"
}
```

| **Code HTTP** | **Description** |
| --- | --- |
| 201 | Grille créée, soumise automatiquement au CRH. |
| 404 | Fonction inconnue. |
| 409 | Une grille est déjà en attente pour cette fonction. **Sprint MM.12** : le contrôle couvre désormais les DEUX statuts d'attente (`EN_ATTENTE_CRH` **ou** `EN_ATTENTE_DRH`) — ne vérifier que l'un des deux laisserait créer deux grilles concurrentes pour la même fonction. Toujours pas de blocage sur ACTIVE : ce serait le cas d'usage normal de mise à jour tarifaire. |
| 409 | `dateDebut` antérieure ou égale à la date de la grille non rejetée la plus récente pour cette fonction (comparée à `dateFin` si elle est déjà clôturée, sinon à `dateDebut`). Ajouté au Sprint 6F.7bis : avant l'introduction de la désactivation manuelle, il existait toujours une grille ACTIVE + `dateFin IS NULL` par fonction, et la validation DRH la fermait automatiquement, imposant de fait un ordre chronologique. Une fois cette grille désactivée manuellement, plus rien n'empêchait de créer une grille avec une `dateDebut` antérieure à l'historique existant — constat remonté manuellement en test, corrigé par ce contrôle explicite. |

### PATCH /grilles-tarifaires/{id} — Implémenté

Modifie une grille **en statut `EN_ATTENTE_CRH` uniquement** (corrige la version
précédente de ce contrat, qui exigeait à tort le statut BROUILLON —
inatteignable puisque la création passe directement au premier statut d'attente).

**Sprint MM.12, arbitrage du 2026-08-09** : le montant est **gelé dès que le CRH
a statué**. Autoriser `EN_ATTENTE_DRH` permettrait à l'ARH de changer le montant
après la validation CRH — la DRH validerait alors un chiffre que le CRH n'a
jamais vu, ce qui viderait l'étape CRH de son sens.

**Sprint MM.14** : rôle **ADMIN retiré**, même raisonnement que pour
`POST /grilles-tarifaires` ci-dessus.

**Rôles autorisés : ARH**

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Modification enregistrée. |
| 400 | Grille non en statut `EN_ATTENTE_CRH` (y compris `EN_ATTENTE_DRH` : le CRH a déjà statué). |
| 404 | Grille introuvable. |

### GET /grilles-tarifaires/en-attente-crh — Implémenté (Sprint MM.12)

Endpoint dédié CRH : liste uniquement les grilles au statut `EN_ATTENTE_CRH`.
Pendant strict de `/en-attente-drh`, et pour la même raison :
`GET /grilles-tarifaires` reste réservé ARH/ADMIN, on ne l'élargit pas.

**Rôles autorisés : CRH**

Réponse succès : même forme que `GET /grilles-tarifaires`.

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Liste retournée (vide si aucune grille en attente CRH). |

### GET /grilles-tarifaires/en-attente-drh — Implémenté (Sprint 6F.7)

Endpoint dédié DRH : liste uniquement les grilles au statut
EN_ATTENTE_DRH (équivalent à `GET /grilles-tarifaires?statut=EN_ATTENTE_DRH`,
mais accessible à la DRH puisque `GET /grilles-tarifaires` reste réservé
ARH/ADMIN — décision actée au Sprint 6F.7 pour ne pas élargir les rôles
de la liste générale).

**Rôles autorisés : DRH**

Réponse succès : même forme que `GET /grilles-tarifaires`.

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Liste retournée (vide si aucune grille en attente). |

### POST /grilles-tarifaires/{id}/valider — Implémenté (Sprint MM.12)

**Endpoint unique partagé par le CRH et la DRH**, la branche étant choisie par
le **statut courant de la grille** — exactement le modèle de
`POST /processus/{id}/valider`, qui sert déjà trois rôles.

**Rôles autorisés : CRH, DRH**

`hasAnyRole('CRH','DRH')` ne suffit pas à garantir l'ordre des étapes : c'est
`SeparationTachesGrilleService.verifierRoleAttendu()`, en tête de chaque branche
du service, qui empêche une DRH de statuer sur une grille `EN_ATTENTE_CRH` (ce
qui sauterait purement et simplement l'étape CRH).

Corps de la requête :

```json
{
  "decision": "VALIDER",
  "motifRejet": null
}
```

`decision` : `VALIDER` | `REJETER`. `motifRejet` obligatoire et non vide si
`REJETER` (RG-07), quel que soit l'acteur.

| Statut avant | Décision | Statut après |
| --- | --- | --- |
| `EN_ATTENTE_CRH` | `VALIDER` | `EN_ATTENTE_DRH` |
| `EN_ATTENTE_CRH` | `REJETER` | `REJETEE` (`origineRejet = "CRH"`) |
| `EN_ATTENTE_DRH` | `VALIDER` | `ACTIVE` — l'ancienne grille ACTIVE voit sa `dateFin` renseignée à `dateDebut - 1 jour` (RG-10) |
| `EN_ATTENTE_DRH` | `REJETER` | `REJETEE` (`origineRejet = "DRH"`) |

Une validation CRH ne met **aucune** grille en vigueur : RG-10 ne s'applique
qu'à la validation DRH.

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Décision enregistrée. |
| 400 | `decision` invalide, ou `motifRejet` absent/vide sur un `REJETER` (RG-07). |
| 403 | RG-05 : le rôle de l'acteur ne correspond pas à l'étage de la grille. |
| 403 | RG-08 : l'acteur a déjà statué sur l'étape précédente de cette grille (CRH = l'ARH créateur, ou DRH = le CRH décideur). |
| 404 | Grille introuvable. |
| 409 | Grille non en attente de décision (`ACTIVE`, `REJETEE`…). |

### GET /grilles-tarifaires/fonction/{code} — Implémenté

Historique chronologique (plus récente d'abord) de toutes les grilles
d'une fonction, tous statuts confondus.

**Rôles autorisés : ARH, DRH, ADMIN**

Réponse succès :

```json
{
  "codeFonction": "JURISTE",
  "libelleFonction": "Agent de Recouvrement",
  "grilles": [
    {
      "id": 31, "montantFcfa": 38000, "dateDebut": "2026-10-01", "dateFin": null,
      "statutValidation": "REJETEE",
      "motifRejet": "Montant supérieur au plafond prévu pour cette fonction selon la note NS 69/17.",
      "origineRejet": "CRH"
    },
    {
      "id": 20, "montantFcfa": 35000, "dateDebut": "2025-01-01", "dateFin": null,
      "statutValidation": "ACTIVE", "motifRejet": null, "origineRejet": null
    }
  ]
}
```

`motifRejet` (Sprint 6F.7, RG-10) et `origineRejet` (Sprint MM.12) : mêmes
règles que sur `GET /grilles-tarifaires`.

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Historique retourné. |
| 404 | Code fonction inconnu. |

### POST /grilles-tarifaires/{id}/desactiver — Implémenté (Sprint 6F.7bis)

Retire volontairement le montant actif d'une grille, sans remplacement.
Comble un écart du cahier des charges (section II.1.7 : *« Le système
permet de créer, modifier et désactiver une grille tarifaire »*) — le
cycle de vie initial (BROUILLON → EN_ATTENTE_DRH → ACTIVE/REJETEE) ne
couvrait que le remplacement automatique d'une grille par une autre
(`dateFin` positionnée à la validation d'une nouvelle grille), jamais un
retrait volontaire. Positionne `dateFin = date du jour` sur la grille ;
`statutValidation` reste `ACTIVE` (même convention que le remplacement
automatique — RG-04 considère une grille "courante" seulement si
`statutValidation = ACTIVE ET dateFin IS NULL`). Aucune nouvelle grille
n'est créée par cette action.

**Rôles autorisés : ARH** (restreint à ARH — DRH écarté en cours de
sprint pour rester cohérent avec `GET /grilles-tarifaires`, réservé
ARH/ADMIN, auquel la route frontend correspondante est elle-même
limitée)

Réponse succès : même forme que `POST /grilles-tarifaires`, avec `statutValidation: "ACTIVE"` et `dateFin` renseignée (implicite, absente du DTO de réponse actuel — voir `dateDebut`/`montantFcfa` retournés).

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Grille désactivée. |
| 404 | Grille introuvable. |
| 409 | Grille non en statut ACTIVE courante (déjà BROUILLON/EN_ATTENTE_DRH/REJETEE, ou déjà clôturée — `statutValidation = ACTIVE` mais `dateFin` déjà renseignée). |

# 8. Groupe Administration — /admin

**Corrigé V3.6 : groupe entièrement implémenté**, documenté à tort "non
implémenté à ce jour" depuis la V3.0. Pas de pagination sur la liste :
volume interne limité à quelques dizaines d'utilisateurs (même décision
que `GET /grilles-tarifaires`).

### GET /admin/utilisateurs — Implémenté (corrigé V3.6)

Liste les utilisateurs, avec filtres optionnels `role` et `actif`.

**Rôles autorisés : ADMIN, DRH** (DRH ajouté par rapport à la version
précédente de ce contrat, qui ne prévoyait qu'ADMIN — la DRH a un accès
lecture seule, il alimente le filtre utilisateur du journal d'audit,
`CLAUDE.md` section 8)

Paramètres de requête (facultatifs) : `role` (`EMPLOYE`|`ARH`|`CRH`|`DRH`|`ADMIN`), `actif` (booléen).

Réponse succès :

```json
{
  "contenu": [
    { "id": 1, "matricule": "1847", "nomPrenoms": "MBARGA Jean Paul", "role": "ARH", "actif": true }
  ]
}
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Liste retournée. |
| 403 | Rôle non autorisé. |

### POST /admin/utilisateurs — Implémenté (corrigé V3.6)

Crée un utilisateur.

**Rôles autorisés : ADMIN**

Corps de la requête :

```json
{
  "matricule": "5521",
  "nom": "ONANA",
  "prenom": "Patrice",
  "email": "patrice_onana@afrilandfirstbank.com",
  "role": "CRH",
  "motDePasse": "Test1234"
}
```

Note : `motDePasse` reste requis dans le contrat mais **sans usage réel**
depuis MM.7 — l'authentification est portée par Keycloak, pas par ce
mot de passe local. Champ conservé le temps qu'une décision explicite
tranche son devenir (voir `docs/monolithe-modulaire/MM.7_keycloak_provisoire.md`
section 3.3, point non clos).

Réponse succès :

```json
{ "id": 15, "matricule": "5521", "nomPrenoms": "ONANA Patrice", "role": "CRH", "actif": true }
```

| **Code HTTP** | **Description** |
| --- | --- |
| 201 | Utilisateur créé. |
| 400 | Corps invalide (Bean Validation). |
| 409 | Matricule déjà utilisé, ou email déjà utilisé. |

### PATCH /admin/utilisateurs/{id}/statut — Implémenté (corrigé V3.6)

Active/désactive un utilisateur.

**Rôles autorisés : ADMIN**

Corps de la requête :

```json
{ "actif": false }
```

Réponse succès : même forme que `POST /admin/utilisateurs`.

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Statut modifié. |
| 404 | Utilisateur introuvable. |
| 409 | Un administrateur ne peut pas se désactiver lui-même, ni désactiver le dernier compte ADMIN actif restant (garde-fous actés le 23/07/2026). |

### PATCH /admin/utilisateurs/{id}/role — Implémenté (corrigé V3.6)

Change le rôle d'un utilisateur.

**Rôles autorisés : ADMIN**

Corps de la requête :

```json
{ "role": "DRH" }
```

Réponse succès : même forme que `POST /admin/utilisateurs`.

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Rôle modifié. |
| 400 | Rôle invalide (ne correspond à aucune valeur de `RoleEnum`). |
| 404 | Utilisateur introuvable. |
| 409 | Un administrateur ne peut pas s'auto-rétrograder vers un rôle différent d'ADMIN, ni rétrograder le dernier compte ADMIN actif restant. |

# 9. Codes HTTP globaux

| **Code** | **Signification globale dans ce module** |
| --- | --- |
| 200 | Succès — opération effectuée |
| 201 | Création réussie |
| 400 | Requête invalide — Bean Validation, ou règle métier de type RG-07/RG-11, ou absence de grille ACTIVE pour RG-04 |
| 401 | Non authentifié — token absent, invalide ou expiré |
| 403 | Non autorisé — rôle insuffisant, ou violation RG-01/RG-02 (éligibilité), ou violation RG-08 (séparation des tâches — processus mensuel depuis le Sprint 5, **et grilles tarifaires depuis le Sprint MM.12**, corrigé V3.6) |
| 404 | Ressource introuvable |
| 409 | Conflit — doublon matricule (RG-03), doublon processus mensuel (RG-12, distincte de RG-03), ou état incompatible avec l'action demandée |

**Correction importante par rapport à la V3.0** : la ligne *"500 — MONTANT_INDISPONIBLE si aucune grille ACTIVE (RG-04)"* était erronée et contredisait la section 2 du même document. L'absence de grille ACTIVE pour RG-04 retourne **400**, jamais 500, via `GrilleTarifaireIntrouvableException` — code déjà correctement implémenté et testé depuis le Sprint 2.3. Cette ligne 500 est supprimée dans cette révision.