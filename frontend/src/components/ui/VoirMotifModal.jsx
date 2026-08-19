import { useId } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from './Card';
import { Button } from './Button';
import { useFocusTrap } from '../../hooks/useFocusTrap';

// Modale de lecture seule partagee entre le motif de rejet d'une grille
// tarifaire (RG-10) et le motif de retour d'un processus mensuel (RG-07) :
// meme besoin d'afficher un texte libre fige, avec origine optionnelle
// (CRH/DRH) pour le cas processus.
export function VoirMotifModal({ titre, origine, motif, onFermer }) {
  const titreId = useId();
  const containerRef = useFocusTrap(onFermer);

  return (
    <div
      ref={containerRef}
      role="dialog"
      aria-modal="true"
      aria-labelledby={titreId}
      tabIndex={-1}
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      onClick={(event) => {
        if (event.target === event.currentTarget) onFermer?.();
      }}
    >
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle id={titreId}>{titre}</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-3">
          {origine && (
            <p className="text-sm text-neutral-500">
              Retourné par : <strong className="text-neutral-900">{origine}</strong>
            </p>
          )}
          <p className="whitespace-pre-wrap text-sm text-neutral-700">{motif}</p>
        </CardContent>
        <CardFooter>
          <Button variant="outline" onClick={onFermer}>
            Fermer
          </Button>
        </CardFooter>
      </Card>
    </div>
  );
}
