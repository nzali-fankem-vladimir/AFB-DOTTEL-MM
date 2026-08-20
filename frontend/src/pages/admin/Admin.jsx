import { useNavigate } from 'react-router-dom';
import { Compass } from 'lucide-react';
import { PageHeader } from '../../components/layout/PageHeader';
import { Card, CardContent } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../contexts/AuthContext';
import { routeAccueil } from '../../router/routeParRole';

// Sprint D.3 : cet ecran n'est PAS une page d'accueil d'administration. Il est
// monte sur la route attrape-tout `/admin/*` (AppRouter.jsx), donc il ne
// s'affiche que pour une adresse `/admin/...` qui ne correspond a aucun ecran
// reel -- les vrais ecrans (utilisateurs, fonctions eligibles) ont leurs
// propres routes declarees avant celle-ci.
//
// Il rendait `<div className="p-8">Administration</div>` : un mot nu, hors
// charte, qui laissait croire a une page d'accueil vide plutot qu'a une
// adresse erronee. Il reprend donc le motif de PageIntrouvable, dont il est
// l'equivalent pour la section administration.
export default function Admin() {
  const { user } = useAuth();
  const navigate = useNavigate();

  return (
    <>
      <PageHeader surTitre="Système" titre="Page introuvable" />

      <div className="p-8">
        <Card className="max-w-2xl">
          <CardContent className="flex gap-4 p-6">
            <div
              className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-neutral-100"
              aria-hidden="true"
            >
              <Compass className="h-5 w-5 text-neutral-600" aria-hidden="true" />
            </div>

            <div className="flex flex-col items-start gap-4">
              <div className="flex flex-col gap-1.5">
                <p className="font-semibold text-neutral-900">
                  Cette adresse ne correspond à aucun écran d'administration.
                </p>
                <p className="text-sm text-neutral-600">
                  Vous êtes toujours connecté. Utilisez le menu de gauche pour
                  rejoindre la gestion des utilisateurs ou celle des fonctions
                  éligibles, ou revenez directement à votre page d'accueil.
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
