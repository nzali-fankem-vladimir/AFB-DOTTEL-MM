# Graph Report - .  (2026-08-03)

## Corpus Check
- 334 files · ~154,309 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2112 nodes · 6070 edges · 172 communities (125 shown, 47 thin omitted)
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 837 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Frontend
- Referentiel
- Processus
- Audit
- Afb Dottel Mm
- Reference
- Processus
- Processus
- Utilisateurs
- Utilisateurs
- Monolithe Modulaire
- Referentiel
- Referentiel
- Utilisateurs
- Beneficiaires
- Referentiel
- Beneficiaires
- Processus
- References
- Config
- Utilisateurs
- Security
- Processus
- Reporting
- Beneficiaires
- Monolithe Modulaire
- Beneficiaires
- Processus
- Utilisateurs
- Referentiel
- Security
- Beneficiaires
- Referentiel
- Beneficiaires
- Utilisateurs
- Reporting
- Processus
- Reporting
- Beneficiaires
- Beneficiaires
- Beneficiaires
- Beneficiaires
- Security
- Beneficiaires
- Reporting
- Processus
- Processus
- Security
- Frontend
- Frontend
- Beneficiaires
- Community 51
- Beneficiaires
- Reporting
- Beneficiaires
- Referentiel
- Processus
- Beneficiaires
- Reference
- Backend
- Processus
- Processus
- Reporting
- Monolithe Modulaire
- Frontend
- Referentiel
- Referentiel
- Referentiel
- Frontend
- Frontend
- Processus
- Frontend
- Afb Dottel Mm
- Config
- Processus
- Processus
- Processus
- Processus
- Processus
- Processus
- Processus
- Processus
- Processus
- Processus
- Referentiel
- Utilisateurs
- Utilisateurs
- Frontend
- K8S
- Processus
- Afb Dottel Mm
- Monolithe Modulaire
- Frontend
- Modularitytests.Java
- Utilisateurs
- Dottelapplicationtests.Java
- Reference
- Beneficiaires
- Dottelapplication.Java
- Processus
- Processus
- Processus
- Processus
- Processus
- Referentiel
- Referentiel
- Referentiel
- Referentiel
- Referentiel
- Referentiel
- Referentiel
- Utilisateurs
- Monolithe Modulaire
- Monolithe Modulaire
- Reference
- Frontend
- Frontend
- Images
- Afb Dottel Mm
- Frontend
- Afb Dottel Mm
- Monolithe Modulaire
- Monolithe Modulaire
- Reference
- Reference
- Frontend
- Frontend
- Frontend
- Frontend
- Frontend
- Frontend
- Frontend
- Frontend
- Frontend
- Frontend
- Backend
- Backend
- Backend
- Backend
- Images
- Images
- Afb Dottel Mm
- References
- Monolithe Modulaire
- Reference
- Frontend
- Backend

## God Nodes (most connected - your core abstractions)
1. `Utilisateur` - 113 edges
2. `ProcessusMensuel` - 88 edges
3. `ProcessusMensuelServiceTest` - 86 edges
4. `LigneEtatMensuel` - 60 edges
5. `EvenementAudit` - 49 edges
6. `GlobalExceptionHandler` - 39 edges
7. `Beneficiaire` - 38 edges
8. `StatutEnum` - 38 edges
9. `ProcessusMensuelService` - 38 edges
10. `FonctionEligible` - 38 edges

## Surprising Connections (you probably didn't know these)
- `Matrice rôles/endpoints (34 endpoints, 9 groupes)` --semantically_similar_to--> `Contrat API AFB_API_DOTTEL_V3.3_2026`  [INFERRED] [semantically similar]
  CLAUDE.md → docs/reference/contrats_api_dotations_v3.md
- `Sécurité stateless JWT / Keycloak, aucune session HTTP serveur` --semantically_similar_to--> `JWT HS384, validité 8 heures, header Authorization Bearer`  [INFERRED] [semantically similar]
  CLAUDE.md → docs/reference/contrats_api_dotations_v3.md
- `Interface AuthProvider — abstraction remplaçable par Keycloak reel` --semantically_similar_to--> `dottel.documents.chapitre-defaut (repli chapitre EHR)`  [INFERRED] [semantically similar]
  sprint6F_6F_3.md → backend/src/main/resources/application.yml
- `Ecart E7c — fichiers .env non charges par Vite, build HTTP clair` --semantically_similar_to--> `APP_CORS_ALLOWED_ORIGINS (origine frontend production)`  [INFERRED] [semantically similar]
  docs/audit_securite_owasp_v1.md → k8s/configmap.yaml
- `afb-dotations-telephoniques (README)` --conceptually_related_to--> `Module DOTTEL — Dotations Téléphoniques Mensuelles`  [INFERRED]
  README.md → CLAUDE.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Graph export targets (wiki, neo4j, falkordb, mcp, benchmark)** — claude_skills_graphify_references_exports_wiki, claude_skills_graphify_references_exports_neo4j, claude_skills_graphify_references_exports_falkordb, claude_skills_graphify_references_exports_mcp, claude_skills_graphify_references_exports_benchmark [EXTRACTED 1.00]
- **Graph query/traversal family (query, path, explain)** — claude_skills_graphify_references_query_vocab_expansion, claude_skills_graphify_references_query_bfs_dfs, claude_skills_graphify_references_query_path_explain, claude_skills_graphify_references_query_save_result_reflect [INFERRED 0.85]
- **Chaine CORS de production : ConfigMap, propriete Spring, audit E8** — k8s_configmap_app_cors_allowed_origins, backend_src_main_resources_application_cors_allowed_origins, docs_audit_securite_owasp_v1_ecart_e8_cors_externalise, docs_audit_securite_owasp_v1_ecart_e7c_https_build [EXTRACTED 1.00]
- **Externalisation des secrets : aucun repli committe, echec au demarrage** — backend_src_main_resources_application_jwt_secret, docs_audit_securite_owasp_v1_ecart_e7b_jwt_secret_prod, backend_src_main_resources_application_dev_db_password_repli, k8s_configmap [EXTRACTED 1.00]
- **Unification de la résolution de grille tarifaire (RG-04) à travers MM.2/MM.3** — docs_monolithe_modulaire_mm_2_refactor_processus_mensuel_service_resoudregrillepourfonction, docs_monolithe_modulaire_mm_3_casser_couplages_restants_c2, docs_monolithe_modulaire_mm_3_casser_couplages_restants_beneficiaireservice, docs_monolithe_modulaire_mm_3_casser_couplages_restants_enrolementservice, docs_monolithe_modulaire_mm_0_cadrage_module_referentiel [EXTRACTED 0.90]
- **Découplage de l'audit par événements applicatifs (MM.4)** — docs_monolithe_modulaire_mm_4_audit_evenementiel_auditservice, docs_monolithe_modulaire_mm_4_audit_evenementiel_auditserviceimpl, docs_monolithe_modulaire_mm_4_audit_evenementiel_mode_transactionnel_options, docs_monolithe_modulaire_mm_4_audit_evenementiel_conception_evenements, docs_monolithe_modulaire_mm_4_audit_evenementiel_piege_adresse_ip [EXTRACTED 0.90]
- **Ensemble des décisions arbitrées pour le Keycloak provisoire (MM.7)** — docs_monolithe_modulaire_mm_7_keycloak_provisoire_decision_p2, docs_monolithe_modulaire_mm_7_keycloak_provisoire_decision_e3, docs_monolithe_modulaire_mm_7_keycloak_provisoire_decision_f2, docs_monolithe_modulaire_mm_7_keycloak_provisoire_decision_i2 [EXTRACTED 0.90]
- **Chaîne de validation ARH → CRH → DRH d'un processus mensuel** — docs_reference_contrats_api_dotations_v3_post_processus_valider, docs_reference_contrats_api_dotations_v3_post_processus_retourner, claude_rg05_sequence_arh_crh_drh, claude_rg08_separation_taches, claude_rg06_pdf_unique [EXTRACTED 1.00]
- **Cycle de vie complet d'une grille tarifaire (création, validation, historique, désactivation)** — docs_reference_contrats_api_dotations_v3_post_grilles_tarifaires, docs_reference_contrats_api_dotations_v3_patch_grille_tarifaire, docs_reference_contrats_api_dotations_v3_post_grille_valider, docs_reference_contrats_api_dotations_v3_post_grille_desactiver, docs_reference_contrats_api_dotations_v3_get_grilles_par_fonction, claude_rg10_workflow_grille [EXTRACTED 1.00]
- **Gestion applicative du référentiel fonction_eligible (Sprint 6F.7bis)** — docs_reference_contrats_api_dotations_v3_get_fonctions_eligibles, docs_reference_contrats_api_dotations_v3_get_fonctions_eligibles_toutes, docs_reference_contrats_api_dotations_v3_post_fonctions_eligibles, docs_reference_contrats_api_dotations_v3_patch_fonction_eligible, docs_reference_contrats_api_dotations_v3_patch_fonction_desactiver, docs_reference_contrats_api_dotations_v3_patch_fonction_reactiver [EXTRACTED 1.00]
- **grille_tarifaire table, StatutGrilleEnum, RG-04, RG-10 and the grilles-tarifaires API group form the tariff-grid validation lifecycle** — docs_reference_dictionnaire_de_donnees_dotations_v3_grille_tarifaire, docs_reference_dictionnaire_de_donnees_dotations_v3_statutgrilleenum [INFERRED 0.85]
- **Kubernetes deployment pipeline for dottel-backend** — k8s_deployment_yaml_dottel_backend, k8s_service_yaml_dottel_backend_svc [EXTRACTED 1.00]
- **Secret/env var externalization from application.yml to K8s Secret/ConfigMap** — k8s_deployment_yaml_dottel_secret [INFERRED 0.85]
- **RG-08 séparation des tâches : écart entre contrat API et périmètre Sprint 3.4** — sprint_3_4, sprint_3_4_rg08_separation_taches [EXTRACTED 0.90]
- **Flux de génération du PDF initial (DocumentService + PieceJointe + SignatureService + CHAPITRE)** — sprint_3_4_documentservice, sprint_3_4_piecejointe, sprint_3_4_signatureservice, sprint_3_4_chapitre_field [EXTRACTED 0.90]
- **Flux d'authentification frontend (6F.3) et son verdict d'audit** — sprint6f_6f_3_authprovider_abstraction, sprint6f_6f_3_authproviderlocal, sprint6f_6f_3_authcontext, sprint6f_6f_3_apiclient_intercepteur_jwt, sprint6f_6f_3_protectedroute, docs_audit_securite_owasp_v1_stockage_token_memoire [EXTRACTED 1.00]
- **Gouvernance du cycle beneficiaires<->referentiel : isolation MM.5, correctif obligatoire MM.8** — docs_monolithe_modulaire_mm_6_verification_et_documentation_cycle_beneficiaires_referentiel_c3, docs_monolithe_modulaire_mm_6_verification_et_documentation_violations_filter, docs_monolithe_modulaire_mm_6_verification_et_documentation_mm5_sprint, docs_monolithe_modulaire_mm_6_verification_et_documentation_mm8_sprint [EXTRACTED 0.90]
- **Génération de la documentation d'architecture Spring Modulith** — docs_monolithe_modulaire_mm_6_verification_et_documentation_documentationtests, docs_monolithe_modulaire_mm_6_verification_et_documentation_documenter, docs_monolithe_modulaire_mm_6_verification_et_documentation_modularitytests, docs_monolithe_modulaire_mm_6_verification_et_documentation_architecture_docs_dir [EXTRACTED 0.90]

## Communities (172 total, 47 thin omitted)

### Community 0 - "Frontend"
Cohesion: 0.05
Nodes (112): apiClient, setAuthToken(), App(), AuthProvider, AuthProviderLocal, AppLayout(), PageHeader(), initiales() (+104 more)

### Community 1 - "Referentiel"
Cohesion: 0.06
Nodes (48): BeneficiaireApi, FonctionEligibleController, GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor (+40 more)

### Community 2 - "Processus"
Cohesion: 0.10
Nodes (14): PieceJointeIntrouvableException, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+6 more)

### Community 3 - "Audit"
Cohesion: 0.07
Nodes (34): AfterEach, AuditLog, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter (+26 more)

### Community 4 - "Afb Dottel Mm"
Cohesion: 0.06
Nodes (48): application.yml (configuration de base Spring Boot), Actuator health show-details always (sondes Kubernetes), app.cors.allowed-origins (jamais de wildcard), application-dev.yml (profil developpement), Repli en clair DB_PASSWORD:postgres (profil dev), dottel.documents.chapitre-defaut (repli chapitre EHR), dottel.documents.chemin-stockage (stockage local du PDF), Configuration Flyway (classpath:db/migration) (+40 more)

### Community 5 - "Reference"
Cohesion: 0.05
Nodes (47): Données de test camerounaises réalistes obligatoires, Jamais d'entité JPA dans une réponse API — DTO obligatoire, fonction_eligible — référentiel des 25 fonctions, grille_tarifaire — source unique du montant, LigneEtatMensuel — table pivot N-N obligatoire, Modèle de données — 10 tables, Module DOTTEL — Dotations Téléphoniques Mensuelles, RG-01 — éligibilité par fonction_eligible.actif (+39 more)

### Community 6 - "Processus"
Cohesion: 0.22
Nodes (6): AjustementLigneEtatDto, Getter, Setter, Getter, Setter, PatchProcessusRequestDto

### Community 7 - "Processus"
Cohesion: 0.12
Nodes (27): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, LigneDocumentDto, AllArgsConstructor, Builder (+19 more)

### Community 8 - "Utilisateurs"
Cohesion: 0.12
Nodes (25): EvenementAudit, AuthController, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, Getter (+17 more)

### Community 9 - "Utilisateurs"
Cohesion: 0.13
Nodes (13): ActionAdminNonAutoriseeException, ApplicationEventPublisher, PasswordEncoder, RequiredArgsConstructor, Service, Transactional, UtilisateurAdminService, ApplicationEventPublisher (+5 more)

### Community 10 - "Monolithe Modulaire"
Cohesion: 0.05
Nodes (39): Décision A - EligibiliteService (A1), Décision B - Référentiel unifié (B1), Décision C - Découpage 6 modules, EligibiliteService, Module audit, Module beneficiaires, Module integration (proposé puis rejeté, réparti), Module processus (+31 more)

### Community 11 - "Referentiel"
Cohesion: 0.20
Nodes (3): Transactional, GrilleTarifaireServiceTest, Test

### Community 12 - "Referentiel"
Cohesion: 0.13
Nodes (18): FonctionEligible, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+10 more)

### Community 13 - "Utilisateurs"
Cohesion: 0.12
Nodes (17): DestinataireNotificationDto, UtilisateurApi, UtilisateurIntrouvableException, RoleEnum, ADMIN, ARH, CRH, DRH (+9 more)

### Community 14 - "Beneficiaires"
Cohesion: 0.13
Nodes (10): BeneficiaireDocumentDto, BeneficiaireDotationDto, BeneficiaireIdentiteDto, BeneficiaireApiImpl, Override, RequiredArgsConstructor, Service, BeneficiaireApiImplTest (+2 more)

### Community 15 - "Referentiel"
Cohesion: 0.15
Nodes (21): GrilleTarifaireController, GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity (+13 more)

### Community 16 - "Beneficiaires"
Cohesion: 0.19
Nodes (4): BeneficiaireIntrouvableException, Transactional, BeneficiaireServiceTest, Test

### Community 17 - "Processus"
Cohesion: 0.13
Nodes (22): EtapeWorkflow, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+14 more)

### Community 18 - "References"
Cohesion: 0.08
Nodes (29): Project .claude/CLAUDE.md (graphify trigger), /graphify add <url>, --watch folder watcher, graphify export falkordb / falkordb-push, MCP stdio server (graphify.serve), graphify export neo4j / neo4j-push, graphify export wiki, Confidence score rubric (0.55-0.95 discrete steps) (+21 more)

### Community 19 - "Config"
Cohesion: 0.13
Nodes (20): EvenementClotureSerializer, ObjectMapper, Override, Bean, Configuration, KafkaTemplate, ObjectMapper, KafkaConfig (+12 more)

### Community 20 - "Utilisateurs"
Cohesion: 0.14
Nodes (21): GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+13 more)

### Community 21 - "Security"
Cohesion: 0.14
Nodes (17): Component, Override, RoleJwtAuthenticationConverter, Bean, Configuration, EnableMethodSecurity, PasswordEncoder, SecurityConfig (+9 more)

### Community 22 - "Processus"
Cohesion: 0.18
Nodes (15): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, LigneEtatMensuel (+7 more)

### Community 23 - "Reporting"
Cohesion: 0.22
Nodes (8): LigneEtatMensuelRepository, ProcessusMensuelRepository, RequiredArgsConstructor, Service, Transactional, ReportingService, Test, ReportingServiceTest

### Community 24 - "Beneficiaires"
Cohesion: 0.21
Nodes (8): FonctionEligibleApi, BeneficiaireImportServiceTest, ApplicationEventPublisher, ExtendWith, Row, Test, InvocationOnMock, MockMultipartFile

### Community 25 - "Monolithe Modulaire"
Cohesion: 0.09
Nodes (23): @ApplicationModule(allowedDependencies = {...}), docs/monolithe-modulaire/architecture/ (diagrammes PlantUML et canvases versionnés), docs/audit_securite_owasp_v1.md, AuditService, CLAUDE.md, Cycle beneficiaires <-> referentiel (couplage C3), DocumentationTests.java, Documenter (org.springframework.modulith.docs) (+15 more)

### Community 26 - "Beneficiaires"
Cohesion: 0.32
Nodes (5): ConfirmerEnrolementRequestDto, Getter, Setter, EnrolementServiceTest, Test

### Community 27 - "Processus"
Cohesion: 0.22
Nodes (12): GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+4 more)

### Community 28 - "Utilisateurs"
Cohesion: 0.19
Nodes (14): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, UtilisateurResponseDto, EnableMethodSecurity, Test (+6 more)

### Community 29 - "Referentiel"
Cohesion: 0.17
Nodes (11): GrilleIntrouvableException, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ModifierGrilleTarifaireRequestDto, GrilleTarifaireService (+3 more)

### Community 30 - "Security"
Cohesion: 0.17
Nodes (8): AccessDeniedException, MethodArgumentNotValidException, IdentifiantsInvalidesException, GlobalExceptionHandlerTest, ExtendWith, MethodArgumentNotValidException, Test, BindingResult

### Community 31 - "Beneficiaires"
Cohesion: 0.20
Nodes (9): BeneficiaireRepository, BeneficiaireExportService, RequiredArgsConstructor, Service, Transactional, BeneficiaireExportServiceTest, BeforeEach, ExtendWith (+1 more)

### Community 32 - "Referentiel"
Cohesion: 0.18
Nodes (17): GrilleHistoriqueLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, GrilleTarifaireListeLigneDto, AllArgsConstructor (+9 more)

### Community 33 - "Beneficiaires"
Cohesion: 0.25
Nodes (11): BeneficiaireController, GetMapping, MultipartFile, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor (+3 more)

### Community 34 - "Utilisateurs"
Cohesion: 0.17
Nodes (12): BeneficiaireImportService, ApplicationEventPublisher, MultipartFile, RequiredArgsConstructor, Row, Service, Transactional, AuthenticatedUserService (+4 more)

### Community 35 - "Reporting"
Cohesion: 0.26
Nodes (10): AuditService, Page, Pageable, GetMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity (+2 more)

### Community 36 - "Processus"
Cohesion: 0.32
Nodes (6): EtapeWorkflowRepository, RequiredArgsConstructor, Service, SeparationTachesService, Test, SeparationTachesServiceTest

### Community 37 - "Reporting"
Cohesion: 0.23
Nodes (13): AuditLogResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, AuditLogPageResponseDto, AllArgsConstructor (+5 more)

### Community 38 - "Beneficiaires"
Cohesion: 0.16
Nodes (8): MatriculeDejaEnroleException, MatriculeInconnuException, EnrolementService, ApplicationEventPublisher, RequiredArgsConstructor, Service, Transactional, GrilleTarifaireApi

### Community 39 - "Beneficiaires"
Cohesion: 0.18
Nodes (10): NonEligibleException, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ModifierBeneficiaireRequestDto, ResolutionGrilleDto (+2 more)

### Community 40 - "Beneficiaires"
Cohesion: 0.23
Nodes (13): BeneficiairePageResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Page, Setter, BeneficiaireResponseDto (+5 more)

### Community 41 - "Beneficiaires"
Cohesion: 0.22
Nodes (10): EmployeEhrDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, EhrIntegrationService, EhrIntegrationServiceStub, Override (+2 more)

### Community 42 - "Security"
Cohesion: 0.17
Nodes (6): ProcessusMensuelIntrouvableException, GrilleNonActiveException, GlobalExceptionHandler, Logger, RoleInvalideException, RestControllerAdvice

### Community 43 - "Beneficiaires"
Cohesion: 0.23
Nodes (12): ConfirmerEnrolementResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, EnrolementVerificationResponseDto, AllArgsConstructor, Builder (+4 more)

### Community 44 - "Reporting"
Cohesion: 0.23
Nodes (10): HistoriqueResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, HistoriqueExportServiceTest, BeforeEach (+2 more)

### Community 45 - "Processus"
Cohesion: 0.19
Nodes (12): StatutEnum, CLOTURE, EN_ATTENTE_CRH, EN_ATTENTE_DRH, EN_COURS_ARH, RETOURNE, AllArgsConstructor, Builder (+4 more)

### Community 46 - "Processus"
Cohesion: 0.22
Nodes (11): Override, Service, SignatureServiceAutonome, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor (+3 more)

### Community 47 - "Security"
Cohesion: 0.24
Nodes (7): Component, JwtUtil, BeforeEach, Test, JwtUtilTest, MacAlgorithm, SecretKey

### Community 48 - "Frontend"
Cohesion: 0.13
Nodes (15): class-variance-authority, date-fns, dependencies, class-variance-authority, date-fns, @hookform/resolvers, lucide-react, @radix-ui/react-checkbox (+7 more)

### Community 49 - "Frontend"
Cohesion: 0.13
Nodes (15): devDependencies, oxlint, tailwindcss, @tailwindcss/vite, @types/react, @types/react-dom, vite, @vitejs/plugin-react (+7 more)

### Community 50 - "Beneficiaires"
Cohesion: 0.24
Nodes (10): Beneficiaire, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+2 more)

### Community 51 - "Community 51"
Cohesion: 0.26
Nodes (14): Dictionnaire de données Dotations V3, Table audit_log, Table beneficiaires, Table etape_workflow, Table fonction_eligible, Table grille_tarifaire, Table ligne_etat_mensuel (pivot N-N), Table piece_jointe (+6 more)

### Community 52 - "Beneficiaires"
Cohesion: 0.23
Nodes (10): BeneficiaireService, ApplicationEventPublisher, Page, Pageable, RequiredArgsConstructor, Service, EligibiliteService, RequiredArgsConstructor (+2 more)

### Community 53 - "Reporting"
Cohesion: 0.27
Nodes (10): HistoriqueLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, HistoriqueExportService, RequiredArgsConstructor (+2 more)

### Community 54 - "Beneficiaires"
Cohesion: 0.29
Nodes (10): ImportErreurDto, AllArgsConstructor, Builder, Data, NoArgsConstructor, ImportRapportDto, AllArgsConstructor, Builder (+2 more)

### Community 55 - "Referentiel"
Cohesion: 0.26
Nodes (9): HistoriqueGrilleTarifaireResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ApplicationEventPublisher, BeforeEach (+1 more)

### Community 56 - "Processus"
Cohesion: 0.42
Nodes (3): DeclencherProcessusRequestDto, Getter, Setter

### Community 57 - "Beneficiaires"
Cohesion: 0.35
Nodes (8): EnrolementController, GetMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController

### Community 58 - "Reference"
Cohesion: 0.20
Nodes (11): @PreAuthorize obligatoire sur chaque endpoint sensible, Matrice rôles/endpoints (34 endpoints, 9 groupes), Sécurité stateless JWT / Keycloak, aucune session HTTP serveur, Contrat API AFB_API_DOTTEL_V3.3_2026, JWT HS384, validité 8 heures, header Authorization Bearer, LoginResponseDto (token, matricule, role, nom, prenom), Logout stateless — aucune liste noire de jetons, POST /auth/login (public) (+3 more)

### Community 59 - "Backend"
Cohesion: 0.33
Nodes (6): mvnw script, clean(), die(), exec_maven(), set_java_home(), verbose()

### Community 60 - "Processus"
Cohesion: 0.36
Nodes (8): GetMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, PieceJointeController, Resource

### Community 61 - "Processus"
Cohesion: 0.20
Nodes (3): MotifRejetObligatoireException, MatriculeUtilisateurDejaUtiliseException, ExceptionHandler

### Community 62 - "Reporting"
Cohesion: 0.33
Nodes (8): DashboardResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, BeforeEach, ExtendWith

### Community 63 - "Monolithe Modulaire"
Cohesion: 0.22
Nodes (10): AuditService (interface), AuditServiceImpl.enregistrer(), Options E-1/E-2 conception des événements d'audit, Options T-1/T-2/T-3 mode transactionnel de l'écouteur, Piège 2 - adresse IP (RequestContextHolder), Piège 1 - transactionnalité (rollback conjoint), AuthenticatedUserService, Décision E-3 - enrôlement pour un tiers assumé (+2 more)

### Community 64 - "Frontend"
Cohesion: 0.20
Nodes (9): name, private, scripts, build, dev, lint, preview, type (+1 more)

### Community 65 - "Referentiel"
Cohesion: 0.22
Nodes (3): DateDebutGrilleAnterieureException, DecisionGrilleInvalideException, ResponseEntity

### Community 66 - "Referentiel"
Cohesion: 0.36
Nodes (6): DecisionGrilleTarifaireRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 67 - "Referentiel"
Cohesion: 0.42
Nodes (8): GrilleTarifaire, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table

### Community 68 - "Frontend"
Cohesion: 0.39
Nodes (9): Afriland First Bank Logo (PNG asset), Identité de marque Afriland First Bank, Charte visuelle frontend BAOBAB / DOTTEL, Palette de marque rouge / noir / gris / blanc, Rouge institutionnel AFB (E30613), Symbole concentrique "C/E" sur bloc rouge, Asset PNG horizontal à fond transparent importable par Vite, Usage : logo en haut de la barre de navigation latérale (+1 more)

### Community 69 - "Frontend"
Cohesion: 0.42
Nodes (9): Charte visuelle AFB - fond clair sobre, aucun rouge E30613 dans le fond, Filigrane discret de la zone de contenu principale (.fond-filigrane), Motif chevron / toit (polyline 110,50 120,36 130,50), Motif cercle (cx28 cy30 r7), Motif deux lignes horizontales (lignes de document/etat), Motif oeil (lentille + pupille, controle/supervision), Style de trait unique (fill=none, stroke #1A1A1A, width 1.5, opacity 0.07), Tuile repetable 160x160 (background-repeat) (+1 more)

### Community 71 - "Processus"
Cohesion: 0.43
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ResultatAjustementDto

### Community 72 - "Frontend"
Cohesion: 0.25
Nodes (7): plugins, rules, react/only-export-components, react/rules-of-hooks, $schema, oxc, warn

### Community 73 - "Afb Dottel Mm"
Cohesion: 0.29
Nodes (8): Sprint 3.4 guide — Validation ARH et génération du PDF initial (US-11), Champ CHAPITRE (donnée EHR par bénéficiaire, décision révisée depuis 'valeur fixe 64310000' vers donnée variable + repli configurable), DocumentService.genererInitiale() (iText 8 PDF, RG-06), Entité EtapeWorkflow (NomEtapeEnum, StatutEtapeEnum, signature_numerique), NotificationService (stub log, appelé par ProcessusMensuelService.valider), Entité PieceJointe (UNIQUE par processus, nombre_signatures), Point de vigilance RG-08 : contrat API mentionne la vérification sur /valider, mais SeparationTachesService est reporté au Sprint 5, SignatureService (interface isolée, trace non-certifiée, remplaçable par intégration type INTRA)

### Community 74 - "Config"
Cohesion: 0.43
Nodes (5): CorsConfig, Configuration, Override, CorsRegistry, WebMvcConfigurer

### Community 75 - "Processus"
Cohesion: 0.52
Nodes (6): BeneficiaireExcluDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 76 - "Processus"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, LigneEtatMensuelDetailDto

### Community 77 - "Processus"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, PatchProcessusResponseDto

### Community 78 - "Processus"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, PieceJointeMetadonneesResponseDto

### Community 79 - "Processus"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusDetailResponseDto

### Community 80 - "Processus"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusListItemDto

### Community 81 - "Processus"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusMensuelResponseDto

### Community 82 - "Processus"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, RetournerProcessusRequestDto

### Community 83 - "Processus"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, RetournerProcessusResponseDto

### Community 84 - "Processus"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ValiderProcessusResponseDto

### Community 85 - "Referentiel"
Cohesion: 0.52
Nodes (6): CreerGrilleTarifaireRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 86 - "Utilisateurs"
Cohesion: 0.52
Nodes (6): CreerUtilisateurRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 87 - "Utilisateurs"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, UtilisateurListeResponseDto

### Community 88 - "Frontend"
Cohesion: 0.29
Nodes (7): icons.svg (Icon Sprite Sheet), Bluesky Icon Symbol, Discord Icon Symbol, Documentation Icon Symbol, GitHub Icon Symbol, Social/Users Icon Symbol, X (Twitter) Icon Symbol

### Community 89 - "K8S"
Cohesion: 0.29
Nodes (7): k8s deployment.yaml (dottel-backend), /api/actuator/health liveness/readiness probe, Deployment dottel-backend, Image harbor.afrilandfirstbank.cm/baobab/dottel-backend:latest, Secret dottel-secret (referenced), k8s service.yaml (dottel-backend-svc), Service dottel-backend-svc (ClusterIP)

### Community 90 - "Processus"
Cohesion: 0.47
Nodes (4): Override, Service, NotificationServiceStub, Slf4j

### Community 91 - "Afb Dottel Mm"
Cohesion: 0.47
Nodes (6): RG-05 — séquence stricte ARH puis CRH puis DRH, RG-06 — un seul PDF par processus, enrichi de 1 à 3 signatures, RG-08 — séparation des tâches (403 si même acteur), GET /pieces-jointes/{id}/download (ARH, CRH, DRH), GET /processus/{id}/piece-jointe (ARH, CRH, DRH), POST /processus/{id}/valider (ARH, CRH, DRH selon statut)

### Community 92 - "Monolithe Modulaire"
Cohesion: 0.40
Nodes (6): AuthService.authentifier() (devient mort), Décision P-2 - Keycloak pour les 5 rôles, EMPLOYE compris, Décision P-3 - non recommandée (parcours EMPLOYE public), JwtUtil.genererToken() (devient mort), RoleJwtAuthenticationConverter, SecurityConfig.jwtDecoder() (bascule issuer-uri)

### Community 93 - "Frontend"
Cohesion: 0.33
Nodes (5): API_BASE_URL, ROLES, STATUTS, STATUTS_GRILLE, STATUTS_LABELS

### Community 94 - "Modularitytests.Java"
Cohesion: 0.60
Nodes (3): ApplicationModules, Test, ModularityTests

### Community 96 - "Dottelapplicationtests.Java"
Cohesion: 0.60
Nodes (3): DottelApplicationTests, Test, SpringBootTest

### Community 97 - "Reference"
Cohesion: 0.40
Nodes (5): GET /admin/utilisateurs (ADMIN), GET /grilles-tarifaires/en-attente-drh (DRH), GET /grilles-tarifaires (ARH, ADMIN), Pas de pagination sur grilles / fonctions / utilisateurs — volume interne limité, POST /admin/utilisateurs (ADMIN)

### Community 113 - "Monolithe Modulaire"
Cohesion: 0.67
Nodes (3): Nécessité du démarrage réel (au-delà des tests unitaires Mockito), DottelApplication, DottelApplicationTests

### Community 114 - "Monolithe Modulaire"
Cohesion: 1.00
Nodes (3): AuthProvider.js / AuthContext.jsx (contrat existant), AuthProviderKeycloak.js, Décision F-2 - Authorization Code + PKCE

### Community 115 - "Reference"
Cohesion: 0.67
Nodes (3): GET /processus — filtres statut et année (ARH, CRH, DRH), GET /reporting/dashboard (ARH, DRH), GET /reporting/historique (DRH)

### Community 116 - "Frontend"
Cohesion: 0.67
Nodes (3): React Framework, React Logo (Vite default asset), Vite/React Starter Template Bootstrap

## Ambiguous Edges - Review These
- `Identité de marque Afriland First Bank` → `Symbole concentrique "C/E" sur bloc rouge`  [AMBIGUOUS]
  frontend/src/assets/logo afriland.png · relation: conceptually_related_to
- `Motif cercle (cx28 cy30 r7)` → `Motif chevron / toit (polyline 110,50 120,36 130,50)`  [AMBIGUOUS]
  frontend/src/assets/watermark-pattern.svg · relation: semantically_similar_to

## Knowledge Gaps
- **199 isolated node(s):** `com.afriland:dottel`, `VALIDATION_ARH`, `VALIDATION_CRH`, `VALIDATION_DRH`, `EN_COURS_ARH` (+194 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **47 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Identité de marque Afriland First Bank` and `Symbole concentrique "C/E" sur bloc rouge`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Motif cercle (cx28 cy30 r7)` and `Motif chevron / toit (polyline 110,50 120,36 130,50)`?**
  _Edge tagged AMBIGUOUS (relation: semantically_similar_to) - confidence is low._
- **Why does `EvenementAudit` connect `Utilisateurs` to `Referentiel`, `Utilisateurs`, `Audit`, `Processus`, `Beneficiaires`, `Processus`, `Beneficiaires`, `Utilisateurs`, `Referentiel`, `Beneficiaires`, `Beneficiaires`, `Processus`, `Beneficiaires`, `Referentiel`, `Processus`, `Beneficiaires`, `Referentiel`?**
  _High betweenness centrality (0.088) - this node is a cross-community bridge._
- **Why does `Utilisateur` connect `Processus` to `Utilisateurs`, `Processus`, `Processus`, `Beneficiaires`, `Processus`, `Processus`, `Utilisateurs`, `Utilisateurs`, `Beneficiaires`, `Referentiel`, `Utilisateurs`, `Beneficiaires`, `Security`, `Beneficiaires`, `Processus`, `Beneficiaires`, `Beneficiaires`, `Processus`?**
  _High betweenness centrality (0.076) - this node is a cross-community bridge._
- **Why does `AuthenticatedUserService` connect `Utilisateurs` to `Referentiel`, `Processus`, `Beneficiaires`, `Processus`, `Processus`, `Beneficiaires`, `Beneficiaires`, `Utilisateurs`, `Referentiel`, `Beneficiaires`, `Processus`, `Beneficiaires`, `Utilisateurs`, `Beneficiaires`, `Beneficiaires`, `Utilisateurs`?**
  _High betweenness centrality (0.057) - this node is a cross-community bridge._
- **What connects `com.afriland:dottel`, `VALIDATION_ARH`, `VALIDATION_CRH` to the rest of the system?**
  _199 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Frontend` be split into smaller, more focused modules?**
  _Cohesion score 0.0505276225946617 - nodes in this community are weakly interconnected._