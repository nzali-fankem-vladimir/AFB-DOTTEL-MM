/**
 * @typedef {{ matricule: string, role: string, nom: string, prenom: string }} Utilisateur
 */

/**
 * Contrat d'authentification. AuthProviderLocal l'implémente aujourd'hui
 * (simulation Keycloak, POST /auth/login) ; un futur AuthProviderKeycloak
 * pourra le remplacer sans changer AuthContext ni le reste de l'application.
 */
export class AuthProvider {
  /**
   * @param {string} matricule
   * @param {string} motDePasse
   * @returns {Promise<{ token: string, user: Utilisateur }>}
   */
  async login(matricule, motDePasse) {
    throw new Error('AuthProvider.login non implémenté');
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