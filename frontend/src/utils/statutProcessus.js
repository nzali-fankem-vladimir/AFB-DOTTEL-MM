// Mapping centralise statut -> libelle/variant/ordre d'etape, partage entre
// ProcessusListPage (badge simple) et ProcessusDetailPage (badge + timeline).
// RETOURNE n'a pas d'etape de timeline dediee : il revient visuellement sur
// l'etape ARH (l'ARH reprend la main via PATCH puis revalide, cf. RG-05/RG-08
// et ProcessusMensuelService.ajuster/valider).
export const STATUTS_PROCESSUS = {
  EN_COURS_ARH: { libelle: 'En cours ARH', variant: 'neutral', etapeCourante: 0 },
  EN_ATTENTE_CRH: { libelle: 'En attente CRH', variant: 'warning', etapeCourante: 1 },
  EN_ATTENTE_DRH: { libelle: 'En attente DRH', variant: 'info', etapeCourante: 2 },
  RETOURNE: { libelle: 'Retourné', variant: 'destructive', etapeCourante: 0 },
  CLOTURE: { libelle: 'Clôturé', variant: 'success', etapeCourante: 3 },
};

export function getStatutProcessusInfo(statut) {
  return STATUTS_PROCESSUS[statut] ?? { libelle: statut, variant: 'neutral', etapeCourante: 0 };
}

export const ETAPES_WORKFLOW = [
  { cle: 'ARH', libelle: 'ARH' },
  { cle: 'CRH', libelle: 'CRH' },
  { cle: 'DRH', libelle: 'DRH' },
];
