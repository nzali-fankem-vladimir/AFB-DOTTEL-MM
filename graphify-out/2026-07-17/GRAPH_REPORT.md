# Graph Report - .  (2026-07-15)

## Corpus Check
- 32 files · ~46,020 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 294 nodes · 388 edges · 27 communities (19 shown, 8 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 27 edges (avg confidence: 0.82)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Auth Backend Core (JPA/JWT)
- DOTTEL Business Rules (CLAUDE.md V3)
- Graphify Skill Reference
- Frontend Package Dependencies
- Frontend Pages & Routing
- Deployment Config (K8s/Env/Yml)
- Spring Security Config
- Auth Controller & DTOs
- Global Exception Handling
- Maven Wrapper Script
- CORS Configuration
- Frontend Icon Sprite
- Frontend Oxlint Config
- Frontend Formatters
- Backend Application Tests
- Frontend Constants
- Spring Boot Application Entry
- Note de Service NS 69/17 (Grille Tarifaire Source)
- Frontend App Entry
- React Logo Asset
- Frontend Favicon
- Frontend Axios Config
- Vite Logo Asset
- Graphify Export Benchmark
- Frontend Hero Banner
- Java Package Root

## God Nodes (most connected - your core abstractions)
1. `Utilisateur` - 14 edges
2. `graphify skill` - 14 edges
3. `AuthService` - 9 edges
4. `LoginResponseDto` - 8 edges
5. `RoleEnum` - 8 edges
6. `RoleJwtAuthenticationConverter` - 8 edges
7. `AuthController` - 7 edges
8. `LoginRequestDto` - 7 edges
9. `GlobalExceptionHandler` - 7 edges
10. `JwtUtil` - 7 edges

## Surprising Connections (you probably didn't know these)
- `grille_tarifaire table (tariff grid)` --semantically_similar_to--> `grille_tarifaire table (V2)`  [INFERRED] [semantically similar]
  CLAUDE.md → CLAUDE v2.md
- `25 fonctions éligibles (NS 69/17 + 3 corps assimilés)` --semantically_similar_to--> `24/25 fonctions éligibles (V2, NS 69/17 + enrichissement métier)`  [INFERRED] [semantically similar]
  CLAUDE.md → CLAUDE v2.md
- `Contrats API V3 - 26 endpoints` --semantically_similar_to--> `Contrats API V2 - 26 endpoints (fonctions/ instead of grilles-tarifaires/)`  [INFERRED] [semantically similar]
  CLAUDE.md → CLAUDE v2.md
- `Ordre d'implémentation des sprints (V3, Sprint 0 to 7 + 4bis + 6F)` --semantically_similar_to--> `Ordre d'implémentation des sprints (V2, Sprint 0 to 7)`  [INFERRED] [semantically similar]
  CLAUDE.md → CLAUDE v2.md
- `afb-dotations-telephoniques (README)` --conceptually_related_to--> `Module DOTTEL - Dotations Téléphoniques Mensuelles (AFRILAND HORIZON 2030)`  [INFERRED]
  README.md → CLAUDE.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Graph query/traversal family (query, path, explain)** — claude_skills_graphify_references_query_vocab_expansion, claude_skills_graphify_references_query_bfs_dfs, claude_skills_graphify_references_query_path_explain, claude_skills_graphify_references_query_save_result_reflect [INFERRED 0.85]
- **Graph export targets (wiki, neo4j, falkordb, mcp, benchmark)** — claude_skills_graphify_references_exports_wiki, claude_skills_graphify_references_exports_neo4j, claude_skills_graphify_references_exports_falkordb, claude_skills_graphify_references_exports_mcp, claude_skills_graphify_references_exports_benchmark [EXTRACTED 1.00]
- **Monthly-process workflow governance rules (RG-05, RG-06, RG-08, RG-09)** — claude_rg05_sequence_workflow, claude_rg06_single_pdf, claude_rg08_separation_taches, claude_rg09_audit_historisation [INFERRED 0.85]
- **Kubernetes deployment pipeline for dottel-backend** — k8s_configmap_yaml_dottel_config, k8s_deployment_yaml_dottel_backend, k8s_service_yaml_dottel_backend_svc [EXTRACTED 1.00]
- **Secret/env var externalization from application.yml to K8s Secret/ConfigMap** — backend_dottel_jwt_secret_env, backend_db_connection_envs, k8s_deployment_yaml_dottel_secret, k8s_configmap_yaml_dottel_config [INFERRED 0.85]

## Communities (27 total, 8 thin omitted)

### Community 0 - "Auth Backend Core (JPA/JWT)"
Cohesion: 0.11
Nodes (23): AllArgsConstructor, Builder, Getter, Setter, Utilisateur, RoleEnum, ADMIN, ARH (+15 more)

### Community 1 - "DOTTEL Business Rules (CLAUDE.md V3)"
Cohesion: 0.08
Nodes (29): Contrats API V3 - 26 endpoints, Modèle de données V3 - 10 tables, Déploiement Kubernetes (Section 14, V3 new), Données de test camerounaises (convention obligatoire), 5 énumérations (StatutEnum, RoleEnum, NomEtapeEnum, StatutEtapeEnum, StatutGrilleEnum), 25 fonctions éligibles (NS 69/17 + 3 corps assimilés), grille_tarifaire table (tariff grid), JURISTE code = "Agent de Recouvrement" label decision (+21 more)

### Community 2 - "Graphify Skill Reference"
Cohesion: 0.08
Nodes (29): Project .claude/CLAUDE.md (graphify trigger), /graphify add <url>, --watch folder watcher, graphify export falkordb / falkordb-push, MCP stdio server (graphify.serve), graphify export neo4j / neo4j-push, graphify export wiki, Confidence score rubric (0.55-0.95 discrete steps) (+21 more)

### Community 3 - "Frontend Package Dependencies"
Cohesion: 0.07
Nodes (28): dependencies, axios, date-fns, @hookform/resolvers, jwt-decode, react, react-dom, react-hook-form (+20 more)

### Community 4 - "Frontend Pages & Routing"
Cohesion: 0.11
Nodes (13): AuthContext, useAuth(), Admin(), AccesInterdit(), Login(), Beneficiaires(), Dashboard(), GrillesTarifaires() (+5 more)

### Community 5 - "Deployment Config (K8s/Env/Yml)"
Cohesion: 0.12
Nodes (20): Actuator health/info exposure config, afb_dotations_telephoniques database, CORS allowed-origins localhost:3000, DB_URL/DB_USER/DB_PASSWORD env vars, DOTTEL_JWT_SECRET / JWT_SECRET env var, Flyway migration config (classpath:db/migration), Backend HELP.md, Swagger UI config (/swagger-ui.html) (+12 more)

### Community 6 - "Spring Security Config"
Cohesion: 0.18
Nodes (13): Component, Override, RoleJwtAuthenticationConverter, Configuration, PasswordEncoder, SecurityConfig, Bean, Converter (+5 more)

### Community 7 - "Auth Controller & DTOs"
Cohesion: 0.20
Nodes (13): AuthController, RequiredArgsConstructor, ResponseEntity, Getter, Setter, LoginRequestDto, Builder, Getter (+5 more)

### Community 8 - "Global Exception Handling"
Cohesion: 0.21
Nodes (8): IdentifiantsInvalidesException, UtilisateurInactifException, GlobalExceptionHandler, ResponseEntity, ExceptionHandler, Logger, MethodArgumentNotValidException, RestControllerAdvice

### Community 9 - "Maven Wrapper Script"
Cohesion: 0.33
Nodes (6): mvnw script, clean(), die(), exec_maven(), set_java_home(), verbose()

### Community 10 - "CORS Configuration"
Cohesion: 0.43
Nodes (5): CorsConfig, Configuration, Override, CorsRegistry, WebMvcConfigurer

### Community 11 - "Frontend Icon Sprite"
Cohesion: 0.29
Nodes (7): icons.svg (Icon Sprite Sheet), Bluesky Icon Symbol, Discord Icon Symbol, Documentation Icon Symbol, GitHub Icon Symbol, Social/Users Icon Symbol, X (Twitter) Icon Symbol

### Community 12 - "Frontend Oxlint Config"
Cohesion: 0.33
Nodes (5): plugins, rules, react/only-export-components, react/rules-of-hooks, $schema

### Community 13 - "Frontend Formatters"
Cohesion: 0.40
Nodes (3): formatDate(), formatDateHeure(), MOIS_LABELS

### Community 14 - "Backend Application Tests"
Cohesion: 0.60
Nodes (3): DottelApplicationTests, SpringBootTest, Test

### Community 15 - "Frontend Constants"
Cohesion: 0.40
Nodes (4): ROLES, STATUTS, STATUTS_GRILLE, STATUTS_LABELS

### Community 17 - "Note de Service NS 69/17 (Grille Tarifaire Source)"
Cohesion: 0.67
Nodes (3): Note de Service NS 69/17 - Dotation Téléphonique Portable (scan), Section 5: 25 Fonctions Éligibles (CLAUDE.md), Concept: Grille Tarifaire (montants forfaitaires)

### Community 19 - "React Logo Asset"
Cohesion: 0.67
Nodes (3): React Framework, React Logo (Vite default asset), Vite/React Starter Template Bootstrap

## Knowledge Gaps
- **73 isolated node(s):** `com.afriland:dottel`, `EMPLOYE`, `ARH`, `CRH`, `DRH` (+68 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **8 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `IdentifiantsInvalidesException` connect `Global Exception Handling` to `Auth Backend Core (JPA/JWT)`?**
  _High betweenness centrality (0.009) - this node is a cross-community bridge._
- **Why does `UtilisateurInactifException` connect `Global Exception Handling` to `Auth Backend Core (JPA/JWT)`?**
  _High betweenness centrality (0.009) - this node is a cross-community bridge._
- **What connects `com.afriland:dottel`, `EMPLOYE`, `ARH` to the rest of the system?**
  _87 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Auth Backend Core (JPA/JWT)` be split into smaller, more focused modules?**
  _Cohesion score 0.10887096774193548 - nodes in this community are weakly interconnected._
- **Should `DOTTEL Business Rules (CLAUDE.md V3)` be split into smaller, more focused modules?**
  _Cohesion score 0.0812807881773399 - nodes in this community are weakly interconnected._
- **Should `Graphify Skill Reference` be split into smaller, more focused modules?**
  _Cohesion score 0.0812807881773399 - nodes in this community are weakly interconnected._
- **Should `Frontend Package Dependencies` be split into smaller, more focused modules?**
  _Cohesion score 0.06896551724137931 - nodes in this community are weakly interconnected._