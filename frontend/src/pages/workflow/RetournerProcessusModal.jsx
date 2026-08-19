import { useId, useState } from 'react';
import { AlertCircle, AlertTriangle } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../../components/ui/Card';
import { Label } from '../../components/ui/Label';
import { Textarea } from '../../components/ui/Textarea';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { useFocusTrap } from '../../hooks/useFocusTrap';

// RG-07 : tout retour exige un motif textuel non vide. La validation est faite
// ici avant l'appel (le backend renverrait 400) pour un retour immediat.
export function RetournerProcessusModal({ idProcessus, onFerme, onSucces }) {
  const [motif, setMotif] = useState('');
  const [erreurMotif, setErreurMotif] = useState(null);
  const [erreur, setErreur] = useState(null);
  const [enCours, setEnCours] = useState(false);
  const titreId = useId();
  const containerRef = useFocusTrap(enCours ? undefined : onFerme);

  const retourner = async (event) => {
    event.preventDefault();
    setErreur(null);

    if (!motif.trim()) {
      setErreurMotif('Le motif de retour est obligatoire.');
      return;
    }
    setErreurMotif(null);

    setEnCours(true);
    try {
      await apiClient.post(`/processus/${idProcessus}/retourner`, { motif: motif.trim() });
      onSucces();
    } catch (err) {
      if (err.response?.status === 400) {
        setErreurMotif('Le motif de retour est obligatoire.');
      } else {
        setErreur('Une erreur est survenue lors du retour du processus. Veuillez réessayer.');
      }
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
        if (event.target === event.currentTarget && !enCours) onFerme?.();
      }}
    >
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle id={titreId}>Retourner le processus à l'ARH</CardTitle>
        </CardHeader>
        <form onSubmit={retourner} noValidate>
          <CardContent className="flex flex-col gap-4">
            {erreur && (
              <Alert variant="destructive">
                <AlertTriangle className="h-4 w-4" />
                <AlertDescription>{erreur}</AlertDescription>
              </Alert>
            )}

            <div className="flex flex-col gap-1.5">
              {/* L'asterisque est desormais porte par le Label lui-meme, pour
                  qu'il soit identique sur les sept formulaires. */}
              <Label htmlFor="retour-motif" obligatoire>
                Motif du retour
              </Label>
              <Textarea
                id="retour-motif"
                value={motif}
                onChange={(e) => setMotif(e.target.value)}
                placeholder="Ex. Le montant appliqué à MBARGA Jeanne ne correspond pas à sa fonction."
                disabled={enCours}
                className={erreurMotif ? 'border-primary-500 focus:ring-primary-500' : undefined}
              />
              {erreurMotif && (
                <p className="flex items-center gap-1 text-xs text-neutral-800">
                  <AlertCircle className="h-3.5 w-3.5 shrink-0 text-primary-500" aria-hidden="true" />
                  {erreurMotif}
                </p>
              )}
            </div>
          </CardContent>
          <CardFooter className="gap-3">
            <Button type="button" variant="outline" onClick={onFerme} disabled={enCours}>
              Annuler
            </Button>
            {/* La modale est le point de confirmation d'un retour : ici le
                rouge fonce se justifie (l'action fait reculer le dossier).
                C'est le bouton d'ENTREE, sur ProcessusDetailPage, qui redevient
                secondaire pour ne plus se confondre avec "Valider". */}
            <Button type="submit" variant="destructive" isLoading={enCours}>
              {enCours ? 'Retour en cours…' : 'Retourner'}
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}