// Mapping centralise statut -> libelle/variant, partage entre
// GrillesListPage, HistoriqueGrillePage et la validation DRH.
// Meme convention de variants que statutProcessus.js.
export const STATUTS_GRILLE = {
  BROUILLON: { libelle: 'Brouillon', variant: 'neutral' },
  EN_ATTENTE_DRH: { libelle: 'En attente DRH', variant: 'info' },
  ACTIVE: { libelle: 'Active', variant: 'success' },
  CLOTUREE: { libelle: 'Clôturée', variant: 'neutral' },
  REJETEE: { libelle: 'Rejetée', variant: 'destructive' },
};

// Le backend ne distingue pas une grille ACTIVE courante d'une grille ACTIVE
// close (remplacement automatique ou desactivation manuelle, Sprint 6F.7bis) :
// dans les deux cas statutValidation reste ACTIVE, seul dateFin est renseigne
// (RG-04 : "active" = ACTIVE ET dateFin IS NULL). getStatutGrilleInfo derive
// donc l'affichage a partir des deux champs, pas du seul statutValidation.
export function getStatutGrilleInfo({ statutValidation, dateFin } = {}) {
  if (statutValidation === 'ACTIVE' && dateFin) {
    return STATUTS_GRILLE.CLOTUREE;
  }
  return STATUTS_GRILLE[statutValidation] ?? { libelle: statutValidation, variant: 'neutral' };
}