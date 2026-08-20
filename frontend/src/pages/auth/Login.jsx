import { useState } from 'react';
import { AlertTriangle } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { Logo } from '../../components/ui/Logo';

// Sprint MM.7 (decision F-2, Authorization Code + PKCE) : plus de formulaire
// matricule/mot de passe -- l'application ne voit jamais le mot de passe.
// Un clic redirige vers la page de connexion Keycloak, comme sur BAOBAB.
export default function Login() {
  const { login } = useAuth();
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState(null);

  const onConnexion = async () => {
    setErreur(null);
    setEnCours(true);
    try {
      await login();
    } catch {
      setErreur('Impossible de contacter le service de connexion. Veuillez réessayer.');
      setEnCours(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-neutral-100 px-4">
      <Card className="w-full max-w-sm">
        <CardHeader className="items-center text-center">
          <Logo size="lg" className="mb-2" />
          {/* Sprint D.4 -- le detecteur signale une hierarchie typographique
              plate sur cette page (14/16/18 px, ratio 1,3). C'est la porte
              d'entree du module et son seul titre : text-xl le detache du
              libelle de bouton, sans sortir de l'echelle. */}
          <CardTitle className="text-xl text-balance">Dotations Téléphoniques Mensuelles</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col gap-4">
            {/* L'icone manquait : c'est la premiere impression du module, et
                le seul message d'erreur que l'utilisateur peut y rencontrer. */}
            {erreur && (
              <Alert variant="destructive">
                <AlertTriangle className="h-4 w-4" aria-hidden="true" />
                <AlertDescription>{erreur}</AlertDescription>
              </Alert>
            )}
            <Button type="button" onClick={onConnexion} isLoading={enCours} className="mt-2">
              {enCours ? 'Redirection…' : 'Se connecter'}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
