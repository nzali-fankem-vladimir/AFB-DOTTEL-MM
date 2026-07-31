-- V4__ajout_chapitre_beneficiaires.sql
-- Module DOTTEL - Sprint 3.4
-- Ajout de la colonne chapitre a beneficiaires : donnee EHR utilisee dans
-- la section "Etat recapitulatif" du PDF genere par DocumentService.
-- Nullable : les beneficiaires enroles avant ce sprint n'ont pas de valeur ;
-- un repli configurable (dottel.documents.chapitre-defaut) est applique
-- cote application quand la valeur est absente.

ALTER TABLE beneficiaires
    ADD COLUMN chapitre VARCHAR(20);