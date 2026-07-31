# Graph Report - afb-dottel  (2026-07-25)

## Corpus Check
- 198 files · ~97,614 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1454 nodes · 3752 edges · 147 communities (72 shown, 75 thin omitted)
- Extraction: 85% EXTRACTED · 15% INFERRED · 0% AMBIGUOUS · INFERRED: 579 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `02c0d5a7`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

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
- React + Vite
- graphify reference: GitHub clone and cross-repo merge
- graphify reference: transcribe video and audio
- CLAUDE.md
- extraction-spec.md
- Project .claude/CLAUDE.md (graphify trigger)
- Module DOTTEL (Dotations Téléphoniques Mensuelles)
- RG-01: Eligibilite via fonction_eligible.actif
- RG-02: Grade NON GRADE bloque corps de controle
- RG-03: Unicite matricule beneficiaire
- RG-04: Montant automatique via grille_tarifaire
- RG-05: Sequence stricte ARH->CRH->DRH
- RG-06: Un seul PDF par processus
- RG-07: Motif obligatoire pour retour
- RG-08: Separation des taches
- RG-09: Historisation via audit_log
- RG-10: Workflow validation grilles tarifaires
- RG-11: Import Excel avec rapport
- RG-12: Unicite processus mensuel (mois/annee)
- /graphify add <url>
- --watch folder watcher
- graphify export falkordb / falkordb-push
- MCP stdio server (graphify.serve)
- graphify export neo4j / neo4j-push
- graphify export wiki
- Confidence score rubric (0.55-0.95 discrete steps)
- Node ID format rule
- Extraction subagent prompt spec
- graphify clone <github-url>
- graphify merge-graphs (cross-repo/monorepo)
- graphify claude install (native CLAUDE.md integration)
- graphify hook install (post-commit auto-rebuild)
- BFS/DFS traversal modes
- /graphify path and /graphify explain
- save-result / reflect work-memory loop
- Constrained query vocabulary expansion (Step 0)
- Whisper transcription for video/audio
- --cluster-only reclustering
- --update incremental re-extraction
- Fast path (existing graph.json check)
- Shrink-guard on graph.json export
- Step 3: Structural + semantic extraction
- Step 4.5: Graph health check
- Step 4: Build graph, cluster, analyze
- Step 5: Label communities
- Groupe Grilles tarifaires /grilles-tarifaires (en cours, Sprint 4bis)
- RG-12 unicité processus mensuel (distincte de RG-03, corrige ambiguïté V3.0)
- Table audit_log
- Table beneficiaires
- Table etape_workflow
- Table fonction_eligible
- Table grille_tarifaire
- Table ligne_etat_mensuel (pivot N-N)
- Table piece_jointe
- Table processus_mensuel
- Table regle_eligibilite
- RoleEnum
- StatutEnum
- StatutGrilleEnum
- Table utilisateurs
- NotificationService (stub log, appelé par ProcessusMensuelService.valider)
- Entité PieceJointe (UNIQUE par processus, nombre_signatures)
- Point de vigilance RG-08 : contrat API mentionne la vérification sur /valider, mais SeparationTachesService est reporté au Sprint 5
- SignatureService (interface isolée, trace non-certifiée, remplaçable par intégration type INTRA)
- HistoriqueLigneDto
- ProcessusEnCoursDto
- ConfirmerEnrolementResponseDto

## God Nodes (most connected - your core abstractions)
1. `Utilisateur` - 83 edges
2. `ProcessusMensuel` - 74 edges
3. `ProcessusMensuelServiceTest` - 55 edges
4. `LigneEtatMensuel` - 39 edges
5. `BeneficiaireRepository` - 36 edges
6. `ProcessusMensuelService` - 36 edges
7. `FonctionEligible` - 34 edges
8. `Beneficiaire` - 33 edges
9. `GlobalExceptionHandler` - 33 edges
10. `FonctionEligibleRepository` - 32 edges

## Surprising Connections (you probably didn't know these)
- `Grille des 22 forfaits mensuels de dotation téléphonique (NS 69/17)` --conceptually_related_to--> `CLAUDE.md Section 5 - 25 fonctions éligibles (Note NS 69/17 + 3 corps assimilés)`  [INFERRED]
  backend/src/main/resources/images/Média.jpg → CLAUDE.md
- `PDF généré — Etat récapitulatif Novembre 2025 (ARH, CRH, DRH tous signés)` --conceptually_related_to--> `POST /processus/{id}/valider — branche ARH implémentée, CRH/DRH et RG-08 en attente`  [INFERRED]
  backend/documents/dotations-telephoniques-11-2025.pdf → docs/reference/contrats_api_dotations_v3.md
- `Note de Service NS 69/17 - Dotation Téléphonique Portable (scan)` --references--> `CLAUDE.md Section 5 - 25 fonctions éligibles (Note NS 69/17 + 3 corps assimilés)`  [EXTRACTED]
  backend/src/main/resources/static/images/Média.jpg → CLAUDE.md
- `PDF généré — Etat récapitulatif Novembre 2025 (ARH, CRH, DRH tous signés)` --references--> `DocumentService.genererInitiale() (iText 8 PDF, RG-06)`  [EXTRACTED]
  backend/documents/dotations-telephoniques-11-2025.pdf → sprint 3.4.md
- `PDF généré — Etat récapitulatif Janvier 2026 (ARH seul signé)` --references--> `DocumentService.genererInitiale() (iText 8 PDF, RG-06)`  [EXTRACTED]
  backend/documents/dotations-telephoniques-1-2026.pdf → sprint 3.4.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Kubernetes deployment pipeline for dottel-backend** — k8s_configmap_yaml_dottel_config, k8s_deployment_yaml_dottel_backend, k8s_service_yaml_dottel_backend_svc [EXTRACTED 1.00]
- **Secret/env var externalization from application.yml to K8s Secret/ConfigMap** — backend_dottel_jwt_secret_env, backend_db_connection_envs, k8s_deployment_yaml_dottel_secret, k8s_configmap_yaml_dottel_config [INFERRED 0.85]
- **Progression des signatures ARH->CRH->DRH observée entre les deux PDF générés** — backend_documents_dotations_telephoniques_1_2026, backend_documents_dotations_telephoniques_11_2025, sprint_3_4_etapeworkflow [INFERRED 0.80]
- **États mensuels de dotation téléphonique générés par DocumentService** — backend_documents_dotations_telephoniques_11_2026, backend_documents_dotations_telephoniques_12_2025, backend_documents_dotations_telephoniques_12_2026, backend_documents_dotations_telephoniques_2_2026, backend_documents_dotations_telephoniques_3_2026, backend_documents_dotations_telephoniques_3_2027, backend_documents_dotations_telephoniques_4_2026, backend_documents_dotations_telephoniques_4_2027, backend_documents_dotations_telephoniques_5_2026, backend_documents_dotations_telephoniques_6_2026, backend_documents_dotations_telephoniques_8_2026 [EXTRACTED 1.00]
- **Workflow séquentiel de validation ARH puis CRH puis DRH (RG-05)** — actor_jean_paul_mbarga, actor_marie_claire_essama, actor_paul_atangana, backend_documents_dotations_telephoniques_12_2026 [EXTRACTED 1.00]
- **Grille tarifaire des forfaits téléphoniques fondée sur la Note de Service NS 69/17** — backend_src_main_resources_images_media_ns_69_17, fonction_gestionnaire_fonds_commerce, fonction_directeur_agence, fonction_conseiller_charge_mission [EXTRACTED 1.00]

## Communities (147 total, 75 thin omitted)

### Community 0 - "Beneficiaireservice Services"
Cohesion: 0.08
Nodes (26): DeclencherProcessusRequestDto, Getter, Setter, Beneficiaire, AllArgsConstructor, Builder, Entity, Getter (+18 more)

### Community 2 - "Beneficiaireservicetest Tests"
Cohesion: 0.34
Nodes (5): ConfirmerEnrolementRequestDto, Getter, Setter, EnrolementServiceTest, Test

### Community 3 - "Beneficiaire DTOs"
Cohesion: 0.06
Nodes (50): BeneficiaireController, GetMapping, MultipartFile, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor (+42 more)

### Community 4 - "Ligneetatmensuel Tests"
Cohesion: 0.16
Nodes (17): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, LigneEtatMensuel (+9 more)

### Community 5 - "Processus DTOs"
Cohesion: 0.06
Nodes (48): GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+40 more)

### Community 6 - "Authservicetest Tests"
Cohesion: 0.18
Nodes (13): Getter, Setter, LoginRequestDto, UtilisateurRepository, AuthService, PasswordEncoder, RequiredArgsConstructor, Service (+5 more)

### Community 7 - "Etapeworkflow Tests"
Cohesion: 0.10
Nodes (25): EtapeWorkflowIntrouvableException, SeparationTachesViolationException, EtapeWorkflow, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor (+17 more)

### Community 8 - "Frontend Package"
Cohesion: 0.04
Nodes (48): axios, date-fns, dependencies, axios, date-fns, @hookform/resolvers, jwt-decode, react (+40 more)

### Community 9 - "Auditserviceimpl Tests"
Cohesion: 0.07
Nodes (32): AfterEach, AuditLogResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, AuditLog (+24 more)

### Community 10 - "Claude Docs"
Cohesion: 0.13
Nodes (15): PieceJointeIntrouvableException, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+7 more)

### Community 11 - "Kafkaconfig DTOs"
Cohesion: 0.13
Nodes (20): EvenementClotureSerializer, ObjectMapper, Override, Bean, Configuration, KafkaTemplate, ObjectMapper, KafkaConfig (+12 more)

### Community 12 - "Claude Docs"
Cohesion: 0.27
Nodes (6): AjustementLigneEtatDto, Getter, Setter, Getter, Setter, PatchProcessusRequestDto

### Community 13 - "Frontend Frontend"
Cohesion: 0.07
Nodes (22): plugins, rules, react/only-export-components, react/rules-of-hooks, $schema, App(), AuthContext, useAuth() (+14 more)

### Community 14 - "Securityconfig Tests"
Cohesion: 0.14
Nodes (17): Component, Override, RoleJwtAuthenticationConverter, Bean, Configuration, PasswordEncoder, SecurityConfig, Test (+9 more)

### Community 15 - "Telephoniques PDF Documents"
Cohesion: 0.14
Nodes (26): Jean Paul MBARGA (ARH, matricule 1847), Marie Claire ESSAMA (CRH, matricule 2093), Paul ATANGANA (DRH, matricule 1562), Sylvie NKOLO (ARH, matricule 2201), État Récapitulatif Dotations Téléphoniques - Novembre 2026, État Récapitulatif Dotations Téléphoniques - Décembre 2025, État Récapitulatif Dotations Téléphoniques - Décembre 2026, État Récapitulatif Dotations Téléphoniques - Février 2026 (+18 more)

### Community 16 - "Utilisateur Services"
Cohesion: 0.12
Nodes (23): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, Utilisateur (+15 more)

### Community 17 - "Sprint Docs"
Cohesion: 0.40
Nodes (6): PDF généré — Etat récapitulatif Novembre 2025 (ARH, CRH, DRH tous signés), PDF généré — Etat récapitulatif Janvier 2026 (ARH seul signé), POST /processus/{id}/valider — branche ARH implémentée, CRH/DRH et RG-08 en attente, Champ CHAPITRE (donnée EHR par bénéficiaire, décision révisée depuis 'valeur fixe 64310000' vers donnée variable + repli configurable), DocumentService.genererInitiale() (iText 8 PDF, RG-06), Entité EtapeWorkflow (NomEtapeEnum, StatutEtapeEnum, signature_numerique)

### Community 18 - "Enrolement Controllers"
Cohesion: 0.22
Nodes (13): EnrolementController, GetMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+5 more)

### Community 19 - "Utilisateuradminservicetest Tests"
Cohesion: 0.17
Nodes (9): ProcessusMensuelIntrouvableException, ProcessusMensuelNonModifiableException, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, RetournerProcessusRequestDto (+1 more)

### Community 20 - "Utilisateuradminservice Services"
Cohesion: 0.14
Nodes (10): ActionAdminNonAutoriseeException, UtilisateurIntrouvableException, AuditService, PasswordEncoder, RequiredArgsConstructor, Service, Transactional, UtilisateurAdminService (+2 more)

### Community 22 - "Globalexceptionhandlertest Exceptions"
Cohesion: 0.08
Nodes (16): AccessDeniedException, FichierImportInvalideException, IdentifiantsInvalidesException, ProcessusMensuelExisteDejaException, UtilisateurInactifException, GlobalExceptionHandler, Logger, MethodArgumentNotValidException (+8 more)

### Community 23 - "Yaml Config"
Cohesion: 0.16
Nodes (15): afb_dotations_telephoniques database, CORS allowed-origins localhost:3000, DB_URL/DB_USER/DB_PASSWORD env vars, DOTTEL_JWT_SECRET / JWT_SECRET env var, CLAUDE.md section 14 (déploiement/infrastructure), index.html (frontend), k8s configmap.yaml (dottel-config), ConfigMap dottel-config (+7 more)

### Community 24 - "Ehrintegrationservicestub Services"
Cohesion: 0.22
Nodes (10): EmployeEhrDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, EhrIntegrationService, EhrIntegrationServiceStub, Override (+2 more)

### Community 25 - "Grilletarifairecontroller Controllers"
Cohesion: 0.31
Nodes (9): GrilleTarifaireController, GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity (+1 more)

### Community 26 - "Utilisateuradmincontroller Controllers"
Cohesion: 0.14
Nodes (21): GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+13 more)

### Community 27 - "Utilisateur DTOs"
Cohesion: 0.33
Nodes (6): CreerUtilisateurRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 28 - "Exception Exceptions"
Cohesion: 0.09
Nodes (13): DecisionGrilleInvalideException, FonctionEligibleIntrouvableException, GrilleEnAttenteDrhExistanteException, GrilleIntrouvableException, GrilleNonEnAttenteDrhException, GrilleNonModifiableException, MotifRejetObligatoireException, GrilleTarifaireService (+5 more)

### Community 29 - "Grille DTOs"
Cohesion: 0.20
Nodes (14): GrilleTarifaire, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+6 more)

### Community 30 - "Grille Services"
Cohesion: 0.17
Nodes (10): StatutEnum, CLOTURE, EN_ATTENTE_CRH, EN_ATTENTE_DRH, EN_COURS_ARH, RETOURNE, RequiredArgsConstructor, Service (+2 more)

### Community 31 - "Grille DTOs"
Cohesion: 0.32
Nodes (4): BeforeEach, ExtendWith, Test, ReportingServiceTest

### Community 32 - "Exception Exceptions"
Cohesion: 0.15
Nodes (6): EmailUtilisateurDejaUtiliseException, MatriculeUtilisateurDejaUtiliseException, RoleInvalideException, BeforeEach, ExtendWith, PasswordEncoder

### Community 33 - "Authenticateduserservice Exceptions"
Cohesion: 0.38
Nodes (4): UtilisateurConnecteIntrouvableException, AuthenticatedUserService, RequiredArgsConstructor, Service

### Community 34 - "Exception Exceptions"
Cohesion: 0.24
Nodes (11): GetMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, ReportingController, HistoriqueExportService (+3 more)

### Community 35 - "Grille DTOs"
Cohesion: 0.11
Nodes (15): GrilleTarifaireIntrouvableException, MatriculeDejaEnroleException, MatriculeInconnuException, NonEligibleException, FonctionEligibleRepository, EnrolementService, RequiredArgsConstructor, Service (+7 more)

### Community 36 - "Grilletarifaire Entities"
Cohesion: 0.25
Nodes (12): BeneficiaireExcluDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, AllArgsConstructor, Builder (+4 more)

### Community 37 - "Corsconfig Addcorsmappings"
Cohesion: 0.43
Nodes (5): CorsConfig, Configuration, Override, CorsRegistry, WebMvcConfigurer

### Community 38 - "Grille DTOs"
Cohesion: 0.24
Nodes (10): HistoriqueResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, HistoriqueExportServiceTest, BeforeEach (+2 more)

### Community 39 - "Grille DTOs"
Cohesion: 0.16
Nodes (13): trim(), BeneficiaireImportService, MultipartFile, RequiredArgsConstructor, Row, Service, Transactional, BeneficiaireImportServiceTest (+5 more)

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
Cohesion: 0.31
Nodes (8): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, ProcessusMensuel

### Community 44 - "Svg Public"
Cohesion: 0.29
Nodes (7): icons.svg (Icon Sprite Sheet), Bluesky Icon Symbol, Discord Icon Symbol, Documentation Icon Symbol, GitHub Icon Symbol, Social/Users Icon Symbol, X (Twitter) Icon Symbol

### Community 45 - "Oxlintrc Frontend"
Cohesion: 0.39
Nodes (6): mvnw script, clean(), die(), exec_maven(), set_java_home(), verbose()

### Community 46 - "Utils Frontend"
Cohesion: 0.40
Nodes (3): formatDate(), formatDateHeure(), MOIS_LABELS

### Community 47 - "Exception Exceptions"
Cohesion: 0.52
Nodes (6): CreerGrilleTarifaireRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 48 - "Dottelapplicationtests Contextloads"
Cohesion: 0.60
Nodes (3): DottelApplicationTests, Test, SpringBootTest

### Community 49 - "Utils Frontend"
Cohesion: 0.40
Nodes (4): ROLES, STATUTS, STATUTS_GRILLE, STATUTS_LABELS

### Community 51 - "Exception Exceptions"
Cohesion: 0.52
Nodes (6): DecisionGrilleTarifaireRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 52 - "Exception Exceptions"
Cohesion: 0.24
Nodes (7): Component, JwtUtil, BeforeEach, Test, JwtUtilTest, MacAlgorithm, SecretKey

### Community 53 - "Exception Exceptions"
Cohesion: 0.36
Nodes (8): GetMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, PieceJointeController, Resource

### Community 54 - "Exception Exceptions"
Cohesion: 0.26
Nodes (10): AuthController, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, Builder, Getter (+2 more)

### Community 55 - "Exception Exceptions"
Cohesion: 0.22
Nodes (11): FonctionEligible, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+3 more)

### Community 56 - "Exception Exceptions"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 57 - "Exception Exceptions"
Cohesion: 0.47
Nodes (4): Override, Service, NotificationServiceStub, Slf4j

### Community 58 - "Exception Exceptions"
Cohesion: 0.52
Nodes (6): GrilleHistoriqueLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 59 - "Exception Exceptions"
Cohesion: 0.52
Nodes (6): GrilleTarifaireResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 60 - "Exception Exceptions"
Cohesion: 0.52
Nodes (6): HistoriqueGrilleTarifaireResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 61 - "Exception Exceptions"
Cohesion: 0.50
Nodes (5): application.yml (base Spring config: JPA, Flyway, Kafka, JWT, dottel.* properties), application-prod.yml (prod datasource/kafka/logging config), Propriétés dottel.documents.chemin-stockage et dottel.documents.chapitre-defaut, docker-compose.yml (dev Kafka service), Kafka service definition (dottel-kafka, KRaft mode, port 9092)

### Community 62 - "Exception Exceptions"
Cohesion: 0.50
Nodes (3): For /graphify add, For --watch, graphify reference: add a URL and watch a folder

### Community 63 - "Exception Exceptions"
Cohesion: 0.50
Nodes (3): For git commit hook, For native CLAUDE.md integration, graphify reference: commit hook and native CLAUDE.md integration

### Community 64 - "Exception Exceptions"
Cohesion: 0.50
Nodes (3): For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only

### Community 65 - "First Images"
Cohesion: 0.67
Nodes (3): Logo Afriland First Bank, Charte visuelle frontend (Section 15, CLAUDE.md), Afriland First Bank (organisation / marque)

### Community 66 - "Frontend Frontend"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ModifierGrilleTarifaireRequestDto

### Community 70 - "Help Docs"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, RetournerProcessusResponseDto

### Community 80 - "React + Vite"
Cohesion: 0.50
Nodes (3): Expanding the Oxlint configuration, React Compiler, React + Vite

### Community 83 - "CLAUDE.md"
Cohesion: 0.39
Nodes (7): AuditLogPageResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Page, Setter

### Community 84 - "extraction-spec.md"
Cohesion: 0.52
Nodes (6): DashboardResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 144 - "HistoriqueLigneDto"
Cohesion: 0.52
Nodes (6): HistoriqueLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 145 - "ProcessusEnCoursDto"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusEnCoursDto

### Community 146 - "ConfirmerEnrolementResponseDto"
Cohesion: 0.60
Nodes (5): EnrolementVerificationResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor

## Ambiguous Edges - Review These
- `ATANGANA Sylvie (bénéficiaire, compte 10014275003, agence GRA-DR01)` → `Sylvie NKOLO (ARH, matricule 2201)`  [AMBIGUOUS]
  backend/documents/dotations-telephoniques-8-2026.pdf · relation: semantically_similar_to

## Knowledge Gaps
- **143 isolated node(s):** `com.afriland:dottel`, `VALIDATION_ARH`, `VALIDATION_CRH`, `VALIDATION_DRH`, `EMPLOYE` (+138 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **75 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `ATANGANA Sylvie (bénéficiaire, compte 10014275003, agence GRA-DR01)` and `Sylvie NKOLO (ARH, matricule 2201)`?**
  _Edge tagged AMBIGUOUS (relation: semantically_similar_to) - confidence is low._
- **Why does `Utilisateur` connect `Utilisateur Services` to `Beneficiaireservice Services`, `Processusmensuelservicetest Tests`, `Authenticateduserservice Exceptions`, `Grille DTOs`, `Beneficiaire DTOs`, `Beneficiaireservicetest Tests`, `Authservicetest Tests`, `Grille DTOs`, `Exception Exceptions`, `Claude Docs`, `Claude Docs`, `Exception Exceptions`, `Utilisateuradminservice Services`, `Exception Exceptions`, `Utilisateuradmincontroller Controllers`, `Utilisateur DTOs`?**
  _High betweenness centrality (0.101) - this node is a cross-community bridge._
- **Why does `AuditService` connect `Utilisateuradminservice Services` to `Processusmensuelservicetest Tests`, `Exception Exceptions`, `Beneficiaire DTOs`, `Grille DTOs`, `Beneficiaireservicetest Tests`, `Authservicetest Tests`, `Grille DTOs`, `Auditserviceimpl Tests`, `Claude Docs`, `Grilletarifaireservicetest Tests`, `Exception Exceptions`?**
  _High betweenness centrality (0.066) - this node is a cross-community bridge._
- **Why does `BeneficiaireRepository` connect `Beneficiaireservice Services` to `Processusmensuelservicetest Tests`, `Beneficiaireservicetest Tests`, `Beneficiaire DTOs`, `Grille DTOs`, `Grille DTOs`, `Claude Docs`, `Utilisateur Services`, `Grille Services`, `Grille DTOs`?**
  _High betweenness centrality (0.049) - this node is a cross-community bridge._
- **What connects `com.afriland:dottel`, `VALIDATION_ARH`, `VALIDATION_CRH` to the rest of the system?**
  _143 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Beneficiaireservice Services` be split into smaller, more focused modules?**
  _Cohesion score 0.08489795918367347 - nodes in this community are weakly interconnected._
- **Should `Beneficiaire DTOs` be split into smaller, more focused modules?**
  _Cohesion score 0.05854049719326383 - nodes in this community are weakly interconnected._