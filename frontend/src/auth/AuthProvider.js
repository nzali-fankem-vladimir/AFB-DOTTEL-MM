/**
 * @typedef {{ matricule: string | null, role: string, nom: string, prenom: string, email: string | null }} Utilisateur
 */

/**
 * Contrat d'authentification. AuthProviderKeycloak l'implémente (Sprint MM.7,
 * Authorization Code + PKCE, décision F-2) : login() ne prend plus de
 * matricule/mot de passe puisque l'application ne les voit jamais — elle
 * redirige vers Keycloak, qui redirige à son tour vers gererRetour().
 */
export class AuthProvider {
  /**
   * Déclenche la connexion. Redirige la page entière vers Keycloak ;
   * ne retourne donc jamais de valeur exploitable côté appelant.
   * @returns {Promise<void>}
   */
  async login() {
    throw new Error('AuthProvider.login non implémenté');
  }

  /**
   * Traite le retour de Keycloak sur /auth/callback (échange code -> jeton).
   * @param {string} code
   * @param {string} state
   * @returns {Promise<{ token: string, user: Utilisateur }>}
   */
  async gererRetour(_code, _state) {
    throw new Error('AuthProvider.gererRetour non implémenté');
  }

  /** @returns {Promise<void>} */
  async logout() {
    throw new Error('AuthProvider.logout non implémenté');
  }

  /** @returns {Utilisateur | null} */
  utilisateurCourant() {
    throw new Error('AuthProvider.utilisateurCourant non implémenté');
  }
}
