-- =====================================================================
-- Sprint MM.12 : workflow des grilles tarifaires a trois acteurs
-- (ARH -> CRH -> DRH), evolution de RG-10.
--
-- Aucun script anterieur n'est modifie : V1 reste tel qu'applique.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Nouveau statut EN_ATTENTE_CRH
--
-- La contrainte CHECK creee en V1 n'enumere que les 4 valeurs d'origine :
-- sans cette reprise, tout INSERT/UPDATE portant EN_ATTENTE_CRH echouerait
-- en base, meme si l'enumeration Java l'accepte.
-- ---------------------------------------------------------------------
ALTER TABLE grille_tarifaire
    DROP CONSTRAINT chk_grille_tarifaire_statut;

ALTER TABLE grille_tarifaire
    ADD CONSTRAINT chk_grille_tarifaire_statut
    CHECK (statut_validation IN ('BROUILLON', 'EN_ATTENTE_CRH', 'EN_ATTENTE_DRH', 'ACTIVE', 'REJETEE'));

-- ---------------------------------------------------------------------
-- 2. Trace de la decision CRH (option W-2 de RG-08, arbitree le 2026-08-09)
--
-- RG-08 est appliquee au workflow des grilles avec un mecanisme PROPRE au
-- module referentiel. Reutiliser SeparationTachesService (module processus)
-- aurait cree un second cycle de modules referentiel -> processus, rejete
-- par ModularityTests -- voir MM.12 section 1bis.
--
-- Nommage : "decideur" et non "validateur", alors que le couple DRH existant
-- s'appelle id_validateur / date_validation. Ce n'est pas une incoherence
-- mais une correction locale : ces colonnes sont renseignees a la validation
-- COMME au rejet. Le nom "validateur" du couple DRH est historique et
-- trompeur au meme titre (validerOuRejeter() y ecrit aussi lors d'un rejet) ;
-- il n'est pas renomme ici -- hors perimetre, et cela exigerait de reprendre
-- les lignes existantes. La derivation de l'origine d'un rejet (CRH ou DRH,
-- equivalent d'origineRetour du processus mensuel) DEPEND de cet invariant
-- "renseigne a toute decision".
-- ---------------------------------------------------------------------
ALTER TABLE grille_tarifaire
    ADD COLUMN id_decideur_crh   BIGINT,
    ADD COLUMN date_decision_crh TIMESTAMP;

ALTER TABLE grille_tarifaire
    ADD CONSTRAINT fk_grille_tarifaire_decideur_crh
    FOREIGN KEY (id_decideur_crh) REFERENCES utilisateurs (id);

COMMENT ON COLUMN grille_tarifaire.id_decideur_crh IS
    'Acteur CRH ayant statue sur la grille -- renseigne a la validation COMME au rejet (RG-08, MM.12)';
COMMENT ON COLUMN grille_tarifaire.date_decision_crh IS
    'Horodatage de la decision CRH, validation ou rejet (MM.12)';

-- ---------------------------------------------------------------------
-- 3. Grilles deja EN_ATTENTE_DRH au moment du deploiement
--
-- Decision utilisateur du 2026-08-09 : elles sont LAISSEES EN L'ETAT. Le CRH
-- est repute avoir implicitement valide -- ces grilles ont ete soumises sous
-- l'ancien circuit a deux acteurs, les faire repasser par le CRH allongerait
-- un circuit deja engage sans gain de controle. Leur id_decideur_crh reste
-- donc NULL, ce qui est coherent : aucun CRH n'a statue.
--
-- Consequence assumee sur RG-08 : pour ces grilles heritees, la verification
-- de separation des taches a l'etape DRH n'a pas de decideur CRH auquel se
-- comparer et est donc inoperante (voir GrilleTarifaireService.validerBrancheDrh,
-- qui ne bloque pas sur un id_decideur_crh NULL). Population finie et
-- transitoire, qui s'eteint d'elle-meme.
--
-- Aucun UPDATE : l'absence d'instruction ici est la decision, pas un oubli.
-- ---------------------------------------------------------------------
