# CONTRATS API

Module : Digitalisation des Dotations Téléphoniques Mensuelles

*Projet AFRILAND HORIZON 2030*

| **Référence** | **AFB_API_DOTTEL_V3.3_2026** |
| --- | --- |
| Version | 3.3 |
| Date | Juillet 2026 |
| Nombre d'endpoints documentés | 35 endpoints répartis en 8 groupes (+ 1 sous-groupe) |
| Version 3.1 | Alignement complet sur l'implémentation réelle : correction des réponses login/PATCH bénéficiaire, ajout de PATCH /processus/{id} (absent de la V3.0), correction de l'erreur RG-04 (400, pas 500), ajout d'un statut d'implémentation par endpoint. |
| Version 3.2 | Ajout de GET /fonctions-eligibles et de PATCH /beneficiaires/{id}/reactiver (Sprint 6F.5), absents de la V3.1 — le premier alimente en frontend les filtres et formulaires liés à la fonction sans liste en dur, le second comble une lacune : aucun endpoint ne permettait de revenir sur une désactivation. |
| Version 3.3 | Sprint 6F.7bis : comble deux écarts face au cahier des charges (section II.1.7). Ajout de POST /grilles-tarifaires/{id}/desactiver (retrait volontaire d'une grille ACTIVE sans remplacement, absent du cycle de vie initial qui ne couvrait que le remplacement automatique). `fonction_eligible` passe d'un référentiel figé par migration Flyway à un référentiel géré par l'application : ajout de GET /fonctions-eligibles/toutes, POST /fonctions-eligibles, PATCH /fonctions-eligibles/{code}, PATCH /fonctions-eligibles/{code}/desactiver et PATCH /fonctions-eligibles/{code}/reactiver. Rôle ADMIN ajouté à GET /fonctions-eligibles (nécessaire au filtre de `GrillesListPage` côté ADMIN, oublié à la V3.2). |

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
| Authentification | JWT — Header Authorization: Bearer {token} — signature HS384 |
| Encodage | UTF-8 |
| Dates | Format ISO 8601 : YYYY-MM-DD |
| Montants | Entiers en FCFA (jamais de décimales, jamais stockés directement sur un bénéficiaire) |

# 1. Groupe Auth — /auth

### POST /auth/login — Implémenté

Connexion avec matricule et mot de passe. Retourne un token JWT valide 8 heures (signature HS384 explicite).

**Rôles autorisés : Public**

Corps de la requête :

```json
{
  "matricule": "1847",
  "motDePasse": "Test1234"
}
```

Réponse succès (structure réelle — corrigée par rapport à la V3.0, qui listait à tort `nomUtilisateur` et `expireAt`, deux champs qui n'existent pas dans `LoginResponseDto`) :

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "matricule": "1847",
  "role": "ARH",
  "nom": "MBARGA",
  "prenom": "Jean Paul"
}
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Connexion réussie. |
| 400 | Corps invalide ou champ manquant. |
| 401 | Identifiants incorrects (`IdentifiantsInvalidesException`). |
| 403 | Compte désactivé (`UtilisateurInactifException` — identité prouvée, statut bloque). |

### POST /auth/logout — Implémenté

Terminaison stateless : aucune liste noire de jetons, le token expire naturellement après 8h. Ce choix est documenté explicitement dans le code.

**Rôles autorisés : Authentifié**

Réponse succès :

```json
{ "message": "Déconnexion réussie." }
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Déconnexion réussie. |
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
| 400 | Données invalides. |
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

Importe des bénéficiaires en masse. Format à 7 colonnes (la colonne MONTANT a été retirée sur décision métier — le montant est dérivé de la grille tarifaire, jamais importé) :

`N°ORDRE, MATRICULE, NOMS & PRENOMS, FONCTION, UNITE, CODE_UNITE, N°COMPTE`

Les corps de contrôle et assimilés (`CORPS_CONTROLE_IG`, `CORPS_CONTROLE_IGA`, `CONTROLEUR_GESTION`, `CONTROLEUR_COMPTABLE`, `COMPTABLE`) sont systématiquement rejetés à l'import — le format n'a pas de colonne GRADE, donc RG-02 ne peut jamais être vérifié via ce canal. Ces cas doivent passer par l'enrôlement individuel.

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

Corps de la requête :

```json
{
  "moisPaiement": 7,
  "anneePaiement": 2026
}
```

Réponse succès (le champ `beneficiairesExclus` a été ajouté par rapport à la V3.0, qui l'omettait) :

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
  ]
}
```

`nombreBeneficiaires` ne compte que les lignes avec `inclus_dans_etat=true` (les exclus n'y figurent pas, mais restent listés dans `beneficiairesExclus` avec leur motif — grille introuvable, ou fonction désactivée depuis l'enrôlement).

| **Code HTTP** | **Description** |
| --- | --- |
| 201 | Processus déclenché. |
| 409 | Processus déjà existant pour cette période (RG-12, distincte de RG-03 — corrige la V3.0 qui référençait cette règle de façon ambiguë). |

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
    "statut": "RETOURNE", "dateCreation": "2026-07-24T20:46:47.624242"
  }
]
```

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
  "origineRetour": "DRH"
}
```

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

### POST /processus/{id}/valider — En cours (branche ARH uniquement)

**Seule la branche ARH est implémentée à ce jour** (transition `EN_COURS_ARH → EN_ATTENTE_CRH`, génération du PDF initial via `DocumentService.genererInitiale()`). Les branches CRH et DRH, ainsi que la vérification RG-08 (séparation des tâches), sont **non implémentées**, prévues au Sprint 5.

**Rôles autorisés (portée actuelle) : ARH, si statut EN_COURS_ARH**
**Rôles autorisés (prévus, non actifs) : CRH si EN_ATTENTE_CRH, DRH si EN_ATTENTE_DRH**

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
  "idPieceJointe": 1
}
```

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Validation réussie. |
| 403 | Séparation des tâches violée (RG-08) — non vérifié à ce jour, réservé aux branches CRH/DRH du Sprint 5. |
| 409 | Statut incompatible avec l'action. |

### POST /processus/{id}/retourner — Planifié (non implémenté)

Retourne le processus à l'ARH pour correction, motif obligatoire (RG-07). Prévu Sprint 5, avec le reste du workflow CRH/DRH.

**Rôles autorisés prévus : CRH, DRH**

### GET /processus/{id}/piece-jointe — Planifié (non implémenté)

Métadonnées de la pièce jointe unique du processus. L'entité `PieceJointe` et son repository existent depuis le Sprint 3.4, mais aucun endpoint de consultation n'a encore été construit.

**Rôles autorisés prévus : ARH, CRH, DRH**

### GET /pieces-jointes/{id}/download — Planifié (non implémenté)

Téléchargement du PDF de l'état mensuel. Prévu avec le reste du groupe Workflow, Sprint 5 ou 6.

**Rôles autorisés prévus : ARH, CRH, DRH**

# 6. Groupe Reporting — /reporting

**Groupe entièrement non implémenté à ce jour.** Prévu au Sprint 6 (tableau de bord, historique, journal d'audit consultable). Les endpoints ci-dessous restent des cibles de conception, pas des contrats figés.

### GET /reporting/dashboard — Planifié (Sprint 6)

**Rôles autorisés prévus : ARH, DRH**

### GET /reporting/historique — Planifié (Sprint 6)

**Rôles autorisés prévus : DRH**

### GET /reporting/audit — Planifié (Sprint 6)

**Rôles autorisés prévus : DRH**

# 7. Groupe Grilles tarifaires — /grilles-tarifaires

**Groupe en cours de construction (Sprint 4bis).**

### GET /grilles-tarifaires — Implémenté (Sprint 6F.7)

Liste les grilles tarifaires, tous statuts confondus, avec filtres
optionnels par fonction et par statut. Pas de pagination : volume
interne limité (25 fonctions éligibles, quelques grilles par fonction
au fil du temps), même décision que pour `GET /admin/utilisateurs`.

**Rôles autorisés : ARH, ADMIN**

Paramètres de requête (facultatifs) : `fonction` (code), `statut`
(`BROUILLON` | `EN_ATTENTE_DRH` | `ACTIVE` | `REJETEE`).

Réponse succès :

```json
{
  "contenu": [
    {
      "id": 12, "codeFonction": "GFC", "libelleFonction": "Gestionnaire de Fonds de Commerce",
      "montantFcfa": 40000, "dateDebut": "2026-01-01", "dateFin": null,
      "statutValidation": "ACTIVE", "motifRejet": null
    }
  ]
}
```

`motifRejet` (Sprint 6F.7, RG-10) : renseigné uniquement pour une grille au
statut `REJETEE`, `null` sinon.

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Liste retournée (vide si aucune grille ne correspond aux filtres). |
| 404 | Code fonction fourni en filtre mais inconnu. |

### POST /grilles-tarifaires — Implémenté

Crée une nouvelle grille tarifaire pour une fonction. La grille passe
directement au statut EN_ATTENTE_DRH (soumission automatique à la DRH,
conforme à US-20 — pas d'étape BROUILLON distincte, décision actée au
Sprint 4bis.1 après contradiction repérée dans une version antérieure
de ce contrat).

**Rôles autorisés : ARH, ADMIN**

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
  "statutValidation": "EN_ATTENTE_DRH"
}
```

| **Code HTTP** | **Description** |
| --- | --- |
| 201 | Grille créée, soumise automatiquement à la DRH. |
| 404 | Fonction inconnue. |
| 409 | Une grille est déjà EN_ATTENTE_DRH pour cette fonction (pas ACTIVE — bloquer sur ACTIVE empêcherait le cas d'usage normal de mise à jour tarifaire). |
| 409 | `dateDebut` antérieure ou égale à la date de la grille non rejetée la plus récente pour cette fonction (comparée à `dateFin` si elle est déjà clôturée, sinon à `dateDebut`). Ajouté au Sprint 6F.7bis : avant l'introduction de la désactivation manuelle, il existait toujours une grille ACTIVE + `dateFin IS NULL` par fonction, et la validation DRH la fermait automatiquement, imposant de fait un ordre chronologique. Une fois cette grille désactivée manuellement, plus rien n'empêchait de créer une grille avec une `dateDebut` antérieure à l'historique existant — constat remonté manuellement en test, corrigé par ce contrôle explicite. |

### PATCH /grilles-tarifaires/{id} — Implémenté

Modifie une grille en statut EN_ATTENTE_DRH (corrige la version
précédente de ce contrat, qui exigeait à tort le statut BROUILLON —
inatteignable puisque la création passe directement à EN_ATTENTE_DRH).

**Rôles autorisés : ARH, ADMIN**

| **Code HTTP** | **Description** |
| --- | --- |
| 200 | Modification enregistrée. |
| 400 | Grille non en statut EN_ATTENTE_DRH. |
| 404 | Grille introuvable. |

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

### POST /grilles-tarifaires/{id}/valider — Planifié (Sprint 4bis.2)

**Rôles autorisés prévus : DRH**

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
      "motifRejet": "Montant supérieur au plafond prévu pour cette fonction selon la note NS 69/17."
    },
    {
      "id": 20, "montantFcfa": 35000, "dateDebut": "2025-01-01", "dateFin": null,
      "statutValidation": "ACTIVE", "motifRejet": null
    }
  ]
}
```

`motifRejet` (Sprint 6F.7, RG-10) : mêmes règles que sur `GET /grilles-tarifaires`.

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

**Groupe entièrement non implémenté à ce jour.** Prévu Sprint 4bis.3 et 4bis.4.

### GET /admin/utilisateurs — Planifié (Sprint 4bis.3)

**Rôles autorisés prévus : ADMIN**

### POST /admin/utilisateurs — Planifié (Sprint 4bis.3)

**Rôles autorisés prévus : ADMIN**

### PATCH /admin/utilisateurs/{id}/statut — Planifié (Sprint 4bis.4)

**Rôles autorisés prévus : ADMIN**

### PATCH /admin/utilisateurs/{id}/role — Planifié (Sprint 4bis.4)

**Rôles autorisés prévus : ADMIN**

# 9. Codes HTTP globaux

| **Code** | **Signification globale dans ce module** |
| --- | --- |
| 200 | Succès — opération effectuée |
| 201 | Création réussie |
| 400 | Requête invalide — Bean Validation, ou règle métier de type RG-07/RG-11, ou absence de grille ACTIVE pour RG-04 |
| 401 | Non authentifié — token absent, invalide ou expiré |
| 403 | Non autorisé — rôle insuffisant, ou violation RG-01/RG-02 (éligibilité), ou violation RG-08 (séparation des tâches, Sprint 5 uniquement) |
| 404 | Ressource introuvable |
| 409 | Conflit — doublon matricule (RG-03), doublon processus mensuel (RG-12, distincte de RG-03), ou état incompatible avec l'action demandée |

**Correction importante par rapport à la V3.0** : la ligne *"500 — MONTANT_INDISPONIBLE si aucune grille ACTIVE (RG-04)"* était erronée et contredisait la section 2 du même document. L'absence de grille ACTIVE pour RG-04 retourne **400**, jamais 500, via `GrilleTarifaireIntrouvableException` — code déjà correctement implémenté et testé depuis le Sprint 2.3. Cette ligne 500 est supprimée dans cette révision.