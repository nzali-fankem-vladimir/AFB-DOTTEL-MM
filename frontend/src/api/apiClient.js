import axios from 'axios';

// VITE_API_BASE_URL vient de .env.development (npm run dev) ou de
// .env.production (npm run build), surchargeable par le pipeline CI/CD.
// Le repli localhost est volontairement limite au mode developpement :
// en production, un fichier .env absent ou mal nomme livrerait sinon un
// bundle pointant en clair sur http://localhost (constat de l'audit
// Sprint 6F.9). Mieux vaut echouer au build que servir du HTTP.
const baseURL = import.meta.env.VITE_API_BASE_URL ?? (import.meta.env.DEV ? 'http://localhost:8080/api' : null);

if (!baseURL) {
  throw new Error(
    "VITE_API_BASE_URL est obligatoire pour un build de production. " +
      'Renseignez-la dans frontend/.env.production ou dans le pipeline de build.'
  );
}

const apiClient = axios.create({
  baseURL,
});

// Token garde en memoire ici (jamais localStorage/sessionStorage), pousse par
// AuthProviderKeycloak a chaque login/logout. Voir decision de securite Sprint 6F.1.
let currentToken = null;

export function setAuthToken(token) {
  currentToken = token;
}

apiClient.interceptors.request.use(
  (config) => {
    if (currentToken) {
      config.headers.Authorization = `Bearer ${currentToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Sprint MM.7 : POST /auth/login a disparu (Keycloak emet le jeton par
    // redirection, hors de apiClient) -- l'exception qui existait pour cet
    // appel n'a plus lieu d'etre, un 401 sur n'importe quel endpoint signifie
    // desormais toujours un jeton absent, expire ou invalide.
    if (error.response && error.response.status === 401) {
      setAuthToken(null);
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;