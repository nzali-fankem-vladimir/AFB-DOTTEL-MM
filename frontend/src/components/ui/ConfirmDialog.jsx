import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from './Card';
import { Button } from './Button';

export function ConfirmDialog({ titre, message, libelleConfirmer = 'Confirmer', onConfirmer, onAnnuler }) {
  const [enCours, setEnCours] = useState(false);

  const confirmer = async () => {
    setEnCours(true);
    try {
      await onConfirmer();
    } finally {
      setEnCours(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle>{titre}</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-neutral-700">{message}</p>
        </CardContent>
        <CardFooter className="gap-3">
          <Button variant="outline" onClick={onAnnuler} disabled={enCours}>
            Annuler
          </Button>
          <Button variant="destructive" onClick={confirmer} disabled={enCours}>
            {enCours ? 'Veuillez patienter…' : libelleConfirmer}
          </Button>
        </CardFooter>
      </Card>
    </div>
  );
}