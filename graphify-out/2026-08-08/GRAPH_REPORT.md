# Graph Report - .  (2026-08-06)

## Corpus Check
- 332 files · ~157,972 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2064 nodes · 5750 edges · 163 communities (132 shown, 31 thin omitted)
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 819 edges (avg confidence: 0.8)
- Token cost: 108,000 input · 12,196 output

## Community Hubs (Navigation)
- Frontend UI Components & Layout
- Processus Mensuel Controller & Workflow
- Beneficiaire API & Workflow Exceptions
- Global Exception Handler (Security)
- Beneficiaire Grade & Ligne Etat Ajustement
- Grille Tarifaire Exceptions
- MM.0 Cadrage Module Decisions
- Application Config (dev/prod) & OWASP Audit
- LigneDocument DTO (Processus)
- Document Signature & Validation Workflow
- Grille Tarifaire Repository & Service
- Beneficiaire Document/Dotation DTOs
- Beneficiaire Enrolement Exceptions
- Utilisateur Email/Matricule Exceptions
- Beneficiaire & Processus Repository Counts
- RoleJwtAuthenticationConverter (Security)
- Audit Events & FonctionEligible Service
- EvenementClotureSerializer (Config)
- Graphify Skill Reference Files
- StatutEnum & Reporting Historique DTO
- EligibiliteService & FonctionEligible Entity
- Processus Motif/PieceJointe Exceptions
- Utilisateur Notification DTO
- RetournerProcessus Request DTO
- GrilleTarifaireController Endpoints
- Beneficiaire NotFound/NonEligible Exceptions
- Beneficiaire Import Service & Tests
- BeneficiaireService & Tests
- FonctionEligibleController Endpoints
- MM.6 Modularity Verification Docs
- Enrolement Confirmation & EHR Integration
- AuditServiceImpl
- UtilisateurAdminController
- Utilisateur Admin/Role Exceptions
- AuditEventListener
- GrilleHistoriqueLigneDto
- BeneficiaireController Endpoints
- FonctionEligible/GrilleTarifaire Entities
- Frontend Auth Provider
- AuditLogResponseDto
- EnrolementController
- BeneficiaireRepository Queries
- Import Excel Exceptions
- BeneficiairePageResponseDto
- EmployeEhrDto (Stub EHR)
- LigneEtatMensuelRepository
- Beneficiaire Entity
- ProcessusMensuel Entity
- SignatureService (Processus)
- Frontend class-variance-authority Dependency
- Frontend Package DevDependencies
- AuditLog Entity
- ProcessusListItemDto
- Dictionnaire de Donnees V3 Docs
- UtilisateurAdminService
- ImportExcel ErreurDto
- ResolutionGrilleDto
- RoleEnum
- application.yml dottel Config Block
- Beneficiaire Dotation Listing API
- LigneEtatMensuel Entity
- FonctionEligibleResponseDto
- ReportingController
- Flyway V1 Table Creation Migration
- Maven Wrapper (mvnw)
- PieceJointeController
- PieceJointe Entity
- CLAUDE.md Modules Metier Section
- Contrats API V3 Admin Group
- Frontend package.json
- CLAUDE.md Erreurs a Ne Jamais Commettre
- CLAUDE.md Regles Metier (RG-01..RG-12)
- Frontend AFB Logo Assets
- Frontend Charte Visuelle Watermark
- BeneficiaireApi Public Interface
- ProcessusEnCoursDto (Reporting)
- Frontend oxlint Config
- Sprint 3.4 Planning Doc
- ModifierBeneficiaireRequestDto
- CorsConfig
- CreerFonctionEligibleRequestDto
- ModifierFonctionEligibleRequestDto
- CreerGrilleTarifaireRequestDto
- DecisionGrilleTarifaireRequestDto
- GrilleTarifaireListeResponseDto
- HistoriqueGrilleTarifaireResponseDto
- ModifierGrilleTarifaireRequestDto
- DashboardResponseDto (Reporting)
- AuthController
- ChangerStatutRequestDto (Utilisateur)
- CreerUtilisateurRequestDto
- UtilisateurListeResponseDto
- UtilisateurResponseDto
- Contrats API V3 Auth/Login Group
- Frontend SVG Icons
- K8s Deployment Manifest
- AuditLogRepository
- ConfirmerEnrolementResponseDto
- MM.4 Audit Evenementiel Docs
- ApplicationModules (Spring Modulith)
- AuditService Public API
- DottelApplicationTests
- Frontend Constants Utils
- MM.7 API Client (JS) Migration
- DottelApplication (Main Class)
- Backend DocumentationTests (ModularityTests)
- CLAUDE.md 34 API Endpoints Section
- MM.7 Bascule AuthenticatedUserService
- MM.6 Demarrage Reel Rationale
- Frontend React Framework Assets
- MM.7 Bascule Claim Matricule
- Frontend axios Dependency
- Flyway V4 Beneficiaires Chapitre Migration
- Backend NS 69/17 Note de Service Image
- Frontend clsx Dependency
- MM.0 Cadrage Doc
- Frontend Root
- Frontend index.html
- Frontend jwt-decode Dependency
- Frontend react Dependency
- Frontend react-hook-form Dependency
- Frontend react-hot-toast Dependency
- Frontend react-router-dom Dependency
- Frontend tailwind-merge Dependency
- Frontend zod Dependency
- Frontend Vite Build Tool Asset
- Backend PID File
- Backend PID File 2
- Backend PID File 3
- Backend PID File 4
- Backend AFB Logo Image
- Backend NS 69/17 Static Image
- Graphify Skill Benchmark Reference
- MM.0 Cadrage Decision D
- Plan Monolithe Modulaire Vision Cible
- Frontend Hero Asset
- Maven pom.xml (com.afriland.dottel)

## God Nodes (most connected - your core abstractions)
1. `Utilisateur` - 99 edges
2. `ProcessusMensuelServiceTest` - 86 edges
3. `EvenementAudit` - 43 edges
4. `ProcessusMensuel` - 41 edges
5. `ProcessusMensuelService` - 38 edges
6. `react` - 38 edges
7. `GlobalExceptionHandler` - 37 edges
8. `GrilleTarifaireServiceTest` - 36 edges
9. `StatutEnum` - 35 edges
10. `BeneficiaireRepository` - 34 edges

## Surprising Connections (you probably didn't know these)
- `Contrats API Dotations Téléphoniques V3.3 (AFB_API_DOTTEL_V3.3_2026)` --semantically_similar_to--> `Contrats API : 34 endpoints`  [INFERRED] [semantically similar]
  docs/reference/contrats_api_dotations_v3.md → CLAUDE.md
- `Ecart E7c — fichiers .env non charges par Vite, build HTTP clair` --semantically_similar_to--> `APP_CORS_ALLOWED_ORIGINS (origine frontend production)`  [INFERRED] [semantically similar]
  docs/audit_securite_owasp_v1.md → k8s/configmap.yaml
- `Sprint MM.7 — Keycloak local provisoire, en remplacement de la simulation JWT` --cites--> `Déploiement Kubernetes (microservice conteneurisé)`  [EXTRACTED]
  docs/monolithe-modulaire/MM.7_keycloak_provisoire.md → CLAUDE.md
- `Rapport d'audit de securite OWASP V1 (AFB_AUDIT_DOTTEL_V1_2026)` --cites--> `Repli en clair DB_PASSWORD:postgres (profil dev)`  [EXTRACTED]
  docs/audit_securite_owasp_v1.md → backend/src/main/resources/application-dev.yml
- `Validation cote client = ergonomie, jamais garantie de securite` --references--> `VerifierMatriculePage.jsx — GET /enrolement/verifier`  [EXTRACTED]
  docs/audit_securite_owasp_v1.md → sprint6F_6F_4.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Graph export targets (wiki, neo4j, falkordb, mcp, benchmark)** — claude_skills_graphify_references_exports_wiki, claude_skills_graphify_references_exports_neo4j, claude_skills_graphify_references_exports_falkordb, claude_skills_graphify_references_exports_mcp, claude_skills_graphify_references_exports_benchmark [EXTRACTED 1.00]
- **Graph query/traversal family (query, path, explain)** — claude_skills_graphify_references_query_vocab_expansion, claude_skills_graphify_references_query_bfs_dfs, claude_skills_graphify_references_query_path_explain, claude_skills_graphify_references_query_save_result_reflect [INFERRED 0.85]
- **Chaine CORS de production : ConfigMap, propriete Spring, audit E8** — k8s_configmap_app_cors_allowed_origins, docs_audit_securite_owasp_v1_ecart_e8_cors_externalise, docs_audit_securite_owasp_v1_ecart_e7c_https_build [EXTRACTED 1.00]
- **Externalisation des secrets : aucun repli committe, echec au demarrage** — docs_audit_securite_owasp_v1_ecart_e7b_jwt_secret_prod, backend_src_main_resources_application_dev_db_password_repli, k8s_configmap [EXTRACTED 1.00]
- **Unification de la résolution de grille tarifaire (RG-04) à travers MM.2/MM.3** — docs_monolithe_modulaire_mm_2_refactor_processus_mensuel_service_resoudregrillepourfonction, docs_monolithe_modulaire_mm_3_casser_couplages_restants_c2, docs_monolithe_modulaire_mm_3_casser_couplages_restants_beneficiaireservice, docs_monolithe_modulaire_mm_3_casser_couplages_restants_enrolementservice, docs_monolithe_modulaire_mm_0_cadrage_module_referentiel [EXTRACTED 0.90]
- **Découplage de l'audit par événements applicatifs (MM.4)** — docs_monolithe_modulaire_mm_4_audit_evenementiel_auditservice, docs_monolithe_modulaire_mm_4_audit_evenementiel_auditserviceimpl, docs_monolithe_modulaire_mm_4_audit_evenementiel_mode_transactionnel_options, docs_monolithe_modulaire_mm_4_audit_evenementiel_conception_evenements, docs_monolithe_modulaire_mm_4_audit_evenementiel_piege_adresse_ip [EXTRACTED 0.90]
- **Gouvernance du cycle beneficiaires<->referentiel : isolation MM.5, correctif obligatoire MM.8** — docs_monolithe_modulaire_mm_6_verification_et_documentation_cycle_beneficiaires_referentiel_c3, docs_monolithe_modulaire_mm_6_verification_et_documentation_violations_filter, docs_monolithe_modulaire_mm_6_verification_et_documentation_mm5_sprint, docs_monolithe_modulaire_mm_6_verification_et_documentation_mm8_sprint [EXTRACTED 0.90]
- **Génération de la documentation d'architecture Spring Modulith** — docs_monolithe_modulaire_mm_6_verification_et_documentation_documentationtests, docs_monolithe_modulaire_mm_6_verification_et_documentation_documenter, docs_monolithe_modulaire_mm_6_verification_et_documentation_modularitytests, docs_monolithe_modulaire_mm_6_verification_et_documentation_architecture_docs_dir [EXTRACTED 0.90]
- **grille_tarifaire table, StatutGrilleEnum, RG-04, RG-10 and the grilles-tarifaires API group form the tariff-grid validation lifecycle** — docs_reference_dictionnaire_de_donnees_dotations_v3_grille_tarifaire, docs_reference_dictionnaire_de_donnees_dotations_v3_statutgrilleenum [INFERRED 0.85]
- **Kubernetes deployment pipeline for dottel-backend** — k8s_deployment_yaml_dottel_backend, k8s_service_yaml_dottel_backend_svc [EXTRACTED 1.00]
- **Secret/env var externalization from application.yml to K8s Secret/ConfigMap** — k8s_deployment_yaml_dottel_secret [INFERRED 0.85]
- **RG-08 séparation des tâches : écart entre contrat API et périmètre Sprint 3.4** — sprint_3_4, sprint_3_4_rg08_separation_taches [EXTRACTED 0.90]
- **Flux de génération du PDF initial (DocumentService + PieceJointe + SignatureService + CHAPITRE)** — sprint_3_4_documentservice, sprint_3_4_piecejointe, sprint_3_4_signatureservice, sprint_3_4_chapitre_field [EXTRACTED 0.90]
- **Flux d'authentification frontend (6F.3) et son verdict d'audit** — sprint6f_6f_3_authprovider_abstraction, sprint6f_6f_3_authproviderlocal, sprint6f_6f_3_authcontext, sprint6f_6f_3_apiclient_intercepteur_jwt, sprint6f_6f_3_protectedroute, docs_audit_securite_owasp_v1_stockage_token_memoire [EXTRACTED 1.00]
- **Les trois questions ouvertes MM.7 et la décision de portée, toutes tranchées le 2026-07-31** — mm7_decision_e3_enrolement_tiers, mm7_decision_f2_pkce, mm7_decision_i2_resolution_email, mm7_decision_p2_portee [EXTRACTED 1.00]
- **Infrastructure locale provisoire simulée en docker-compose (Kafka + Keycloak)** — docker_compose_service_kafka, docker_compose_service_keycloak, docker_compose_realm_dottel_dev_json, claude_md_deploiement_k8s [EXTRACTED 1.00]
- **Cycle de modules beneficiaires<->referentiel, isolé par ModularityTests, correctif prévu MM.8** — recap_modularity_tests, recap_cycle_beneficiaires_referentiel, recap_mm8_prerequis, recap_dette_tracee_utilisateurs_entity [EXTRACTED 1.00]

## Communities (163 total, 31 thin omitted)

### Community 0 - "Frontend UI Components & Layout"
Cohesion: 0.06
Nodes (109): apiClient, App(), AppLayout(), PageHeader(), initiales(), NAV_LINKS, Sidebar(), trouverHrefActif() (+101 more)

### Community 1 - "Processus Mensuel Controller & Workflow"
Cohesion: 0.05
Nodes (60): GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+52 more)

### Community 2 - "Beneficiaire API & Workflow Exceptions"
Cohesion: 0.07
Nodes (32): EtapeWorkflowIntrouvableException, RoleEtapeNonAutoriseException, SeparationTachesViolationException, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter (+24 more)

### Community 3 - "Global Exception Handler (Security)"
Cohesion: 0.10
Nodes (12): AccessDeniedException, GlobalExceptionHandler, Logger, MethodArgumentNotValidException, ResponseEntity, GlobalExceptionHandlerTest, ExtendWith, MethodArgumentNotValidException (+4 more)

### Community 4 - "Beneficiaire Grade & Ligne Etat Ajustement"
Cohesion: 0.22
Nodes (7): AjustementLigneEtatDto, Getter, Setter, Getter, Setter, PatchProcessusRequestDto, ResolutionGrilleDto

### Community 5 - "Grille Tarifaire Exceptions"
Cohesion: 0.07
Nodes (18): DateDebutGrilleAnterieureException, DecisionGrilleInvalideException, FonctionEligibleBeneficiairesActifsException, FonctionEligibleIntrouvableException, GrilleEnAttenteDrhExistanteException, GrilleIntrouvableException, GrilleNonActiveException, GrilleNonEnAttenteDrhException (+10 more)

### Community 6 - "MM.0 Cadrage Module Decisions"
Cohesion: 0.05
Nodes (39): Décision A - EligibiliteService (A1), Décision B - Référentiel unifié (B1), Décision C - Découpage 6 modules, EligibiliteService, Module audit, Module beneficiaires, Module integration (proposé puis rejeté, réparti), Module processus (+31 more)

### Community 7 - "Application Config (dev/prod) & OWASP Audit"
Cohesion: 0.07
Nodes (39): application-dev.yml (profil developpement), Repli en clair DB_PASSWORD:postgres (profil dev), application-prod.yml (profil production), server.error.* (prefixe corrige, ecart E6), Rapport d'audit de securite OWASP V1 (AFB_AUDIT_DOTTEL_V1_2026), A01 — Alignement des routes frontend sur la matrice de roles, A03 — Injection et echappement (zero dangerouslySetInnerHTML), Ecart E1 — CRH redirige vers /processus (ROUTE_PAR_ROLE) (+31 more)

### Community 8 - "LigneDocument DTO (Processus)"
Cohesion: 0.15
Nodes (19): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, LigneDocumentDto, PieceJointeRepository, DocumentService (+11 more)

### Community 11 - "Beneficiaire Document/Dotation DTOs"
Cohesion: 0.13
Nodes (10): BeneficiaireDocumentDto, BeneficiaireDotationDto, BeneficiaireIdentiteDto, BeneficiaireApiImpl, Override, RequiredArgsConstructor, Service, BeneficiaireApiImplTest (+2 more)

### Community 12 - "Beneficiaire Enrolement Exceptions"
Cohesion: 0.09
Nodes (18): MatriculeDejaEnroleException, MatriculeInconnuException, EnrolementService, ApplicationEventPublisher, RequiredArgsConstructor, Service, Transactional, GrilleTarifaireApi (+10 more)

### Community 13 - "Utilisateur Email/Matricule Exceptions"
Cohesion: 0.14
Nodes (8): EmailUtilisateurDejaUtiliseException, MatriculeUtilisateurDejaUtiliseException, ApplicationEventPublisher, BeforeEach, ExtendWith, PasswordEncoder, Test, UtilisateurAdminServiceTest

### Community 14 - "Beneficiaire & Processus Repository Counts"
Cohesion: 0.18
Nodes (10): BeneficiaireRepository, ProcessusMensuelRepository, RequiredArgsConstructor, Service, Transactional, ReportingService, BeforeEach, ExtendWith (+2 more)

### Community 15 - "RoleJwtAuthenticationConverter (Security)"
Cohesion: 0.14
Nodes (18): Component, Logger, Override, RoleJwtAuthenticationConverter, Bean, Configuration, EnableMethodSecurity, PasswordEncoder (+10 more)

### Community 16 - "Audit Events & FonctionEligible Service"
Cohesion: 0.23
Nodes (5): EvenementAudit, Override, Transactional, FonctionEligibleServiceTest, Test

### Community 17 - "EvenementClotureSerializer (Config)"
Cohesion: 0.13
Nodes (20): EvenementClotureSerializer, ObjectMapper, Override, Bean, Configuration, KafkaTemplate, ObjectMapper, KafkaConfig (+12 more)

### Community 18 - "Graphify Skill Reference Files"
Cohesion: 0.08
Nodes (29): Project .claude/CLAUDE.md (graphify trigger), /graphify add <url>, --watch folder watcher, graphify export falkordb / falkordb-push, MCP stdio server (graphify.serve), graphify export neo4j / neo4j-push, graphify export wiki, Confidence score rubric (0.55-0.95 discrete steps) (+21 more)

### Community 19 - "StatutEnum & Reporting Historique DTO"
Cohesion: 0.13
Nodes (20): HistoriqueLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, HistoriqueResponseDto, AllArgsConstructor (+12 more)

### Community 20 - "EligibiliteService & FonctionEligible Entity"
Cohesion: 0.14
Nodes (15): EligibiliteService, RequiredArgsConstructor, Service, FonctionEligible, AllArgsConstructor, Builder, Entity, Getter (+7 more)

### Community 21 - "Processus Motif/PieceJointe Exceptions"
Cohesion: 0.10
Nodes (12): MotifRejetObligatoireException, PieceJointeIntrouvableException, ProcessusMensuelExisteDejaException, ProcessusMensuelIntrouvableException, ProcessusMensuelNonModifiableException, ApplicationEventPublisher, RequiredArgsConstructor, Service (+4 more)

### Community 22 - "Utilisateur Notification DTO"
Cohesion: 0.14
Nodes (15): UtilisateurApi, RoleEnum, ADMIN, ARH, CRH, DRH, EMPLOYE, UtilisateurRepository (+7 more)

### Community 23 - "RetournerProcessus Request DTO"
Cohesion: 0.16
Nodes (12): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, RetournerProcessusRequestDto, NotificationService, Override (+4 more)

### Community 24 - "GrilleTarifaireController Endpoints"
Cohesion: 0.19
Nodes (15): GrilleTarifaireController, GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity (+7 more)

### Community 25 - "Beneficiaire NotFound/NonEligible Exceptions"
Cohesion: 0.13
Nodes (12): BeneficiaireIntrouvableException, NonEligibleException, BeneficiaireService, ApplicationEventPublisher, Page, Pageable, RequiredArgsConstructor, Service (+4 more)

### Community 26 - "Beneficiaire Import Service & Tests"
Cohesion: 0.24
Nodes (7): BeneficiaireImportServiceTest, ApplicationEventPublisher, ExtendWith, Row, Test, InvocationOnMock, MockMultipartFile

### Community 28 - "FonctionEligibleController Endpoints"
Cohesion: 0.21
Nodes (15): FonctionEligibleController, GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity (+7 more)

### Community 29 - "MM.6 Modularity Verification Docs"
Cohesion: 0.09
Nodes (23): @ApplicationModule(allowedDependencies = {...}), docs/monolithe-modulaire/architecture/ (diagrammes PlantUML et canvases versionnés), docs/audit_securite_owasp_v1.md, AuditService, CLAUDE.md, Cycle beneficiaires <-> referentiel (couplage C3), DocumentationTests.java, Documenter (org.springframework.modulith.docs) (+15 more)

### Community 30 - "Enrolement Confirmation & EHR Integration"
Cohesion: 0.32
Nodes (5): ConfirmerEnrolementRequestDto, Getter, Setter, EnrolementServiceTest, Test

### Community 31 - "AuditServiceImpl"
Cohesion: 0.19
Nodes (11): AuditServiceImpl, Override, Page, Pageable, RequiredArgsConstructor, Service, Transactional, AuditServiceImplTest (+3 more)

### Community 32 - "UtilisateurAdminController"
Cohesion: 0.20
Nodes (15): GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+7 more)

### Community 33 - "Utilisateur Admin/Role Exceptions"
Cohesion: 0.18
Nodes (9): ActionAdminNonAutoriseeException, RoleInvalideException, UtilisateurIntrouvableException, ApplicationEventPublisher, PasswordEncoder, RequiredArgsConstructor, Service, Transactional (+1 more)

### Community 34 - "AuditEventListener"
Cohesion: 0.18
Nodes (11): AuditEventListener, Component, Logger, ObjectMapper, RequiredArgsConstructor, AuditEventListenerTest, AfterEach, BeforeEach (+3 more)

### Community 35 - "GrilleHistoriqueLigneDto"
Cohesion: 0.18
Nodes (17): GrilleHistoriqueLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, GrilleTarifaireListeLigneDto, AllArgsConstructor (+9 more)

### Community 36 - "BeneficiaireController Endpoints"
Cohesion: 0.25
Nodes (11): BeneficiaireController, GetMapping, MultipartFile, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor (+3 more)

### Community 37 - "FonctionEligible/GrilleTarifaire Entities"
Cohesion: 0.17
Nodes (12): FonctionEligibleCodeDejaUtiliseException, GrilleTarifaire, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter (+4 more)

### Community 38 - "Frontend Auth Provider"
Cohesion: 0.16
Nodes (8): setAuthToken(), AuthProvider, AuthProviderKeycloak, base64UrlEncode(), calculerCodeChallenge(), construireUtilisateur(), genererValeurAleatoire(), ROLES_DOTTEL

### Community 39 - "AuditLogResponseDto"
Cohesion: 0.22
Nodes (13): AuditLogResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, AuditLogPageResponseDto, AllArgsConstructor (+5 more)

### Community 40 - "EnrolementController"
Cohesion: 0.22
Nodes (13): EnrolementController, GetMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+5 more)

### Community 41 - "BeneficiaireRepository Queries"
Cohesion: 0.23
Nodes (8): BeneficiaireExportService, RequiredArgsConstructor, Service, Transactional, BeneficiaireExportServiceTest, BeforeEach, ExtendWith, Test

### Community 42 - "Import Excel Exceptions"
Cohesion: 0.17
Nodes (10): FichierImportInvalideException, BeneficiaireImportService, ApplicationEventPublisher, MultipartFile, RequiredArgsConstructor, Row, Service, Transactional (+2 more)

### Community 43 - "BeneficiairePageResponseDto"
Cohesion: 0.23
Nodes (13): BeneficiairePageResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Page, Setter, BeneficiaireResponseDto (+5 more)

### Community 44 - "EmployeEhrDto (Stub EHR)"
Cohesion: 0.22
Nodes (10): EmployeEhrDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, EhrIntegrationService, EhrIntegrationServiceStub, Override (+2 more)

### Community 45 - "LigneEtatMensuelRepository"
Cohesion: 0.30
Nodes (7): LigneEtatMensuelRepository, EcartMensuelService, RequiredArgsConstructor, Service, EcartMensuelServiceTest, ExtendWith, Test

### Community 46 - "Beneficiaire Entity"
Cohesion: 0.22
Nodes (10): Beneficiaire, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+2 more)

### Community 47 - "ProcessusMensuel Entity"
Cohesion: 0.24
Nodes (8): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, ProcessusMensuel

### Community 48 - "SignatureService (Processus)"
Cohesion: 0.22
Nodes (11): Override, Service, SignatureServiceAutonome, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor (+3 more)

### Community 49 - "Frontend class-variance-authority Dependency"
Cohesion: 0.13
Nodes (15): class-variance-authority, date-fns, dependencies, class-variance-authority, date-fns, @hookform/resolvers, lucide-react, @radix-ui/react-checkbox (+7 more)

### Community 50 - "Frontend Package DevDependencies"
Cohesion: 0.13
Nodes (15): devDependencies, oxlint, tailwindcss, @tailwindcss/vite, @types/react, @types/react-dom, vite, @vitejs/plugin-react (+7 more)

### Community 51 - "AuditLog Entity"
Cohesion: 0.24
Nodes (10): AuditLog, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+2 more)

### Community 52 - "ProcessusListItemDto"
Cohesion: 0.21
Nodes (12): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusListItemDto, StatutEnum, CLOTURE (+4 more)

### Community 53 - "Dictionnaire de Donnees V3 Docs"
Cohesion: 0.26
Nodes (14): Dictionnaire de données Dotations V3, Table audit_log, Table beneficiaires, Table etape_workflow, Table fonction_eligible, Table grille_tarifaire, Table ligne_etat_mensuel (pivot N-N), Table piece_jointe (+6 more)

### Community 54 - "UtilisateurAdminService"
Cohesion: 0.29
Nodes (8): EnableMethodSecurity, Test, MethodSecurityConfig, UtilisateurAdminControllerTest, Import, MockMvc, WebMvcTest, WithMockUser

### Community 55 - "ImportExcel ErreurDto"
Cohesion: 0.29
Nodes (10): ImportErreurDto, AllArgsConstructor, Builder, Data, NoArgsConstructor, ImportRapportDto, AllArgsConstructor, Builder (+2 more)

### Community 56 - "ResolutionGrilleDto"
Cohesion: 0.35
Nodes (4): Override, GrilleTarifaireApiImplTest, ExtendWith, Test

### Community 57 - "RoleEnum"
Cohesion: 0.32
Nodes (4): AuthenticatedUserServiceTest, AfterEach, ExtendWith, Test

### Community 58 - "application.yml dottel Config Block"
Cohesion: 0.26
Nodes (12): application.yml — configuration Spring dottel, dottel.kafka.topic-cloture (dottel.processus.cloture), DOTTEL_KEYCLOAK_ISSUER_URI (issuer-uri OAuth2 resource server), Simulation Keycloak en développement, keycloak/realm-dottel-dev.json (realm de développement versionné), Service Docker Compose kafka (dottel-kafka), Service Docker Compose keycloak (dottel-keycloak, local provisoire), Fédération Active Directory (hypothèse non confirmée) (+4 more)

### Community 59 - "Beneficiaire Dotation Listing API"
Cohesion: 0.42
Nodes (3): DeclencherProcessusRequestDto, Getter, Setter

### Community 60 - "LigneEtatMensuel Entity"
Cohesion: 0.31
Nodes (9): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, LigneEtatMensuel (+1 more)

### Community 61 - "FonctionEligibleResponseDto"
Cohesion: 0.31
Nodes (6): FonctionEligibleResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 62 - "ReportingController"
Cohesion: 0.40
Nodes (7): GetMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, ReportingController

### Community 63 - "Flyway V1 Table Creation Migration"
Cohesion: 0.38
Nodes (10): audit_log, beneficiaires, etape_workflow, fonction_eligible, grille_tarifaire, ligne_etat_mensuel, piece_jointe, processus_mensuel (+2 more)

### Community 64 - "Maven Wrapper (mvnw)"
Cohesion: 0.33
Nodes (6): mvnw script, clean(), die(), exec_maven(), set_java_home(), verbose()

### Community 65 - "PieceJointeController"
Cohesion: 0.36
Nodes (8): GetMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, PieceJointeController, Resource

### Community 66 - "PieceJointe Entity"
Cohesion: 0.36
Nodes (8): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, PieceJointe

### Community 67 - "CLAUDE.md Modules Metier Section"
Cohesion: 0.20
Nodes (10): 6 modules métier (utilisateurs, beneficiaires, referentiel, processus, reporting, audit), Groupe Reporting /reporting (non implémenté, planifié Sprint 6), Récapitulatif du chantier Monolithe Modulaire (MM.0 à MM.6), Cycle beneficiaires <-> referentiel (exception pinée, MM.8 prévu), Dette tracée — dépendance vers utilisateurs::entity, GrilleTarifaireApi.resoudrePourFonction() (résolution unique RG-04), MM.8 — refactor événementiel prévu pour casser le cycle, ModularityTests (Spring Modulith + ArchUnit) (+2 more)

### Community 68 - "Contrats API V3 Admin Group"
Cohesion: 0.20
Nodes (10): Groupe Administration /admin (non implémenté), PATCH /beneficiaires/{id}/reactiver (Sprint 6F.5), Contrats API Dotations Téléphoniques V3.3 (AFB_API_DOTTEL_V3.3_2026), GET /enrolement/verifier, GET /fonctions-eligibles, PATCH /fonctions-eligibles/{code}, POST /fonctions-eligibles (Sprint 6F.7bis), POST /grilles-tarifaires/{id}/desactiver (Sprint 6F.7bis) (+2 more)

### Community 69 - "Frontend package.json"
Cohesion: 0.20
Nodes (9): name, private, scripts, build, dev, lint, preview, type (+1 more)

### Community 70 - "CLAUDE.md Erreurs a Ne Jamais Commettre"
Cohesion: 0.22
Nodes (9): Erreurs à ne jamais commettre (20 points), Note de comportement E-3 dans le contrat enrôlement, POST /enrolement/confirmer, GrilleTarifaireIntrouvableException (400, pas 500), Décision E-3 — enrôlement pour un tiers, statu quo documenté, EnrolementController (vérifier/confirmer, rôle EMPLOYE), EnrolementService.confirmer() (matricule du corps de requête), Option P-3 — parcours EMPLOYE rendu public (non recommandée) (+1 more)

### Community 71 - "CLAUDE.md Regles Metier (RG-01..RG-12)"
Cohesion: 0.22
Nodes (9): Règles métier critiques RG-01 à RG-12, RG-08 Séparation des tâches, RG-09 Historisation (audit_log), RG-12 Unicité du processus mensuel, POST /beneficiaires/import (RG-11), POST /processus/declencher (RG-12), POST /processus/{id}/valider (branche ARH implémentée), Constat MM.7 — audit_log ne trace plus les connexions Keycloak (+1 more)

### Community 72 - "Frontend AFB Logo Assets"
Cohesion: 0.39
Nodes (9): Afriland First Bank Logo (PNG asset), Identité de marque Afriland First Bank, Charte visuelle frontend BAOBAB / DOTTEL, Palette de marque rouge / noir / gris / blanc, Rouge institutionnel AFB (E30613), Symbole concentrique "C/E" sur bloc rouge, Asset PNG horizontal à fond transparent importable par Vite, Usage : logo en haut de la barre de navigation latérale (+1 more)

### Community 73 - "Frontend Charte Visuelle Watermark"
Cohesion: 0.42
Nodes (9): Charte visuelle AFB - fond clair sobre, aucun rouge E30613 dans le fond, Filigrane discret de la zone de contenu principale (.fond-filigrane), Motif chevron / toit (polyline 110,50 120,36 130,50), Motif cercle (cx28 cy30 r7), Motif deux lignes horizontales (lignes de document/etat), Motif oeil (lentille + pupille, controle/supervision), Style de trait unique (fill=none, stroke #1A1A1A, width 1.5, opacity 0.07), Tuile repetable 160x160 (background-repeat) (+1 more)

### Community 74 - "BeneficiaireApi Public Interface"
Cohesion: 0.39
Nodes (5): BeneficiaireApi, FonctionEligibleService, ApplicationEventPublisher, RequiredArgsConstructor, Service

### Community 75 - "ProcessusEnCoursDto (Reporting)"
Cohesion: 0.43
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusEnCoursDto

### Community 76 - "Frontend oxlint Config"
Cohesion: 0.25
Nodes (7): plugins, rules, react/only-export-components, react/rules-of-hooks, $schema, oxc, warn

### Community 77 - "Sprint 3.4 Planning Doc"
Cohesion: 0.29
Nodes (8): Sprint 3.4 guide — Validation ARH et génération du PDF initial (US-11), Champ CHAPITRE (donnée EHR par bénéficiaire, décision révisée depuis 'valeur fixe 64310000' vers donnée variable + repli configurable), DocumentService.genererInitiale() (iText 8 PDF, RG-06), Entité EtapeWorkflow (NomEtapeEnum, StatutEtapeEnum, signature_numerique), NotificationService (stub log, appelé par ProcessusMensuelService.valider), Entité PieceJointe (UNIQUE par processus, nombre_signatures), Point de vigilance RG-08 : contrat API mentionne la vérification sur /valider, mais SeparationTachesService est reporté au Sprint 5, SignatureService (interface isolée, trace non-certifiée, remplaçable par intégration type INTRA)

### Community 78 - "ModifierBeneficiaireRequestDto"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ModifierBeneficiaireRequestDto

### Community 79 - "CorsConfig"
Cohesion: 0.43
Nodes (5): CorsConfig, Configuration, Override, CorsRegistry, WebMvcConfigurer

### Community 80 - "CreerFonctionEligibleRequestDto"
Cohesion: 0.52
Nodes (6): CreerFonctionEligibleRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 81 - "ModifierFonctionEligibleRequestDto"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ModifierFonctionEligibleRequestDto

### Community 82 - "CreerGrilleTarifaireRequestDto"
Cohesion: 0.52
Nodes (6): CreerGrilleTarifaireRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 83 - "DecisionGrilleTarifaireRequestDto"
Cohesion: 0.52
Nodes (6): DecisionGrilleTarifaireRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 84 - "GrilleTarifaireListeResponseDto"
Cohesion: 0.52
Nodes (6): GrilleTarifaireListeResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 85 - "HistoriqueGrilleTarifaireResponseDto"
Cohesion: 0.52
Nodes (6): HistoriqueGrilleTarifaireResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 86 - "ModifierGrilleTarifaireRequestDto"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ModifierGrilleTarifaireRequestDto

### Community 87 - "DashboardResponseDto (Reporting)"
Cohesion: 0.52
Nodes (6): DashboardResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 88 - "AuthController"
Cohesion: 0.48
Nodes (5): AuthController, PostMapping, RequestMapping, ResponseEntity, RestController

### Community 89 - "ChangerStatutRequestDto (Utilisateur)"
Cohesion: 0.52
Nodes (6): ChangerStatutRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 90 - "CreerUtilisateurRequestDto"
Cohesion: 0.52
Nodes (6): CreerUtilisateurRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 91 - "UtilisateurListeResponseDto"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, UtilisateurListeResponseDto

### Community 92 - "UtilisateurResponseDto"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, UtilisateurResponseDto

### Community 93 - "Contrats API V3 Auth/Login Group"
Cohesion: 0.33
Nodes (7): POST /auth/login, AuthService.authentifier() (émission JWT symétrique, supprimé), RoleJwtAuthenticationConverter (forme du claim de rôle), Décision de portée P-2 — Keycloak pour les 5 rôles, EMPLOYE compris, JwtUtil.genererToken() (HS384 symétrique, supprimé), Option P-1 — Keycloak pour les 4 rôles internes uniquement (écartée), SecurityConfig.jwtDecoder() (bascule vers issuer-uri/JWKS)

### Community 94 - "Frontend SVG Icons"
Cohesion: 0.29
Nodes (7): icons.svg (Icon Sprite Sheet), Bluesky Icon Symbol, Discord Icon Symbol, Documentation Icon Symbol, GitHub Icon Symbol, Social/Users Icon Symbol, X (Twitter) Icon Symbol

### Community 95 - "K8s Deployment Manifest"
Cohesion: 0.29
Nodes (7): k8s deployment.yaml (dottel-backend), /api/actuator/health liveness/readiness probe, Deployment dottel-backend, Image harbor.afrilandfirstbank.cm/baobab/dottel-backend:latest, Secret dottel-secret (referenced), k8s service.yaml (dottel-backend-svc), Service dottel-backend-svc (ClusterIP)

### Community 96 - "AuditLogRepository"
Cohesion: 0.53
Nodes (3): AuditLogRepository, JpaRepository, JpaSpecificationExecutor

### Community 97 - "ConfirmerEnrolementResponseDto"
Cohesion: 0.60
Nodes (5): ConfirmerEnrolementResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor

### Community 98 - "MM.4 Audit Evenementiel Docs"
Cohesion: 0.40
Nodes (6): AuditService (interface), AuditServiceImpl.enregistrer(), Options E-1/E-2 conception des événements d'audit, Options T-1/T-2/T-3 mode transactionnel de l'écouteur, Piège 2 - adresse IP (RequestContextHolder), Piège 1 - transactionnalité (rollback conjoint)

### Community 99 - "ApplicationModules (Spring Modulith)"
Cohesion: 0.60
Nodes (3): ApplicationModules, Test, ModularityTests

### Community 100 - "AuditService Public API"
Cohesion: 0.60
Nodes (3): AuditService, Page, Pageable

### Community 101 - "DottelApplicationTests"
Cohesion: 0.60
Nodes (3): DottelApplicationTests, Test, SpringBootTest

### Community 102 - "Frontend Constants Utils"
Cohesion: 0.40
Nodes (4): ROLES, STATUTS, STATUTS_GRILLE, STATUTS_LABELS

### Community 103 - "MM.7 API Client (JS) Migration"
Cohesion: 0.40
Nodes (5): apiClient.js (jeton en mémoire seule), AuthContext.jsx (câblé au singleton authProviderLocal), AuthProvider.js (contrat frontend à 3 méthodes), AuthProviderLocal.js (implémentation matricule/mot de passe), Décision F-2 — Authorization Code + PKCE

### Community 106 - "CLAUDE.md 34 API Endpoints Section"
Cohesion: 0.50
Nodes (4): Contrats API : 34 endpoints, Déploiement Kubernetes (microservice conteneurisé), Module DOTTEL (Dotations Téléphoniques Mensuelles), Stack technique (Spring Boot 4.1.0, React 19, PostgreSQL 16)

### Community 107 - "MM.7 Bascule AuthenticatedUserService"
Cohesion: 0.67
Nodes (4): AuthenticatedUserService (résolution par claim email), Décision I-2 — résolution d'identité par email, Utilisateur.java (entité, sans champ d'identité externe), V3__insertion_utilisateurs_test.sql (5 utilisateurs de test, écart tiret bas corrigé)

### Community 108 - "MM.6 Demarrage Reel Rationale"
Cohesion: 0.67
Nodes (3): Nécessité du démarrage réel (au-delà des tests unitaires Mockito), DottelApplication, DottelApplicationTests

### Community 109 - "Frontend React Framework Assets"
Cohesion: 0.67
Nodes (3): React Framework, React Logo (Vite default asset), Vite/React Starter Template Bootstrap

### Community 110 - "MM.7 Bascule Claim Matricule"
Cohesion: 0.67
Nodes (3): Claim matricule (protocol mapper Keycloak), UtilisateurAdminService (garde-fou serveur), UtilisateursListPage.jsx (garde-fou frontend admin)

## Ambiguous Edges - Review These
- `Identité de marque Afriland First Bank` → `Symbole concentrique "C/E" sur bloc rouge`  [AMBIGUOUS]
  frontend/src/assets/logo afriland.png · relation: conceptually_related_to
- `Motif cercle (cx28 cy30 r7)` → `Motif chevron / toit (polyline 110,50 120,36 130,50)`  [AMBIGUOUS]
  frontend/src/assets/watermark-pattern.svg · relation: semantically_similar_to
- `Variables d'environnement Keycloak (DOTTEL_KEYCLOAK_ISSUER_URI, VITE_KEYCLOAK_*)` → `RoleJwtAuthenticationConverter (forme du claim de rôle)`  [AMBIGUOUS]
  docs/monolithe-modulaire/MM.7_bascule_realm_dsi.md · relation: conceptually_related_to

## Knowledge Gaps
- **186 isolated node(s):** `com.afriland:dottel`, `VALIDATION_ARH`, `VALIDATION_CRH`, `VALIDATION_DRH`, `EN_COURS_ARH` (+181 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **31 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Identité de marque Afriland First Bank` and `Symbole concentrique "C/E" sur bloc rouge`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Motif cercle (cx28 cy30 r7)` and `Motif chevron / toit (polyline 110,50 120,36 130,50)`?**
  _Edge tagged AMBIGUOUS (relation: semantically_similar_to) - confidence is low._
- **What is the exact relationship between `Variables d'environnement Keycloak (DOTTEL_KEYCLOAK_ISSUER_URI, VITE_KEYCLOAK_*)` and `RoleJwtAuthenticationConverter (forme du claim de rôle)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `EvenementAudit` connect `Audit Events & FonctionEligible Service` to `Utilisateur Admin/Role Exceptions`, `AuditEventListener`, `Beneficiaire Grade & Ligne Etat Ajustement`, `Grille Tarifaire Exceptions`, `FonctionEligible/GrilleTarifaire Entities`, `Beneficiaire Dotation Listing API`, `Import Excel Exceptions`, `Document Signature & Validation Workflow`, `Beneficiaire Enrolement Exceptions`, `BeneficiaireApi Public Interface`, `Grille Tarifaire Repository & Service`, `Utilisateur Email/Matricule Exceptions`, `Processus Motif/PieceJointe Exceptions`, `RetournerProcessus Request DTO`, `Beneficiaire NotFound/NonEligible Exceptions`, `BeneficiaireService & Tests`, `Enrolement Confirmation & EHR Integration`?**
  _High betweenness centrality (0.072) - this node is a cross-community bridge._
- **Why does `AuthenticatedUserService` connect `Beneficiaire Enrolement Exceptions` to `UtilisateurAdminController`, `Beneficiaire Grade & Ligne Etat Ajustement`, `Document Signature & Validation Workflow`, `Import Excel Exceptions`, `Processus Motif/PieceJointe Exceptions`, `Utilisateur Notification DTO`, `UtilisateurAdminService`, `GrilleTarifaireController Endpoints`, `Beneficiaire NotFound/NonEligible Exceptions`, `Beneficiaire Import Service & Tests`, `BeneficiaireService & Tests`, `FonctionEligibleController Endpoints`, `Enrolement Confirmation & EHR Integration`, `RoleEnum`?**
  _High betweenness centrality (0.071) - this node is a cross-community bridge._
- **Why does `ReportingService` connect `Beneficiaire & Processus Repository Counts` to `ProcessusEnCoursDto (Reporting)`, `LigneEtatMensuelRepository`, `StatutEnum & Reporting Historique DTO`, `ProcessusListItemDto`, `ReportingController`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **What connects `com.afriland:dottel`, `VALIDATION_ARH`, `VALIDATION_CRH` to the rest of the system?**
  _186 weakly-connected nodes found - possible documentation gaps or missing edges._