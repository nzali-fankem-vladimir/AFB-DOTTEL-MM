import { useState } from 'react';
import { AlertTriangle } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../../components/ui/Card';
import { Label } from '../../components/ui/Label';
import { Textarea } from '../../components/ui/Textarea';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { formatMontantFCFA, formatDate } from '../../utils/formatters';

// RG-07 : le motif n'est obligatoire que pour un rejet, pas pour une
// validation. Validation front avant l'appel (le backend renverrait 400).
export function ValiderGrilleModal({ grille, onFerme, onSucces }) {
  const [motifRejet, setMotifRejet] = useState('');
  const [erreurMotif, setErreurMotif] = useState(null);
  const [erreur, setErreur] = useState(null);
  const [decisionEnCours, setDecisionEnCours] = useState(null);

  const decider = async (decision) => {
    setErreur(null);

    if (decision === 'REJETER' && !motifRejet.trim()) {
      setErreurMotif('Le motif de rejet est obligatoire.');
      return;
    }
    setErreurMotif(null);

    setDecisionEnCours(decision);
    try {
      await apiClient.post(`/grilles-tarifaires/${grille.id}/valider`, {
        decision,
        motifRejet: decision === 'REJETER' ? motifRejet.trim() : undefined,
      });
      onSucces();
    } catch (err) {
      if (err.response?.status === 400) {
        setErreurMotif('Le motif de rejet est obligatoire.');
      } else if (err.response?.status === 409) {
        setErreur("Cette grille n'est plus en attente de validation DRH.");
      } else {
        setErreur('Une erreur est survenue lors de la décision. Veuillez réessayer.');
      }
    } finally {
      setDecisionEnCours(null);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Décision — {grille.libelleFonction}</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          {erreur && (
            <Alert variant="destructive">
              <AlertTriangle className="h-4 w-4" />
              <AlertDescription>{erreur}</AlertDescription>
            </Alert>
          )}

          <p className="text-sm text-neutral-700">
            Montant proposé : <strong>{formatMontantFCFA(grille.montantFcfa)}</strong>
            <br />
            Date de début : {formatDate(grille.dateDebut)}
          </p>

          <div className="flex flex-col gap-1.5">
            <Label htmlFor="valider-motif-rejet">
              Motif du rejet <span className="text-neutral-400">(obligatoire en cas de rejet)</span>
            </Label>
            <Textarea
              id="valider-motif-rejet"
              value={motifRejet}
              onChange={(e) => setMotifRejet(e.target.value)}
              placeholder="Ex. Montant supérieur au plafond prévu pour cette fonction."
              disabled={decisionEnCours !== null}
              className={erreurMotif ? 'border-primary-500 focus:ring-primary-500' : undefined}
            />
            {erreurMotif && <p className="text-xs text-primary-500">{erreurMotif}</p>}
          </div>
        </CardContent>
        <CardFooter className="gap-3">
          <Button type="button" variant="outline" onClick={onFerme} disabled={decisionEnCours !== null}>
            Annuler
          </Button>
          <Button
            type="button"
            variant="destructive"
            onClick={() => decider('REJETER')}
            disabled={decisionEnCours !== null}
          >
            {decisionEnCours === 'REJETER' ? 'Rejet en cours…' : 'Rejeter'}
          </Button>
          <Button type="button" onClick={() => decider('VALIDER')} disabled={decisionEnCours !== null}>
            {decisionEnCours === 'VALIDER' ? 'Validation en cours…' : 'Valider'}
          </Button>
        </CardFooter>
      </Card>
    </div>
  );
}