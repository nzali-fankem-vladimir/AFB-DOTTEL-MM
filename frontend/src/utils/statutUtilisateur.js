// Mapping centralise role -> libelle/variant de badge, partage entre
// UtilisateursListPage et toute future page consommant RoleEnum.
export const ROLES_UTILISATEUR = {
  EMPLOYE: { libelle: 'Employé', variant: 'neutral' },
  ARH: { libelle: 'ARH', variant: 'info' },
  CRH: { libelle: 'CRH', variant: 'warning' },
  DRH: { libelle: 'DRH', variant: 'success' },
  ADMIN: { libelle: 'Admin', variant: 'destructive' },
};

export function getRoleInfo(role) {
  return ROLES_UTILISATEUR[role] ?? { libelle: role, variant: 'neutral' };
}