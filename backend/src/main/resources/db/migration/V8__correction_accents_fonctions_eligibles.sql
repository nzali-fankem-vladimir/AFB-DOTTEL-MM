-- V8__correction_accents_fonctions_eligibles.sql
-- Module DOTTEL - Sprint 7.2 (adapte MM)
-- Correction des accents manquants dans fonction_eligible.libelle,
-- insere sans accents par V2__insertion_donnees_reference.sql.
-- V2 ne doit jamais etre modifie directement (deja applique, le
-- checksum Flyway casserait au prochain demarrage sur une base
-- existante) -- correction ciblee ici par UPDATE sur le code (stable),
-- jamais sur le libelle (c'est justement ce qu'on corrige).
UPDATE fonction_eligible SET libelle = 'Administrateur Directeur Général' WHERE code = 'ADG';
UPDATE fonction_eligible SET libelle = 'Directeur Général Adjoint' WHERE code = 'DGA';
UPDATE fonction_eligible SET libelle = 'Inspecteur Général' WHERE code = 'CORPS_CONTROLE_IG';
UPDATE fonction_eligible SET libelle = 'Inspecteur Général Adjoint' WHERE code = 'CORPS_CONTROLE_IGA';
UPDATE fonction_eligible SET libelle = 'Directeur Central/Succursale/Régional' WHERE code = 'DIRECTEUR';
UPDATE fonction_eligible SET libelle = 'Conseiller/Chargé de Mission/Chef Mission' WHERE code = 'CONSEILLER';
UPDATE fonction_eligible SET libelle = 'Chef de Département' WHERE code = 'CHEF_DEPARTEMENT';
UPDATE fonction_eligible SET libelle = 'Contrôleur de Gestion' WHERE code = 'CONTROLEUR_GESTION';
UPDATE fonction_eligible SET libelle = 'Contrôleur Comptable' WHERE code = 'CONTROLEUR_COMPTABLE';
UPDATE fonction_eligible SET libelle = 'Attaché Commercial' WHERE code = 'ATTACHE_COMMERCIAL';
