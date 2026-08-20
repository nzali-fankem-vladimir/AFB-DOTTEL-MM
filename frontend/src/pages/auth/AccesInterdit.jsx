import { useNavigate } from 'react-router-dom';
import { ShieldAlert } from 'lucide-react';
import { PageHeader } from '../../components/layout/PageHeader';
import { Card, CardContent } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../contexts/AuthContext';
import { routeAccueil } from '../../router/routeParRole';

// Sprint MM.14 (ecart E5 de l'audit) : cette page etait un <div> nu, hors
// charte visuelle (CLAUDE.md section 15) et sans aucun moyen de repartir --
// l'utilisateur y arrivait par ProtectedRoute et s'y trouvait coince, sans
// meme savoir quel role lui manquait. Elle est rendue dans AppLayout, donc la
// barre laterale reste presente : le message explique, la navigation permet
// de repartir, et le bouton offre le chemin direct vers l'accueil du role.
export default function AccesInterdit() {
  const { user } = useAuth();
  const navigate = useNavigate();

  return (
    <>
      <PageHeader surTitre="Autorisation" titre="Accès interdit" />

      <div className="p-8">
        <Card className="max-w-2xl">
          <CardContent className="flex gap-4 p-6">
            <div
              className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary-50"
              aria-hidden="true"
            >
              <ShieldAlert className="h-5 w-5 text-primary-500" aria-hidden="true" />
            </div>

            <div className="flex flex-col items-start gap-4">
              <div className="flex flex-col gap-1.5">
                <p className="font-semibold text-neutral-900">
                  Cette page n'est pas accessible avec votre profil.
                </p>
                <p className="text-sm text-neutral-600">
                  {user?.role
                    ? `Votre rôle (${user.role}) ne donne pas accès à cet écran. Si vous pensez qu'il s'agit d'une erreur, contactez l'administrateur du module.`
                    : "Votre profil ne donne pas accès à cet écran. Si vous pensez qu'il s'agit d'une erreur, contactez l'administrateur du module."}
                </p>
              </div>

              <Button
                type="button"
                onClick={() => navigate(routeAccueil(user?.role), { replace: true })}
              >
                Retour à l'accueil
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
