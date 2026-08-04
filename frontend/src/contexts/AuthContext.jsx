import { createContext, useContext, useState } from 'react';
import { authProviderKeycloak } from '../auth/AuthProviderKeycloak';

const AuthContext = createContext(null);

// AuthContext delegue toute la logique d'authentification a un AuthProvider
// (authProviderKeycloak depuis le Sprint MM.7). Decision de securite Sprint
// 6F.1, preservee : le token est garde en memoire par l'AuthProvider (jamais
// dans localStorage/sessionStorage) pour reduire la surface XSS. Consequence
// acceptee : deconnexion a chaque rechargement de page, faute de refresh
// token cote backend.
export function AuthContextProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);

  // Declenche la redirection vers Keycloak. Ne retourne rien d'exploitable :
  // la navigation quitte la page avant que la promesse ne se resolve.
  const login = () => authProviderKeycloak.login();

  // Appele par la page de callback (/auth/callback) une fois le code
  // d'autorisation recu de Keycloak.
  const gererRetourKeycloak = async (code, state) => {
    const { token: newToken, user: newUser } = await authProviderKeycloak.gererRetour(code, state);
    setToken(newToken);
    setUser(newUser);
    return newUser;
  };

  const logout = async () => {
    setToken(null);
    setUser(null);
    await authProviderKeycloak.logout();
  };

  return (
    <AuthContext.Provider value={{ user, token, login, gererRetourKeycloak, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth doit être utilisé à l\'intérieur d\'un AuthContextProvider');
  }
  return context;
}
