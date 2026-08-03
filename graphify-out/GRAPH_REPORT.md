# Graph Report - .  (2026-08-03)

## Corpus Check
- 23 files · ~152,220 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2248 nodes · 5208 edges · 193 communities (119 shown, 74 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 385 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Processusdetailpage
- Misc
- Misc
- Misc
- Misc
- Com
- Misc
- Misc
- Com
- Audit Securite Owasp V1
- Contrats Api Dotations V3
- Misc
- Com
- Com
- Com
- Com
- Com
- Com
- Com
- Skill
- Misc
- Com
- Misc
- Misc
- Misc
- Com
- Misc
- Com
- Com
- Com
- Com
- Com
- Com
- Misc
- Package.Json
- Package.Json
- Com
- Misc
- Misc
- Misc
- Com
- Com
- Mm.0 Cadrage
- Misc
- Misc
- Contrats Api Dotations V3
- Mvnw
- Misc
- Mm.4 Audit Evenementiel
- Package.Json
- Misc
- Misc
- Plan Monolithe Modulaire
- Logo Afriland.Png
- Watermark-Pattern.Svg
- Misc
- Misc
- .Oxlintrc.Json
- Sprint 3.4
- Misc
- Misc
- Misc
- Misc
- Misc
- Misc
- Misc
- Com
- Misc
- Misc
- Misc
- Misc
- Mm.3 Casser Couplages Restants
- Icons.Svg
- Deployment.Yaml
- Com
- Claude
- Mm.3 Casser Couplages Restants
- Mm.7 Keycloak Provisoire
- Constants
- Com
- Com
- Mm.0 Cadrage
- Mm.1 Repackaging Et Modularite
- Contrats Api Dotations V3
- Com
- Com
- Mm.7 Keycloak Provisoire
- Contrats Api Dotations V3
- React.Svg
- Package.Json
- Images
- Claude
- Package.Json
- Docker-Compose.Yml
- Mm.0 Cadrage
- Mm.0 Cadrage
- Contrats Api Dotations V3
- Contrats Api Dotations V3
- Favicon.Svg
- Index.Html
- Package.Json
- Package.Json
- Package.Json
- Package.Json
- Package.Json
- Package.Json
- Package.Json
- Vite.Svg
- Pid.Txt
- Pid2.Txt
- Pid3.Txt
- Pid4.Txt
- Images
- Static
- Claude
- Exports
- Contrats Api Dotations V3
- Hero.Png
- Vite.Config
- Pom.Xml
- Community 121
- Community 122
- Community 123
- Community 124
- Community 125
- Community 126
- Community 127
- Community 128
- Community 129
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
- Community 140
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
- Community 154
- Community 155
- Community 156
- Community 158
- Community 159
- Community 160
- Community 163
- Community 164
- Community 165
- Community 166
- Community 167
- Community 171
- Community 172
- Community 173
- Community 175
- Community 178
- Community 179
- Community 180
- Community 181
- Community 182
- Community 183
- Community 184
- Community 185
- Community 186
- Community 187
- Community 188
- Community 189
- Community 191
- Community 192

## God Nodes (most connected - your core abstractions)
1. `ProcessusMensuelServiceTest` - 86 edges
2. `EvenementAudit` - 49 edges
3. `GlobalExceptionHandler` - 38 edges
4. `ProcessusMensuelService` - 38 edges
5. `react` - 37 edges
6. `GrilleTarifaireServiceTest` - 36 edges
7. `StatutEnum` - 34 edges
8. `ProcessusMensuel` - 33 edges
9. `Utilisateur` - 31 edges
10. `FonctionEligibleService` - 31 edges

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

## Communities (193 total, 74 thin omitted)

### Community 0 - "Processusdetailpage"
Cohesion: 0.05
Nodes (112): apiClient, setAuthToken(), App(), AuthProvider, AuthProviderLocal, AppLayout(), PageHeader(), initiales() (+104 more)

### Community 1 - "Misc"
Cohesion: 0.05
Nodes (53): FonctionEligibleController, GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity (+45 more)

### Community 2 - "Misc"
Cohesion: 0.06
Nodes (46): AfterEach, EvenementAudit, AuditEventListener, AuditLogRepository, RequiredArgsConstructor, AuthController, PostMapping, RequestMapping (+38 more)

### Community 3 - "Misc"
Cohesion: 0.07
Nodes (43): EnrolementController, GetMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+35 more)

### Community 4 - "Misc"
Cohesion: 0.10
Nodes (25): GrilleTarifaireService, ApplicationEventPublisher, FonctionEligibleRepository, GrilleTarifaire, GrilleTarifaireRepository, RequiredArgsConstructor, Service, Transactional (+17 more)

### Community 5 - "Com"
Cohesion: 0.08
Nodes (28): GrilleTarifaireApi, ResolutionGrilleDto, FonctionEligible, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor (+20 more)

### Community 6 - "Misc"
Cohesion: 0.10
Nodes (25): BeneficiaireService, ApplicationEventPublisher, AuthenticatedUserService, Beneficiaire, BeneficiaireRepository, EligibiliteService, GrilleTarifaireApi, Page (+17 more)

### Community 7 - "Misc"
Cohesion: 0.06
Nodes (48): application.yml (configuration de base Spring Boot), Actuator health show-details always (sondes Kubernetes), app.cors.allowed-origins (jamais de wildcard), application-dev.yml (profil developpement), Repli en clair DB_PASSWORD:postgres (profil dev), dottel.documents.chapitre-defaut (repli chapitre EHR), dottel.documents.chemin-stockage (stockage local du PDF), Configuration Flyway (classpath:db/migration) (+40 more)

### Community 8 - "Com"
Cohesion: 0.10
Nodes (30): AllArgsConstructor, LigneDocumentDto, DocumentService, EcartMensuelService, LigneEtatMensuel, PieceJointe, PieceJointeRepository, ProcessusMensuel (+22 more)

### Community 9 - "Audit Securite Owasp V1"
Cohesion: 0.12
Nodes (20): ApplicationEventPublisher, PasswordEncoder, RequiredArgsConstructor, RoleEnum, Service, Transactional, Utilisateur, UtilisateurRepository (+12 more)

### Community 10 - "Contrats Api Dotations V3"
Cohesion: 0.05
Nodes (45): Données de test camerounaises réalistes obligatoires, Jamais d'entité JPA dans une réponse API — DTO obligatoire, fonction_eligible — référentiel des 25 fonctions, grille_tarifaire — source unique du montant, LigneEtatMensuel — table pivot N-N obligatoire, Modèle de données — 10 tables, Module DOTTEL — Dotations Téléphoniques Mensuelles, RG-01 — éligibilité par fonction_eligible.actif (+37 more)

### Community 11 - "Misc"
Cohesion: 0.11
Nodes (24): BeneficiaireImportService, ApplicationEventPublisher, AuthenticatedUserService, Beneficiaire, BeneficiaireRepository, RequiredArgsConstructor, Row, Service (+16 more)

### Community 12 - "Com"
Cohesion: 0.08
Nodes (17): BeneficiaireApi, BeneficiaireDotationDto, BeneficiaireIdentiteDto, BeneficiaireDocumentDto, BeneficiaireDotationDto, BeneficiaireIdentiteDto, BeneficiaireApiImpl, BeneficiaireDotationDto (+9 more)

### Community 13 - "Com"
Cohesion: 0.09
Nodes (27): AuditLog, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+19 more)

### Community 14 - "Com"
Cohesion: 0.09
Nodes (31): AjustementLigneEtatDto, ApplicationEventPublisher, AuthenticatedUserService, BeneficiaireApi, DocumentService, EligibiliteService, EtapeWorkflowRepository, EvenementClotureService (+23 more)

### Community 15 - "Com"
Cohesion: 0.12
Nodes (7): DocumentService, EvenementClotureService, NotificationService, SeparationTachesService, SignatureService, Test, ProcessusMensuelServiceTest

### Community 16 - "Com"
Cohesion: 0.10
Nodes (20): NotificationService, Override, Service, NotificationServiceStub, DestinataireNotificationDto, RoleEnum, RoleEnum, UtilisateurApi (+12 more)

### Community 17 - "Com"
Cohesion: 0.12
Nodes (24): AuditService, AuditLogResponseDto, Page, Pageable, GetMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor (+16 more)

### Community 18 - "Com"
Cohesion: 0.16
Nodes (17): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, LigneEtatMensuel (+9 more)

### Community 19 - "Skill"
Cohesion: 0.13
Nodes (20): EvenementClotureSerializer, ObjectMapper, Override, Bean, Configuration, KafkaTemplate, ObjectMapper, KafkaConfig (+12 more)

### Community 20 - "Misc"
Cohesion: 0.08
Nodes (29): Project .claude/CLAUDE.md (graphify trigger), /graphify add <url>, --watch folder watcher, graphify export falkordb / falkordb-push, MCP stdio server (graphify.serve), graphify export neo4j / neo4j-push, graphify export wiki, Confidence score rubric (0.55-0.95 discrete steps) (+21 more)

### Community 21 - "Com"
Cohesion: 0.14
Nodes (20): HistoriqueLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, HistoriqueResponseDto, AllArgsConstructor (+12 more)

### Community 22 - "Misc"
Cohesion: 0.13
Nodes (20): EtapeWorkflow, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+12 more)

### Community 23 - "Misc"
Cohesion: 0.14
Nodes (17): Component, Override, RoleJwtAuthenticationConverter, Bean, Configuration, EnableMethodSecurity, PasswordEncoder, SecurityConfig (+9 more)

### Community 24 - "Misc"
Cohesion: 0.13
Nodes (9): AccessDeniedException, MethodArgumentNotValidException, IdentifiantsInvalidesException, UtilisateurInactifException, GlobalExceptionHandlerTest, ExtendWith, MethodArgumentNotValidException, Test (+1 more)

### Community 25 - "Com"
Cohesion: 0.14
Nodes (14): SignatureService, UtilisateurConnecteIntrouvableException, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter (+6 more)

### Community 26 - "Misc"
Cohesion: 0.15
Nodes (13): EmployeEhrDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, EhrIntegrationService, EhrIntegrationServiceStub, Override (+5 more)

### Community 27 - "Com"
Cohesion: 0.16
Nodes (17): GetMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController, PieceJointeController, AllArgsConstructor (+9 more)

### Community 29 - "Com"
Cohesion: 0.25
Nodes (3): Transactional, Test, ReportingServiceTest

### Community 30 - "Com"
Cohesion: 0.18
Nodes (14): GrilleTarifaire, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+6 more)

### Community 31 - "Com"
Cohesion: 0.20
Nodes (14): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, UtilisateurResponseDto, EnableMethodSecurity, Test (+6 more)

### Community 32 - "Com"
Cohesion: 0.18
Nodes (15): StatutEnum, CLOTURE, EN_ATTENTE_CRH, EN_ATTENTE_DRH, EN_COURS_ARH, RETOURNE, AllArgsConstructor, Builder (+7 more)

### Community 33 - "Com"
Cohesion: 0.26
Nodes (11): BeneficiaireController, GetMapping, MultipartFile, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor (+3 more)

### Community 34 - "Misc"
Cohesion: 0.23
Nodes (8): BeneficiaireExportService, RequiredArgsConstructor, Service, Transactional, BeneficiaireExportServiceTest, BeforeEach, ExtendWith, Test

### Community 35 - "Package.Json"
Cohesion: 0.29
Nodes (9): GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+1 more)

### Community 36 - "Package.Json"
Cohesion: 0.29
Nodes (9): GrilleTarifaireController, GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity (+1 more)

### Community 37 - "Com"
Cohesion: 0.17
Nodes (6): FichierImportInvalideException, PieceJointeIntrouvableException, GrilleNonEnAttenteDrhException, GlobalExceptionHandler, Logger, RestControllerAdvice

### Community 38 - "Misc"
Cohesion: 0.24
Nodes (7): Component, JwtUtil, BeforeEach, Test, JwtUtilTest, MacAlgorithm, SecretKey

### Community 39 - "Misc"
Cohesion: 0.13
Nodes (15): class-variance-authority, date-fns, dependencies, class-variance-authority, date-fns, @hookform/resolvers, lucide-react, @radix-ui/react-checkbox (+7 more)

### Community 40 - "Misc"
Cohesion: 0.13
Nodes (15): devDependencies, oxlint, tailwindcss, @tailwindcss/vite, @types/react, @types/react-dom, vite, @vitejs/plugin-react (+7 more)

### Community 41 - "Com"
Cohesion: 0.24
Nodes (10): Beneficiaire, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table (+2 more)

### Community 42 - "Com"
Cohesion: 0.25
Nodes (9): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, Setter, Table, ProcessusMensuel (+1 more)

### Community 43 - "Mm.0 Cadrage"
Cohesion: 0.31
Nodes (9): GetMapping, PatchMapping, PostMapping, PreAuthorize, RequestMapping, RequiredArgsConstructor, ResponseEntity, RestController (+1 more)

### Community 44 - "Misc"
Cohesion: 0.26
Nodes (14): Dictionnaire de données Dotations V3, Table audit_log, Table beneficiaires, Table etape_workflow, Table fonction_eligible, Table grille_tarifaire, Table ligne_etat_mensuel (pivot N-N), Table piece_jointe (+6 more)

### Community 45 - "Misc"
Cohesion: 0.44
Nodes (3): SeparationTachesService, Test, SeparationTachesServiceTest

### Community 46 - "Contrats Api Dotations V3"
Cohesion: 0.23
Nodes (12): CreerUtilisateurRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, RoleEnum, ADMIN (+4 more)

### Community 47 - "Mvnw"
Cohesion: 0.15
Nodes (12): ApplicationEventPublisher, AuthenticatedUserService, BeneficiaireApi, EligibiliteService, EtapeWorkflowRepository, ExtendWith, GrilleTarifaireApi, LigneEtatMensuelRepository (+4 more)

### Community 48 - "Misc"
Cohesion: 0.17
Nodes (13): Décision C - Découpage 6 modules, Module audit, Module beneficiaires, Module integration (proposé puis rejeté, réparti), Module processus, Module reporting, Module utilisateurs, Ordre de déplacement des modules (audit -> processus) (+5 more)

### Community 49 - "Mm.4 Audit Evenementiel"
Cohesion: 0.29
Nodes (8): DashboardResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, BeforeEach, ExtendWith

### Community 50 - "Package.Json"
Cohesion: 0.20
Nodes (11): @PreAuthorize obligatoire sur chaque endpoint sensible, Matrice rôles/endpoints (34 endpoints, 9 groupes), Sécurité stateless JWT / Keycloak, aucune session HTTP serveur, Contrat API AFB_API_DOTTEL_V3.3_2026, JWT HS384, validité 8 heures, header Authorization Bearer, LoginResponseDto (token, matricule, role, nom, prenom), Logout stateless — aucune liste noire de jetons, POST /auth/login (public) (+3 more)

### Community 51 - "Misc"
Cohesion: 0.33
Nodes (6): mvnw script, clean(), die(), exec_maven(), set_java_home(), verbose()

### Community 52 - "Misc"
Cohesion: 0.31
Nodes (4): AuditLogRepository, BeneficiaireRepository, JpaRepository, JpaSpecificationExecutor

### Community 53 - "Plan Monolithe Modulaire"
Cohesion: 0.36
Nodes (7): BeneficiairePageResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Page, Setter

### Community 55 - "Watermark-Pattern.Svg"
Cohesion: 0.22
Nodes (10): AuditService (interface), AuditServiceImpl.enregistrer(), Options E-1/E-2 conception des événements d'audit, Options T-1/T-2/T-3 mode transactionnel de l'écouteur, Piège 2 - adresse IP (RequestContextHolder), Piège 1 - transactionnalité (rollback conjoint), AuthenticatedUserService, Décision E-3 - enrôlement pour un tiers assumé (+2 more)

### Community 56 - "Misc"
Cohesion: 0.20
Nodes (9): name, private, scripts, build, dev, lint, preview, type (+1 more)

### Community 57 - "Misc"
Cohesion: 0.22
Nodes (3): BeneficiaireIntrouvableException, GrilleNonActiveException, ExceptionHandler

### Community 58 - ".Oxlintrc.Json"
Cohesion: 0.22
Nodes (3): RoleEtapeNonAutoriseException, ResponseEntity, ActionAdminNonAutoriseeException

### Community 60 - "Misc"
Cohesion: 0.22
Nodes (9): MM.2 Refactor ProcessusMensuelService, ProcessusMensuelService (god service, 16 dépendances), MM.3 Casser couplages restants, MM.4 Audit événementiel, MM.5 API publiques NamedInterface, MM.6 Vérification et documentation, Foyer C1 - ProcessusMensuelService (16 dépendances), Foyer C6 - AuditService appelé en synchrone par 8 services (+1 more)

### Community 61 - "Misc"
Cohesion: 0.39
Nodes (9): Afriland First Bank Logo (PNG asset), Identité de marque Afriland First Bank, Charte visuelle frontend BAOBAB / DOTTEL, Palette de marque rouge / noir / gris / blanc, Rouge institutionnel AFB (E30613), Symbole concentrique "C/E" sur bloc rouge, Asset PNG horizontal à fond transparent importable par Vite, Usage : logo en haut de la barre de navigation latérale (+1 more)

### Community 62 - "Misc"
Cohesion: 0.42
Nodes (9): Charte visuelle AFB - fond clair sobre, aucun rouge E30613 dans le fond, Filigrane discret de la zone de contenu principale (.fond-filigrane), Motif chevron / toit (polyline 110,50 120,36 130,50), Motif cercle (cx28 cy30 r7), Motif deux lignes horizontales (lignes de document/etat), Motif oeil (lentille + pupille, controle/supervision), Style de trait unique (fill=none, stroke #1A1A1A, width 1.5, opacity 0.07), Tuile repetable 160x160 (background-repeat) (+1 more)

### Community 63 - "Misc"
Cohesion: 0.39
Nodes (6): AjustementLigneEtatDto, Getter, Setter, Getter, Setter, PatchProcessusRequestDto

### Community 64 - "Misc"
Cohesion: 0.39
Nodes (3): Transactional, DeclencherProcessusRequestDto, ProcessusMensuelResponseDto

### Community 65 - "Misc"
Cohesion: 0.25
Nodes (7): plugins, rules, react/only-export-components, react/rules-of-hooks, $schema, oxc, warn

### Community 66 - "Misc"
Cohesion: 0.29
Nodes (8): Sprint 3.4 guide — Validation ARH et génération du PDF initial (US-11), Champ CHAPITRE (donnée EHR par bénéficiaire, décision révisée depuis 'valeur fixe 64310000' vers donnée variable + repli configurable), DocumentService.genererInitiale() (iText 8 PDF, RG-06), Entité EtapeWorkflow (NomEtapeEnum, StatutEtapeEnum, signature_numerique), NotificationService (stub log, appelé par ProcessusMensuelService.valider), Entité PieceJointe (UNIQUE par processus, nombre_signatures), Point de vigilance RG-08 : contrat API mentionne la vérification sur /valider, mais SeparationTachesService est reporté au Sprint 5, SignatureService (interface isolée, trace non-certifiée, remplaçable par intégration type INTRA)

### Community 67 - "Com"
Cohesion: 0.52
Nodes (6): BeneficiaireResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 68 - "Misc"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ModifierBeneficiaireRequestDto

### Community 69 - "Misc"
Cohesion: 0.43
Nodes (5): CorsConfig, Configuration, Override, CorsRegistry, WebMvcConfigurer

### Community 70 - "Misc"
Cohesion: 0.52
Nodes (6): BeneficiaireExcluDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 71 - "Misc"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, LigneEtatMensuelDetailDto

### Community 72 - "Mm.3 Casser Couplages Restants"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, PatchProcessusResponseDto

### Community 73 - "Icons.Svg"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, PieceJointeMetadonneesResponseDto

### Community 74 - "Deployment.Yaml"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusDetailResponseDto

### Community 75 - "Com"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusListItemDto

### Community 76 - "Claude"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ProcessusMensuelResponseDto

### Community 77 - "Mm.3 Casser Couplages Restants"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ResultatAjustementDto

### Community 78 - "Mm.7 Keycloak Provisoire"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, RetournerProcessusRequestDto

### Community 79 - "Constants"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, RetournerProcessusResponseDto

### Community 80 - "Com"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ValiderProcessusResponseDto

### Community 81 - "Com"
Cohesion: 0.52
Nodes (6): CreerGrilleTarifaireRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 82 - "Mm.0 Cadrage"
Cohesion: 0.52
Nodes (6): DecisionGrilleTarifaireRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 83 - "Mm.1 Repackaging Et Modularite"
Cohesion: 0.52
Nodes (6): GrilleHistoriqueLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 84 - "Contrats Api Dotations V3"
Cohesion: 0.52
Nodes (6): GrilleTarifaireListeLigneDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 85 - "Com"
Cohesion: 0.52
Nodes (6): GrilleTarifaireListeResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 86 - "Com"
Cohesion: 0.52
Nodes (6): GrilleTarifaireResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 87 - "Mm.7 Keycloak Provisoire"
Cohesion: 0.52
Nodes (6): HistoriqueGrilleTarifaireResponseDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 88 - "Contrats Api Dotations V3"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, ModifierGrilleTarifaireRequestDto

### Community 89 - "React.Svg"
Cohesion: 0.52
Nodes (6): ChangerRoleRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 90 - "Package.Json"
Cohesion: 0.52
Nodes (6): ChangerStatutRequestDto, AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter

### Community 91 - "Images"
Cohesion: 0.52
Nodes (6): AllArgsConstructor, Builder, Getter, NoArgsConstructor, Setter, UtilisateurListeResponseDto

### Community 92 - "Claude"
Cohesion: 0.29
Nodes (6): record ResolutionGrille, resoudreGrillePourFonction() (privée), Couplage C2 - résolution de grille en triple, Couplage C4 - DocumentService lit deux domaines étrangers, DocumentService, EnrolementService (résolution grille en ligne)

### Community 93 - "Package.Json"
Cohesion: 0.29
Nodes (7): icons.svg (Icon Sprite Sheet), Bluesky Icon Symbol, Discord Icon Symbol, Documentation Icon Symbol, GitHub Icon Symbol, Social/Users Icon Symbol, X (Twitter) Icon Symbol

### Community 94 - "Docker-Compose.Yml"
Cohesion: 0.29
Nodes (7): k8s deployment.yaml (dottel-backend), /api/actuator/health liveness/readiness probe, Deployment dottel-backend, Image harbor.afrilandfirstbank.cm/baobab/dottel-backend:latest, Secret dottel-secret (referenced), k8s service.yaml (dottel-backend-svc), Service dottel-backend-svc (ClusterIP)

### Community 95 - "Mm.0 Cadrage"
Cohesion: 0.60
Nodes (5): ImportErreurDto, AllArgsConstructor, Builder, Data, NoArgsConstructor

### Community 96 - "Mm.0 Cadrage"
Cohesion: 0.60
Nodes (5): ImportRapportDto, AllArgsConstructor, Builder, Data, NoArgsConstructor

### Community 97 - "Contrats Api Dotations V3"
Cohesion: 0.47
Nodes (6): RG-05 — séquence stricte ARH puis CRH puis DRH, RG-06 — un seul PDF par processus, enrichi de 1 à 3 signatures, RG-08 — séparation des tâches (403 si même acteur), GET /pieces-jointes/{id}/download (ARH, CRH, DRH), GET /processus/{id}/piece-jointe (ARH, CRH, DRH), POST /processus/{id}/valider (ARH, CRH, DRH selon statut)

### Community 98 - "Contrats Api Dotations V3"
Cohesion: 0.33
Nodes (6): Couplage C3 - FonctionEligibleService mute Beneficiaire, Couplage C5 - ReportingService lit trois domaines étrangers, FonctionEligibleService, HistoriqueExportService, ReportingService, RECAPITULATIF_CHANTIER.md

### Community 99 - "Favicon.Svg"
Cohesion: 0.40
Nodes (6): AuthService.authentifier() (devient mort), Décision P-2 - Keycloak pour les 5 rôles, EMPLOYE compris, Décision P-3 - non recommandée (parcours EMPLOYE public), JwtUtil.genererToken() (devient mort), RoleJwtAuthenticationConverter, SecurityConfig.jwtDecoder() (bascule issuer-uri)

### Community 100 - "Index.Html"
Cohesion: 0.33
Nodes (5): API_BASE_URL, ROLES, STATUTS, STATUTS_GRILLE, STATUTS_LABELS

### Community 101 - "Package.Json"
Cohesion: 0.60
Nodes (3): ApplicationModules, Test, ModularityTests

### Community 102 - "Package.Json"
Cohesion: 0.60
Nodes (3): DottelApplicationTests, Test, SpringBootTest

### Community 103 - "Package.Json"
Cohesion: 0.40
Nodes (5): Décision A - EligibiliteService (A1), Décision B - Référentiel unifié (B1), EligibiliteService, Module referentiel, Conception des API de modules (MM.2)

### Community 104 - "Package.Json"
Cohesion: 0.40
Nodes (5): MM.1 Repackaging et Modularité, ModularityTests, Spring Modulith (dépendance ajoutée), @NamedInterface, Documenter (org.springframework.modulith.docs)

### Community 105 - "Package.Json"
Cohesion: 0.40
Nodes (5): GET /admin/utilisateurs (ADMIN), GET /grilles-tarifaires/en-attente-drh (DRH), GET /grilles-tarifaires (ARH, ADMIN), Pas de pagination sur grilles / fonctions / utilisateurs — volume interne limité, POST /admin/utilisateurs (ADMIN)

### Community 116 - "Exports"
Cohesion: 0.83
Nodes (3): DeclencherProcessusRequestDto, Getter, Setter

### Community 117 - "Contrats Api Dotations V3"
Cohesion: 0.83
Nodes (3): Getter, Setter, ValiderProcessusRequestDto

### Community 131 - "Community 131"
Cohesion: 1.00
Nodes (3): AuthProvider.js / AuthContext.jsx (contrat existant), AuthProviderKeycloak.js, Décision F-2 - Authorization Code + PKCE

### Community 132 - "Community 132"
Cohesion: 0.67
Nodes (3): GET /processus — filtres statut et année (ARH, CRH, DRH), GET /reporting/dashboard (ARH, DRH), GET /reporting/historique (DRH)

### Community 133 - "Community 133"
Cohesion: 0.67
Nodes (3): React Framework, React Logo (Vite default asset), Vite/React Starter Template Bootstrap

## Ambiguous Edges - Review These
- `Identité de marque Afriland First Bank` → `Symbole concentrique "C/E" sur bloc rouge`  [AMBIGUOUS]
  frontend/src/assets/logo afriland.png · relation: conceptually_related_to
- `Motif cercle (cx28 cy30 r7)` → `Motif chevron / toit (polyline 110,50 120,36 130,50)`  [AMBIGUOUS]
  frontend/src/assets/watermark-pattern.svg · relation: semantically_similar_to

## Knowledge Gaps
- **187 isolated node(s):** `com.afriland:dottel`, `VALIDATION_ARH`, `VALIDATION_CRH`, `VALIDATION_DRH`, `EN_COURS_ARH` (+182 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **74 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Identité de marque Afriland First Bank` and `Symbole concentrique "C/E" sur bloc rouge`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Motif cercle (cx28 cy30 r7)` and `Motif chevron / toit (polyline 110,50 120,36 130,50)`?**
  _Edge tagged AMBIGUOUS (relation: semantically_similar_to) - confidence is low._
- **Why does `EvenementAudit` connect `Misc` to `Misc`, `Misc`, `Misc`, `Misc`, `Misc`, `Audit Securite Owasp V1`, `Misc`, `Com`, `Mvnw`, `Sprint 3.4`?**
  _High betweenness centrality (0.154) - this node is a cross-community bridge._
- **Why does `FonctionEligibleService` connect `Misc` to `Misc`, `Misc`, `Com`, `Com`, `Mvnw`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **Why does `ProcessusMensuelService` connect `Com` to `Misc`, `Misc`, `Package.Json`, `Sprint 3.4`, `Com`, `Com`, `Com`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **What connects `com.afriland:dottel`, `VALIDATION_ARH`, `VALIDATION_CRH` to the rest of the system?**
  _187 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Processusdetailpage` be split into smaller, more focused modules?**
  _Cohesion score 0.0505276225946617 - nodes in this community are weakly interconnected._