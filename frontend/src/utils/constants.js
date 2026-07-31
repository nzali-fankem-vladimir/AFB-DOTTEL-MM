export const ROLES = {
  EMPLOYE: 'EMPLOYE',
  ARH: 'ARH',
  CRH: 'CRH',
  DRH: 'DRH',
  ADMIN: 'ADMIN',
};

export const STATUTS = {
  EN_COURS_ARH: 'EN_COURS_ARH',
  EN_ATTENTE_CRH: 'EN_ATTENTE_CRH',
  EN_ATTENTE_DRH: 'EN_ATTENTE_DRH',
  RETOURNE: 'RETOURNE',
  CLOTURE: 'CLOTURE',
};

export const STATUTS_LABELS = {
  [STATUTS.EN_COURS_ARH]: 'En cours ARH',
  [STATUTS.EN_ATTENTE_CRH]: 'En attente CRH',
  [STATUTS.EN_ATTENTE_DRH]: 'En attente DRH',
  [STATUTS.RETOURNE]: 'Retourné',
  [STATUTS.CLOTURE]: 'Clôturé',
};

export const STATUTS_GRILLE = {
  BROUILLON: 'BROUILLON',
  EN_ATTENTE_DRH: 'EN_ATTENTE_DRH',
  ACTIVE: 'ACTIVE',
  REJETEE: 'REJETEE',
};

export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';