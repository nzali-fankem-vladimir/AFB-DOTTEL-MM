-- V6__ajout_rattrapage_processus_mensuel.sql
-- Module DOTTEL - Sprint MM.11
-- RG-12 evolue (CLAUDE.md section 7) : distingue un processus de rattrapage
-- d'un processus normal, plutot que de supprimer l'unicite mois/annee.
--
-- rattrapage : FALSE pour tout processus normal (comportement historique,
-- valeur par defaut). TRUE pour un processus cree pour rattraper des
-- beneficiaires non payes sur une periode deja traitee.
--
-- id_processus_original : trace, pour un rattrapage, le processus normal
-- CLOTURE dont il decoule (traçabilite RG-09 et reconstruction de
-- l'historique d'une periode). NULL pour un processus normal.
ALTER TABLE processus_mensuel
    ADD COLUMN rattrapage BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN id_processus_original BIGINT;

ALTER TABLE processus_mensuel
    ADD CONSTRAINT fk_processus_mensuel_original
    FOREIGN KEY (id_processus_original) REFERENCES processus_mensuel (id);

-- Remplacement de la contrainte d'unicite globale (script V1) par une
-- contrainte partielle : l'unicite mois/annee ne s'applique plus qu'aux
-- processus normaux. Plusieurs rattrapages peuvent coexister sur la meme
-- periode. Precedent dans ce projet : idx_grille_active_unique
-- (grille_tarifaire, script V1) suit deja ce pattern d'index partiel.
ALTER TABLE processus_mensuel
    DROP CONSTRAINT uq_processus_mensuel_mois_annee;

CREATE UNIQUE INDEX idx_processus_mensuel_normal_unique
    ON processus_mensuel (mois_paiement, annee_paiement)
    WHERE rattrapage = FALSE;
