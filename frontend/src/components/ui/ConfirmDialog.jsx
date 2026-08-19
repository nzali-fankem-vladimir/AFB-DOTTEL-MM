import { useId, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from './Card';
import { Button } from './Button';
import { useFocusTrap } from '../../hooks/useFocusTrap';

// `contenu` (Sprint MM.12) : alternative facultative a `message` pour les
// confirmations qui ne tiennent pas dans une phrase -- typiquement le
// recapitulatif de resynchronisation, qui doit presenter deux blocs distincts.
// `message` reste rendu dans un <p>, ou aucun bloc ne peut etre imbrique sans
// produire du HTML invalide. Tous les appelants existants sont inchanges.
export function ConfirmDialog({
  titre,
  message,
  contenu,
  libelleConfirmer = 'Confirmer',
  largeur = 'max-w-sm',
  onConfirmer,
  onAnnuler,
}) {
  const [enCours, setEnCours] = useState(false);
  const titreId = useId();
  const containerRef = useFocusTrap(enCours ? undefined : onAnnuler);

  const confirmer = async () => {
    setEnCours(true);
    try {
      await onConfirmer();
    } finally {
      setEnCours(false);
    }
  };

  return (
    <div
      ref={containerRef}
      role="dialog"
      aria-modal="true"
      aria-labelledby={titreId}
      tabIndex={-1}
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      onClick={(event) => {
        if (event.target === event.currentTarget && !enCours) onAnnuler?.();
      }}
    >
      <Card className={`w-full ${largeur}`}>
        <CardHeader>
          <CardTitle id={titreId}>{titre}</CardTitle>
        </CardHeader>
        <CardContent>
          {contenu ?? <p className="text-sm text-neutral-700">{message}</p>}
        </CardContent>
        <CardFooter className="gap-3">
          {/* Sans onConfirmer, la boite est purement informative : elle expose
              une situation que l'utilisateur doit resoudre ailleurs, et ne doit
              donc offrir AUCUN bouton d'action (Sprint MM.12, cas d'une grille
              en cours de validation). */}
          <Button variant="outline" onClick={onAnnuler} disabled={enCours}>
            {onConfirmer ? 'Annuler' : 'Fermer'}
          </Button>
          {onConfirmer && (
            <Button variant="destructive" onClick={confirmer} disabled={enCours}>
              {enCours ? 'Veuillez patienter…' : libelleConfirmer}
            </Button>
          )}
        </CardFooter>
      </Card>
    </div>
  );
}