import { useState } from 'react';
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
          <CardTitle>Dotations Téléphoniques Mensuelles</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col gap-4">
            {erreur && (
              <Alert variant="destructive">
                <AlertDescription>{erreur}</AlertDescription>
              </Alert>
            )}
            <Button type="button" onClick={onConnexion} disabled={enCours} className="mt-2">
              {enCours ? 'Redirection…' : 'Se connecter'}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
