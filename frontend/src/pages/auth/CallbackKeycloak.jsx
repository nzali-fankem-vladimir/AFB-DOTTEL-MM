import { useEffect, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/Card';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { Logo } from '../../components/ui/Logo';

// Route de destination apres connexion selon le role de l'utilisateur.
// Deplacee ici depuis Login.jsx (Sprint MM.7) : c'est desormais le retour de
// Keycloak, pas la soumission d'un formulaire, qui declenche la redirection.
// Chaque valeur doit rester accessible au role concerne dans AppRouter,
// sinon la connexion aboutit sur /acces-interdit (constat de l'audit
// Sprint 6F.9 : le CRH pointait encore vers /dashboard, devenu ARH/DRH
// seulement le 2026-07-30).
const ROUTE_PAR_ROLE = {
  EMPLOYE: '/enrolement',
  ARH: '/dashboard',
  CRH: '/processus',
  DRH: '/dashboard',
  ADMIN: '/grilles-tarifaires',
};

// Page de retour du flux Authorization Code + PKCE (decision F-2, Sprint
// MM.7). Keycloak redirige ici avec ?code=...&state=... (ou ?error=... si
// l'utilisateur annule ou si le realm refuse). Ce composant echange le code
// contre un jeton puis redirige vers la page appropriee au role.
export default function CallbackKeycloak() {
  const { gererRetourKeycloak } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [erreur, setErreur] = useState(null);
  const dejaTraite = useRef(false);

  useEffect(() => {
    // React 19 en StrictMode invoque les effets deux fois en developpement ;
    // le code d'autorisation Keycloak n'est utilisable qu'une seule fois.
    if (dejaTraite.current) {
      return;
    }
    dejaTraite.current = true;

    const erreurKeycloak = searchParams.get('error');
    if (erreurKeycloak) {
      setErreur("La connexion a été refusée ou annulée.");
      return;
    }

    const code = searchParams.get('code');
    const state = searchParams.get('state');
    if (!code || !state) {
      setErreur('Réponse de connexion incomplète.');
      return;
    }

    gererRetourKeycloak(code, state)
      .then((user) => {
        navigate(ROUTE_PAR_ROLE[user.role] || '/acces-interdit', { replace: true });
      })
      .catch(() => {
        setErreur('La connexion a échoué. Veuillez réessayer.');
      });
  }, [searchParams, gererRetourKeycloak, navigate]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-neutral-100 px-4">
      <Card className="w-full max-w-sm">
        <CardHeader className="items-center text-center">
          <Logo size="lg" className="mb-2" />
          <CardTitle>Dotations Téléphoniques Mensuelles</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col gap-4">
            {erreur ? (
              <>
                <Alert variant="destructive">
                  <AlertDescription>{erreur}</AlertDescription>
                </Alert>
                <Button type="button" onClick={() => navigate('/login', { replace: true })}>
                  Retour à la connexion
                </Button>
              </>
            ) : (
              <p className="text-center text-neutral-600">Connexion en cours…</p>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
