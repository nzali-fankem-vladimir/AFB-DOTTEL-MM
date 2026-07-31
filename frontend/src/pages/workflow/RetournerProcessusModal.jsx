import { useState } from 'react';
import { AlertTriangle } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../../components/ui/Card';
import { Label } from '../../components/ui/Label';
import { Textarea } from '../../components/ui/Textarea';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';

// RG-07 : tout retour exige un motif textuel non vide. La validation est faite
// ici avant l'appel (le backend renverrait 400) pour un retour immediat.
export function RetournerProcessusModal({ idProcessus, onFerme, onSucces }) {
  const [motif, setMotif] = useState('');
  const [erreurMotif, setErreurMotif] = useState(null);
  const [erreur, setErreur] = useState(null);
  const [enCours, setEnCours] = useState(false);

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
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Retourner le processus à l'ARH</CardTitle>
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
              <Label htmlFor="retour-motif">
                Motif du retour <span className="text-primary-500">*</span>
              </Label>
              <Textarea
                id="retour-motif"
                value={motif}
                onChange={(e) => setMotif(e.target.value)}
                placeholder="Ex. Le montant appliqué à MBARGA Jeanne ne correspond pas à sa fonction."
                disabled={enCours}
                className={erreurMotif ? 'border-primary-500 focus:ring-primary-500' : undefined}
              />
              {erreurMotif && <p className="text-xs text-primary-500">{erreurMotif}</p>}
            </div>
          </CardContent>
          <CardFooter className="gap-3">
            <Button type="button" variant="outline" onClick={onFerme} disabled={enCours}>
              Annuler
            </Button>
            <Button type="submit" variant="destructive" disabled={enCours}>
              {enCours ? 'Retour en cours…' : 'Retourner'}
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}