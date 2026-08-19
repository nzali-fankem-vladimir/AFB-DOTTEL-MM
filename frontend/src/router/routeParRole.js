// Route de destination par defaut selon le role de l'utilisateur.
//
// Extraite de CallbackKeycloak.jsx au Sprint MM.14 : elle y etait privee,
// alors que trois ecrans en ont besoin -- le retour de Keycloak (redirection
// apres connexion), AccesInterdit et PageIntrouvable (lien "retour a
// l'accueil"). Une seule source pour eviter qu'un role soit corrige a un
// endroit et pas aux autres.
//
// Chaque valeur doit rester accessible au role concerne dans AppRouter,
// sinon la connexion aboutit sur /acces-interdit (constat de l'audit
// Sprint 6F.9 : le CRH pointait encore vers /dashboard, devenu ARH/DRH
// seulement le 2026-07-30).
export const ROUTE_PAR_ROLE = {
  EMPLOYE: '/enrolement',
  ARH: '/dashboard',
  CRH: '/processus',
  DRH: '/dashboard',
  ADMIN: '/grilles-tarifaires',
};

// Repli sur /login pour un role inconnu : mieux vaut renvoyer vers la
// connexion que proposer un lien mort.
export function routeAccueil(role) {
  return ROUTE_PAR_ROLE[role] ?? '/login';
}
