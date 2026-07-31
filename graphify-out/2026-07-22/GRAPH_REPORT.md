# Graph Report - afb-dottel  (2026-07-22)

## Corpus Check
- 132 files · ~82,638 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 855 nodes · 1969 edges · 41 communities (30 shown, 11 thin omitted)
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 271 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `1554cd19`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Utilisateur
- .confirmer
- .importer
- GlobalExceptionHandler.java
- dependencies
- CLAUDE.md — Projet Dotations Téléphoniques Mensuelles
- AppRouter.jsx
- GrilleTarifaire
- AuditLog
- graphify skill
- RoleJwtAuthenticationConverter
- EnrolementController.java
- application.yml
- ProcessusMensuel
- mvnw
- CorsConfig.java
- icons.svg (Icon Sprite Sheet)
- formatters.js
- DottelApplicationTests.java
- constants.js
- DottelApplication
- React Logo (Vite default asset)
- Frontend (React + Vite App)
- axiosConfig.js
- Vite Build Tool
- Note de Service NS 69/17 - Dotation Téléphonique Portable (scan)
- graphify benchmark (token reduction)
- Hero Banner Image
- com.afriland:dottel
- afb-dotations-telephoniques (README)
- ProcessusMensuelController.java
- graphify reference: extra exports and benchmark
- graphify reference: add a URL and watch a folder
- graphify reference: commit hook and native CLAUDE.md integration
- graphify reference: incremental update and cluster-only
- graphify reference: GitHub clone and cross-repo merge
- graphify reference: transcribe video and audio
- ProcessusMensuel
- EtapeWorkflow
- GrilleTarifaire

## God Nodes (most connected - your core abstractions)
1. `Utilisateur` - 55 edges
2. `ProcessusMensuel` - 39 edges
3. `LigneEtatMensuel` - 33 edges
4. `ProcessusMensuelServiceTest` - 32 edges
5. `FonctionEligible` - 28 edges
6. `FonctionEligibleRepository` - 24 edges
7. `ProcessusMensuelService` - 24 edges
8. `Beneficiaire` - 22 edges
9. `BeneficiaireRepository` - 22 edges
10. `EnrolementServiceTest` - 21 edges

## Surprising Connections (you probably didn't know these)
- `Secret dottel-secret (referenced)` --conceptually_related_to--> `DOTTEL_JWT_SECRET / JWT_SECRET env var`  [INFERRED]
  k8s/deployment.yaml → backend/src/main/resources/application-dev.yml
- `Secret dottel-secret (referenced)` --conceptually_related_to--> `DB_URL/DB_USER/DB_PASSWORD env vars`  [INFERRED]
  k8s/deployment.yaml → backend/src/main/resources/application-dev.yml
- `CORS allowed-origins localhost:3000` --conceptually_related_to--> `index.html (frontend)`  [INFERRED]
  backend/src/main/resources/application-dev.yml → frontend/index.html
- `Actuator health/info exposure config` --shares_data_with--> `/api/actuator/health liveness/readiness probe`  [INFERRED]
  backend/src/main/resources/application.yml → k8s/deployment.yaml
- `CLAUDE.md — Projet Dotations Téléphoniques Mensuelles` --conceptually_related_to--> `Contrats API Dotations V3`  [INFERRED]
  CLAUDE.md → docs/reference/contrats_api_dotations_v3.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Graph export targets (wiki, neo4j, falkordb, mcp, benchmark)** — claude_skills_graphify_references_exports_wiki, claude_skills_graphify_references_exports_neo4j, claude_skills_graphify_references_exports_falkordb, claude_skills_graphify_references_exports_mcp, claude_skills_graphify_references_exports_benchmark [EXTRACTED 1.00]
- **Graph query/traversal family (query, path, explain)** — claude_skills_graphify_references_query_vocab_expansion, claude_skills_graphify_references_query_bfs_dfs, claude_skills_graphify_references_query_path_explain, claude_skills_graphify_references_query_save_result_reflect [INFERRED 0.85]
- **Kubernetes deployment pipeline for dottel-backend** — k8s_configmap_yaml_dottel_config, k8s_deployment_yaml_dottel_backend, k8s_service_yaml_dottel_backend_svc [EXTRACTED 1.00]
- **Secret/env var externalization from application.yml to K8s Secret/ConfigMap** — backend_dottel_jwt_secret_env, backend_db_connection_envs, k8s_deployment_yaml_dottel_secret, k8s_configmap_yaml_dottel_config [INFERRED 0.85]
- **RG-05/RG-06/RG-07/RG-08 govern the ARH-CRH-DRH validation workflow** — claude_md_rg05, claude_md_rg06, claude_md_rg07, claude_md_rg08, docs_reference_contrats_api_dotations_v3_post_processus_valider, docs_reference_contrats_api_dotations_v3_post_processus_retourner [INFERRED 0.85]
- **grille_tarifaire table, StatutGrilleEnum, RG-04, RG-10 and the grilles-tarifaires API group form the tariff-grid validation lifecycle** — docs_reference_dictionnaire_de_donnees_dotations_v3_grille_tarifaire, docs_reference_dictionnaire_de_donnees_dotations_v3_statutgrilleenum, claude_md_rg04, claude_md_rg10, docs_reference_contrats_api_dotations_v3_grilles_tarifaires_group [INFERRED 0.85]
- **fonction_eligible, beneficiaires, RG-01 and RG-02 form the enrollment eligibility pipeline** — docs_reference_dictionnaire_de_donnees_dotations_v3_fonction_eligible, docs_reference_dictionnaire_de_donnees_dotations_v3_beneficiaires, claude_md_rg01, claude_md_rg02, docs_reference_contrats_api_dotations_v3_get_enrolement_verifier [INFERRED 0.85]

## Communities (41 total, 11 thin omitted)

### Community 0 - "Utilisateur"
Cohesion: 0.08
Nodes (29): AuthController, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, Getter, Setter (+21 more)

### Community 1 - ".confirmer"
Cohesion: 0.09
Nodes (31): EmployeEhrDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, ConfirmerEnrolementRequestDto, Getter, Setter (+23 more)

### Community 2 - ".importer"
Cohesion: 0.09
Nodes (31): BeneficiaireController, MultipartFile, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+23 more)

### Community 3 - "GlobalExceptionHandler.java"
Cohesion: 0.06
Nodes (21): FichierImportInvalideException, GrilleTarifaireIntrouvableException, IdentifiantsInvalidesException, MatriculeDejaEnroleException, MatriculeInconnuException, NonEligibleException, ProcessusMensuelExisteDejaException, ProcessusMensuelIntrouvableException (+13 more)

### Community 4 - "dependencies"
Cohesion: 0.04
Nodes (45): axios, date-fns, dependencies, axios, date-fns, @hookform/resolvers, jwt-decode, react (+37 more)

### Community 5 - "CLAUDE.md — Projet Dotations Téléphoniques Mensuelles"
Cohesion: 0.09
Nodes (44): CLAUDE.md — Projet Dotations Téléphoniques Mensuelles, Module DOTTEL (Dotations Téléphoniques Mensuelles), RG-01: Eligibilite via fonction_eligible.actif, RG-02: Grade NON GRADE bloque corps de controle, RG-03: Unicite matricule beneficiaire, RG-04: Montant automatique via grille_tarifaire, RG-05: Sequence stricte ARH->CRH->DRH, RG-06: Un seul PDF par processus (+36 more)

### Community 6 - "AppRouter.jsx"
Cohesion: 0.07
Nodes (22): plugins, rules, react/only-export-components, react/rules-of-hooks, $schema, App(), AuthContext, useAuth() (+14 more)

### Community 7 - "GrilleTarifaire"
Cohesion: 0.07
Nodes (33): AjustementLigneEtatDto, Getter, Setter, DeclencherProcessusRequestDto, Getter, Setter, Getter, Setter (+25 more)

### Community 8 - "AuditLog"
Cohesion: 0.13
Nodes (20): AfterEach, AuditLog, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter (+12 more)

### Community 9 - "graphify skill"
Cohesion: 0.08
Nodes (29): Project .claude/CLAUDE.md (graphify trigger), /graphify add <url>, --watch folder watcher, graphify export falkordb / falkordb-push, MCP stdio server (graphify.serve), graphify export neo4j / neo4j-push, graphify export wiki, Confidence score rubric (0.55-0.95 discrete steps) (+21 more)

### Community 10 - "RoleJwtAuthenticationConverter"
Cohesion: 0.15
Nodes (16): Component, Override, RoleJwtAuthenticationConverter, Configuration, PasswordEncoder, SecurityConfig, Test, RoleJwtAuthenticationConverterTest (+8 more)

### Community 11 - "EnrolementController.java"
Cohesion: 0.16
Nodes (18): EnrolementController, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, ConfirmerEnrolementResponseDto (+10 more)

### Community 12 - "application.yml"
Cohesion: 0.13
Nodes (19): Actuator health/info exposure config, afb_dotations_telephoniques database, CORS allowed-origins localhost:3000, DB_URL/DB_USER/DB_PASSWORD env vars, DOTTEL_JWT_SECRET / JWT_SECRET env var, Flyway migration config (classpath:db/migration), Swagger UI config (/swagger-ui.html), CLAUDE.md section 14 (déploiement/infrastructure) (+11 more)

### Community 13 - "ProcessusMensuel"
Cohesion: 0.07
Nodes (45): UtilisateurConnecteIntrouvableException, Beneficiaire, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter (+37 more)

### Community 14 - "mvnw"
Cohesion: 0.33
Nodes (6): mvnw script, clean(), die(), exec_maven(), set_java_home(), verbose()

### Community 15 - "CorsConfig.java"
Cohesion: 0.43
Nodes (5): CorsConfig, Configuration, Override, CorsRegistry, WebMvcConfigurer

### Community 16 - "icons.svg (Icon Sprite Sheet)"
Cohesion: 0.29
Nodes (7): icons.svg (Icon Sprite Sheet), Bluesky Icon Symbol, Discord Icon Symbol, Documentation Icon Symbol, GitHub Icon Symbol, Social/Users Icon Symbol, X (Twitter) Icon Symbol

### Community 17 - "formatters.js"
Cohesion: 0.40
Nodes (3): formatDate(), formatDateHeure(), MOIS_LABELS

### Community 18 - "DottelApplicationTests.java"
Cohesion: 0.60
Nodes (3): DottelApplicationTests, Test, SpringBootTest

### Community 19 - "constants.js"
Cohesion: 0.40
Nodes (4): ROLES, STATUTS, STATUTS_GRILLE, STATUTS_LABELS

### Community 21 - "React Logo (Vite default asset)"
Cohesion: 0.67
Nodes (3): React Framework, React Logo (Vite default asset), Vite/React Starter Template Bootstrap

### Community 31 - "ProcessusMensuelController.java"
Cohesion: 0.06
Nodes (47): PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, ProcessusMensuelController, BeneficiaireExcluDto (+39 more)

### Community 32 - "graphify reference: extra exports and benchmark"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 33 - "graphify reference: add a URL and watch a folder"
Cohesion: 0.50
Nodes (3): For /graphify add, For --watch, graphify reference: add a URL and watch a folder

### Community 34 - "graphify reference: commit hook and native CLAUDE.md integration"
Cohesion: 0.50
Nodes (3): For git commit hook, For native CLAUDE.md integration, graphify reference: commit hook and native CLAUDE.md integration

### Community 35 - "graphify reference: incremental update and cluster-only"
Cohesion: 0.50
Nodes (3): For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only

### Community 38 - "ProcessusMensuel"
Cohesion: 0.11
Nodes (25): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, LigneEtatMensuel (+17 more)

### Community 39 - "EtapeWorkflow"
Cohesion: 0.15
Nodes (16): EtapeWorkflow, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+8 more)

### Community 40 - "GrilleTarifaire"
Cohesion: 0.21
Nodes (14): GrilleTarifaire, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+6 more)

## Knowledge Gaps
- **102 isolated node(s):** `com.afriland:dottel`, `VALIDATION_ARH`, `VALIDATION_CRH`, `VALIDATION_DRH`, `EMPLOYE` (+97 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **11 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Utilisateur` connect `GrilleTarifaire` to `Utilisateur`, `.confirmer`, `.importer`, `ProcessusMensuel`, `ProcessusMensuel`?**
  _High betweenness centrality (0.116) - this node is a cross-community bridge._
- **Why does `FonctionEligibleRepository` connect `ProcessusMensuel` to `.confirmer`, `.importer`?**
  _High betweenness centrality (0.036) - this node is a cross-community bridge._
- **Why does `UtilisateurRepository` connect `ProcessusMensuel` to `Utilisateur`, `GrilleTarifaire`?**
  _High betweenness centrality (0.034) - this node is a cross-community bridge._
- **What connects `com.afriland:dottel`, `VALIDATION_ARH`, `VALIDATION_CRH` to the rest of the system?**
  _102 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Utilisateur` be split into smaller, more focused modules?**
  _Cohesion score 0.08470588235294117 - nodes in this community are weakly interconnected._
- **Should `.confirmer` be split into smaller, more focused modules?**
  _Cohesion score 0.09176587301587301 - nodes in this community are weakly interconnected._
- **Should `.importer` be split into smaller, more focused modules?**
  _Cohesion score 0.08862745098039215 - nodes in this community are weakly interconnected._