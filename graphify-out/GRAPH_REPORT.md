# Graph Report - .  (2026-08-08)

## Corpus Check
- 17 files · ~178,548 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2162 nodes · 5591 edges · 156 communities (105 shown, 51 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 583 edges (avg confidence: 0.8)
- Token cost: 151,821 input · 4,000 output

## Community Hubs (Navigation)
- Community 0
- Community 1
- Community 2
- Community 3
- Community 4
- Community 5
- Community 6
- Community 7
- Community 8
- Community 9
- Community 10
- Community 11
- Community 12
- Community 13
- Community 14
- Community 15
- Community 16
- Community 17
- Community 18
- Community 19
- Community 20
- Community 21
- Community 22
- Community 23
- Community 24
- Community 25
- Community 26
- Community 27
- Community 28
- Community 29
- Community 30
- Community 31
- Community 32
- Community 33
- Community 34
- Community 35
- Community 36
- Community 37
- Community 38
- Community 39
- Community 40
- Community 41
- Community 42
- Community 43
- Community 44
- Community 45
- Community 46
- Community 47
- Community 48
- Community 49
- Community 50
- Community 51
- Community 52
- Community 53
- Community 54
- Community 55
- Community 56
- Community 57
- Community 58
- Community 59
- Community 60
- Community 61
- Community 62
- Community 63
- Community 64
- Community 65
- Community 66
- Community 67
- Community 68
- Community 69
- Community 70
- Community 71
- Community 72
- Community 73
- Community 74
- Community 75
- Community 76
- Community 77
- Community 78
- Community 79
- Community 80
- Community 81
- Community 82
- Community 83
- Community 84
- Community 85
- Community 86
- Community 87
- Community 88
- Community 89
- Community 90
- Community 91
- Community 92
- Community 93
- Community 94
- Community 95
- Community 96
- Community 97
- Community 98
- Community 99
- Community 100
- Community 101
- Community 102
- Community 103
- Community 104
- Community 105
- Community 106
- Community 107
- Community 108
- Community 109
- Community 110
- Community 111
- Community 112
- Community 113
- Community 114
- Community 115
- Community 116
- Community 117
- Community 118
- Community 119
- Community 120
- Community 121
- Community 122
- Community 123
- Community 124
- Community 125
- Community 126
- Community 127
- Community 128
- Community 130
- Community 131
- Community 132
- Community 133
- Community 134
- Community 135
- Community 136
- Community 137
- Community 138
- Community 139
- Community 141
- Community 142
- Community 143
- Community 144
- Community 145
- Community 146
- Community 147
- Community 148
- Community 149
- Community 150
- Community 151
- Community 152
- Community 153
- Community 155

## God Nodes (most connected - your core abstractions)
1. `ProcessusMensuelServiceTest` - 87 edges
2. `Utilisateur` - 43 edges
3. `Beneficiaire` - 38 edges
4. `FonctionEligible` - 38 edges
5. `react` - 38 edges
6. `ProcessusMensuelService` - 38 edges
7. `GlobalExceptionHandler` - 37 edges
8. `GrilleTarifaireServiceTest` - 36 edges
9. `EvenementAudit` - 35 edges
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

## Communities (156 total, 51 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.06
Nodes (109): apiClient, App(), AppLayout(), PageHeader(), initiales(), NAV_LINKS, Sidebar(), trouverHrefActif() (+101 more)

### Community 1 - "Community 1"
Cohesion: 0.05
Nodes (51): BeneficiaireController, GetMapping, MultipartFile, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor (+43 more)

### Community 2 - "Community 2"
Cohesion: 0.07
Nodes (30): AuditLogRepository, BeneficiaireDocumentDto, BeneficiaireDotationDto, BeneficiaireIdentiteDto, Beneficiaire, AllArgsConstructor, Builder, Entity (+22 more)

### Community 3 - "Community 3"
Cohesion: 0.07
Nodes (32): EtapeWorkflowIntrouvableException, RoleEtapeNonAutoriseException, SeparationTachesViolationException, EtapeWorkflow, AllArgsConstructor, Builder, Entity, Getter (+24 more)

### Community 4 - "Community 4"
Cohesion: 0.05
Nodes (52): application.yml — configuration Spring dottel, dottel.kafka.topic-cloture (dottel.processus.cloture), DOTTEL_KEYCLOAK_ISSUER_URI (issuer-uri OAuth2 resource server), Contrats API : 34 endpoints, Déploiement Kubernetes (microservice conteneurisé), Erreurs à ne jamais commettre (20 points), Simulation Keycloak en développement, Module DOTTEL (Dotations Téléphoniques Mensuelles) (+44 more)

### Community 5 - "Community 5"
Cohesion: 0.10
Nodes (12): AccessDeniedException, GlobalExceptionHandler, Logger, MethodArgumentNotValidException, ResponseEntity, GlobalExceptionHandlerTest, ExtendWith, MethodArgumentNotValidException (+4 more)

### Community 6 - "Community 6"
Cohesion: 0.06
Nodes (30): MotifRejetObligatoireException, PieceJointeIntrouvableException, ProcessusMensuelExisteDejaException, ProcessusMensuelIntrouvableException, ProcessusMensuelNonModifiableException, AjustementLigneEtatDto, ApplicationEventPublisher, AuthenticatedUserService (+22 more)

### Community 7 - "Community 7"
Cohesion: 0.08
Nodes (46): BeneficiaireApi, BeneficiaireDocumentDto, Code agence (5 chiffres), Code unité (4 chiffres), Sprint MM.10 — Référentiel unité/agence, EhrIntegrationServiceStub, Décision A2 — affichage rattrapage (tout afficher, non-payés pré-cochés), Sprint MM.11 — Période de déclenchement et rattrapage (+38 more)

### Community 8 - "Community 8"
Cohesion: 0.12
Nodes (25): DocumentService, EcartMensuelService, LigneDocumentDto, LigneEtatMensuel, NomEtapeEnum, PieceJointe, PieceJointeRepository, ProcessusMensuel (+17 more)

### Community 9 - "Community 9"
Cohesion: 0.09
Nodes (22): NotificationService, Override, Service, NotificationServiceStub, DestinataireNotificationDto, UtilisateurApi, UtilisateurIntrouvableException, RoleEnum (+14 more)

### Community 10 - "Community 10"
Cohesion: 0.05
Nodes (39): Décision A - EligibiliteService (A1), Décision B - Référentiel unifié (B1), Décision C - Découpage 6 modules, EligibiliteService, Module audit, Module beneficiaires, Module integration (proposé puis rejeté, réparti), Module processus (+31 more)

### Community 11 - "Community 11"
Cohesion: 0.10
Nodes (21): ApplicationEventPublisher, AuthenticatedUserService, BeneficiaireApi, EligibiliteService, EtapeWorkflowRepository, EvenementClotureService, ExtendWith, FonctionEligibleApi (+13 more)

### Community 12 - "Community 12"
Cohesion: 0.08
Nodes (17): DateDebutGrilleAnterieureException, DecisionGrilleInvalideException, FonctionEligibleIntrouvableException, GrilleEnAttenteDrhExistanteException, GrilleIntrouvableException, GrilleNonActiveException, GrilleNonEnAttenteDrhException, GrilleNonModifiableException (+9 more)

### Community 13 - "Community 13"
Cohesion: 0.07
Nodes (39): application-dev.yml (profil developpement), Repli en clair DB_PASSWORD:postgres (profil dev), application-prod.yml (profil production), server.error.* (prefixe corrige, ecart E6), Rapport d'audit de securite OWASP V1 (AFB_AUDIT_DOTTEL_V1_2026), A01 — Alignement des routes frontend sur la matrice de roles, A03 — Injection et echappement (zero dangerouslySetInnerHTML), Ecart E1 — CRH redirige vers /processus (ROUTE_PAR_ROLE) (+31 more)

### Community 14 - "Community 14"
Cohesion: 0.09
Nodes (23): MatriculeDejaEnroleException, MatriculeInconnuException, NonEligibleException, ConfirmerEnrolementResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor (+15 more)

### Community 16 - "Community 16"
Cohesion: 0.20
Nodes (3): BeneficiaireIntrouvableException, BeneficiaireServiceTest, Test

### Community 17 - "Community 17"
Cohesion: 0.16
Nodes (17): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, LigneEtatMensuel (+9 more)

### Community 19 - "Community 19"
Cohesion: 0.12
Nodes (4): ProcessusMensuel, Test, ProcessusDetailResponseDto, ValiderProcessusResponseDto

### Community 20 - "Community 20"
Cohesion: 0.14
Nodes (18): Component, Logger, Override, RoleJwtAuthenticationConverter, Bean, Configuration, EnableMethodSecurity, PasswordEncoder (+10 more)

### Community 21 - "Community 21"
Cohesion: 0.13
Nodes (20): EvenementClotureSerializer, ObjectMapper, Override, Bean, Configuration, KafkaTemplate, ObjectMapper, KafkaConfig (+12 more)

### Community 22 - "Community 22"
Cohesion: 0.13
Nodes (19): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, ProcessusMensuel (+11 more)

### Community 23 - "Community 23"
Cohesion: 0.08
Nodes (29): Project .claude/CLAUDE.md (graphify trigger), /graphify add <url>, --watch folder watcher, graphify export falkordb / falkordb-push, MCP stdio server (graphify.serve), graphify export neo4j / neo4j-push, graphify export wiki, Confidence score rubric (0.55-0.95 discrete steps) (+21 more)

### Community 24 - "Community 24"
Cohesion: 0.14
Nodes (20): HistoriqueLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, HistoriqueResponseDto, AllArgsConstructor (+12 more)

### Community 25 - "Community 25"
Cohesion: 0.26
Nodes (4): Override, Transactional, FonctionEligibleServiceTest, Test

### Community 26 - "Community 26"
Cohesion: 0.17
Nodes (15): EligibiliteService, RequiredArgsConstructor, Service, FonctionEligible, AllArgsConstructor, Builder, Entity, Getter (+7 more)

### Community 27 - "Community 27"
Cohesion: 0.11
Nodes (13): ActionAdminNonAutoriseeException, EmailUtilisateurDejaUtiliseException, MatriculeUtilisateurDejaUtiliseException, RoleInvalideException, ApplicationEventPublisher, PasswordEncoder, RequiredArgsConstructor, Service (+5 more)

### Community 28 - "Community 28"
Cohesion: 0.19
Nodes (15): FonctionEligibleController, GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity (+7 more)

### Community 29 - "Community 29"
Cohesion: 0.24
Nodes (4): EvenementAudit, Transactional, Test, UtilisateurAdminServiceTest

### Community 30 - "Community 30"
Cohesion: 0.32
Nodes (5): ConfirmerEnrolementRequestDto, Getter, Setter, EnrolementServiceTest, Test

### Community 31 - "Community 31"
Cohesion: 0.09
Nodes (23): @ApplicationModule(allowedDependencies = {...}), docs/monolithe-modulaire/architecture/ (diagrammes PlantUML et canvases versionnés), docs/audit_securite_owasp_v1.md, AuditService, CLAUDE.md, Cycle beneficiaires <-> referentiel (couplage C3), DocumentationTests.java, Documenter (org.springframework.modulith.docs) (+15 more)

### Community 32 - "Community 32"
Cohesion: 0.16
Nodes (17): GetMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, PieceJointeController, AllArgsConstructor (+9 more)

### Community 33 - "Community 33"
Cohesion: 0.12
Nodes (11): BeneficiaireApi, FonctionEligibleBeneficiairesActifsException, FonctionEligibleCodeDejaUtiliseException, CreerFonctionEligibleRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor (+3 more)

### Community 34 - "Community 34"
Cohesion: 0.20
Nodes (15): GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+7 more)

### Community 35 - "Community 35"
Cohesion: 0.18
Nodes (11): AuditEventListener, Component, Logger, ObjectMapper, RequiredArgsConstructor, AuditEventListenerTest, AfterEach, BeforeEach (+3 more)

### Community 36 - "Community 36"
Cohesion: 0.20
Nodes (14): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, UtilisateurResponseDto, EnableMethodSecurity, Test (+6 more)

### Community 37 - "Community 37"
Cohesion: 0.20
Nodes (10): AuditServiceImpl, Override, Page, Pageable, RequiredArgsConstructor, Service, Transactional, AuditServiceImplTest (+2 more)

### Community 38 - "Community 38"
Cohesion: 0.27
Nodes (9): GrilleTarifaireController, GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity (+1 more)

### Community 39 - "Community 39"
Cohesion: 0.16
Nodes (8): setAuthToken(), AuthProvider, AuthProviderKeycloak, base64UrlEncode(), calculerCodeChallenge(), construireUtilisateur(), genererValeurAleatoire(), ROLES_DOTTEL

### Community 40 - "Community 40"
Cohesion: 0.22
Nodes (13): EnrolementController, GetMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+5 more)

### Community 41 - "Community 41"
Cohesion: 0.29
Nodes (9): GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+1 more)

### Community 42 - "Community 42"
Cohesion: 0.20
Nodes (12): SignatureService, Override, Service, SignatureServiceAutonome, AllArgsConstructor, Builder, Entity, Getter (+4 more)

### Community 43 - "Community 43"
Cohesion: 0.24
Nodes (7): GrilleTarifaireApiImpl, Override, RequiredArgsConstructor, Service, GrilleTarifaireApiImplTest, ExtendWith, Test

### Community 44 - "Community 44"
Cohesion: 0.25
Nodes (10): AuditService, Page, Pageable, GetMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity (+2 more)

### Community 45 - "Community 45"
Cohesion: 0.22
Nodes (10): EmployeEhrDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, EhrIntegrationService, EhrIntegrationServiceStub, Override (+2 more)

### Community 46 - "Community 46"
Cohesion: 0.20
Nodes (10): BeneficiaireSpecifications, Specification, BeneficiaireService, ApplicationEventPublisher, Page, Pageable, RequiredArgsConstructor, Service (+2 more)

### Community 48 - "Community 48"
Cohesion: 0.22
Nodes (13): GrilleTarifaire, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+5 more)

### Community 49 - "Community 49"
Cohesion: 0.24
Nodes (5): UtilisateurConnecteIntrouvableException, AuthenticatedUserServiceTest, AfterEach, ExtendWith, Test

### Community 50 - "Community 50"
Cohesion: 0.13
Nodes (15): class-variance-authority, date-fns, dependencies, class-variance-authority, date-fns, @hookform/resolvers, lucide-react, @radix-ui/react-checkbox (+7 more)

### Community 51 - "Community 51"
Cohesion: 0.13
Nodes (15): devDependencies, oxlint, tailwindcss, @tailwindcss/vite, @types/react, @types/react-dom, vite, @vitejs/plugin-react (+7 more)

### Community 52 - "Community 52"
Cohesion: 0.24
Nodes (10): AuditLog, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+2 more)

### Community 53 - "Community 53"
Cohesion: 0.25
Nodes (12): GrilleHistoriqueLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, HistoriqueGrilleTarifaireResponseDto, AllArgsConstructor (+4 more)

### Community 54 - "Community 54"
Cohesion: 0.25
Nodes (12): GrilleTarifaireListeLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, GrilleTarifaireListeResponseDto, AllArgsConstructor (+4 more)

### Community 55 - "Community 55"
Cohesion: 0.26
Nodes (14): Dictionnaire de données Dotations V3, Table audit_log, Table beneficiaires, Table etape_workflow, Table fonction_eligible, Table grille_tarifaire, Table ligne_etat_mensuel (pivot N-N), Table piece_jointe (+6 more)

### Community 56 - "Community 56"
Cohesion: 0.32
Nodes (10): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ModifierFonctionEligibleRequestDto, FonctionEligibleService, ApplicationEventPublisher (+2 more)

### Community 57 - "Community 57"
Cohesion: 0.29
Nodes (7): GrilleTarifaireResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, Transactional

### Community 58 - "Community 58"
Cohesion: 0.31
Nodes (6): FonctionEligibleResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 59 - "Community 59"
Cohesion: 0.29
Nodes (8): DashboardResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, BeforeEach, ExtendWith

### Community 60 - "Community 60"
Cohesion: 0.33
Nodes (6): mvnw script, clean(), die(), exec_maven(), set_java_home(), verbose()

### Community 61 - "Community 61"
Cohesion: 0.36
Nodes (7): AuditLogPageResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Page, Setter

### Community 62 - "Community 62"
Cohesion: 0.20
Nodes (9): name, private, scripts, build, dev, lint, preview, type (+1 more)

### Community 63 - "Community 63"
Cohesion: 0.39
Nodes (7): AuditLogResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ExtendWith

### Community 64 - "Community 64"
Cohesion: 0.39
Nodes (9): Afriland First Bank Logo (PNG asset), Identité de marque Afriland First Bank, Charte visuelle frontend BAOBAB / DOTTEL, Palette de marque rouge / noir / gris / blanc, Rouge institutionnel AFB (E30613), Symbole concentrique "C/E" sur bloc rouge, Asset PNG horizontal à fond transparent importable par Vite, Usage : logo en haut de la barre de navigation latérale (+1 more)

### Community 65 - "Community 65"
Cohesion: 0.42
Nodes (9): Charte visuelle AFB - fond clair sobre, aucun rouge E30613 dans le fond, Filigrane discret de la zone de contenu principale (.fond-filigrane), Motif chevron / toit (polyline 110,50 120,36 130,50), Motif cercle (cx28 cy30 r7), Motif deux lignes horizontales (lignes de document/etat), Motif oeil (lentille + pupille, controle/supervision), Style de trait unique (fill=none, stroke #1A1A1A, width 1.5, opacity 0.07), Tuile repetable 160x160 (background-repeat) (+1 more)

### Community 66 - "Community 66"
Cohesion: 0.39
Nodes (6): AjustementLigneEtatDto, Getter, Setter, Getter, Setter, PatchProcessusRequestDto

### Community 67 - "Community 67"
Cohesion: 0.39
Nodes (3): Transactional, DeclencherProcessusRequestDto, ProcessusMensuelResponseDto

### Community 68 - "Community 68"
Cohesion: 0.43
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusEnCoursDto

### Community 69 - "Community 69"
Cohesion: 0.25
Nodes (7): plugins, rules, react/only-export-components, react/rules-of-hooks, $schema, oxc, warn

### Community 70 - "Community 70"
Cohesion: 0.29
Nodes (8): Sprint 3.4 guide — Validation ARH et génération du PDF initial (US-11), Champ CHAPITRE (donnée EHR par bénéficiaire, décision révisée depuis 'valeur fixe 64310000' vers donnée variable + repli configurable), DocumentService.genererInitiale() (iText 8 PDF, RG-06), Entité EtapeWorkflow (NomEtapeEnum, StatutEtapeEnum, signature_numerique), NotificationService (stub log, appelé par ProcessusMensuelService.valider), Entité PieceJointe (UNIQUE par processus, nombre_signatures), Point de vigilance RG-08 : contrat API mentionne la vérification sur /valider, mais SeparationTachesService est reporté au Sprint 5, SignatureService (interface isolée, trace non-certifiée, remplaçable par intégration type INTRA)

### Community 71 - "Community 71"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ModifierBeneficiaireRequestDto

### Community 72 - "Community 72"
Cohesion: 0.43
Nodes (5): CorsConfig, Configuration, Override, CorsRegistry, WebMvcConfigurer

### Community 73 - "Community 73"
Cohesion: 0.52
Nodes (6): BeneficiaireExcluDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 74 - "Community 74"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, LigneDocumentDto

### Community 75 - "Community 75"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, LigneEtatMensuelDetailDto

### Community 76 - "Community 76"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, PatchProcessusResponseDto

### Community 77 - "Community 77"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, PieceJointeMetadonneesResponseDto

### Community 78 - "Community 78"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusDetailResponseDto

### Community 79 - "Community 79"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusListItemDto

### Community 80 - "Community 80"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusMensuelResponseDto

### Community 81 - "Community 81"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ResultatAjustementDto

### Community 82 - "Community 82"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, RetournerProcessusRequestDto

### Community 83 - "Community 83"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, RetournerProcessusResponseDto

### Community 84 - "Community 84"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ValiderProcessusResponseDto

### Community 85 - "Community 85"
Cohesion: 0.52
Nodes (6): CreerGrilleTarifaireRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 86 - "Community 86"
Cohesion: 0.52
Nodes (6): DecisionGrilleTarifaireRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 87 - "Community 87"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ModifierGrilleTarifaireRequestDto

### Community 88 - "Community 88"
Cohesion: 0.48
Nodes (5): AuthController, PostMapping, RequestMapping, ResponseEntity, RestController

### Community 89 - "Community 89"
Cohesion: 0.52
Nodes (6): ChangerRoleRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 90 - "Community 90"
Cohesion: 0.52
Nodes (6): ChangerStatutRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 91 - "Community 91"
Cohesion: 0.52
Nodes (6): CreerUtilisateurRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 92 - "Community 92"
Cohesion: 0.29
Nodes (7): icons.svg (Icon Sprite Sheet), Bluesky Icon Symbol, Discord Icon Symbol, Documentation Icon Symbol, GitHub Icon Symbol, Social/Users Icon Symbol, X (Twitter) Icon Symbol

### Community 93 - "Community 93"
Cohesion: 0.29
Nodes (7): k8s deployment.yaml (dottel-backend), /api/actuator/health liveness/readiness probe, Deployment dottel-backend, Image harbor.afrilandfirstbank.cm/baobab/dottel-backend:latest, Secret dottel-secret (referenced), k8s service.yaml (dottel-backend-svc), Service dottel-backend-svc (ClusterIP)

### Community 94 - "Community 94"
Cohesion: 0.40
Nodes (6): AuditService (interface), AuditServiceImpl.enregistrer(), Options E-1/E-2 conception des événements d'audit, Options T-1/T-2/T-3 mode transactionnel de l'écouteur, Piège 2 - adresse IP (RequestContextHolder), Piège 1 - transactionnalité (rollback conjoint)

### Community 95 - "Community 95"
Cohesion: 0.33
Nodes (5): API_BASE_URL, ROLES, STATUTS, STATUTS_GRILLE, STATUTS_LABELS

### Community 96 - "Community 96"
Cohesion: 0.60
Nodes (3): ApplicationModules, Test, ModularityTests

### Community 97 - "Community 97"
Cohesion: 0.60
Nodes (3): DottelApplicationTests, Test, SpringBootTest

### Community 98 - "Community 98"
Cohesion: 0.40
Nodes (5): apiClient.js (jeton en mémoire seule), AuthContext.jsx (câblé au singleton authProviderLocal), AuthProvider.js (contrat frontend à 3 méthodes), AuthProviderLocal.js (implémentation matricule/mot de passe), Décision F-2 — Authorization Code + PKCE

### Community 100 - "Community 100"
Cohesion: 0.83
Nodes (3): DeclencherProcessusRequestDto, Getter, Setter

### Community 101 - "Community 101"
Cohesion: 0.83
Nodes (3): Getter, Setter, ValiderProcessusRequestDto

### Community 103 - "Community 103"
Cohesion: 0.67
Nodes (4): AuthenticatedUserService (résolution par claim email), Décision I-2 — résolution d'identité par email, Utilisateur.java (entité, sans champ d'identité externe), V3__insertion_utilisateurs_test.sql (5 utilisateurs de test, écart tiret bas corrigé)

### Community 104 - "Community 104"
Cohesion: 0.67
Nodes (3): Nécessité du démarrage réel (au-delà des tests unitaires Mockito), DottelApplication, DottelApplicationTests

### Community 105 - "Community 105"
Cohesion: 0.67
Nodes (3): React Framework, React Logo (Vite default asset), Vite/React Starter Template Bootstrap

### Community 106 - "Community 106"
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
- **213 isolated node(s):** `com.afriland:dottel`, `audit/api/package-info.java`, `audit/package-info.java`, `beneficiaires/api/package-info.java`, `beneficiaires/exception/package-info.java` (+208 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **51 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Identité de marque Afriland First Bank` and `Symbole concentrique "C/E" sur bloc rouge`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Motif cercle (cx28 cy30 r7)` and `Motif chevron / toit (polyline 110,50 120,36 130,50)`?**
  _Edge tagged AMBIGUOUS (relation: semantically_similar_to) - confidence is low._
- **What is the exact relationship between `Variables d'environnement Keycloak (DOTTEL_KEYCLOAK_ISSUER_URI, VITE_KEYCLOAK_*)` and `RoleJwtAuthenticationConverter (forme du claim de rôle)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `BeneficiaireRepository` connect `Community 2` to `Community 1`, `Community 46`, `Community 14`, `Community 47`, `Community 16`, `Community 22`, `Community 59`, `Community 30`?**
  _High betweenness centrality (0.082) - this node is a cross-community bridge._
- **Why does `EvenementAudit` connect `Community 29` to `Community 1`, `Community 33`, `Community 35`, `Community 6`, `Community 12`, `Community 46`, `Community 14`, `Community 16`, `Community 15`, `Community 56`, `Community 25`, `Community 27`, `Community 30`, `Community 57`?**
  _High betweenness centrality (0.072) - this node is a cross-community bridge._
- **Why does `StatutEnum` connect `Community 22` to `Community 68`, `Community 41`, `Community 78`, `Community 79`, `Community 80`, `Community 47`, `Community 83`, `Community 84`, `Community 24`, `Community 59`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **What connects `com.afriland:dottel`, `audit/api/package-info.java`, `audit/package-info.java` to the rest of the system?**
  _213 weakly-connected nodes found - possible documentation gaps or missing edges._