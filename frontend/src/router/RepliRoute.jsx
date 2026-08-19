import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

// Sprint MM.14 (ecart E4 de l'audit) : arbitre unique de la route "*".
//
// Le repli precedent renvoyait tout le monde vers /login, y compris un
// utilisateur deja authentifie qui avait simplement mal tape une URL -- il
// avait alors l'impression d'avoir ete deconnecte.
//
// Pourquoi une redirection vers /introuvable plutot qu'un <Route path="*">
// imbrique sous AppLayout : deux branches "*" concurrentes (l'une a la
// racine, l'autre sous les routes sans chemin ProtectedRoute + AppLayout)
// obtiennent le meme score de specificite dans React Router, et l'arbitrage
// se jouerait alors sur l'ordre de declaration -- fragile a la premiere
// reorganisation du routeur. Un seul "*", qui decide explicitement, ne peut
// pas devenir ambigu.
export default function RepliRoute() {
  const { user, token } = useAuth();

  if (!token || !user) {
    return <Navigate to="/login" replace />;
  }

  return <Navigate to="/introuvable" replace />;
}
