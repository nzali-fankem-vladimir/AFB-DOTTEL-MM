package com.afriland.dottel.referentiel.model.enums;

/**
 * Cycle de vie d'une grille tarifaire (RG-10).
 *
 * Sprint MM.12 : le CRH est insere entre l'ARH et la DRH, sur le modele du
 * workflow du processus mensuel (StatutEnum). La sequence devient :
 *
 * <pre>
 * creation ARH --&gt; EN_ATTENTE_CRH --(CRH valide)--&gt; EN_ATTENTE_DRH --(DRH valide)--&gt; ACTIVE
 *                        |                                |
 *                  (CRH rejette)                    (DRH rejette)
 *                        +------------&gt; REJETEE &lt;---------+
 * </pre>
 *
 * BROUILLON reste declare mais demeure INATTEIGNABLE depuis la decision du
 * Sprint 4bis.1 (la creation passe directement au premier statut d'attente).
 * Conserve pour ne pas invalider d'eventuelles lignes historiques.
 *
 * NB (CLAUDE.md section 6) : ajouter EN_ATTENTE_CRH porte cette enumeration a
 * 5 valeurs mais ne cree PAS une 6e enumeration -- le projet en compte
 * toujours 5.
 */
public enum StatutGrilleEnum {
    BROUILLON,
    EN_ATTENTE_CRH,
    EN_ATTENTE_DRH,
    ACTIVE,
    REJETEE
}