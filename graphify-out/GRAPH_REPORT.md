# Graph Report - afb-dottel-mm  (2026-08-09)

## Corpus Check
- 321 files · ~180,502 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2159 nodes · 5984 edges · 151 communities (118 shown, 33 thin omitted)
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 856 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `1b65a3b4`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- AppRouter.jsx
- ProcessusMensuelController.java
- SeparationTachesService
- GlobalExceptionHandlerTest
- .utilisateurCourant
- GlobalExceptionHandler.java
- Décision C - Découpage 6 modules
- Rapport d'audit de securite OWASP V1 (AFB_AUDIT_DOTTEL_V1_2026)
- PieceJointe
- ProcessusMensuel
- EvenementAudit
- BeneficiaireApiImpl
- EnrolementServiceTest.java
- UtilisateurAdminServiceTest
- ProcessusMensuelRepository
- RoleJwtAuthenticationConverter
- .findByCode
- EvenementClotureDto
- Graphify Skill Reference Files
- HistoriqueResponseDto
- FonctionEligible
- ProcessusMensuelServiceTest.java
- UtilisateurRepository
- RetournerProcessusRequestDto
- GrilleTarifaireResponseDto
- BeneficiaireService.java
- .importer
- BeneficiaireServiceTest
- FonctionEligibleAdminResponseDto
- RECAPITULATIF_CHANTIER.md
- .confirmer
- AuditLog
- UtilisateurAdminController.java
- UtilisateurAdminService
- SPRINT MM.8
- GrilleTarifaireServiceTest.java
- BeneficiaireController.java
- FonctionEligibleCodeDejaUtiliseException
- AuthProviderKeycloak.js
- AuditLogPageResponseDto
- EnrolementController.java
- BeneficiaireRepository
- AuthenticatedUserService
- BeneficiairePageResponseDto
- EmployeEhrDto
- LigneEtatMensuel
- Beneficiaire
- SPRINT MM.10
- Utilisateur
- dependencies
- devDependencies
- SPRINT MM.9
- StatutEnum
- Dictionnaire de données Dotations V3
- UtilisateurResponseDto
- ImportRapportDto
- GrilleTarifaire
- AuthenticatedUserServiceTest.java
- Sprint MM.7 — Keycloak local provisoire, en remplacement de la simulation JWT
- PLAN — AJOUTS MÉTIER, VERSION MONOLITHE MODULAIRE
- SPRINT MM.12
- FonctionEligibleServiceTest.java
- ReportingController.java
- SPRINT MM.13
- mvnw
- PieceJointeController.java
- SPRINT MM.11
- Module DOTTEL (Dotations Téléphoniques Mensuelles)
- Contrats API Dotations Téléphoniques V3.3 (AFB_API_DOTTEL_V3.3_2026)
- package.json
- Option P-3 — parcours EMPLOYE rendu public (non recommandée)
- Récapitulatif du chantier Monolithe Modulaire (MM.0 à MM.6)
- Afriland First Bank Logo (PNG asset)
- Watermark Pattern (filigrane decoratif 160x160)
- EtapeWorkflow
- ReportingService
- .oxlintrc.json
- DocumentService.genererInitiale() (iText 8 PDF, RG-06)
- BeneficiaireServiceTest.java
- CorsConfig.java
- ProcessusMensuelResponseDto
- HistoriqueLigneDto
- ModifierGrilleTarifaireRequestDto
- DashboardResponseDto
- AuthController.java
- ChangerStatutRequestDto
- EnrolementService.java
- GlobalExceptionHandler
- Décision de portée P-2 — Keycloak pour les 5 rôles, EMPLOYE compris
- icons.svg (Icon Sprite Sheet)
- Deployment dottel-backend
- .consulterDetail
- BeneficiaireResponseDto
- AuditServiceImpl.enregistrer
- ModularityTests.java
- constants.js
- AuthProviderLocal.js (implémentation matricule/mot de passe)
- DottelApplication
- DocumentationTests.java
- ValiderProcessusResponseDto
- Décision I-2 — résolution d'identité par email
- Nécessité du démarrage réel (au-delà des tests unitaires Mockito)
- React Logo (Vite default asset)
- Claim matricule (protocol mapper Keycloak)
- axios
- ChangerRoleRequestDto
- Note de Service NS 69/17 - Dotation Téléphonique Portable (scan)
- clsx
- MM.0 Cadrage
- Frontend (React + Vite App)
- index.html (frontend)
- jwt-decode
- react
- react-hook-form
- react-hot-toast
- react-router-dom
- tailwind-merge
- zod
- utilisateurs/api/package-info.java
- utilisateurs/exception/package-info.java
- utilisateurs/model/entity/package-info.java
- utilisateurs/model/enums/package-info.java
- utilisateurs/package-info.java
- NotificationServiceStub
- Logo Afriland First Bank

## God Nodes (most connected - your core abstractions)
1. `Utilisateur` - 104 edges
2. `ProcessusMensuelServiceTest` - 87 edges
3. `EvenementAudit` - 43 edges
4. `ProcessusMensuel` - 41 edges
5. `ProcessusMensuelService` - 38 edges
6. `GlobalExceptionHandler` - 38 edges
7. `react` - 38 edges
8. `GrilleTarifaireServiceTest` - 36 edges
9. `StatutEnum` - 35 edges
10. `BeneficiaireRepository` - 34 edges

## Surprising Connections (you probably didn't know these)
- `Contrats API Dotations Téléphoniques V3.3 (AFB_API_DOTTEL_V3.3_2026)` --semantically_similar_to--> `Contrats API : 34 endpoints`  [INFERRED] [semantically similar]
  docs/reference/contrats_api_dotations_v3.md → CLAUDE.md
- `Ecart E7c — fichiers .env non charges par Vite, build HTTP clair` --semantically_similar_to--> `APP_CORS_ALLOWED_ORIGINS (origine frontend production)`  [INFERRED] [semantically similar]
  docs/audit_securite_owasp_v1.md → k8s/configmap.yaml
- `Rapport d'audit de securite OWASP V1 (AFB_AUDIT_DOTTEL_V1_2026)` --cites--> `Repli en clair DB_PASSWORD:postgres (profil dev)`  [EXTRACTED]
  docs/audit_securite_owasp_v1.md → backend/src/main/resources/application-dev.yml
- `Validation cote client = ergonomie, jamais garantie de securite` --references--> `VerifierMatriculePage.jsx — GET /enrolement/verifier`  [EXTRACTED]
  docs/audit_securite_owasp_v1.md → sprint6F_6F_4.md
- `POST /beneficiaires/import (RG-11)` --references--> `Règles métier critiques RG-01 à RG-12`  [EXTRACTED]
  docs/reference/contrats_api_dotations_v3.md → CLAUDE.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Flux de resynchronisation non silencieuse des montants (B2-resync)** — docs_chantier_ajout_metier_mm_mm_12_workflow_grilles_et_montants_decision_b2_resync, docs_chantier_ajout_metier_mm_mm_12_workflow_grilles_et_montants_grille_tarifaire_api, docs_chantier_ajout_metier_mm_mm_8_anomalies_et_cloture_cycle_rg_09, docs_chantier_ajout_metier_mm_mm_11_periode_declenchement_processus_mensuel_service [INFERRED 0.80]
- **Pattern du workflow des grilles à 3 acteurs et son risque architectural** — docs_chantier_ajout_metier_mm_mm_12_workflow_grilles_et_montants_workflow_trois_acteurs, docs_chantier_ajout_metier_mm_mm_12_workflow_grilles_et_montants_statut_grille_enum, docs_chantier_ajout_metier_mm_mm_12_workflow_grilles_et_montants_rg_08, docs_chantier_ajout_metier_mm_mm_12_workflow_grilles_et_montants_second_cycle_modules_risque [EXTRACTED 1.00]
- **Cycle de modules beneficiaires ↔ referentiel, assumé par décision G-2** — docs_chantier_ajout_metier_mm_mm_8_anomalies_et_cloture_cycle_cycle_beneficiaires_referentiel, docs_chantier_ajout_metier_mm_mm_8_anomalies_et_cloture_cycle_decision_g2, docs_chantier_ajout_metier_mm_mm_8_anomalies_et_cloture_cycle_modularity_tests, docs_chantier_ajout_metier_mm_mm_10_referentiel_unite_agence_beneficiaire_api [EXTRACTED 1.00]
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

## Communities (151 total, 33 thin omitted)

### Community 0 - "AppRouter.jsx"
Cohesion: 0.06
Nodes (111): apiClient, App(), AppLayout(), PageHeader(), initiales(), NAV_LINKS, Sidebar(), trouverHrefActif() (+103 more)

### Community 1 - "ProcessusMensuelController.java"
Cohesion: 0.10
Nodes (25): FichierImportInvalideException, ImportErreurDto, AllArgsConstructor, Builder, Data, NoArgsConstructor, ImportRapportDto, AllArgsConstructor (+17 more)

### Community 2 - "SeparationTachesService"
Cohesion: 0.06
Nodes (28): BeneficiaireDocumentDto, BeneficiaireIdentiteDto, Beneficiaire, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor (+20 more)

### Community 3 - "GlobalExceptionHandlerTest"
Cohesion: 0.13
Nodes (14): EtapeWorkflowIntrouvableException, RoleEtapeNonAutoriseException, SeparationTachesViolationException, NomEtapeEnum, VALIDATION_ARH, VALIDATION_CRH, VALIDATION_DRH, EtapeWorkflowRepository (+6 more)

### Community 4 - ".utilisateurCourant"
Cohesion: 0.05
Nodes (52): application.yml — configuration Spring dottel, dottel.kafka.topic-cloture (dottel.processus.cloture), DOTTEL_KEYCLOAK_ISSUER_URI (issuer-uri OAuth2 resource server), Contrats API : 34 endpoints, Déploiement Kubernetes (microservice conteneurisé), Erreurs à ne jamais commettre (20 points), Simulation Keycloak en développement, Module DOTTEL (Dotations Téléphoniques Mensuelles) (+44 more)

### Community 5 - "GlobalExceptionHandler.java"
Cohesion: 0.10
Nodes (12): AccessDeniedException, GlobalExceptionHandler, Logger, MethodArgumentNotValidException, ResponseEntity, GlobalExceptionHandlerTest, ExtendWith, MethodArgumentNotValidException (+4 more)

### Community 6 - "Décision C - Découpage 6 modules"
Cohesion: 0.13
Nodes (14): MotifRejetObligatoireException, ProcessusMensuelExisteDejaException, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ResultatAjustementDto (+6 more)

### Community 7 - "Rapport d'audit de securite OWASP V1 (AFB_AUDIT_DOTTEL_V1_2026)"
Cohesion: 0.08
Nodes (46): BeneficiaireApi, BeneficiaireDocumentDto, Code agence (5 chiffres), Code unité (4 chiffres), Sprint MM.10 — Référentiel unité/agence, EhrIntegrationServiceStub, Décision A2 — affichage rattrapage (tout afficher, non-payés pré-cochés), Sprint MM.11 — Période de déclenchement et rattrapage (+38 more)

### Community 8 - "PieceJointe"
Cohesion: 0.08
Nodes (38): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, LigneDocumentDto, AllArgsConstructor, Builder (+30 more)

### Community 9 - "ProcessusMensuel"
Cohesion: 0.15
Nodes (15): UtilisateurApi, RoleEnum, ADMIN, ARH, CRH, DRH, EMPLOYE, UtilisateurRepository (+7 more)

### Community 10 - "EvenementAudit"
Cohesion: 0.05
Nodes (39): Décision A - EligibiliteService (A1), Décision B - Référentiel unifié (B1), Décision C - Découpage 6 modules, EligibiliteService, Module audit, Module beneficiaires, Module integration (proposé puis rejeté, réparti), Module processus (+31 more)

### Community 11 - "BeneficiaireApiImpl"
Cohesion: 0.18
Nodes (8): BeneficiaireApi, FonctionEligibleCodeDejaUtiliseException, FonctionEligibleService, ApplicationEventPublisher, Override, RequiredArgsConstructor, Service, Transactional

### Community 12 - "EnrolementServiceTest.java"
Cohesion: 0.09
Nodes (16): DateDebutGrilleAnterieureException, DecisionGrilleInvalideException, GrilleEnAttenteDrhExistanteException, GrilleIntrouvableException, GrilleNonActiveException, GrilleNonEnAttenteDrhException, GrilleNonModifiableException, GrilleTarifaireMotifRejetObligatoireException (+8 more)

### Community 13 - "UtilisateurAdminServiceTest"
Cohesion: 0.07
Nodes (39): application-dev.yml (profil developpement), Repli en clair DB_PASSWORD:postgres (profil dev), application-prod.yml (profil production), server.error.* (prefixe corrige, ecart E6), Rapport d'audit de securite OWASP V1 (AFB_AUDIT_DOTTEL_V1_2026), A01 — Alignement des routes frontend sur la matrice de roles, A03 — Injection et echappement (zero dangerouslySetInnerHTML), Ecart E1 — CRH redirige vers /processus (ROUTE_PAR_ROLE) (+31 more)

### Community 14 - "ProcessusMensuelRepository"
Cohesion: 0.08
Nodes (27): EvenementAudit, MatriculeDejaEnroleException, MatriculeInconnuException, NonEligibleException, UniteInconnueException, BeneficiaireRepository, BeneficiaireService, ApplicationEventPublisher (+19 more)

### Community 16 - ".findByCode"
Cohesion: 0.06
Nodes (43): BeneficiaireController, GetMapping, MultipartFile, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor (+35 more)

### Community 17 - "EvenementClotureDto"
Cohesion: 0.25
Nodes (9): LigneEtatMensuelRepository, ProcessusMensuelRepository, EcartMensuelService, RequiredArgsConstructor, Service, EcartMensuelServiceTest, ExtendWith, Test (+1 more)

### Community 18 - "Graphify Skill Reference Files"
Cohesion: 0.06
Nodes (37): BeneficiaireDotationDto, PieceJointeIntrouvableException, ProcessusMensuelIntrouvableException, ProcessusMensuelNonModifiableException, AjustementLigneEtatDto, Getter, Setter, DeclencherProcessusRequestDto (+29 more)

### Community 19 - "HistoriqueResponseDto"
Cohesion: 0.22
Nodes (12): EtapeWorkflow, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+4 more)

### Community 20 - "FonctionEligible"
Cohesion: 0.14
Nodes (18): Component, Logger, Override, RoleJwtAuthenticationConverter, Bean, Configuration, EnableMethodSecurity, PasswordEncoder (+10 more)

### Community 21 - "ProcessusMensuelServiceTest.java"
Cohesion: 0.13
Nodes (20): EvenementClotureSerializer, ObjectMapper, Override, Bean, Configuration, KafkaTemplate, ObjectMapper, KafkaConfig (+12 more)

### Community 22 - "UtilisateurRepository"
Cohesion: 0.23
Nodes (8): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, ProcessusMensuel

### Community 23 - "RetournerProcessusRequestDto"
Cohesion: 0.08
Nodes (29): Project .claude/CLAUDE.md (graphify trigger), /graphify add <url>, --watch folder watcher, graphify export falkordb / falkordb-push, MCP stdio server (graphify.serve), graphify export neo4j / neo4j-push, graphify export wiki, Confidence score rubric (0.55-0.95 discrete steps) (+21 more)

### Community 24 - "GrilleTarifaireResponseDto"
Cohesion: 0.16
Nodes (14): HistoriqueResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, HistoriqueExportService, RequiredArgsConstructor (+6 more)

### Community 25 - "BeneficiaireService.java"
Cohesion: 0.27
Nodes (3): FonctionEligibleIntrouvableException, FonctionEligibleServiceTest, Test

### Community 26 - ".importer"
Cohesion: 0.30
Nodes (3): EligibiliteServiceTest, ExtendWith, Test

### Community 27 - "BeneficiaireServiceTest"
Cohesion: 0.10
Nodes (15): ActionAdminNonAutoriseeException, EmailUtilisateurDejaUtiliseException, MatriculeUtilisateurDejaUtiliseException, RoleInvalideException, UtilisateurIntrouvableException, ApplicationEventPublisher, PasswordEncoder, RequiredArgsConstructor (+7 more)

### Community 28 - "FonctionEligibleAdminResponseDto"
Cohesion: 0.21
Nodes (15): FonctionEligibleController, GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity (+7 more)

### Community 30 - ".confirmer"
Cohesion: 0.22
Nodes (11): EmployeEhrDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, ConfirmerEnrolementRequestDto, Getter, Setter (+3 more)

### Community 31 - "AuditLog"
Cohesion: 0.09
Nodes (23): @ApplicationModule(allowedDependencies = {...}), docs/monolithe-modulaire/architecture/ (diagrammes PlantUML et canvases versionnés), docs/audit_securite_owasp_v1.md, AuditService, CLAUDE.md, Cycle beneficiaires <-> referentiel (couplage C3), DocumentationTests.java, Documenter (org.springframework.modulith.docs) (+15 more)

### Community 32 - "UtilisateurAdminController.java"
Cohesion: 0.36
Nodes (8): GetMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, PieceJointeController, Resource

### Community 33 - "UtilisateurAdminService"
Cohesion: 0.52
Nodes (6): CreerFonctionEligibleRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 34 - "SPRINT MM.8"
Cohesion: 0.20
Nodes (15): GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+7 more)

### Community 35 - "GrilleTarifaireServiceTest.java"
Cohesion: 0.18
Nodes (11): AuditEventListener, Component, Logger, ObjectMapper, RequiredArgsConstructor, AuditEventListenerTest, AfterEach, BeforeEach (+3 more)

### Community 36 - "BeneficiaireController.java"
Cohesion: 0.19
Nodes (14): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, UtilisateurResponseDto, EnableMethodSecurity, Test (+6 more)

### Community 37 - "FonctionEligibleCodeDejaUtiliseException"
Cohesion: 0.19
Nodes (11): AuditServiceImpl, Override, Page, Pageable, RequiredArgsConstructor, Service, Transactional, AuditServiceImplTest (+3 more)

### Community 38 - "AuthProviderKeycloak.js"
Cohesion: 0.19
Nodes (15): GrilleTarifaireController, GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity (+7 more)

### Community 39 - "AuditLogPageResponseDto"
Cohesion: 0.16
Nodes (8): setAuthToken(), AuthProvider, AuthProviderKeycloak, base64UrlEncode(), calculerCodeChallenge(), construireUtilisateur(), genererValeurAleatoire(), ROLES_DOTTEL

### Community 40 - "EnrolementController.java"
Cohesion: 0.16
Nodes (18): EnrolementController, GetMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+10 more)

### Community 41 - "BeneficiaireRepository"
Cohesion: 0.05
Nodes (60): GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+52 more)

### Community 42 - "AuthenticatedUserService"
Cohesion: 0.38
Nodes (10): audit_log, beneficiaires, etape_workflow, fonction_eligible, grille_tarifaire, ligne_etat_mensuel, piece_jointe, processus_mensuel (+2 more)

### Community 43 - "BeneficiairePageResponseDto"
Cohesion: 0.16
Nodes (12): FonctionEligibleBeneficiairesActifsException, FonctionEligibleRepository, GrilleTarifaireRepository, GrilleTarifaireApiImpl, Override, RequiredArgsConstructor, Service, ApplicationEventPublisher (+4 more)

### Community 44 - "EmployeEhrDto"
Cohesion: 0.40
Nodes (7): GetMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, ReportingController

### Community 45 - "LigneEtatMensuel"
Cohesion: 0.31
Nodes (4): EhrIntegrationServiceStub, Override, Service, PostConstruct

### Community 46 - "Beneficiaire"
Cohesion: 0.36
Nodes (6): HistoriqueLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 47 - "SPRINT MM.10"
Cohesion: 0.32
Nodes (3): Transactional, Test, ReportingServiceTest

### Community 48 - "Utilisateur"
Cohesion: 0.22
Nodes (13): GrilleTarifaire, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+5 more)

### Community 49 - "dependencies"
Cohesion: 0.26
Nodes (5): UtilisateurConnecteIntrouvableException, AuthenticatedUserServiceTest, AfterEach, ExtendWith, Test

### Community 50 - "devDependencies"
Cohesion: 0.13
Nodes (15): class-variance-authority, date-fns, dependencies, class-variance-authority, date-fns, @hookform/resolvers, lucide-react, @radix-ui/react-checkbox (+7 more)

### Community 51 - "SPRINT MM.9"
Cohesion: 0.13
Nodes (15): devDependencies, oxlint, tailwindcss, @tailwindcss/vite, @types/react, @types/react-dom, vite, @vitejs/plugin-react (+7 more)

### Community 52 - "StatutEnum"
Cohesion: 0.24
Nodes (10): AuditLog, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+2 more)

### Community 53 - "Dictionnaire de données Dotations V3"
Cohesion: 0.25
Nodes (12): GrilleHistoriqueLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, HistoriqueGrilleTarifaireResponseDto, AllArgsConstructor (+4 more)

### Community 54 - "UtilisateurResponseDto"
Cohesion: 0.25
Nodes (12): GrilleTarifaireListeLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, GrilleTarifaireListeResponseDto, AllArgsConstructor (+4 more)

### Community 55 - "ImportRapportDto"
Cohesion: 0.26
Nodes (14): Dictionnaire de données Dotations V3, Table audit_log, Table beneficiaires, Table etape_workflow, Table fonction_eligible, Table grille_tarifaire, Table ligne_etat_mensuel (pivot N-N), Table piece_jointe (+6 more)

### Community 56 - "GrilleTarifaire"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ModifierFonctionEligibleRequestDto

### Community 57 - "AuthenticatedUserServiceTest.java"
Cohesion: 0.42
Nodes (8): FonctionEligible, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table

### Community 58 - "Sprint MM.7 — Keycloak local provisoire, en remplacement de la simulation JWT"
Cohesion: 0.31
Nodes (6): FonctionEligibleResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 59 - "PLAN — AJOUTS MÉTIER, VERSION MONOLITHE MODULAIRE"
Cohesion: 0.27
Nodes (8): DashboardResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, BeforeEach, ExtendWith

### Community 60 - "SPRINT MM.12"
Cohesion: 0.33
Nodes (6): mvnw script, clean(), die(), exec_maven(), set_java_home(), verbose()

### Community 61 - "FonctionEligibleServiceTest.java"
Cohesion: 0.22
Nodes (13): AuditLogResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, AuditLogPageResponseDto, AllArgsConstructor (+5 more)

### Community 62 - "ReportingController.java"
Cohesion: 0.20
Nodes (9): name, private, scripts, build, dev, lint, preview, type (+1 more)

### Community 63 - "SPRINT MM.13"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 64 - "mvnw"
Cohesion: 0.39
Nodes (9): Afriland First Bank Logo (PNG asset), Identité de marque Afriland First Bank, Charte visuelle frontend BAOBAB / DOTTEL, Palette de marque rouge / noir / gris / blanc, Rouge institutionnel AFB (E30613), Symbole concentrique "C/E" sur bloc rouge, Asset PNG horizontal à fond transparent importable par Vite, Usage : logo en haut de la barre de navigation latérale (+1 more)

### Community 65 - "PieceJointeController.java"
Cohesion: 0.42
Nodes (9): Charte visuelle AFB - fond clair sobre, aucun rouge E30613 dans le fond, Filigrane discret de la zone de contenu principale (.fond-filigrane), Motif chevron / toit (polyline 110,50 120,36 130,50), Motif cercle (cx28 cy30 r7), Motif deux lignes horizontales (lignes de document/etat), Motif oeil (lentille + pupille, controle/supervision), Style de trait unique (fill=none, stroke #1A1A1A, width 1.5, opacity 0.07), Tuile repetable 160x160 (background-repeat) (+1 more)

### Community 66 - "SPRINT MM.11"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, UtilisateurListeResponseDto

### Community 67 - "Module DOTTEL (Dotations Téléphoniques Mensuelles)"
Cohesion: 0.53
Nodes (3): AuditLogRepository, JpaRepository, JpaSpecificationExecutor

### Community 68 - "Contrats API Dotations Téléphoniques V3.3 (AFB_API_DOTTEL_V3.3_2026)"
Cohesion: 0.17
Nodes (15): StatutEnum, CLOTURE, EN_ATTENTE_CRH, EN_ATTENTE_DRH, EN_COURS_ARH, RETOURNE, AllArgsConstructor, Builder (+7 more)

### Community 69 - "package.json"
Cohesion: 0.25
Nodes (7): plugins, rules, react/only-export-components, react/rules-of-hooks, $schema, oxc, warn

### Community 70 - "Option P-3 — parcours EMPLOYE rendu public (non recommandée)"
Cohesion: 0.29
Nodes (8): Sprint 3.4 guide — Validation ARH et génération du PDF initial (US-11), Champ CHAPITRE (donnée EHR par bénéficiaire, décision révisée depuis 'valeur fixe 64310000' vers donnée variable + repli configurable), DocumentService.genererInitiale() (iText 8 PDF, RG-06), Entité EtapeWorkflow (NomEtapeEnum, StatutEtapeEnum, signature_numerique), NotificationService (stub log, appelé par ProcessusMensuelService.valider), Entité PieceJointe (UNIQUE par processus, nombre_signatures), Point de vigilance RG-08 : contrat API mentionne la vérification sur /valider, mais SeparationTachesService est reporté au Sprint 5, SignatureService (interface isolée, trace non-certifiée, remplaçable par intégration type INTRA)

### Community 71 - "Récapitulatif du chantier Monolithe Modulaire (MM.0 à MM.6)"
Cohesion: 0.60
Nodes (3): AuditService, Page, Pageable

### Community 72 - "Afriland First Bank Logo (PNG asset)"
Cohesion: 0.43
Nodes (5): CorsConfig, Configuration, Override, CorsRegistry, WebMvcConfigurer

### Community 73 - "Watermark Pattern (filigrane decoratif 160x160)"
Cohesion: 0.50
Nodes (3): For /graphify add, For --watch, graphify reference: add a URL and watch a folder

### Community 74 - "EtapeWorkflow"
Cohesion: 0.50
Nodes (3): For git commit hook, For native CLAUDE.md integration, graphify reference: commit hook and native CLAUDE.md integration

### Community 75 - "ReportingService"
Cohesion: 0.50
Nodes (3): For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only

### Community 79 - "CorsConfig.java"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusListItemDto

### Community 85 - "HistoriqueLigneDto"
Cohesion: 0.52
Nodes (6): CreerGrilleTarifaireRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 86 - "ModifierGrilleTarifaireRequestDto"
Cohesion: 0.52
Nodes (6): DecisionGrilleTarifaireRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 87 - "DashboardResponseDto"
Cohesion: 0.43
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ModifierGrilleTarifaireRequestDto

### Community 88 - "AuthController.java"
Cohesion: 0.48
Nodes (5): AuthController, PostMapping, RequestMapping, ResponseEntity, RestController

### Community 89 - "ChangerStatutRequestDto"
Cohesion: 0.52
Nodes (6): ChangerRoleRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 91 - "EnrolementService.java"
Cohesion: 0.52
Nodes (6): CreerUtilisateurRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 92 - "GlobalExceptionHandler"
Cohesion: 0.29
Nodes (7): icons.svg (Icon Sprite Sheet), Bluesky Icon Symbol, Discord Icon Symbol, Documentation Icon Symbol, GitHub Icon Symbol, Social/Users Icon Symbol, X (Twitter) Icon Symbol

### Community 93 - "Décision de portée P-2 — Keycloak pour les 5 rôles, EMPLOYE compris"
Cohesion: 0.29
Nodes (7): k8s deployment.yaml (dottel-backend), /api/actuator/health liveness/readiness probe, Deployment dottel-backend, Image harbor.afrilandfirstbank.cm/baobab/dottel-backend:latest, Secret dottel-secret (referenced), k8s service.yaml (dottel-backend-svc), Service dottel-backend-svc (ClusterIP)

### Community 94 - "icons.svg (Icon Sprite Sheet)"
Cohesion: 0.40
Nodes (6): AuditService (interface), AuditServiceImpl.enregistrer(), Options E-1/E-2 conception des événements d'audit, Options T-1/T-2/T-3 mode transactionnel de l'écouteur, Piège 2 - adresse IP (RequestContextHolder), Piège 1 - transactionnalité (rollback conjoint)

### Community 95 - "Deployment dottel-backend"
Cohesion: 0.40
Nodes (4): ROLES, STATUTS, STATUTS_GRILLE, STATUTS_LABELS

### Community 96 - ".consulterDetail"
Cohesion: 0.60
Nodes (3): ApplicationModules, Test, ModularityTests

### Community 97 - "BeneficiaireResponseDto"
Cohesion: 0.60
Nodes (3): DottelApplicationTests, Test, SpringBootTest

### Community 98 - "AuditServiceImpl.enregistrer"
Cohesion: 0.40
Nodes (5): apiClient.js (jeton en mémoire seule), AuthContext.jsx (câblé au singleton authProviderLocal), AuthProvider.js (contrat frontend à 3 méthodes), AuthProviderLocal.js (implémentation matricule/mot de passe), Décision F-2 — Authorization Code + PKCE

### Community 103 - "AuthProviderLocal.js (implémentation matricule/mot de passe)"
Cohesion: 0.67
Nodes (4): AuthenticatedUserService (résolution par claim email), Décision I-2 — résolution d'identité par email, Utilisateur.java (entité, sans champ d'identité externe), V3__insertion_utilisateurs_test.sql (5 utilisateurs de test, écart tiret bas corrigé)

### Community 104 - "DottelApplication"
Cohesion: 0.67
Nodes (3): Nécessité du démarrage réel (au-delà des tests unitaires Mockito), DottelApplication, DottelApplicationTests

### Community 105 - "DocumentationTests.java"
Cohesion: 0.67
Nodes (3): React Framework, React Logo (Vite default asset), Vite/React Starter Template Bootstrap

### Community 106 - "ValiderProcessusResponseDto"
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
- **209 isolated node(s):** `com.afriland:dottel`, `VALIDATION_ARH`, `VALIDATION_CRH`, `VALIDATION_DRH`, `EN_COURS_ARH` (+204 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **33 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Identité de marque Afriland First Bank` and `Symbole concentrique "C/E" sur bloc rouge`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Motif cercle (cx28 cy30 r7)` and `Motif chevron / toit (polyline 110,50 120,36 130,50)`?**
  _Edge tagged AMBIGUOUS (relation: semantically_similar_to) - confidence is low._
- **What is the exact relationship between `Variables d'environnement Keycloak (DOTTEL_KEYCLOAK_ISSUER_URI, VITE_KEYCLOAK_*)` and `RoleJwtAuthenticationConverter (forme du claim de rôle)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `EvenementAudit` connect `ProcessusMensuelRepository` to `ProcessusMensuelController.java`, `GrilleTarifaireServiceTest.java`, `GlobalExceptionHandlerTest`, `Décision C - Découpage 6 modules`, `BeneficiaireApiImpl`, `EnrolementServiceTest.java`, `BeneficiairePageResponseDto`, `RoleJwtAuthenticationConverter`, `.findByCode`, `Graphify Skill Reference Files`, `BeneficiaireService.java`, `BeneficiaireServiceTest`, `RECAPITULATIF_CHANTIER.md`, `.confirmer`?**
  _High betweenness centrality (0.067) - this node is a cross-community bridge._
- **Why does `AuthenticatedUserService` connect `ProcessusMensuelRepository` to `ProcessusMensuelController.java`, `SPRINT MM.8`, `BeneficiaireController.java`, `AuthProviderKeycloak.js`, `Décision C - Découpage 6 modules`, `ProcessusMensuel`, `.findByCode`, `dependencies`, `Graphify Skill Reference Files`, `FonctionEligibleAdminResponseDto`, `.confirmer`?**
  _High betweenness centrality (0.052) - this node is a cross-community bridge._
- **Why does `Utilisateur` connect `Graphify Skill Reference Files` to `ProcessusMensuelController.java`, `GlobalExceptionHandlerTest`, `Décision C - Découpage 6 modules`, `PieceJointe`, `ProcessusMensuel`, `ProcessusMensuelRepository`, `.findByCode`, `dependencies`, `BeneficiaireServiceTest`, `RECAPITULATIF_CHANTIER.md`, `.confirmer`?**
  _High betweenness centrality (0.048) - this node is a cross-community bridge._
- **What connects `com.afriland:dottel`, `VALIDATION_ARH`, `VALIDATION_CRH` to the rest of the system?**
  _209 weakly-connected nodes found - possible documentation gaps or missing edges._