# Graph Report - .  (2026-07-24)

## Corpus Check
- 218 files · ~99,512 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1257 nodes · 3033 edges · 80 communities (50 shown, 30 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 360 edges (avg confidence: 0.8)
- Token cost: 204,625 input · 0 output

## Community Hubs (Navigation)
- Beneficiaireservice Services
- Processusmensuelservicetest Tests
- Beneficiaireservicetest Tests
- Beneficiaire DTOs
- Ligneetatmensuel Tests
- Processus DTOs
- Authservicetest Tests
- Etapeworkflow Tests
- Frontend Package
- Auditserviceimpl Tests
- Claude Docs
- Kafkaconfig DTOs
- Claude Docs
- Frontend Frontend
- Securityconfig Tests
- Telephoniques PDF Documents
- Utilisateur Services
- Sprint Docs
- Enrolement Controllers
- Utilisateuradminservicetest Tests
- Utilisateuradminservice Services
- Grilletarifaireservicetest Tests
- Globalexceptionhandlertest Exceptions
- Yaml Config
- Ehrintegrationservicestub Services
- Grilletarifairecontroller Controllers
- Utilisateuradmincontroller Controllers
- Utilisateur DTOs
- Exception Exceptions
- Grille DTOs
- Grille Services
- Grille DTOs
- Exception Exceptions
- Authenticateduserservice Exceptions
- Exception Exceptions
- Grille DTOs
- Grilletarifaire Entities
- Corsconfig Addcorsmappings
- Grille DTOs
- Grille DTOs
- Utilisateur DTOs
- Utilisateur DTOs
- Utilisateur DTOs
- Utilisateur DTOs
- Svg Public
- Oxlintrc Frontend
- Utils Frontend
- Exception Exceptions
- Dottelapplicationtests Contextloads
- Utils Frontend
- Dottelapplication Springbootapplication
- Exception Exceptions
- Exception Exceptions
- Exception Exceptions
- Exception Exceptions
- Exception Exceptions
- Exception Exceptions
- Exception Exceptions
- Exception Exceptions
- Exception Exceptions
- Exception Exceptions
- Exception Exceptions
- Exception Exceptions
- Exception Exceptions
- Exception Exceptions
- First Images
- Frontend Frontend
- Frontend Svg
- Axiosconfig Frontend
- Vite Frontend
- Help Docs
- Pid
- Pid2
- Pid3
- Pid4
- Exports Docs
- Assets Images
- Pkg
- Telephoniques Docs

## God Nodes (most connected - your core abstractions)
1. `ProcessusMensuelServiceTest` - 50 edges
2. `Utilisateur` - 49 edges
3. `GlobalExceptionHandler` - 33 edges
4. `FonctionEligibleRepository` - 32 edges
5. `BeneficiaireRepository` - 31 edges
6. `ProcessusMensuelService` - 31 edges
7. `ProcessusMensuel` - 28 edges
8. `BeneficiaireServiceTest` - 26 edges
9. `Beneficiaire` - 24 edges
10. `GrilleTarifaireServiceTest` - 24 edges

## Surprising Connections (you probably didn't know these)
- `Grille des 22 forfaits mensuels de dotation téléphonique (NS 69/17)` --conceptually_related_to--> `CLAUDE.md Section 5 - 25 fonctions éligibles (Note NS 69/17 + 3 corps assimilés)`  [INFERRED]
  backend/src/main/resources/images/Média.jpg → CLAUDE.md
- `CLAUDE.md v2 (draft, 24 fonctions, RG-01..RG-11)` --semantically_similar_to--> `CLAUDE.md v3 (draft, 24 fonctions, RG-01..RG-11)`  [INFERRED] [semantically similar]
  CLAUDE v2.md → CLAUDE v3.md
- `PDF généré — Etat récapitulatif Novembre 2025 (ARH, CRH, DRH tous signés)` --conceptually_related_to--> `POST /processus/{id}/valider — branche ARH implémentée, CRH/DRH et RG-08 en attente`  [INFERRED]
  backend/documents/dotations-telephoniques-11-2025.pdf → docs/reference/contrats_api_dotations_v3.md
- `Jeu de règles métier RG-01 à RG-11 (v2)` --semantically_similar_to--> `RG-12 unicité processus mensuel (distincte de RG-03, corrige ambiguïté V3.0)`  [INFERRED] [semantically similar]
  CLAUDE v2.md → docs/reference/contrats_api_dotations_v3.md
- `Jeu de règles métier RG-01 à RG-11 (v2)` --semantically_similar_to--> `Jeu de règles métier RG-01 à RG-11 (v3)`  [INFERRED] [semantically similar]
  CLAUDE v2.md → CLAUDE v3.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Graph export targets (wiki, neo4j, falkordb, mcp, benchmark)** — claude_skills_graphify_references_exports_wiki, claude_skills_graphify_references_exports_neo4j, claude_skills_graphify_references_exports_falkordb, claude_skills_graphify_references_exports_mcp, claude_skills_graphify_references_exports_benchmark [EXTRACTED 1.00]
- **Graph query/traversal family (query, path, explain)** — claude_skills_graphify_references_query_vocab_expansion, claude_skills_graphify_references_query_bfs_dfs, claude_skills_graphify_references_query_path_explain, claude_skills_graphify_references_query_save_result_reflect [INFERRED 0.85]
- **RG-05/RG-06/RG-07/RG-08 govern the ARH-CRH-DRH validation workflow** — claude_md_rg05, claude_md_rg06, claude_md_rg07, claude_md_rg08, contrats_api_dotations_v3_post_processus_valider, contrats_api_dotations_v3_post_processus_retourner [INFERRED 0.85]
- **fonction_eligible, beneficiaires, RG-01 and RG-02 form the enrollment eligibility pipeline** — docs_reference_dictionnaire_de_donnees_dotations_v3_fonction_eligible, docs_reference_dictionnaire_de_donnees_dotations_v3_beneficiaires, claude_md_rg01, claude_md_rg02, contrats_api_dotations_v3_get_enrolement_verifier [INFERRED 0.85]
- **grille_tarifaire table, StatutGrilleEnum, RG-04, RG-10 and the grilles-tarifaires API group form the tariff-grid validation lifecycle** — docs_reference_dictionnaire_de_donnees_dotations_v3_grille_tarifaire, docs_reference_dictionnaire_de_donnees_dotations_v3_statutgrilleenum, claude_md_rg04, claude_md_rg10, contrats_api_dotations_v3_grilles_tarifaires_group [INFERRED 0.85]
- **Kubernetes deployment pipeline for dottel-backend** — k8s_configmap_yaml_dottel_config, k8s_deployment_yaml_dottel_backend, k8s_service_yaml_dottel_backend_svc [EXTRACTED 1.00]
- **Secret/env var externalization from application.yml to K8s Secret/ConfigMap** — backend_dottel_jwt_secret_env, backend_db_connection_envs, k8s_deployment_yaml_dottel_secret, k8s_configmap_yaml_dottel_config [INFERRED 0.85]
- **RG-08 séparation des tâches : écart entre contrat API et périmètre Sprint 3.4** — sprint_3_4, docs_reference_contrats_api_dotations_v3, sprint_3_4_rg08_separation_taches [EXTRACTED 0.90]
- **Flux de génération du PDF initial (DocumentService + PieceJointe + SignatureService + CHAPITRE)** — sprint_3_4_documentservice, sprint_3_4_piecejointe, sprint_3_4_signatureservice, sprint_3_4_chapitre_field [EXTRACTED 0.90]
- **Progression des signatures ARH->CRH->DRH observée entre les deux PDF générés** — backend_documents_dotations_telephoniques_1_2026, backend_documents_dotations_telephoniques_11_2025, sprint_3_4_etapeworkflow [INFERRED 0.80]
- **États mensuels de dotation téléphonique générés par DocumentService** — backend_documents_dotations_telephoniques_11_2026, backend_documents_dotations_telephoniques_12_2025, backend_documents_dotations_telephoniques_12_2026, backend_documents_dotations_telephoniques_2_2026, backend_documents_dotations_telephoniques_3_2026, backend_documents_dotations_telephoniques_3_2027, backend_documents_dotations_telephoniques_4_2026, backend_documents_dotations_telephoniques_4_2027, backend_documents_dotations_telephoniques_5_2026, backend_documents_dotations_telephoniques_6_2026, backend_documents_dotations_telephoniques_8_2026 [EXTRACTED 1.00]
- **Workflow séquentiel de validation ARH puis CRH puis DRH (RG-05)** — actor_jean_paul_mbarga, actor_marie_claire_essama, actor_paul_atangana, backend_documents_dotations_telephoniques_12_2026 [EXTRACTED 1.00]
- **Grille tarifaire des forfaits téléphoniques fondée sur la Note de Service NS 69/17** — backend_src_main_resources_images_media_ns_69_17, fonction_gestionnaire_fonds_commerce, fonction_directeur_agence, fonction_conseiller_charge_mission [EXTRACTED 1.00]

## Communities (80 total, 30 thin omitted)

### Community 0 - "Beneficiaireservice Services"
Cohesion: 0.06
Nodes (50): NonEligibleException, Beneficiaire, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter (+42 more)

### Community 1 - "Processusmensuelservicetest Tests"
Cohesion: 0.08
Nodes (25): AjustementLigneEtatDto, Getter, Setter, DeclencherProcessusRequestDto, Getter, Setter, Getter, Setter (+17 more)

### Community 2 - "Beneficiaireservicetest Tests"
Cohesion: 0.08
Nodes (20): ConfirmerEnrolementRequestDto, Getter, Setter, Transactional, BeneficiaireImportServiceTest, ExtendWith, Row, Test (+12 more)

### Community 3 - "Beneficiaire DTOs"
Cohesion: 0.05
Nodes (54): mvnw script, clean(), die(), exec_maven(), set_java_home(), trim(), verbose(), BeneficiaireController (+46 more)

### Community 4 - "Ligneetatmensuel Tests"
Cohesion: 0.07
Nodes (41): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, LigneEtatMensuel (+33 more)

### Community 5 - "Processus DTOs"
Cohesion: 0.06
Nodes (53): PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, ProcessusMensuelController (+45 more)

### Community 6 - "Authservicetest Tests"
Cohesion: 0.08
Nodes (29): AuthController, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, Getter, Setter (+21 more)

### Community 7 - "Etapeworkflow Tests"
Cohesion: 0.10
Nodes (24): EtapeWorkflowIntrouvableException, EtapeWorkflow, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter (+16 more)

### Community 8 - "Frontend Package"
Cohesion: 0.06
Nodes (31): dependencies, axios, date-fns, @hookform/resolvers, jwt-decode, react, react-dom, react-hook-form (+23 more)

### Community 9 - "Auditserviceimpl Tests"
Cohesion: 0.12
Nodes (20): AfterEach, AuditLog, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter (+12 more)

### Community 10 - "Claude Docs"
Cohesion: 0.08
Nodes (29): Project .claude/CLAUDE.md (graphify trigger), /graphify add <url>, --watch folder watcher, graphify export falkordb / falkordb-push, MCP stdio server (graphify.serve), graphify export neo4j / neo4j-push, graphify export wiki, Confidence score rubric (0.55-0.95 discrete steps) (+21 more)

### Community 11 - "Kafkaconfig DTOs"
Cohesion: 0.13
Nodes (20): EvenementClotureSerializer, ObjectMapper, Override, Bean, Configuration, KafkaTemplate, ObjectMapper, KafkaConfig (+12 more)

### Community 12 - "Claude Docs"
Cohesion: 0.12
Nodes (28): CLAUDE.md — Projet Dotations Téléphoniques Mensuelles, Module DOTTEL (Dotations Téléphoniques Mensuelles), RG-01: Eligibilite via fonction_eligible.actif, RG-02: Grade NON GRADE bloque corps de controle, RG-03: Unicite matricule beneficiaire, RG-04: Montant automatique via grille_tarifaire, RG-05: Sequence stricte ARH->CRH->DRH, RG-06: Un seul PDF par processus (+20 more)

### Community 13 - "Frontend Frontend"
Cohesion: 0.11
Nodes (13): AuthContext, useAuth(), Admin(), AccesInterdit(), Login(), Beneficiaires(), Dashboard(), GrillesTarifaires() (+5 more)

### Community 14 - "Securityconfig Tests"
Cohesion: 0.14
Nodes (17): Component, Override, RoleJwtAuthenticationConverter, Bean, Configuration, PasswordEncoder, SecurityConfig, Test (+9 more)

### Community 15 - "Telephoniques PDF Documents"
Cohesion: 0.14
Nodes (26): Jean Paul MBARGA (ARH, matricule 1847), Marie Claire ESSAMA (CRH, matricule 2093), Paul ATANGANA (DRH, matricule 1562), Sylvie NKOLO (ARH, matricule 2201), État Récapitulatif Dotations Téléphoniques - Novembre 2026, État Récapitulatif Dotations Téléphoniques - Décembre 2025, État Récapitulatif Dotations Téléphoniques - Décembre 2026, État Récapitulatif Dotations Téléphoniques - Février 2026 (+18 more)

### Community 16 - "Utilisateur Services"
Cohesion: 0.13
Nodes (17): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, Utilisateur (+9 more)

### Community 17 - "Sprint Docs"
Cohesion: 0.12
Nodes (23): PDF généré — Etat récapitulatif Novembre 2025 (ARH, CRH, DRH tous signés), PDF généré — Etat récapitulatif Janvier 2026 (ARH seul signé), application.yml (base Spring config: JPA, Flyway, Kafka, JWT, dottel.* properties), application-prod.yml (prod datasource/kafka/logging config), Propriétés dottel.documents.chemin-stockage et dottel.documents.chapitre-defaut, CLAUDE.md v2 (draft, 24 fonctions, RG-01..RG-11), Jeu de règles métier RG-01 à RG-11 (v2), CLAUDE.md v3 (draft, 24 fonctions, RG-01..RG-11) (+15 more)

### Community 18 - "Enrolement Controllers"
Cohesion: 0.16
Nodes (18): EnrolementController, GetMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+10 more)

### Community 19 - "Utilisateuradminservicetest Tests"
Cohesion: 0.20
Nodes (5): BeforeEach, ExtendWith, PasswordEncoder, Test, UtilisateurAdminServiceTest

### Community 20 - "Utilisateuradminservice Services"
Cohesion: 0.18
Nodes (7): ActionAdminNonAutoriseeException, UtilisateurIntrouvableException, PasswordEncoder, RequiredArgsConstructor, Service, Transactional, UtilisateurAdminService

### Community 22 - "Globalexceptionhandlertest Exceptions"
Cohesion: 0.17
Nodes (8): AccessDeniedException, IdentifiantsInvalidesException, MethodArgumentNotValidException, GlobalExceptionHandlerTest, ExtendWith, MethodArgumentNotValidException, Test, BindingResult

### Community 23 - "Yaml Config"
Cohesion: 0.15
Nodes (16): afb_dotations_telephoniques database, CORS allowed-origins localhost:3000, DB_URL/DB_USER/DB_PASSWORD env vars, DOTTEL_JWT_SECRET / JWT_SECRET env var, CLAUDE.md section 14 (déploiement/infrastructure), index.html (frontend), Frontend README.md, k8s configmap.yaml (dottel-config) (+8 more)

### Community 24 - "Ehrintegrationservicestub Services"
Cohesion: 0.20
Nodes (10): EmployeEhrDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, EhrIntegrationService, EhrIntegrationServiceStub, Override (+2 more)

### Community 25 - "Grilletarifairecontroller Controllers"
Cohesion: 0.31
Nodes (9): GrilleTarifaireController, GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity (+1 more)

### Community 26 - "Utilisateuradmincontroller Controllers"
Cohesion: 0.31
Nodes (9): GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+1 more)

### Community 27 - "Utilisateur DTOs"
Cohesion: 0.23
Nodes (12): CreerUtilisateurRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, RoleEnum, ADMIN (+4 more)

### Community 28 - "Exception Exceptions"
Cohesion: 0.23
Nodes (5): GrilleNonModifiableException, PieceJointeIntrouvableException, GlobalExceptionHandler, Logger, RestControllerAdvice

### Community 29 - "Grille DTOs"
Cohesion: 0.26
Nodes (11): GrilleHistoriqueLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, StatutGrilleEnum, ACTIVE (+3 more)

### Community 30 - "Grille Services"
Cohesion: 0.33
Nodes (7): GrilleTarifaireResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, Transactional

### Community 31 - "Grille DTOs"
Cohesion: 0.27
Nodes (8): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ModifierGrilleTarifaireRequestDto, BeforeEach, ExtendWith

### Community 32 - "Exception Exceptions"
Cohesion: 0.20
Nodes (3): GrilleTarifaireIntrouvableException, MotifRejetObligatoireException, ExceptionHandler

### Community 33 - "Authenticateduserservice Exceptions"
Cohesion: 0.27
Nodes (5): UtilisateurConnecteIntrouvableException, UtilisateurRepository, AuthenticatedUserService, RequiredArgsConstructor, Service

### Community 34 - "Exception Exceptions"
Cohesion: 0.22
Nodes (3): BeneficiaireIntrouvableException, FonctionEligibleIntrouvableException, ResponseEntity

### Community 35 - "Grille DTOs"
Cohesion: 0.36
Nodes (6): HistoriqueGrilleTarifaireResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 36 - "Grilletarifaire Entities"
Cohesion: 0.42
Nodes (8): GrilleTarifaire, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table

### Community 37 - "Corsconfig Addcorsmappings"
Cohesion: 0.43
Nodes (5): CorsConfig, Configuration, Override, CorsRegistry, WebMvcConfigurer

### Community 38 - "Grille DTOs"
Cohesion: 0.52
Nodes (6): CreerGrilleTarifaireRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 39 - "Grille DTOs"
Cohesion: 0.52
Nodes (6): DecisionGrilleTarifaireRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 40 - "Utilisateur DTOs"
Cohesion: 0.52
Nodes (6): ChangerRoleRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 41 - "Utilisateur DTOs"
Cohesion: 0.52
Nodes (6): ChangerStatutRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 42 - "Utilisateur DTOs"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, UtilisateurListeResponseDto

### Community 43 - "Utilisateur DTOs"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, UtilisateurResponseDto

### Community 44 - "Svg Public"
Cohesion: 0.29
Nodes (7): icons.svg (Icon Sprite Sheet), Bluesky Icon Symbol, Discord Icon Symbol, Documentation Icon Symbol, GitHub Icon Symbol, Social/Users Icon Symbol, X (Twitter) Icon Symbol

### Community 45 - "Oxlintrc Frontend"
Cohesion: 0.33
Nodes (5): plugins, rules, react/only-export-components, react/rules-of-hooks, $schema

### Community 46 - "Utils Frontend"
Cohesion: 0.40
Nodes (3): formatDate(), formatDateHeure(), MOIS_LABELS

### Community 48 - "Dottelapplicationtests Contextloads"
Cohesion: 0.60
Nodes (3): DottelApplicationTests, Test, SpringBootTest

### Community 49 - "Utils Frontend"
Cohesion: 0.40
Nodes (4): ROLES, STATUTS, STATUTS_GRILLE, STATUTS_LABELS

### Community 65 - "First Images"
Cohesion: 0.67
Nodes (3): Logo Afriland First Bank, Charte visuelle frontend (Section 15, CLAUDE.md), Afriland First Bank (organisation / marque)

## Ambiguous Edges - Review These
- `ATANGANA Sylvie (bénéficiaire, compte 10014275003, agence GRA-DR01)` → `Sylvie NKOLO (ARH, matricule 2201)`  [AMBIGUOUS]
  backend/documents/dotations-telephoniques-8-2026.pdf · relation: semantically_similar_to

## Knowledge Gaps
- **97 isolated node(s):** `com.afriland:dottel`, `VALIDATION_ARH`, `VALIDATION_CRH`, `VALIDATION_DRH`, `EMPLOYE` (+92 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **30 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `ATANGANA Sylvie (bénéficiaire, compte 10014275003, agence GRA-DR01)` and `Sylvie NKOLO (ARH, matricule 2201)`?**
  _Edge tagged AMBIGUOUS (relation: semantically_similar_to) - confidence is low._
- **Why does `Utilisateur` connect `Utilisateur Services` to `Beneficiaireservice Services`, `Authenticateduserservice Exceptions`, `Processusmensuelservicetest Tests`, `Beneficiaire DTOs`, `Ligneetatmensuel Tests`, `Beneficiaireservicetest Tests`, `Authservicetest Tests`, `Utilisateuradminservicetest Tests`, `Utilisateuradminservice Services`, `Utilisateur DTOs`?**
  _High betweenness centrality (0.119) - this node is a cross-community bridge._
- **Why does `FonctionEligibleRepository` connect `Beneficiaireservice Services` to `Processusmensuelservicetest Tests`, `Beneficiaireservicetest Tests`, `Beneficiaire DTOs`, `Ligneetatmensuel Tests`, `Grilletarifaireservicetest Tests`, `Grille DTOs`?**
  _High betweenness centrality (0.067) - this node is a cross-community bridge._
- **Why does `UtilisateurRepository` connect `Authenticateduserservice Exceptions` to `Beneficiaireservice Services`, `Processusmensuelservicetest Tests`, `Authservicetest Tests`, `Utilisateur Services`, `Utilisateuradminservicetest Tests`, `Utilisateuradminservice Services`?**
  _High betweenness centrality (0.048) - this node is a cross-community bridge._
- **What connects `com.afriland:dottel`, `VALIDATION_ARH`, `VALIDATION_CRH` to the rest of the system?**
  _105 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Beneficiaireservice Services` be split into smaller, more focused modules?**
  _Cohesion score 0.056022408963585436 - nodes in this community are weakly interconnected._
- **Should `Processusmensuelservicetest Tests` be split into smaller, more focused modules?**
  _Cohesion score 0.07846018219218337 - nodes in this community are weakly interconnected._