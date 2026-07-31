import { createContext, useContext, useState } from 'react';
import { authProviderLocal } from '../auth/AuthProviderLocal';

const AuthContext = createContext(null);

// AuthContext delegue toute la logique d'authentification a un AuthProvider
// (authProviderLocal aujourd'hui). Le jour d'une vraie integration Keycloak,
// seule cette dependance changera -- le reste de l'application est inchange.
// Decision de securite Sprint 6F.1 : le token est garde en memoire par
// l'AuthProvider (jamais dans localStorage/sessionStorage) pour reduire la
// surface XSS. Consequence acceptee : deconnexion a chaque rechargement de
// page, faute de mecanisme de refresh token cote backend.
export function AuthContextProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);

  const login = async (matricule, motDePasse) => {
    const { token: newToken, user: newUser } = await authProviderLocal.login(matricule, motDePasse);
    setToken(newToken);
    setUser(newUser);
    return newUser;
  };

  const logout = async () => {
    await authProviderLocal.logout();
    setToken(null);
    setUser(null);
    window.location.href = '/login';
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout }}>
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