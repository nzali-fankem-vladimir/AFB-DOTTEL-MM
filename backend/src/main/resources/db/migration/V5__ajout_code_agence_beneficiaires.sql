-- V5__ajout_code_agence_beneficiaires.sql
-- Module DOTTEL - Sprint MM.10
-- Ajout de la colonne code_agence a beneficiaires : agence ou direction ou
-- le compte courant du beneficiaire est domicilie (5 CHIFFRES, ex. 00001),
-- distinct de code_unite (unite d'affectation professionnelle, 4 chiffres).
-- Donnee EHR au meme titre que code_unite et num_compte_courant.
--
-- NOT NULL : contrairement a chapitre (Sprint 3.4, reste nullable avec repli
-- applicatif), le format et le referentiel de code_agence sont desormais
-- confirmes par le metier -- decision explicite d'imposer la contrainte en
-- base. Les beneficiaires deja enroles recoivent la valeur de repli 00001
-- (Agence Retraite / Siege Social, Yaounde), en l'absence de domiciliation
-- connue pour l'existant.

ALTER TABLE beneficiaires
    ADD COLUMN code_agence VARCHAR(5);

UPDATE beneficiaires
    SET code_agence = '00001'
    WHERE code_agence IS NULL;

ALTER TABLE beneficiaires
    ALTER COLUMN code_agence SET NOT NULL;
