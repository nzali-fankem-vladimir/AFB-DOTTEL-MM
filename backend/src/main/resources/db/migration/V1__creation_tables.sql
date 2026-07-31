-- V1__creation_tables.sql
-- Module DOTTEL - Dotations Telephoniques Mensuelles - AFRILAND HORIZON 2030
-- Creation du schema : 10 tables (voir CLAUDE.md section 4)

-- =====================================================================
-- 1. utilisateurs
-- FK vers beneficiaires ajoutee en ALTER TABLE apres creation de
-- la table beneficiaires (reference avant).
-- =====================================================================
CREATE TABLE utilisateurs (
    id                  BIGSERIAL PRIMARY KEY,
    matricule           VARCHAR(20) NOT NULL,
    nom                 VARCHAR(100) NOT NULL,
    prenom              VARCHAR(100) NOT NULL,
    email               VARCHAR(150) NOT NULL,
    role                VARCHAR(50) NOT NULL,
    mot_de_passe_hash   VARCHAR(255) NOT NULL,
    actif               BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_beneficiaire     BIGINT,
    CONSTRAINT uq_utilisateurs_matricule UNIQUE (matricule),
    CONSTRAINT uq_utilisateurs_email UNIQUE (email),
    CONSTRAINT chk_utilisateurs_role CHECK (role IN ('EMPLOYE', 'ARH', 'CRH', 'DRH', 'ADMIN'))
);

-- =====================================================================
-- 2. beneficiaires
-- =====================================================================
CREATE TABLE beneficiaires (
    id                  BIGSERIAL PRIMARY KEY,
    matricule           VARCHAR(20) NOT NULL,
    nom_prenoms         VARCHAR(200) NOT NULL,
    fonction            VARCHAR(50) NOT NULL,
    grade               VARCHAR(100),
    unite_rattachement  VARCHAR(150) NOT NULL,
    code_unite          VARCHAR(20) NOT NULL,
    num_compte_courant  VARCHAR(30) NOT NULL,
    date_enrolement     DATE NOT NULL DEFAULT CURRENT_DATE,
    actif               BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_beneficiaires_matricule UNIQUE (matricule)
);

-- Ajout de la FK utilisateurs.id_beneficiaire -> beneficiaires.id
ALTER TABLE utilisateurs
    ADD CONSTRAINT fk_utilisateurs_beneficiaire
    FOREIGN KEY (id_beneficiaire) REFERENCES beneficiaires (id);

-- =====================================================================
-- 3. fonction_eligible
-- =====================================================================
CREATE TABLE fonction_eligible (
    id      BIGSERIAL PRIMARY KEY,
    code    VARCHAR(50) NOT NULL,
    libelle VARCHAR(150) NOT NULL,
    actif   BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_fonction_eligible_code UNIQUE (code)
);

-- Ajout de la FK beneficiaires.fonction -> fonction_eligible.code
ALTER TABLE beneficiaires
    ADD CONSTRAINT fk_beneficiaires_fonction
    FOREIGN KEY (fonction) REFERENCES fonction_eligible (code);

-- =====================================================================
-- 4. grille_tarifaire (id_createur et id_validateur nullable)
-- =====================================================================
CREATE TABLE grille_tarifaire (
    id                    BIGSERIAL PRIMARY KEY,
    id_fonction_eligible  BIGINT NOT NULL,
    montant_fcfa          INTEGER NOT NULL,
    date_debut            DATE NOT NULL,
    date_fin              DATE,
    statut_validation     VARCHAR(50) NOT NULL,
    id_createur           BIGINT,
    id_validateur         BIGINT,
    date_creation         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_validation       TIMESTAMP,
    motif_rejet           TEXT,
    CONSTRAINT fk_grille_tarifaire_fonction
        FOREIGN KEY (id_fonction_eligible) REFERENCES fonction_eligible (id),
    CONSTRAINT fk_grille_tarifaire_createur
        FOREIGN KEY (id_createur) REFERENCES utilisateurs (id),
    CONSTRAINT fk_grille_tarifaire_validateur
        FOREIGN KEY (id_validateur) REFERENCES utilisateurs (id),
    CONSTRAINT chk_grille_tarifaire_statut
        CHECK (statut_validation IN ('BROUILLON', 'EN_ATTENTE_DRH', 'ACTIVE', 'REJETEE'))
);

-- Index partiel : une seule grille ACTIVE sans date_fin par fonction
CREATE UNIQUE INDEX idx_grille_active_unique
    ON grille_tarifaire (id_fonction_eligible)
    WHERE statut_validation = 'ACTIVE' AND date_fin IS NULL;

-- =====================================================================
-- 5. ligne_etat_mensuel (table pivot N-N processus_mensuel <-> beneficiaires)
-- FK vers processus_mensuel ajoutee en ALTER TABLE apres creation de
-- la table processus_mensuel (reference avant).
-- =====================================================================
CREATE TABLE ligne_etat_mensuel (
    id                  BIGSERIAL PRIMARY KEY,
    id_processus        BIGINT NOT NULL,
    id_beneficiaire     BIGINT NOT NULL,
    montant_applique    INTEGER NOT NULL,
    inclus_dans_etat    BOOLEAN NOT NULL DEFAULT TRUE,
    fonction_retenue    VARCHAR(50) NOT NULL,
    CONSTRAINT fk_ligne_etat_mensuel_beneficiaire
        FOREIGN KEY (id_beneficiaire) REFERENCES beneficiaires (id),
    CONSTRAINT uq_ligne_etat_mensuel_processus_beneficiaire
        UNIQUE (id_processus, id_beneficiaire)
);

-- =====================================================================
-- 6. processus_mensuel
-- =====================================================================
CREATE TABLE processus_mensuel (
    id              BIGSERIAL PRIMARY KEY,
    mois_paiement   INTEGER NOT NULL,
    annee_paiement  INTEGER NOT NULL,
    statut          VARCHAR(50) NOT NULL,
    date_creation   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_cloture    TIMESTAMP,
    id_createur     BIGINT NOT NULL,
    CONSTRAINT fk_processus_mensuel_createur
        FOREIGN KEY (id_createur) REFERENCES utilisateurs (id),
    CONSTRAINT uq_processus_mensuel_mois_annee
        UNIQUE (mois_paiement, annee_paiement),
    CONSTRAINT chk_processus_mensuel_statut
        CHECK (statut IN ('EN_COURS_ARH', 'EN_ATTENTE_CRH', 'EN_ATTENTE_DRH', 'RETOURNE', 'CLOTURE'))
);

-- Ajout de la FK ligne_etat_mensuel.id_processus -> processus_mensuel.id
ALTER TABLE ligne_etat_mensuel
    ADD CONSTRAINT fk_ligne_etat_mensuel_processus
    FOREIGN KEY (id_processus) REFERENCES processus_mensuel (id);

-- =====================================================================
-- 7. etape_workflow
-- =====================================================================
CREATE TABLE etape_workflow (
    id                  BIGSERIAL PRIMARY KEY,
    id_processus        BIGINT NOT NULL,
    id_acteur           BIGINT NOT NULL,
    ordre_etape         INTEGER NOT NULL,
    nom_etape           VARCHAR(50) NOT NULL,
    statut_etape        VARCHAR(50) NOT NULL,
    date_action         TIMESTAMP,
    motif_retour        TEXT,
    signature_numerique TEXT,
    CONSTRAINT fk_etape_workflow_processus
        FOREIGN KEY (id_processus) REFERENCES processus_mensuel (id),
    CONSTRAINT fk_etape_workflow_acteur
        FOREIGN KEY (id_acteur) REFERENCES utilisateurs (id),
    CONSTRAINT chk_etape_workflow_nom_etape
        CHECK (nom_etape IN ('VALIDATION_ARH', 'VALIDATION_CRH', 'VALIDATION_DRH')),
    CONSTRAINT chk_etape_workflow_statut_etape
        CHECK (statut_etape IN ('EN_ATTENTE', 'VALIDEE', 'RETOURNEE'))
);

-- =====================================================================
-- 8. piece_jointe (un seul document par processus)
-- =====================================================================
CREATE TABLE piece_jointe (
    id                          BIGSERIAL PRIMARY KEY,
    id_processus                BIGINT NOT NULL,
    nom_fichier                 VARCHAR(255) NOT NULL,
    chemin_stockage             VARCHAR(500) NOT NULL,
    date_generation_initiale    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_mise_a_jour   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nombre_signatures           INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_piece_jointe_processus
        FOREIGN KEY (id_processus) REFERENCES processus_mensuel (id),
    CONSTRAINT uq_piece_jointe_processus
        UNIQUE (id_processus)
);

-- =====================================================================
-- 9. regle_eligibilite
-- =====================================================================
CREATE TABLE regle_eligibilite (
    id                  BIGSERIAL PRIMARY KEY,
    code_regle          VARCHAR(50) NOT NULL,
    description         VARCHAR(500) NOT NULL,
    type_regle          VARCHAR(50) NOT NULL,
    valeur_condition    VARCHAR(255),
    valeur_reponse      VARCHAR(500) NOT NULL,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_regle_eligibilite_code UNIQUE (code_regle)
);

-- =====================================================================
-- 10. audit_log
-- =====================================================================
CREATE TABLE audit_log (
    id              BIGSERIAL PRIMARY KEY,
    id_utilisateur  BIGINT NOT NULL,
    action          VARCHAR(100) NOT NULL,
    entite_cible    VARCHAR(100) NOT NULL,
    id_entite       BIGINT,
    date_action     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    adresse_ip      VARCHAR(45),
    detail_json     TEXT,
    CONSTRAINT fk_audit_log_utilisateur
        FOREIGN KEY (id_utilisateur) REFERENCES utilisateurs (id)
);