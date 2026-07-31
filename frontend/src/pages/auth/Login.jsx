import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuth } from '../../contexts/AuthContext';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/Card';
import { FormField } from '../../components/ui/FormField';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { Logo } from '../../components/ui/Logo';

const schema = z.object({
  matricule: z.string().min(1, 'Le matricule est requis'),
  motDePasse: z.string().min(1, 'Le mot de passe est requis'),
});

// Route de destination apres connexion selon le role de l'utilisateur.
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

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [erreur, setErreur] = useState(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) });

  const onSubmit = async ({ matricule, motDePasse }) => {
    setErreur(null);
    try {
      const user = await login(matricule, motDePasse);
      navigate(ROUTE_PAR_ROLE[user.role] || '/dashboard', { replace: true });
    } catch (err) {
      const statut = err.response?.status;
      if (statut === 401) {
        setErreur('Matricule ou mot de passe incorrect.');
      } else if (statut === 403) {
        setErreur('Ce compte est désactivé. Contactez votre administrateur.');
      } else {
        setErreur('Une erreur est survenue. Veuillez réessayer.');
      }
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
          <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
            {erreur && (
              <Alert variant="destructive">
                <AlertDescription>{erreur}</AlertDescription>
              </Alert>
            )}
            <FormField
              id="matricule"
              label="Matricule"
              autoComplete="username"
              error={errors.matricule}
              {...register('matricule')}
            />
            <FormField
              id="motDePasse"
              label="Mot de passe"
              type="password"
              autoComplete="current-password"
              error={errors.motDePasse}
              {...register('motDePasse')}
            />
            <Button type="submit" disabled={isSubmitting} className="mt-2">
              {isSubmitting ? 'Connexion…' : 'Se connecter'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}