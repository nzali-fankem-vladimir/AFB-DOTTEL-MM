-- V2__insertion_donnees_reference.sql
-- Module DOTTEL - Dotations Telephoniques Mensuelles - AFRILAND HORIZON 2030
-- Insertion des donnees de reference : 25 fonctions eligibles,
-- 25 grilles tarifaires initiales, regles metier residuelles.
-- Source : Note de service NS 69/17 + enrichissement metier (CLAUDE.md section 5).

-- =====================================================================
-- PARTIE 1 : fonction_eligible (25 fonctions, toutes actives)
-- =====================================================================
INSERT INTO fonction_eligible (code, libelle, actif)
VALUES
    ('ADG', 'Administrateur Directeur General', TRUE),
    ('DGA', 'Directeur General Adjoint', TRUE),
    ('CORPS_CONTROLE_IG', 'Inspecteur General', TRUE),
    ('DIRECTEUR_GROUPE', 'Directeur Groupe', TRUE),
    ('CORPS_CONTROLE_IGA', 'Inspecteur General Adjoint', TRUE),
    ('DIRECTEUR', 'Directeur Central/Succursale/Regional', TRUE),
    ('DCA', 'Directeur Central Adjoint', TRUE),
    ('DA', 'Directeur d''Agence', TRUE),
    ('CONSEILLER', 'Conseiller/Charge de Mission/Chef Mission', TRUE),
    ('DA_ADJOINT', 'Directeur d''Agence Adjoint', TRUE),
    ('CHEF_DEPARTEMENT', 'Chef de Departement', TRUE),
    ('CHEF_ANTENNE', 'Chef d''Antenne', TRUE),
    ('GFC', 'Gestionnaire de Fonds de Commerce', TRUE),
    ('ASSISTANTE_PCA', 'Assistante du PCA', TRUE),
    ('ASSISTANTE_ADG', 'Assistante de l''ADG', TRUE),
    ('ASSISTANTE_DGA', 'Assistante du DGA', TRUE),
    ('CHEF_DIVISION', 'Chef de Division', TRUE),
    ('CHEF_PRODUIT', 'Chef de Produit', TRUE),
    ('COORDONNATEUR', 'Coordonnateur', TRUE),
    ('JURISTE', 'Agent de Recouvrement', TRUE),
    ('CONTROLEUR_GESTION', 'Controleur de Gestion', TRUE),
    ('CONTROLEUR_COMPTABLE', 'Controleur Comptable', TRUE),
    ('COMPTABLE', 'Comptable', TRUE),
    ('ASSISTANT_CHEF_PROD', 'Assistant Chef de Produit', TRUE),
    ('ATTACHE_COMMERCIAL', 'Attache Commercial', TRUE)
ON CONFLICT (code) DO NOTHING;

-- =====================================================================
-- PARTIE 2 : grille_tarifaire (24 grilles initiales, statut ACTIVE)
-- Grilles systeme : id_createur et id_validateur NULL,
-- date_debut '2025-01-01', date_fin NULL.
-- =====================================================================
INSERT INTO grille_tarifaire (id_fonction_eligible, montant_fcfa, date_debut, date_fin, statut_validation, id_createur, id_validateur, date_creation, date_validation, motif_rejet)
SELECT fe.id, v.montant_fcfa, DATE '2025-01-01', NULL, 'ACTIVE', NULL, NULL, NOW(), NULL, NULL
FROM (VALUES
    ('ADG', 150000),
    ('DGA', 130000),
    ('CORPS_CONTROLE_IG', 70000),
    ('DIRECTEUR_GROUPE', 70000),
    ('CORPS_CONTROLE_IGA', 65000),
    ('DIRECTEUR', 60000),
    ('DCA', 50000),
    ('DA', 50000),
    ('CONSEILLER', 50000),
    ('DA_ADJOINT', 40000),
    ('CHEF_DEPARTEMENT', 40000),
    ('CHEF_ANTENNE', 40000),
    ('GFC', 40000),
    ('ASSISTANTE_PCA', 40000),
    ('ASSISTANTE_ADG', 40000),
    ('ASSISTANTE_DGA', 40000),
    ('CHEF_DIVISION', 35000),
    ('CHEF_PRODUIT', 35000),
    ('COORDONNATEUR', 35000),
    ('JURISTE', 35000),
    ('CONTROLEUR_GESTION', 35000),
    ('CONTROLEUR_COMPTABLE', 35000),
    ('COMPTABLE', 35000),
    ('ASSISTANT_CHEF_PROD', 30000),
    ('ATTACHE_COMMERCIAL', 30000)
) AS v(code_fonction, montant_fcfa)
JOIN fonction_eligible fe ON fe.code = v.code_fonction
ON CONFLICT (id_fonction_eligible) WHERE statut_validation = 'ACTIVE' AND date_fin IS NULL
    DO NOTHING;

-- =====================================================================
-- PARTIE 3 : regle_eligibilite (regles metier residuelles)
-- =====================================================================
INSERT INTO regle_eligibilite (code_regle, description, type_regle, valeur_condition, valeur_reponse, active)
VALUES
    ('SEUIL_VALIDATION_CRH', 'Seuil de montant a partir duquel une validation CRH est requise', 'SEUIL', NULL, '100000', TRUE),
    ('CORPS_CONTROLE_CODES', 'Codes fonction soumis a la verification du grade (RG-02)', 'LISTE_CODES', NULL, 'CORPS_CONTROLE_IG,CORPS_CONTROLE_IGA,CONTROLEUR_GESTION,CONTROLEUR_COMPTABLE,COMPTABLE', TRUE)
ON CONFLICT (code_regle) DO NOTHING;