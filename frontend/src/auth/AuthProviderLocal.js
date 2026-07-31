import apiClient, { setAuthToken } from '../api/apiClient';
import { AuthProvider } from './AuthProvider';

// Decision de securite Sprint 6F.1 : token JWT garde uniquement en memoire
// (jamais dans localStorage/sessionStorage) pour reduire la surface XSS.
// Consequence acceptee : l'utilisateur est deconnecte a chaque rechargement
// de page, faute de mecanisme de refresh token cote backend.
let utilisateurEnMemoire = null;

/**
 * Implémentation actuelle du contrat AuthProvider : simulation Keycloak
 * locale, authentifie contre POST /auth/login exactement comme le ferait
 * un vrai flux Keycloak côté client. Remplaçable par un AuthProviderKeycloak
 * futur sans changer AuthContext.
 */
export class AuthProviderLocal extends AuthProvider {
  async login(matricule, motDePasse) {
    const { data } = await apiClient.post('/auth/login', { matricule, motDePasse });
    const { token, ...user } = data;
    utilisateurEnMemoire = user;
    setAuthToken(token);
    return { token, user };
  }

  async logout() {
    try {
      await apiClient.post('/auth/logout');
    } catch {
      // best-effort : le jeton est stateless côté backend (CLAUDE.md section 17 point 5),
      // l'echec de cet appel ne doit pas empecher la deconnexion locale
    }
    utilisateurEnMemoire = null;
    setAuthToken(null);
  }

  utilisateurCourant() {
    return utilisateurEnMemoire;
  }
}

export const authProviderLocal = new AuthProviderLocal();