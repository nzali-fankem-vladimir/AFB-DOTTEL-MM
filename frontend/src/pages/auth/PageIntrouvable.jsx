import { useNavigate } from 'react-router-dom';
import { Compass } from 'lucide-react';
import { PageHeader } from '../../components/layout/PageHeader';
import { Card, CardContent } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../contexts/AuthContext';
import { routeAccueil } from '../../router/routeParRole';

// Sprint MM.14 (ecart E4 de l'audit) : la route de repli "*" renvoyait vers
// /login MEME pour un utilisateur deja authentifie. Aucune consequence de
// securite -- le jeton restait en memoire et les routes protegees restaient
// protegees -- mais une simple faute de frappe dans l'URL donnait a l'agent
// connecte l'impression d'avoir ete deconnecte.
//
// Desormais : l'utilisateur authentifie voit cette page, dans la charte et
// dans AppLayout (barre laterale conservee, donc navigation toujours
// possible) ; seul l'utilisateur non authentifie continue d'etre renvoye
// vers /login, ce qui est le comportement correct dans son cas.
export default function PageIntrouvable() {
  const { user } = useAuth();
  const navigate = useNavigate();

  return (
    <>
      <PageHeader surTitre="Navigation" titre="Page introuvable" />

      <div className="p-8">
        <Card className="max-w-2xl">
          <CardContent className="flex gap-4 p-6">
            <div
              className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-neutral-100"
              aria-hidden="true"
            >
              <Compass className="h-5 w-5 text-neutral-500" />
            </div>

            <div className="flex flex-col items-start gap-4">
              <div className="flex flex-col gap-1.5">
                <p className="font-semibold text-neutral-900">
                  Cette adresse ne correspond à aucun écran du module.
                </p>
                <p className="text-sm text-neutral-600">
                  Vous êtes toujours connecté. Utilisez le menu de gauche pour
                  reprendre votre navigation, ou revenez directement à votre
                  page d'accueil.
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
