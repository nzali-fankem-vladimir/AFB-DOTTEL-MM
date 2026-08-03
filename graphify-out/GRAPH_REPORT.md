# Graph Report - .  (2026-08-02)

## Corpus Check
- 20 files · ~150,508 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2065 nodes · 5621 edges · 131 communities (97 shown, 34 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 668 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

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
- Community 120
- Community 123
- Community 124
- Community 125
- Community 126
- Community 127
- Community 128
- Community 130

## God Nodes (most connected - your core abstractions)
1. `ProcessusMensuelServiceTest` - 85 edges
2. `Utilisateur` - 62 edges
3. `FonctionEligible` - 52 edges
4. `ProcessusMensuel` - 42 edges
5. `Beneficiaire` - 40 edges
6. `BeneficiaireRepository` - 38 edges
7. `GlobalExceptionHandler` - 38 edges
8. `react` - 37 edges
9. `GrilleTarifaireServiceTest` - 36 edges
10. `ProcessusMensuelService` - 36 edges

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
- **Gestion applicative du référentiel fonction_eligible (Sprint 6F.7bis)** — docs_reference_contrats_api_dotations_v3_get_fonctions_eligibles, docs_reference_contrats_api_dotations_v3_get_fonctions_eligibles_toutes, docs_reference_contrats_api_dotations_v3_post_fonctions_eligibles, docs_reference_contrats_api_dotations_v3_patch_fonction_eligible [EXTRACTED 1.00]
- **grille_tarifaire table, StatutGrilleEnum, RG-04, RG-10 and the grilles-tarifaires API group form the tariff-grid validation lifecycle** — docs_reference_dictionnaire_de_donnees_dotations_v3_grille_tarifaire, docs_reference_dictionnaire_de_donnees_dotations_v3_statutgrilleenum [INFERRED 0.85]
- **Kubernetes deployment pipeline for dottel-backend** — k8s_deployment_yaml_dottel_backend, k8s_service_yaml_dottel_backend_svc [EXTRACTED 1.00]
- **Secret/env var externalization from application.yml to K8s Secret/ConfigMap** — k8s_deployment_yaml_dottel_secret [INFERRED 0.85]
- **RG-08 séparation des tâches : écart entre contrat API et périmètre Sprint 3.4** — sprint_3_4, sprint_3_4_rg08_separation_taches [EXTRACTED 0.90]
- **Flux de génération du PDF initial (DocumentService + PieceJointe + SignatureService + CHAPITRE)** — sprint_3_4_documentservice, sprint_3_4_piecejointe, sprint_3_4_signatureservice, sprint_3_4_chapitre_field [EXTRACTED 0.90]
- **Flux d'authentification frontend (6F.3) et son verdict d'audit** — sprint6f_6f_3_authprovider_abstraction, sprint6f_6f_3_authproviderlocal, sprint6f_6f_3_authcontext, sprint6f_6f_3_apiclient_intercepteur_jwt, sprint6f_6f_3_protectedroute, docs_audit_securite_owasp_v1_stockage_token_memoire [EXTRACTED 1.00]

## Communities (131 total, 34 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.05
Nodes (112): apiClient, setAuthToken(), App(), AuthProvider, AuthProviderLocal, AppLayout(), PageHeader(), initiales() (+104 more)

### Community 1 - "Community 1"
Cohesion: 0.07
Nodes (45): AuditService, BeneficiaireRepository, FonctionEligibleController, GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping (+37 more)

### Community 2 - "Community 2"
Cohesion: 0.06
Nodes (44): EnrolementController, GetMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+36 more)

### Community 3 - "Community 3"
Cohesion: 0.07
Nodes (18): AccessDeniedException, PieceJointeIntrouvableException, ProcessusMensuelExisteDejaException, ProcessusMensuelIntrouvableException, ProcessusMensuelNonModifiableException, GlobalExceptionHandler, Logger, MethodArgumentNotValidException (+10 more)

### Community 4 - "Community 4"
Cohesion: 0.07
Nodes (35): AfterEach, AuditLog, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter (+27 more)

### Community 5 - "Community 5"
Cohesion: 0.08
Nodes (33): GetMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, PieceJointeController, AllArgsConstructor (+25 more)

### Community 6 - "Community 6"
Cohesion: 0.09
Nodes (26): EtapeWorkflowIntrouvableException, RoleEtapeNonAutoriseException, SeparationTachesViolationException, EtapeWorkflow, AllArgsConstructor, Builder, Entity, Getter (+18 more)

### Community 7 - "Community 7"
Cohesion: 0.06
Nodes (48): application.yml (configuration de base Spring Boot), Actuator health show-details always (sondes Kubernetes), app.cors.allowed-origins (jamais de wildcard), application-dev.yml (profil developpement), Repli en clair DB_PASSWORD:postgres (profil dev), dottel.documents.chapitre-defaut (repli chapitre EHR), dottel.documents.chemin-stockage (stockage local du PDF), Configuration Flyway (classpath:db/migration) (+40 more)

### Community 8 - "Community 8"
Cohesion: 0.11
Nodes (9): ProcessusMensuel, DocumentService, EvenementClotureService, SeparationTachesService, SignatureService, Test, ProcessusMensuelServiceTest, ProcessusDetailResponseDto (+1 more)

### Community 9 - "Community 9"
Cohesion: 0.09
Nodes (21): NotificationService, Override, Service, NotificationServiceStub, DestinataireNotificationDto, RoleEnum, RoleEnum, UtilisateurApi (+13 more)

### Community 10 - "Community 10"
Cohesion: 0.05
Nodes (45): Données de test camerounaises réalistes obligatoires, Jamais d'entité JPA dans une réponse API — DTO obligatoire, fonction_eligible — référentiel des 25 fonctions, grille_tarifaire — source unique du montant, LigneEtatMensuel — table pivot N-N obligatoire, Modèle de données — 10 tables, Module DOTTEL — Dotations Téléphoniques Mensuelles, RG-01 — éligibilité par fonction_eligible.actif (+37 more)

### Community 11 - "Community 11"
Cohesion: 0.11
Nodes (22): MotifRejetObligatoireException, GrilleTarifaireController, GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor (+14 more)

### Community 12 - "Community 12"
Cohesion: 0.12
Nodes (13): ActionAdminNonAutoriseeException, RoleInvalideException, UtilisateurIntrouvableException, PasswordEncoder, RequiredArgsConstructor, Service, Transactional, UtilisateurAdminService (+5 more)

### Community 13 - "Community 13"
Cohesion: 0.08
Nodes (33): AjustementLigneEtatDto, AuditService, AuthenticatedUserService, DocumentService, EligibiliteService, EtapeWorkflowRepository, EvenementClotureService, LigneEtatMensuelRepository (+25 more)

### Community 14 - "Community 14"
Cohesion: 0.13
Nodes (23): AuthController, PostMapping, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, Getter, Setter (+15 more)

### Community 16 - "Community 16"
Cohesion: 0.10
Nodes (26): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusListItemDto, StatutEnum, CLOTURE (+18 more)

### Community 17 - "Community 17"
Cohesion: 0.14
Nodes (17): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, LigneEtatMensuel (+9 more)

### Community 19 - "Community 19"
Cohesion: 0.19
Nodes (9): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ModifierBeneficiaireRequestDto, BeneficiaireServiceTest, ExtendWith (+1 more)

### Community 20 - "Community 20"
Cohesion: 0.11
Nodes (21): Component, JwtUtil, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter (+13 more)

### Community 21 - "Community 21"
Cohesion: 0.11
Nodes (17): BeneficiaireIntrouvableException, NonEligibleException, BeneficiaireResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter (+9 more)

### Community 22 - "Community 22"
Cohesion: 0.18
Nodes (10): FichierImportInvalideException, BeneficiaireImportService, MultipartFile, RequiredArgsConstructor, Row, Service, Transactional, BeneficiaireImportServiceTest (+2 more)

### Community 23 - "Community 23"
Cohesion: 0.13
Nodes (17): Beneficiaire, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+9 more)

### Community 24 - "Community 24"
Cohesion: 0.13
Nodes (20): EvenementClotureSerializer, ObjectMapper, Override, Bean, Configuration, KafkaTemplate, ObjectMapper, KafkaConfig (+12 more)

### Community 25 - "Community 25"
Cohesion: 0.08
Nodes (29): Project .claude/CLAUDE.md (graphify trigger), /graphify add <url>, --watch folder watcher, graphify export falkordb / falkordb-push, MCP stdio server (graphify.serve), graphify export neo4j / neo4j-push, graphify export wiki, Confidence score rubric (0.55-0.95 discrete steps) (+21 more)

### Community 26 - "Community 26"
Cohesion: 0.16
Nodes (15): FonctionEligible, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+7 more)

### Community 27 - "Community 27"
Cohesion: 0.16
Nodes (18): BeneficiaireController, GetMapping, MultipartFile, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor (+10 more)

### Community 28 - "Community 28"
Cohesion: 0.14
Nodes (17): Component, Override, RoleJwtAuthenticationConverter, Bean, Configuration, EnableMethodSecurity, PasswordEncoder, SecurityConfig (+9 more)

### Community 29 - "Community 29"
Cohesion: 0.10
Nodes (13): DateDebutGrilleAnterieureException, GrilleEnAttenteDrhExistanteException, GrilleNonActiveException, GrilleNonEnAttenteDrhException, GrilleNonModifiableException, HistoriqueGrilleTarifaireResponseDto, AllArgsConstructor, Builder (+5 more)

### Community 30 - "Community 30"
Cohesion: 0.17
Nodes (12): BeneficiaireApi, BeneficiaireDotationDto, BeneficiaireIdentiteDto, BeneficiaireApiImpl, BeneficiaireRepository, Override, RequiredArgsConstructor, Service (+4 more)

### Community 31 - "Community 31"
Cohesion: 0.17
Nodes (13): GrilleTarifaireApi, ResolutionGrilleDto, GrilleTarifaireApiImpl, FonctionEligibleRepository, GrilleTarifaireRepository, Override, RequiredArgsConstructor, Service (+5 more)

### Community 32 - "Community 32"
Cohesion: 0.20
Nodes (14): GetMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, ReportingController, AuditLogPageResponseDto (+6 more)

### Community 33 - "Community 33"
Cohesion: 0.25
Nodes (3): Transactional, Test, ReportingServiceTest

### Community 34 - "Community 34"
Cohesion: 0.20
Nodes (14): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, UtilisateurResponseDto, EnableMethodSecurity, Test (+6 more)

### Community 35 - "Community 35"
Cohesion: 0.29
Nodes (9): GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+1 more)

### Community 36 - "Community 36"
Cohesion: 0.18
Nodes (8): EmailUtilisateurDejaUtiliseException, MatriculeUtilisateurDejaUtiliseException, CreerUtilisateurRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 37 - "Community 37"
Cohesion: 0.20
Nodes (13): ImportErreurDto, AllArgsConstructor, Builder, Data, NoArgsConstructor, ImportRapportDto, AllArgsConstructor, Builder (+5 more)

### Community 38 - "Community 38"
Cohesion: 0.21
Nodes (14): GrilleTarifaire, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+6 more)

### Community 39 - "Community 39"
Cohesion: 0.13
Nodes (15): class-variance-authority, date-fns, dependencies, class-variance-authority, date-fns, @hookform/resolvers, lucide-react, @radix-ui/react-checkbox (+7 more)

### Community 40 - "Community 40"
Cohesion: 0.13
Nodes (15): devDependencies, oxlint, tailwindcss, @tailwindcss/vite, @types/react, @types/react-dom, vite, @vitejs/plugin-react (+7 more)

### Community 41 - "Community 41"
Cohesion: 0.25
Nodes (12): GrilleTarifaireListeLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, GrilleTarifaireListeResponseDto, AllArgsConstructor (+4 more)

### Community 42 - "Community 42"
Cohesion: 0.31
Nodes (9): GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+1 more)

### Community 43 - "Community 43"
Cohesion: 0.26
Nodes (14): Dictionnaire de données Dotations V3, Table audit_log, Table beneficiaires, Table etape_workflow, Table fonction_eligible, Table grille_tarifaire, Table ligne_etat_mensuel (pivot N-N), Table piece_jointe (+6 more)

### Community 44 - "Community 44"
Cohesion: 0.17
Nodes (13): Décision C - Découpage 6 modules, Module audit, Module beneficiaires, Module integration (proposé puis rejeté, réparti), Module processus, Module reporting, Module utilisateurs, Ordre de déplacement des modules (audit -> processus) (+5 more)

### Community 45 - "Community 45"
Cohesion: 0.29
Nodes (9): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusEnCoursDto, RequiredArgsConstructor, Service (+1 more)

### Community 46 - "Community 46"
Cohesion: 0.29
Nodes (8): DashboardResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, BeforeEach, ExtendWith

### Community 47 - "Community 47"
Cohesion: 0.20
Nodes (11): @PreAuthorize obligatoire sur chaque endpoint sensible, Matrice rôles/endpoints (34 endpoints, 9 groupes), Sécurité stateless JWT / Keycloak, aucune session HTTP serveur, Contrat API AFB_API_DOTTEL_V3.3_2026, JWT HS384, validité 8 heures, header Authorization Bearer, LoginResponseDto (token, matricule, role, nom, prenom), Logout stateless — aucune liste noire de jetons, POST /auth/login (public) (+3 more)

### Community 48 - "Community 48"
Cohesion: 0.33
Nodes (6): mvnw script, clean(), die(), exec_maven(), set_java_home(), verbose()

### Community 49 - "Community 49"
Cohesion: 0.22
Nodes (10): AuditService (interface), AuditServiceImpl.enregistrer(), Options E-1/E-2 conception des événements d'audit, Options T-1/T-2/T-3 mode transactionnel de l'écouteur, Piège 2 - adresse IP (RequestContextHolder), Piège 1 - transactionnalité (rollback conjoint), AuthenticatedUserService, Décision E-3 - enrôlement pour un tiers assumé (+2 more)

### Community 50 - "Community 50"
Cohesion: 0.20
Nodes (9): name, private, scripts, build, dev, lint, preview, type (+1 more)

### Community 51 - "Community 51"
Cohesion: 0.42
Nodes (3): DeclencherProcessusRequestDto, ProcessusMensuelResponseDto, Transactional

### Community 52 - "Community 52"
Cohesion: 0.42
Nodes (8): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, ProcessusMensuel

### Community 53 - "Community 53"
Cohesion: 0.22
Nodes (9): MM.2 Refactor ProcessusMensuelService, ProcessusMensuelService (god service, 16 dépendances), MM.3 Casser couplages restants, MM.4 Audit événementiel, MM.5 API publiques NamedInterface, MM.6 Vérification et documentation, Foyer C1 - ProcessusMensuelService (16 dépendances), Foyer C6 - AuditService appelé en synchrone par 8 services (+1 more)

### Community 54 - "Community 54"
Cohesion: 0.39
Nodes (9): Afriland First Bank Logo (PNG asset), Identité de marque Afriland First Bank, Charte visuelle frontend BAOBAB / DOTTEL, Palette de marque rouge / noir / gris / blanc, Rouge institutionnel AFB (E30613), Symbole concentrique "C/E" sur bloc rouge, Asset PNG horizontal à fond transparent importable par Vite, Usage : logo en haut de la barre de navigation latérale (+1 more)

### Community 55 - "Community 55"
Cohesion: 0.42
Nodes (9): Charte visuelle AFB - fond clair sobre, aucun rouge E30613 dans le fond, Filigrane discret de la zone de contenu principale (.fond-filigrane), Motif chevron / toit (polyline 110,50 120,36 130,50), Motif cercle (cx28 cy30 r7), Motif deux lignes horizontales (lignes de document/etat), Motif oeil (lentille + pupille, controle/supervision), Style de trait unique (fill=none, stroke #1A1A1A, width 1.5, opacity 0.07), Tuile repetable 160x160 (background-repeat) (+1 more)

### Community 56 - "Community 56"
Cohesion: 0.39
Nodes (6): AjustementLigneEtatDto, Getter, Setter, Getter, Setter, PatchProcessusRequestDto

### Community 57 - "Community 57"
Cohesion: 0.43
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ModifierGrilleTarifaireRequestDto

### Community 58 - "Community 58"
Cohesion: 0.25
Nodes (7): plugins, rules, react/only-export-components, react/rules-of-hooks, $schema, oxc, warn

### Community 59 - "Community 59"
Cohesion: 0.29
Nodes (8): Sprint 3.4 guide — Validation ARH et génération du PDF initial (US-11), Champ CHAPITRE (donnée EHR par bénéficiaire, décision révisée depuis 'valeur fixe 64310000' vers donnée variable + repli configurable), DocumentService.genererInitiale() (iText 8 PDF, RG-06), Entité EtapeWorkflow (NomEtapeEnum, StatutEtapeEnum, signature_numerique), NotificationService (stub log, appelé par ProcessusMensuelService.valider), Entité PieceJointe (UNIQUE par processus, nombre_signatures), Point de vigilance RG-08 : contrat API mentionne la vérification sur /valider, mais SeparationTachesService est reporté au Sprint 5, SignatureService (interface isolée, trace non-certifiée, remplaçable par intégration type INTRA)

### Community 60 - "Community 60"
Cohesion: 0.43
Nodes (5): CorsConfig, Configuration, Override, CorsRegistry, WebMvcConfigurer

### Community 61 - "Community 61"
Cohesion: 0.52
Nodes (6): BeneficiaireExcluDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 62 - "Community 62"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, LigneEtatMensuelDetailDto

### Community 63 - "Community 63"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, PatchProcessusResponseDto

### Community 64 - "Community 64"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, PieceJointeMetadonneesResponseDto

### Community 65 - "Community 65"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusDetailResponseDto

### Community 66 - "Community 66"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusMensuelResponseDto

### Community 67 - "Community 67"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ResultatAjustementDto

### Community 68 - "Community 68"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, RetournerProcessusRequestDto

### Community 69 - "Community 69"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, RetournerProcessusResponseDto

### Community 70 - "Community 70"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ValiderProcessusResponseDto

### Community 71 - "Community 71"
Cohesion: 0.52
Nodes (6): CreerGrilleTarifaireRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 72 - "Community 72"
Cohesion: 0.52
Nodes (6): DecisionGrilleTarifaireRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 73 - "Community 73"
Cohesion: 0.52
Nodes (6): GrilleHistoriqueLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 74 - "Community 74"
Cohesion: 0.52
Nodes (6): HistoriqueResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 75 - "Community 75"
Cohesion: 0.52
Nodes (6): ChangerRoleRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 76 - "Community 76"
Cohesion: 0.52
Nodes (6): ChangerStatutRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 77 - "Community 77"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, UtilisateurListeResponseDto

### Community 78 - "Community 78"
Cohesion: 0.29
Nodes (6): record ResolutionGrille, resoudreGrillePourFonction() (privée), Couplage C2 - résolution de grille en triple, Couplage C4 - DocumentService lit deux domaines étrangers, DocumentService, EnrolementService (résolution grille en ligne)

### Community 79 - "Community 79"
Cohesion: 0.29
Nodes (7): icons.svg (Icon Sprite Sheet), Bluesky Icon Symbol, Discord Icon Symbol, Documentation Icon Symbol, GitHub Icon Symbol, Social/Users Icon Symbol, X (Twitter) Icon Symbol

### Community 80 - "Community 80"
Cohesion: 0.29
Nodes (7): k8s deployment.yaml (dottel-backend), /api/actuator/health liveness/readiness probe, Deployment dottel-backend, Image harbor.afrilandfirstbank.cm/baobab/dottel-backend:latest, Secret dottel-secret (referenced), k8s service.yaml (dottel-backend-svc), Service dottel-backend-svc (ClusterIP)

### Community 81 - "Community 81"
Cohesion: 0.47
Nodes (6): RG-05 — séquence stricte ARH puis CRH puis DRH, RG-06 — un seul PDF par processus, enrichi de 1 à 3 signatures, RG-08 — séparation des tâches (403 si même acteur), GET /pieces-jointes/{id}/download (ARH, CRH, DRH), GET /processus/{id}/piece-jointe (ARH, CRH, DRH), POST /processus/{id}/valider (ARH, CRH, DRH selon statut)

### Community 82 - "Community 82"
Cohesion: 0.33
Nodes (6): Couplage C3 - FonctionEligibleService mute Beneficiaire, Couplage C5 - ReportingService lit trois domaines étrangers, FonctionEligibleService, HistoriqueExportService, ReportingService, RECAPITULATIF_CHANTIER.md

### Community 83 - "Community 83"
Cohesion: 0.40
Nodes (6): AuthService.authentifier() (devient mort), Décision P-2 - Keycloak pour les 5 rôles, EMPLOYE compris, Décision P-3 - non recommandée (parcours EMPLOYE public), JwtUtil.genererToken() (devient mort), RoleJwtAuthenticationConverter, SecurityConfig.jwtDecoder() (bascule issuer-uri)

### Community 84 - "Community 84"
Cohesion: 0.33
Nodes (5): API_BASE_URL, ROLES, STATUTS, STATUTS_GRILLE, STATUTS_LABELS

### Community 85 - "Community 85"
Cohesion: 0.60
Nodes (3): ApplicationModules, Test, ModularityTests

### Community 86 - "Community 86"
Cohesion: 0.60
Nodes (3): DottelApplicationTests, Test, SpringBootTest

### Community 87 - "Community 87"
Cohesion: 0.40
Nodes (5): Décision A - EligibiliteService (A1), Décision B - Référentiel unifié (B1), EligibiliteService, Module referentiel, Conception des API de modules (MM.2)

### Community 88 - "Community 88"
Cohesion: 0.40
Nodes (5): MM.1 Repackaging et Modularité, ModularityTests, Spring Modulith (dépendance ajoutée), @NamedInterface, Documenter (org.springframework.modulith.docs)

### Community 89 - "Community 89"
Cohesion: 0.40
Nodes (5): GET /admin/utilisateurs (ADMIN), GET /grilles-tarifaires/en-attente-drh (DRH), GET /grilles-tarifaires (ARH, ADMIN), Pas de pagination sur grilles / fonctions / utilisateurs — volume interne limité, POST /admin/utilisateurs (ADMIN)

### Community 91 - "Community 91"
Cohesion: 0.83
Nodes (3): DeclencherProcessusRequestDto, Getter, Setter

### Community 92 - "Community 92"
Cohesion: 0.83
Nodes (3): Getter, Setter, ValiderProcessusRequestDto

### Community 93 - "Community 93"
Cohesion: 1.00
Nodes (3): AuthProvider.js / AuthContext.jsx (contrat existant), AuthProviderKeycloak.js, Décision F-2 - Authorization Code + PKCE

### Community 94 - "Community 94"
Cohesion: 0.67
Nodes (3): GET /processus — filtres statut et année (ARH, CRH, DRH), GET /reporting/dashboard (ARH, DRH), GET /reporting/historique (DRH)

### Community 95 - "Community 95"
Cohesion: 0.67
Nodes (3): React Framework, React Logo (Vite default asset), Vite/React Starter Template Bootstrap

## Ambiguous Edges - Review These
- `Identité de marque Afriland First Bank` → `Symbole concentrique "C/E" sur bloc rouge`  [AMBIGUOUS]
  frontend/src/assets/logo afriland.png · relation: conceptually_related_to
- `Motif cercle (cx28 cy30 r7)` → `Motif chevron / toit (polyline 110,50 120,36 130,50)`  [AMBIGUOUS]
  frontend/src/assets/watermark-pattern.svg · relation: semantically_similar_to

## Knowledge Gaps
- **185 isolated node(s):** `com.afriland:dottel`, `VALIDATION_ARH`, `VALIDATION_CRH`, `VALIDATION_DRH`, `EN_COURS_ARH` (+180 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **34 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Identité de marque Afriland First Bank` and `Symbole concentrique "C/E" sur bloc rouge`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Motif cercle (cx28 cy30 r7)` and `Motif chevron / toit (polyline 110,50 120,36 130,50)`?**
  _Edge tagged AMBIGUOUS (relation: semantically_similar_to) - confidence is low._
- **Why does `Utilisateur` connect `Community 20` to `Community 2`, `Community 36`, `Community 5`, `Community 6`, `Community 37`, `Community 12`, `Community 14`, `Community 19`, `Community 21`, `Community 22`, `Community 23`?**
  _High betweenness centrality (0.087) - this node is a cross-community bridge._
- **Why does `StatutEnum` connect `Community 16` to `Community 65`, `Community 66`, `Community 35`, `Community 33`, `Community 69`, `Community 70`, `Community 45`, `Community 46`, `Community 52`, `Community 23`?**
  _High betweenness centrality (0.075) - this node is a cross-community bridge._
- **Why does `AuditService` connect `Community 1` to `Community 32`, `Community 2`, `Community 4`, `Community 37`, `Community 11`, `Community 12`, `Community 14`, `Community 18`, `Community 19`, `Community 21`, `Community 22`, `Community 29`?**
  _High betweenness centrality (0.059) - this node is a cross-community bridge._
- **What connects `com.afriland:dottel`, `VALIDATION_ARH`, `VALIDATION_CRH` to the rest of the system?**
  _185 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.0505276225946617 - nodes in this community are weakly interconnected._