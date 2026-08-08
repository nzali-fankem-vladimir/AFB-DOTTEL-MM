# PLAN — AJOUTS MÉTIER, VERSION MONOLITHE MODULAIRE

## Adaptation du lot M.0–M.5 à l'architecture issue du chantier MM

*Module Dotations Téléphoniques Mensuelles*

| | |
|---|---|
| **Objet** | Porter les 10 ajouts demandés par le métier sur la version **monolithe modulaire** du projet, et corriger deux anomalies détectées en cours de chantier |
| **Livrable** | 6 guides de sous-sprint (MM.8 à MM.13), directement démarrables |
| **Cible** | Copie `afb-dottel-mm`, base `afb_dotations_telephoniques_mm` — **jamais** le dépôt d'origine |
| **Prérequis** | Chantier MM.0 → MM.6 clos (`ModularityTests` vert, 226 tests) |
| **Origine** | Guides `M.0` à `M.5` du dépôt d'origine, dossier `docs/chantier d'ajout metier/` |

---

## 0. Pourquoi ce dossier existe

Les guides `M.0` à `M.5` ont été rédigés pour la version du projet **antérieure** au chantier monolithe modulaire, et leur propre plan maître le dit explicitement :

> « ils s'appliquent sur la version du projet **antérieure** au chantier de passage en monolithe modulaire […] Ces deux chantiers ne doivent pas être mélangés »
> — `PLAN_AJOUTS_METIER.md` §1

Or le métier a validé le travail présenté, et ces ajouts doivent exister dans **les deux** versions. Ce dossier est l'adaptation de ce lot à l'architecture modulaire.

**Ils ne sont pas applicables tels quels.** Trois raisons, de la plus mécanique à la plus structurelle — détail en section 2.

---

## 1. Les 4 décisions de M.0, reportées ici

Actées le 2026-08-03. Elles s'appliquent **à l'identique** sur la version modulaire.

| # | Décision | Contenu acté |
|---|---|---|
| **A** | Affichage du rattrapage | **A2** — tout afficher, les non-payés pré-cochés. |
| **B** | Montants obsolètes à la validation ARH | **Variante « B2-resync »** — ni B1, ni B2, ni B3 tels quels. À la validation ARH, tout montant obsolète est **resynchronisé automatiquement** sur la grille ACTIVE (RG-04), mais de façon **non silencieuse** : l'ARH voit un récapitulatif clair des lignes mises à jour (ancien montant → nouveau montant) **avant** de confirmer. Motif : bloquer (B1) forcerait à refaire tout le workflow d'une grille déjà validée par la DRH juste pour resynchroniser — de la lourdeur sans gain ; B3 était écarté car silencieux. Cette variante **corrige ET montre**, sans faux choix « continuer avec un montant périmé » qui n'a aucun sens métier. **L'audit doit tracer le delta de resynchronisation.** |
| **C** | Persistance des filtres | **C1** — filtres portés par l'URL (query params), y compris la propagation de la query string jusqu'au `LienRetour` des **4** pages à navigation sortante. Non reporté. |
| **D** | Schéma Kafka | Schéma provisoire proposé par DOTTEL, **clairement marqué « à valider avec la comptabilité »**. Ne bloque pas MM.13. |

**Point RG-12 acté, à traiter en MM.11 (pas maintenant)** : la règle d'unicité mois/année a été écrite avant que le cas du rattrapage n'existe. Elle est donc à **faire évoluer, pas à contourner** : au démarrage de MM.11, distinguer un processus de rattrapage d'un processus normal, plutôt que supprimer l'unicité — qui reste utile contre un vrai doublon accidentel. À concevoir explicitement en MM.11.

---

## 2. Ce qui empêche l'application directe des guides M.*

### 2.1 Chemins de fichiers : tous obsolètes *(mécanique)*

Chaque guide M référence l'ancienne arborescence en couches techniques. Table de correspondance complète :

| Référencé dans M.* | Chemin réel en version modulaire |
|---|---|
| `service/ProcessusMensuelService.java` | `processus/service/ProcessusMensuelService.java` |
| `service/DocumentService.java` | `processus/service/DocumentService.java` |
| `service/SeparationTachesService.java` | `processus/service/SeparationTachesService.java` |
| `service/NotificationService.java`, `NotificationServiceStub.java` | `processus/service/` |
| `service/EvenementClotureService.java` | `processus/service/` |
| `service/SignatureService.java`, `SignatureServiceAutonome.java` | `processus/service/` |
| `service/GrilleTarifaireService.java` | **`referentiel/service/`** |
| `service/FonctionEligibleService.java` | **`referentiel/service/`** |
| `service/rules/EligibiliteService.java` | **`referentiel/api/`** *(décision A1 de MM.0)* |
| `service/BeneficiaireService.java`, `BeneficiaireImportService.java`, `BeneficiaireExportService.java`, `EnrolementService.java` | **`beneficiaires/service/`** |
| `service/EhrIntegrationService.java`, `EhrIntegrationServiceStub.java` | **`beneficiaires/service/`** |
| `service/AuthenticatedUserService.java` | **`utilisateurs/api/`** |
| `service/AuditService.java`, `AuditServiceImpl.java` | **`audit/api/`** et `audit/service/` |
| `service/ReportingService.java`, `HistoriqueExportService.java` | **`reporting/service/`** |
| `repository/BeneficiaireRepository.java`, `BeneficiaireSpecifications.java` | **`beneficiaires/repository/`** |
| `repository/ProcessusMensuelRepository.java`, `LigneEtatMensuelRepository.java`, `EtapeWorkflowRepository.java`, `PieceJointeRepository.java` | **`processus/repository/`** |
| `repository/FonctionEligibleRepository.java`, `GrilleTarifaireRepository.java` | **`referentiel/repository/`** |
| `repository/UtilisateurRepository.java` | **`utilisateurs/repository/`** |
| `model/entity/Beneficiaire.java` | **`beneficiaires/model/entity/`** |
| `model/entity/ProcessusMensuel.java`, `LigneEtatMensuel.java`, `EtapeWorkflow.java`, `PieceJointe.java` | **`processus/model/entity/`** |
| `model/dto/processus/EvenementClotureDto.java` | **`processus/api/`** |
| `controller/*` | `<module>/controller/` |
| `security/GlobalExceptionHandler.java`, `JwtUtil.java`, `SecurityConfig.java` | `security/` *(inchangé, module `OPEN`)* |
| `config/KafkaConfig.java`, `EvenementClotureSerializer.java` | `config/` *(inchangé, module `OPEN`)* |
| `exception/*` (32 classes à plat) | **réparties** dans `<module>/exception/` |

Trois écarts supplémentaires dans les en-têtes des guides M :
- **Rappel Graphify** : `py -3.14 -m graphify update .` ne fonctionne pas sur ce poste. Utiliser `/graphify . --update`.
- **Variables d'environnement** : les guides M en citent 2. Il en faut **3** sur la copie modulaire (voir section 4).
- **Aucune vérification d'espace de travail** : absente des guides M, indispensable ici.

### 2.2 ⚠️ M.4 créerait un **second cycle de modules** *(structurel — le point le plus sérieux)*

`M.4_workflow_grilles_et_montants.md` étape 2, question 2, demande : *« la séparation des tâches (RG-08) doit-elle s'appliquer au workflow des grilles ? »*.

Dans l'ancienne architecture, c'était une pure question métier. **En version modulaire, répondre « oui » a une conséquence structurelle immédiate** :

| Élément | Module |
|---|---|
| `GrilleTarifaireService` (workflow des grilles) | `referentiel` |
| `SeparationTachesService` (RG-08) | **`processus`** |
| Entité `EtapeWorkflow` (trace des étapes) | **`processus`** |
| Dépendance existante | `processus` → `referentiel :: api` *(résolution de grille)* |

Appliquer RG-08 aux grilles ferait dépendre `referentiel` de `processus` → **deuxième cycle de modules**, en plus de celui déjà épinglé (`beneficiaires` ↔ `referentiel`). `ModularityTests` échouerait, et son filtre actuel ne couvre **que** le cycle connu — c'est précisément sa raison d'être.

Traité explicitement en **MM.12**, avec trois options présentées et aucune tranchée.

### 2.3 ⚠️ M.5 exige d'étendre une API inter-modules *(absent de M.5)*

M.5 veut enrichir l'événement Kafka avec, **par bénéficiaire** : code unité, code agence, numéro de compte courant, chapitre, nom et prénom, fonction retenue, montant attribué.

Or `EvenementClotureService` vit dans `processus` et **ne peut plus lire `BeneficiaireRepository`** — supprimé de ses dépendances en MM.2. Il devra passer par `BeneficiaireApi`, donc **étendre cette API** (nouvelle méthode ou extension de `BeneficiaireDocumentDto`). Travail inter-modules que M.5 ne mentionne pas.

**En sens inverse, la version modulaire est mieux préparée** pour le volet notifications du même sprint : `UtilisateurApi.destinatairesParRole(RoleEnum)` retournant `DestinataireNotificationDto(email, role)` **existe déjà** depuis MM.2, exactement ce dont M.5 a besoin. Aucune plomberie à créer.

### 2.4 Ce qui se réplique sans friction

| Élément | Pourquoi c'est simple |
|---|---|
| **M.1 déjà validé** | Ses 3 fichiers backend (`BeneficiaireSpecifications`, `BeneficiaireService`, `BeneficiaireController`) sont **tous dans le module `beneficiaires`** — aucun franchissement de frontière. Le frontend n'a jamais été touché par le chantier MM : réplication à l'identique. |
| **Plan d'anomalie signature CRH** | Déjà rédigé avec les chemins **modulaires** (`processus/service/DocumentService.java`) — directement exploitable. `DocumentService`, `SeparationTachesService` et `ProcessusMensuelService` sont tous les trois dans `processus` : intra-module. |
| **M.3 (période + rattrapage)** | `declencher()`, `LigneEtatMensuel`, `ProcessusMensuel` : tout dans `processus`. La jointure « qui n'a pas été payé au mois M » est intra-module. |
| **M.4 volet montants** | La comparaison montant stocké ↔ `GrilleTarifaireApi.resoudrePourFonction()` utilise une dépendance `processus → referentiel :: api` **déjà déclarée**. Aucun nouveau couplage. |

---

## 3. Les 6 sous-sprints

| Sprint | Contenu | Origine | Risque |
|---|---|---|---|
| **MM.8** | Anomalies : signature CRH invalidée au retour DRH, RG-08 comparant contre une ligne obsolète, clôture formelle du cycle `beneficiaires` ↔ `referentiel` | Plan d'anomalie + décision G-2 | Moyen |
| **MM.9** | Réplication de M.1 (ergonomie modal + persistance des filtres), déjà validé sur la version d'origine | `M.1` + `SPRINT_M1_REALISE.md` | **Faible** |
| **MM.10** | Référentiel unité/agence + formats numériques réels (25 codes agence, codes unité à 4 chiffres) | `M.2` | Moyen |
| **MM.11** | Période de déclenchement et rattrapage d'un mois passé, évolution encadrée de RG-12 | `M.3` | Moyen à élevé |
| **MM.12** | Workflow des grilles à 3 acteurs + resynchronisation des montants (variante B2-resync) | `M.4` | **Élevé** |
| **MM.13** | Notifications Outlook provisoires + événement Kafka enrichi par bénéficiaire | `M.5` | Moyen |

### Ordre d'exécution

**MM.8 d'abord** — ce sont des corrections d'anomalies existantes ; les faire avant d'empiler des fonctionnalités évite de déboguer deux choses à la fois. Il clôt aussi formellement la question du cycle laissée ouverte par MM.6.

**Puis MM.9** — déjà validé métier sur l'autre version, donc le moins risqué : bon sprint pour roder la méthode sur la copie modulaire.

**Ensuite MM.10 → MM.11 → MM.12 → MM.13.** MM.12 et MM.13 en dernier : ils touchent des règles métier (RG-08, RG-10, RG-04) et des intégrations externes.

**Dépendance à noter** : MM.13 (Kafka enrichi) a besoin de `code_agence`, créé en **MM.10**. Ne pas inverser ces deux-là.

---

## 4. Règles non négociables, valables pour les 6 sprints

### 4.1 Vérification d'espace de travail, au début de chaque session

```powershell
(Get-Location).Path      # doit se terminer par afb-dottel-mm
git log --oneline -1     # ne doit PAS afficher d9be38c
git remote -v            # doit être vide
```

### 4.2 Les trois variables d'environnement

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/afb_dotations_telephoniques_mm"
$env:DB_PASSWORD="admin"
$env:DOTTEL_JWT_SECRET="dottel-dev-secret-key-2026-afriland-first-bank-32chars"
```

`DB_URL` est **nouvelle par rapport aux guides M.*** (qui n'en citent que 2). L'oublier fait retomber silencieusement sur la base du projet d'origine.

### 4.3 Suite de tests au vert avant tout commit

```powershell
cd backend ; .\mvnw.cmd test
```

**Référence de non-régression : 226 tests, 0 échec, 0 erreur** (mesurée à la clôture de MM.6 — et non 205, chiffre des guides M.* écrits avant le chantier).

Le nombre de tests ne doit jamais **diminuer**. S'il augmente, tant mieux.

### 4.4 `ModularityTests` doit rester vert

Chaque sprint ajoutant du code inter-modules doit relancer `ModularityTests`. Il est vert avec **une seule exception filtrée** (cycle `beneficiaires` ↔ `referentiel`). Toute violation **nouvelle ou différente** le fait échouer — c'est voulu.

Si un sprint a besoin d'une nouvelle dépendance inter-modules, elle doit être **déclarée** dans le `package-info.java` du module concerné (`allowedDependencies`), avec une justification écrite — jamais contournée.

### 4.5 Aucune décision métier tranchée seul

Tout écart de règle métier découvert en cours de route est **signalé**, pas tranché. Les guides ci-dessous marquent explicitement les points d'arrêt.

---

## 5. Ce que ces sprints ne changent pas

- **Le découpage en 6 modules** acté en MM.0 (décisions A, B, C). Aucun module créé ni supprimé.
- **Le frontend**, sauf pour les volets qui le concernent explicitement (MM.9, MM.10 étape 3, MM.11 étape 3, MM.12 étapes 4 et 6).
- **Le passage en microservices** — hors périmètre (`CLAUDE.md` sections 14 et 18).
- **MM.7 (Keycloak provisoire)** — sprint distinct, déjà documenté dans `docs/monolithe-modulaire/MM.7_keycloak_provisoire.md`, sans interaction avec ce lot.

---

## 6. Points en attente externes

| Point | Nature |
|---|---|
| **EHR réel** | Non branché (attente DSI). MM.10 s'appuie sur `EhrIntegrationServiceStub`. Les formats numériques y sont **provisoires** jusqu'à l'intégration réelle. |
| **Canal Outlook** | Serveur SMTP ou API Graph, identifiants, adresse d'expédition : inconnus. MM.13 développe en configuration externalisée, mode local désactivable. |
| **Schéma Kafka** | Le contrat exact attendu par la comptabilité n'est pas confirmé. Décision D : schéma provisoire proposé par DOTTEL, marqué comme tel. |
| **Codes unité (4 chiffres)** | Seul `DSI = 4060` est confirmé. La liste complète des codes unité n'est pas fournie — MM.10 utilisera des valeurs camerounaises réalistes marquées provisoires, à remplacer le jour de l'intégration EHR. |
| **Codes agence (5 chiffres)** | **25 codes confirmés et fournis**, intégrés en MM.10. Le métier signale qu'il en existe d'autres au-delà de ces 25. |

---

**Fin du plan** — *MM.8 peut démarrer immédiatement*
