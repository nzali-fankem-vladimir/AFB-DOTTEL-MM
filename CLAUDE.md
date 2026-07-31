# CLAUDE.md — Projet Dotations Téléphoniques Mensuelles

# AFRILAND HORIZON 2030 — Module INTRA

# À lire intégralement avant toute action de code.

---

## 1. CONTEXTE DU PROJET

Ce module digitalise le processus de paiement des dotations téléphoniques
mensuelles d'Afriland First Bank. Il remplace un circuit papier manuel par
un workflow électronique multi-niveaux intégré à la plateforme BAOBAB.

Tous les documents de conception ont été produits en amont : spécifications
fonctionnelles, analyse, conception, dictionnaire de données V3, contrats
API V3, user stories V3, scénarios V3, architecture V3.

Tu n'inventes rien : tu implémentes ce qui est déjà décrit. En cas de doute
sur une règle métier, tu poses la question plutôt que de décider seul.

---

## 2. STACK TECHNIQUE (décisions figées, ne pas dévier)

Backend  : Spring Boot 4.1.0, Java 21 (Temurin), Maven
Frontend : React 19 + Vite + Tailwind CSS + axios simple
           Sans React Query. Gestion d'état via Context API pour l'auth,
           useState local pour le reste.
           Dépendances additionnelles validées : react-hook-form
           (formulaires), zod (validation de schémas), react-hot-toast
           (notifications).
Intégration BAOBAB : application liée autonome, realm Keycloak partagé.
           Pas de Client Extension Liferay.
Base     : PostgreSQL 16, base nommée afb_dotations_telephoniques
Sécurité : Spring Security + OAuth2 Resource Server + Keycloak SSO
           Simulation Keycloak en développement. OWASP respecté.
ORM      : Spring Data JPA + Hibernate
Migrations DB : Flyway, scripts dans src/main/resources/db/migration/
           Nommage obligatoire :
           V1__creation_tables.sql
           V2__insertion_donnees_reference.sql
           V3__insertion_utilisateurs_test.sql
PDF      : iText 8 (com.itextpdf:itext-core)
Excel    : Apache POI (org.apache.poi:poi-ooxml)
API doc  : Springdoc OpenAPI, Swagger UI sur /swagger-ui.html
Port backend  : 8080
Port frontend : 3000
URL base API dev : http://localhost:8080/api

Déploiement : microservice conteneurisé sur Kubernetes (voir section 14).

---

## 3. STRUCTURE DES PACKAGES JAVA

com.afriland.dottel/
├── controller/     Endpoints REST uniquement, aucune logique métier
├── service/        Toute la logique métier
│   └── rules/      RegleN1Service, RegleN2Service
├── repository/     Interfaces JpaRepository uniquement
├── model/
│   ├── entity/     Entités JPA annotées @Entity
│   ├── dto/        Objets de transfert (jamais d'entité dans une réponse API)
│   └── enums/      Tous les types enum
├── security/       SecurityConfig, JwtAuthenticationFilter,
│                   GlobalExceptionHandler, KeycloakConfig
└── config/         CorsConfig, SwaggerConfig, ApplicationConfig

---

## 4. MODÈLE DE DONNÉES : 10 TABLES

Ne pas modifier le schéma sans accord explicite.

### utilisateurs
id (PK), matricule (UQ), nom, prenom, email (UQ), role (RoleEnum),
mot_de_passe_hash, actif, date_creation,
id_beneficiaire (FK nullable vers beneficiaires)

### beneficiaires
id (PK), matricule (UQ), nom_prenoms,
fonction (VARCHAR référençant fonction_eligible.code),
grade (nullable, obligatoire si Corps de Contrôle ou assimilé),
unite_rattachement, code_unite,
num_compte_courant, date_enrolement, actif

### fonction_eligible
id (PK), code (UQ), libelle, actif

Référentiel stable des postes. Le montant courant est dans grille_tarifaire.
Toutes les 24 fonctions sont insérées par Flyway V2, toutes actives par défaut.

### grille_tarifaire
id (PK), id_fonction_eligible (FK), montant_fcfa,
date_debut, date_fin (nullable pour la grille courante),
statut_validation (BROUILLON | EN_ATTENTE_DRH | ACTIVE | REJETEE),
id_createur (FK nullable vers utilisateurs),
id_validateur (FK nullable vers utilisateurs),
date_creation, date_validation (nullable),
motif_rejet (nullable)

Contrainte d'unicité partielle :
UNIQUE sur id_fonction_eligible WHERE statut_validation = 'ACTIVE'
AND date_fin IS NULL

Une seule grille ACTIVE sans date_fin par fonction à un instant donné.
L'ARH crée une grille (BROUILLON), la soumet à la DRH (EN_ATTENTE_DRH).
La DRH valide (ACTIVE) ou rejette (REJETEE). À la validation, l'ancienne
grille ACTIVE voit sa date_fin renseignée automatiquement.

id_createur et id_validateur sont nullable pour permettre l'insertion
des grilles initiales par Flyway V2 sans référence à un utilisateur réel.

### ligne_etat_mensuel (TABLE PIVOT N-N entre processus_mensuel et beneficiaires)
id (PK), id_processus (FK), id_beneficiaire (FK),
montant_applique, inclus_dans_etat, fonction_retenue (VARCHAR)
UNIQUE(id_processus, id_beneficiaire)

Cette table porte le montant et la fonction retenus pour un mois donné.
Ces valeurs peuvent différer de celles du bénéficiaire si l'ARH ajuste
l'état mensuel avant validation.

### processus_mensuel
id (PK), mois_paiement, annee_paiement, statut (StatutEnum),
date_creation, date_cloture (nullable),
id_createur (FK vers utilisateurs)
UNIQUE(mois_paiement, annee_paiement)

### etape_workflow
id (PK), id_processus (FK), id_acteur (FK vers utilisateurs),
ordre_etape, nom_etape (NomEtapeEnum), statut_etape (StatutEtapeEnum),
date_action, motif_retour (nullable), signature_numerique (nullable)

### piece_jointe (UN SEUL document par processus, enrichi progressivement)
id (PK), id_processus (FK, UNIQUE), nom_fichier, chemin_stockage,
date_generation_initiale, date_derniere_mise_a_jour, nombre_signatures
UNIQUE(id_processus)

### regle_eligibilite
id (PK), code_regle (UQ), description, type_regle,
valeur_condition (nullable), valeur_reponse, active

### audit_log
id (PK), id_utilisateur (FK), action, entite_cible,
id_entite (nullable), date_action, adresse_ip (nullable),
detail_json (nullable)

Le champ detail_json stocke un delta JSON avant/après pour toute
modification d'attribut métier.
Format : {"avant": {"champ": "ancienne_valeur"}, "apres": {"champ": "nouvelle_valeur"}}

---

## 5. DONNÉES DE RÉFÉRENCE : 25 FONCTIONS ÉLIGIBLES

Toutes insérées par Flyway V2, toutes actives par défaut.
Le montant initial de chaque fonction est inséré en même temps dans
grille_tarifaire avec statut_validation = ACTIVE.

Source : Note de service NS 69/17 (22 postes) + 3 corps assimilés
ajoutés par le métier (CONTROLEUR_GESTION, CONTROLEUR_COMPTABLE,
COMPTABLE), soit 25 fonctions au total.

ADG | Administrateur Directeur Général | 150000
DGA | Directeur Général Adjoint | 130000
CORPS_CONTROLE_IG | Inspecteur Général | 70000
DIRECTEUR_GROUPE | Directeur Groupe | 70000
CORPS_CONTROLE_IGA | Inspecteur Général Adjoint | 65000
DIRECTEUR | Directeur Central/Succursale/Régional | 60000
DCA | Directeur Central Adjoint | 50000
DA | Directeur d'Agence | 50000
CONSEILLER | Conseiller/Chargé de Mission/Chef Mission | 50000
DA_ADJOINT | Directeur d'Agence Adjoint | 40000
CHEF_DEPARTEMENT | Chef de Département | 40000
CHEF_ANTENNE | Chef d'Antenne | 40000
GFC | Gestionnaire de Fonds de Commerce | 40000
ASSISTANTE_PCA | Assistante du PCA | 40000
ASSISTANTE_ADG | Assistante de l'ADG | 40000
ASSISTANTE_DGA | Assistante du DGA | 40000
CHEF_DIVISION | Chef de Division | 35000
CHEF_PRODUIT | Chef de Produit | 35000
COORDONNATEUR | Coordonnateur | 35000
JURISTE | Agent de Recouvrement | 35000
CONTROLEUR_GESTION | Contrôleur de Gestion | 35000
CONTROLEUR_COMPTABLE | Contrôleur Comptable | 35000
COMPTABLE | Comptable | 35000
ASSISTANT_CHEF_PROD | Assistant Chef de Produit | 30000
ATTACHE_COMMERCIAL | Attaché Commercial | 30000

Note : le code JURISTE porte le libellé "Agent de Recouvrement" par
choix métier confirmé — c'est le poste n°20 de la note NS 69/17,
forfait 35 000 FCFA. Confirmé par le responsable métier le 15 juillet
2026 : aucun autre poste n'est considéré comme agent de recouvrement pour le moment.

CONTROLEUR_GESTION, CONTROLEUR_COMPTABLE et COMPTABLE sont des corps
assimilés ajoutés suite au retour métier. Ils ne figurent pas dans la
note NS 69/17 (22 postes) mais sont validés par le responsable métier,
portant le total à 25 fonctions éligibles.

---

## 6. ÉNUMÉRATIONS (5 énumérations)

StatutEnum       : EN_COURS_ARH | EN_ATTENTE_CRH | EN_ATTENTE_DRH | RETOURNE | CLOTURE
RoleEnum         : EMPLOYE | ARH | CRH | DRH | ADMIN
NomEtapeEnum     : VALIDATION_ARH | VALIDATION_CRH | VALIDATION_DRH
StatutEtapeEnum  : EN_ATTENTE | VALIDEE | RETOURNEE
StatutGrilleEnum : BROUILLON | EN_ATTENTE_DRH | ACTIVE | REJETEE

FonctionEnum et TypeDocumentEnum n'existent pas dans ce projet.

---

## 7. RÈGLES MÉTIER CRITIQUES (appliquer sans exception)

RG-01 : Toute fonction dont actif = true dans la table fonction_eligible
        est éligible à la dotation. Le système ne limite plus à un sous-
        ensemble fixe. Une fonction inconnue de l'EHR ou avec actif = false
        entraîne un refus avec le motif NON ELIGIBLE.

RG-02 : Pour les fonctions de corps de contrôle et assimilés
        (CORPS_CONTROLE_IG, CORPS_CONTROLE_IGA, CONTROLEUR_GESTION,
        CONTROLEUR_COMPTABLE, COMPTABLE) : si grade = "NON GRADE"
        alors NON ELIGIBLE. Pour toutes les autres fonctions, le grade
        n'est pas vérifié.

RG-03 : Un matricule ne peut être enrôlé qu'une seule fois
        (contrainte UNIQUE sur beneficiaires.matricule).
        Doublon retourne 409 Conflict.

RG-04 : Le montant est déterminé automatiquement en interrogeant la
        table grille_tarifaire avec statut_validation = ACTIVE et
        date_fin IS NULL pour la fonction de l'employé.
        Jamais de valeur en dur dans le code Java.

RG-05 : Séquence de validation stricte : ARH puis CRH puis DRH.
        Aucun saut autorisé.

RG-06 : Un seul PDF par processus. Ne jamais créer trois fichiers distincts.
        Validation ARH : DocumentService.genererInitiale()
        Validation CRH : DocumentService.ajouterSignature()
        Validation DRH : DocumentService.ajouterSignature()
        Le compteur nombre_signatures passe de 1 à 3.

RG-07 : Tout retour exige un motif textuel non vide.
        Rejeter avec 400 Bad Request si motif absent ou vide.

RG-08 : SÉPARATION DES TÂCHES. L'acteur qui valide une étape ne peut
        pas être le même que celui qui a validé l'étape précédente.
        Vérification dans SeparationTachesService. Retourner 403.

RG-09 : HISTORISATION. Toute modification d'attribut métier doit être
        tracée dans audit_log avec delta JSON avant/après dans detail_json.

RG-10 : GRILLES TARIFAIRES. Toute création ou modification de grille
        tarifaire par l'ARH passe en statut EN_ATTENTE_DRH. La DRH
        valide ou rejette. À la validation, l'ancienne grille ACTIVE
        voit sa date_fin renseignée et la nouvelle passe à ACTIVE.
        Le système empêche deux grilles ACTIVE sans date_fin pour
        la même fonction.

RG-11 : IMPORT EXCEL. L'import de bénéficiaires via fichier Excel
        valide chaque ligne avant insertion. Les lignes invalides
        (doublon matricule, fonction inconnue, champs manquants) sont
        rejetées et listées dans le rapport d'import. Les lignes
        valides sont insérées même si d'autres lignes sont rejetées.

RG-12 : UNICITÉ DU PROCESSUS MENSUEL. Un seul processus mensuel peut
        exister pour une combinaison mois/année donnée. Contrainte
        UNIQUE(mois_paiement, annee_paiement) en base (script V1).
        Toute tentative de créer un second processus pour la même
        période retourne 409 Conflict. Cette règle est distincte de
        RG-03 (unicité du matricule bénéficiaire) — ne pas confondre
        les deux malgré la mention "RG-03 étendu" trouvée dans un
        document antérieur (user stories V3), qui référençait cette
        contrainte par erreur avant que RG-12 ne soit créée.

---

## 8. CONTRATS API : 34 ENDPOINTS

Base URL : /api
Auth : header Authorization Bearer token sur tous les endpoints
sauf /auth/login qui est public.

Groupe 1 : Auth
POST   /auth/login                           public
POST   /auth/logout                          authentifié

Groupe 2 : Enrôlement
GET    /enrolement/verifier?matricule={m}    EMPLOYE
POST   /enrolement/confirmer                 EMPLOYE

Groupe 3 : Bénéficiaires
GET    /beneficiaires                        ARH, DRH
PATCH  /beneficiaires/{id}                   ARH
DELETE /beneficiaires/{id}                   ARH
PATCH  /beneficiaires/{id}/reactiver         ARH
GET    /beneficiaires/export                 ARH, DRH
POST   /beneficiaires/import                 ARH

Groupe 4 : Fonctions éligibles (référentiel)
GET    /fonctions-eligibles                       ARH, DRH, ADMIN (actives uniquement)
GET    /fonctions-eligibles/toutes                ADMIN (actives et inactives)
POST   /fonctions-eligibles                       ADMIN
PATCH  /fonctions-eligibles/{code}                ADMIN
PATCH  /fonctions-eligibles/{code}/desactiver     ADMIN
PATCH  /fonctions-eligibles/{code}/reactiver      ADMIN

Groupe 5 : Processus mensuel
POST   /processus/declencher                 ARH
GET    /processus                            ARH, CRH, DRH
GET    /processus/{id}                       ARH, CRH, DRH
       Retourne lignesEtatMensuel[], pas beneficiaires[].
PATCH  /processus/{id}                       ARH (ajustement des lignes d'état mensuel)

Groupe 6 : Workflow
POST   /processus/{id}/valider               ARH si EN_COURS_ARH
                                             CRH si EN_ATTENTE_CRH
                                             DRH si EN_ATTENTE_DRH
POST   /processus/{id}/retourner             CRH, DRH
GET    /processus/{id}/piece-jointe          ARH, CRH, DRH (singulier)
GET    /pieces-jointes/{id}/download         ARH, CRH, DRH

Groupe 7 : Reporting
GET    /reporting/dashboard                  ARH, DRH
GET    /reporting/historique                 DRH
GET    /reporting/historique/export          DRH
GET    /reporting/audit                      DRH

Groupe 8 : Grilles tarifaires
GET    /grilles-tarifaires                        ARH, ADMIN
POST   /grilles-tarifaires                        ARH, ADMIN
PATCH  /grilles-tarifaires/{id}                   ARH, ADMIN
GET    /grilles-tarifaires/en-attente-drh         DRH
POST   /grilles-tarifaires/{id}/valider           DRH
GET    /grilles-tarifaires/fonction/{code}        ARH, DRH, ADMIN (historique par fonction)
POST   /grilles-tarifaires/{id}/desactiver        ARH

Groupe 9 : Administration
GET    /admin/utilisateurs                   ADMIN, DRH (DRH : lecture seule, alimente le
                                             filtre utilisateur du journal d'audit)
POST   /admin/utilisateurs                   ADMIN
PATCH  /admin/utilisateurs/{id}/statut       ADMIN
PATCH  /admin/utilisateurs/{id}/role         ADMIN

Codes HTTP : 200, 201, 400, 401, 403, 404, 409.

---

## 9. CONVENTIONS DE CODE OBLIGATOIRES

Attributs et visibilité :

- Tous les attributs JPA en private.
- Jamais d'entité JPA dans une réponse API. Toujours passer par un DTO.

Lombok :

- @Data ou @Getter + @Setter sur les entités.
- @Builder pour la construction fluide.
- @RequiredArgsConstructor pour l'injection par constructeur.

Transactions et sécurité :

- @Transactional sur toutes les méthodes de service qui écrivent.
- @PreAuthorize sur chaque endpoint sensible avec le rôle exact.
- Respecter les règles OWASP pour l'authentification.

Gestion des exceptions :

- Une seule classe GlobalExceptionHandler annotée @RestControllerAdvice.
- Ne pas gérer les exceptions dans les contrôleurs.

Nommage Java :

- UpperCamelCase pour les classes et interfaces.
- lowerCamelCase pour les méthodes et attributs.
- UPPER_SNAKE_CASE pour les constantes.
- Pas de préfixe I devant les interfaces.

Nommage des DTOs :

- RequestDto : corps de requête entrant (POST, PATCH).
- ResponseDto : corps de réponse sortant.
- Dto : générique. Exemples : LoginRequestDto, ProcessusDetailDto.

Audit :

- AuditService.enregistrer() en fin de chaque méthode de service.
- Pour toute modification d'attribut, passer le delta JSON (RG-09).

Tests :

- Fichier NomServiceTest.java dans src/test/java/com/afriland/dottel/service/.
- Mockito pour mocker les dépendances.
- Cas nominal et deux cas d'erreur minimum par méthode publique.

### Données de test et de démonstration

Toute donnée factice (stub EHR, jeux de test, données Flyway V3, données
Postman) doit être réaliste et camerounaise : noms et prénoms courants
au Cameroun (ex. MBARGA, ESSAMA, NKOLO, ATANGANA, TCHINDA, FOUDA,
BELINGA, ONANA, NDONGO, EYENGA), unités et agences situées dans des
villes camerounaises (Douala, Yaoundé, Bafoussam, Garoua, Bertoua,
Ngaoundéré, Buea), numéros de compte au format AFB plausible.
Ne jamais utiliser de données génériques type "John Doe", "Test User",
"Foo Bar".

---

## 10. ORDRE DES OPÉRATIONS DANS UN SERVICE

1. Validation des paramètres d'entrée.
2. Vérification des règles métier applicables (RG-01 à RG-12).
3. Récupération des entités depuis les repositories.
4. Exécution de l'opération métier.
5. Appel à AuditService.enregistrer().
6. Retour d'un DTO au contrôleur.

En cas d'échec aux étapes 1 ou 2, lever une exception métier explicite.
GlobalExceptionHandler la transformera en réponse HTTP appropriée.

---

## 11. PÉRIMÈTRE DU MODULE

Ce module couvre le circuit RH : enrôlement, déclenchement mensuel,
workflow ARH/CRH/DRH, gestion des grilles tarifaires et administration.

Les étapes comptables (génération du fichier d'écritures, validation
analyste comptable, imputation CBS) sont hors périmètre. À la clôture
d'un processus, le module publie un événement de clôture consommé par
le module de comptabilité existant. L'événement contient l'id du processus.
Le module comptable appelle ensuite GET /processus/{id} pour récupérer
les lignesEtatMensuel et générer les écritures selon le schéma :
DEBIT  : CODE_UNITE - 64310090002 - montant - DOT TEL MM/AAAA
CREDIT : AGENCE - N_COMPTE_COURANT - montant - DOT TEL MM/AAAA

---

## 12. INTÉGRATION EHR (simulation en développement)

L'EHR réel n'est pas accessible en stage. Implémenter un stub simulant
une base de données EHR en mémoire. Le stub retourne des données
cohérentes selon le matricule : nom, prénom, fonction (code correspondant
à une valeur de fonction_eligible.code), grade, unite_rattachement,
code_unite, num_compte_courant.

Les données du stub doivent respecter la convention de la section 9
(noms et unités camerounais réalistes).

Prévoir une correspondance entre les libellés EHR et les codes de
fonction_eligible pour gérer les écarts de nomenclature.

---

## 13. AUTHENTIFICATION KEYCLOAK (simulation en développement)

Mode retenu : application liée autonome partageant le realm Keycloak.
Pas de Client Extension Liferay.

En développement : simuler Keycloak avec une instance locale ou avec
l'authentification locale BCrypt. Respecter OWASP.

En production : spring-boot-starter-oauth2-resource-server pointant
vers le realm Keycloak de la banque.

---

## 14. DÉPLOIEMENT ET INFRASTRUCTURE

Décision confirmée par le manager DSI : le module DOTTEL est un
microservice conteneurisé, cohérent avec l'ensemble des modules BAOBAB.

Orchestration : Kubernetes. Le déploiement n'est pas un push manuel
d'image Docker sur Harbor ; Harbor sert uniquement de registre d'images,
Kubernetes orchestre les pods (déploiement, mise à l'échelle, réplication,
redémarrage automatique en cas d'échec).

Service registry / API Gateway : aucun pour le moment (pas d'Eureka,
pas de Consul, pas de gateway centralisée). Chaque module BAOBAB,
DOTTEL inclus, est exposé via son propre Service Kubernetes et sa
propre URL. Point à réévaluer si la DSI impose une gateway centralisée
plus tard — ne pas anticiper cette architecture dans le code actuel.

Conséquences concrètes pour le code :
- Le backend doit exposer /actuator/health en détail (déjà en place),
  utilisé par Kubernetes pour les liveness et readiness probes.
- Un Dockerfile est requis à la racine de backend/.
- Aucun secret ne doit être codé en dur ; toutes les valeurs sensibles
  (DB_PASSWORD, DOTTEL_JWT_SECRET, DB_URL, DB_USER) sont lues depuis
  des variables d'environnement, destinées à être injectées via des
  Secrets Kubernetes en production.
- Les manifests Kubernetes (Deployment, Service, ConfigMap, Secret)
  sont introduits progressivement à partir du Sprint 0.5.
- Namespace, conventions de nommage des Deployments/Services et
  méthode de gestion des Secrets restent à confirmer avec la DSI.

---

## 15. CHARTE VISUELLE FRONTEND

Basée sur les captures d'écran des modules BAOBAB existants.

- Navigation latérale gauche : fond sombre, texte et icônes blancs,
  élément actif en rouge E30613.
- Contenu principal : fond blanc ou gris très clair F5F5F5.
- Boutons primaires en rouge E30613, secondaires en gris ou contour.
- Tableaux : en-tête gris clair, texte sombre, bordures discrètes.
- Formulaires : champs blancs, bordure grise, sobres.
- Typographie sans-serif sobre.
- Logo AFB en haut de la barre de navigation.

---

## 16. ORDRE D'IMPLÉMENTATION DES SPRINTS

Sprint 0    : Initialisation, configuration, structure, CLAUDE.md,
              conteneurisation de base (Dockerfile, manifests K8s).
Sprint 1    : Auth JWT/Keycloak simulé, Spring Security, tests.
Sprint 2    : Enrôlement UC01, éligibilité UC02, stub EHR (données
              camerounaises), import Excel, tests unitaires.
Sprint 3    : Processus mensuel UC03, LigneEtatMensuel,
              DocumentService.genererInitiale.
Sprint 4    : Gestion bénéficiaires UC04, export Excel.
Sprint 4bis : Grilles tarifaires (RG-10), administration utilisateurs.
Sprint 5    : Workflow UC05, UC06, UC07, DocumentService.ajouterSignature,
              événement de clôture vers module comptable.
Sprint 6    : Reporting, tableau de bord, journal d'audit.
Sprint 6F   : Frontend complet (une série de sessions dédiées).
Sprint 7    : Tests d'intégration, recette, corrections, README.

---

## 17. POINTS D'ATTENTION SPÉCIAUX

1.  LigneEtatMensuel est la table pivot N-N obligatoire entre
    processus_mensuel et beneficiaires. Jamais de @ManyToMany direct.

2.  PieceJointe : un seul enregistrement par processus. En cas de
    resoumission, le document est remplacé, pas dupliqué.

3.  FonctionEnum n'existe plus. La vérification d'éligibilité interroge
    fonction_eligible (actif = true) puis grille_tarifaire (ACTIVE,
    date_fin IS NULL) pour le montant courant.

4.  Les 3 corps assimilés (CONTROLEUR_GESTION, CONTROLEUR_COMPTABLE,
    COMPTABLE) sont soumis à RG-02 au même titre que CORPS_CONTROLE_IG
    et CORPS_CONTROLE_IGA.

5.  EligibiliteService interroge l'EHR via le stub en développement,
    avec des données camerounaises réalistes. Prévoir la correspondance
    entre libellés EHR et codes fonction_eligible.

6.  Aucun secret en clair dans application*.yml — variables
    d'environnement, destinées à des Secrets Kubernetes en production.

7.  TypeDocumentEnum n'existe pas dans ce projet.

8.  NotificationService est un service pur sans table en base, appelé
    par WorkflowService à chaque transition de statut.

9.  À la clôture d'un processus mensuel, WorkflowService publie un
    événement de clôture. Ne pas implémenter les écritures comptables
    dans ce module.

10. Le module est déployé comme microservice Kubernetes, sans
    service registry ni gateway pour l'instant (voir section 14).

---

## 18. ERREURS À NE JAMAIS COMMETTRE

1.  Créer trois PieceJointe distinctes par processus (violation RG-06).
2.  Créer TypeDocumentEnum ou FonctionEnum.
3.  Créer un @ManyToMany direct entre Beneficiaire et ProcessusMensuel.
4.  Retourner une entité JPA dans un endpoint.
5.  Utiliser une session HTTP côté serveur (stateless via JWT/Keycloak).
6.  Coder en dur un montant de dotation (violation RG-04).
7.  Valider sans passer par SeparationTachesService (violation RG-08).
8.  Générer un PDF à chaque validation CRH ou DRH (violation RG-06).
9.  Créer un endpoint métier sans @PreAuthorize.
10. Exposer un secret dans application*.yml ou dans un manifest K8s en clair.
11. Implémenter les écritures comptables dans ce module (hors périmètre).
12. Appeler AuditService sans delta JSON pour une modification (RG-09).
13. Vérifier l'éligibilité contre FonctionEnum au lieu de fonction_eligible.
14. Lire le montant depuis fonction_eligible au lieu de grille_tarifaire.
15. Activer une grille tarifaire sans validation DRH (violation RG-10).
16. Insérer des bénéficiaires en masse sans rapport d'import (violation RG-11).
17. Utiliser des données de test génériques non camerounaises
    (John Doe, Test User, Foo Bar) dans les stubs ou les jeux de test.
18. Introduire un service registry ou une gateway non demandée
    par la DSI (voir section 14).
19. Créer un second processus mensuel pour la même combinaison
    mois/année (violation RG-12).