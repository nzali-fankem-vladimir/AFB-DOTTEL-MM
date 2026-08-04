import { jwtDecode } from 'jwt-decode';
import { setAuthToken } from '../api/apiClient';
import { AuthProvider } from './AuthProvider';

// Sprint MM.7 : Keycloak local et provisoire. URL/realm/client injectes par
// variable d'environnement Vite (jamais de valeur en dur), meme regle que
// VITE_API_BASE_URL. Voir docker-compose.yml (service keycloak) et
// keycloak/realm-dottel-dev.json pour les valeurs locales de developpement.
const KEYCLOAK_URL = import.meta.env.VITE_KEYCLOAK_URL;
const REALM = import.meta.env.VITE_KEYCLOAK_REALM;
const CLIENT_ID = import.meta.env.VITE_KEYCLOAK_CLIENT_ID;

if (!KEYCLOAK_URL || !REALM || !CLIENT_ID) {
  throw new Error(
    'VITE_KEYCLOAK_URL, VITE_KEYCLOAK_REALM et VITE_KEYCLOAK_CLIENT_ID sont obligatoires. ' +
      'Renseignez-les dans frontend/.env.development ou frontend/.env.production.'
  );
}

const AUTHORIZATION_ENDPOINT = `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/auth`;
const TOKEN_ENDPOINT = `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`;
const END_SESSION_ENDPOINT = `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/logout`;
const REDIRECT_URI = `${window.location.origin}/auth/callback`;

const CLE_CODE_VERIFIER = 'dottel_pkce_code_verifier';
const CLE_STATE = 'dottel_pkce_state';

const ROLES_DOTTEL = ['ADMIN', 'DRH', 'CRH', 'ARH', 'EMPLOYE'];

// Decision de securite Sprint 6F.1, preservee avec Keycloak : jeton garde
// uniquement en memoire (jamais localStorage/sessionStorage). Consequence
// acceptee : deconnexion a chaque rechargement de page, faute de refresh
// token persiste. sessionStorage n'est utilise ci-dessous que pour le
// code_verifier/state PKCE, le temps du seul aller-retour vers Keycloak --
// ce ne sont pas des identifiants, ils sont effaces des leur usage.
let utilisateurEnMemoire = null;
// id_token garde en memoire, uniquement pour id_token_hint au logout (RP-Initiated
// Logout, spec OIDC) : sans lui, Keycloak affiche un ecran de confirmation
// "voulez-vous vous deconnecter ?" au lieu de rediriger directement -- constate
// en test reel navigateur (etape 9), absent en curl faute de session active.
let idTokenEnMemoire = null;

function genererValeurAleatoire(nombreOctets) {
  const octets = new Uint8Array(nombreOctets);
  crypto.getRandomValues(octets);
  return Array.from(octets, (o) => o.toString(16).padStart(2, '0')).join('');
}

function base64UrlEncode(buffer) {
  const octets = new Uint8Array(buffer);
  let binaire = '';
  octets.forEach((o) => {
    binaire += String.fromCharCode(o);
  });
  return btoa(binaire).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

async function calculerCodeChallenge(codeVerifier) {
  const donnees = new TextEncoder().encode(codeVerifier);
  const hachage = await crypto.subtle.digest('SHA-256', donnees);
  return base64UrlEncode(hachage);
}

// Claims Keycloak -> objet utilisateur DOTTEL (voir tableau de mapping,
// Sprint MM.7 etape 6). "matricule" vient d'un attribut utilisateur Keycloak
// + protocol mapper dedies au realm local (aucun champ natif Keycloak ne le
// porte) -- a confirmer aupres de la DSI pour le realm reel.
function construireUtilisateur(accessToken) {
  const claims = jwtDecode(accessToken);
  const rolesKeycloak = claims.realm_access?.roles ?? [];
  const role = ROLES_DOTTEL.find((r) => rolesKeycloak.includes(r)) ?? null;

  return {
    matricule: claims.matricule ?? null,
    role,
    nom: claims.family_name ?? '',
    prenom: claims.given_name ?? '',
    email: claims.email ?? null,
  };
}

/**
 * Implementation Keycloak du contrat AuthProvider (Sprint MM.7).
 * Authorization Code + PKCE (decision F-2) : redirection reelle vers
 * Keycloak, l'application ne voit jamais le mot de passe. Couvre les 5
 * roles sans exception (decision de portee P-2).
 */
export class AuthProviderKeycloak extends AuthProvider {
  /** Declenche la redirection vers Keycloak. Ne retourne jamais (navigation complete de page). */
  async login() {
    const codeVerifier = genererValeurAleatoire(32);
    const state = genererValeurAleatoire(16);
    const codeChallenge = await calculerCodeChallenge(codeVerifier);

    sessionStorage.setItem(CLE_CODE_VERIFIER, codeVerifier);
    sessionStorage.setItem(CLE_STATE, state);

    const parametres = new URLSearchParams({
      client_id: CLIENT_ID,
      redirect_uri: REDIRECT_URI,
      response_type: 'code',
      scope: 'openid',
      code_challenge: codeChallenge,
      code_challenge_method: 'S256',
      state,
    });

    window.location.href = `${AUTHORIZATION_ENDPOINT}?${parametres.toString()}`;
  }

  /**
   * Echange le code d'autorisation retourne par Keycloak contre un jeton.
   * Appele uniquement par la page de callback (/auth/callback).
   */
  async gererRetour(code, state) {
    const stateAttendu = sessionStorage.getItem(CLE_STATE);
    const codeVerifier = sessionStorage.getItem(CLE_CODE_VERIFIER);
    sessionStorage.removeItem(CLE_STATE);
    sessionStorage.removeItem(CLE_CODE_VERIFIER);

    if (!state || state !== stateAttendu) {
      throw new Error("Paramètre state invalide, la tentative de connexion est rejetée.");
    }
    if (!codeVerifier) {
      throw new Error('Session de connexion expirée, veuillez réessayer.');
    }

    const corps = new URLSearchParams({
      grant_type: 'authorization_code',
      client_id: CLIENT_ID,
      redirect_uri: REDIRECT_URI,
      code,
      code_verifier: codeVerifier,
    });

    const reponse = await fetch(TOKEN_ENDPOINT, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: corps.toString(),
    });

    if (!reponse.ok) {
      throw new Error("Échec de l'échange du code d'autorisation auprès de Keycloak.");
    }

    const { access_token: accessToken, id_token: idToken } = await reponse.json();
    const user = construireUtilisateur(accessToken);

    utilisateurEnMemoire = user;
    idTokenEnMemoire = idToken ?? null;
    setAuthToken(accessToken);

    return { token: accessToken, user };
  }

  async logout() {
    utilisateurEnMemoire = null;
    setAuthToken(null);
    const idToken = idTokenEnMemoire;
    idTokenEnMemoire = null;

    // Contrat API V3.3 (POST /auth/logout) : comportement etendu par rapport
    // a AuthProviderLocal. Avant : backend stateless sans liste noire, la
    // deconnexion se limitait a effacer le jeton local. Avec Keycloak : la
    // redirection ferme aussi la session SSO cote realm, pas seulement le
    // jeton applicatif -- signale explicitement (guide MM.7, etape 5).
    const parametres = new URLSearchParams({
      client_id: CLIENT_ID,
      post_logout_redirect_uri: `${window.location.origin}/login`,
    });
    if (idToken) {
      parametres.set('id_token_hint', idToken);
    }
    window.location.href = `${END_SESSION_ENDPOINT}?${parametres.toString()}`;
  }

  utilisateurCourant() {
    return utilisateurEnMemoire;
  }
}

export const authProviderKeycloak = new AuthProviderKeycloak();
