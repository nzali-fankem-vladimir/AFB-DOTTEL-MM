import { useState } from 'react';
import { AlertTriangle } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../../components/ui/Card';
import { Label } from '../../components/ui/Label';
import { Textarea } from '../../components/ui/Textarea';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { ConfirmDialog } from '../../components/ui/ConfirmDialog';
import { formatMontantFCFA, formatDate } from '../../utils/formatters';

// RG-07 : le motif n'est obligatoire que pour un rejet, pas pour une
// validation. Validation front avant l'appel (le backend renverrait 400).
export function ValiderGrilleModal({ grille, onFerme, onSucces }) {
  const [motifRejet, setMotifRejet] = useState('');
  const [erreurMotif, setErreurMotif] = useState(null);
  const [erreur, setErreur] = useState(null);
  const [decisionEnCours, setDecisionEnCours] = useState(null);
  const [decisionAConfirmer, setDecisionAConfirmer] = useState(null);

  // Sprint MM.12 : la decision n'est plus appliquee au clic. Une validation
  // fait avancer la grille d'un etage (et, cote DRH, met un montant EN VIGUEUR
  // pour toutes les dotations a venir) ; un rejet est TERMINAL -- il faut
  // recreer une grille et refaire tout le circuit. Ni l'un ni l'autre ne
  // supporte un clic trop rapide.
  const demanderDecision = (decision) => {
    setErreur(null);

    if (decision === 'REJETER' && !motifRejet.trim()) {
      setErreurMotif('Le motif de rejet est obligatoire.');
      return;
    }
    setErreurMotif(null);
    setDecisionAConfirmer(decision);
  };

  const decider = async (decision) => {
    setErreur(null);
    setDecisionAConfirmer(null);
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
      } else if (err.response?.status === 403) {
        // Sprint MM.12 : RG-08 (vous avez deja statue sur cette grille a
        // l'etape precedente) ou RG-05 (mauvais etage pour votre role). Le
        // backend renvoie un motif precis, on l'affiche plutot que de le
        // reformuler a l'aveugle.
        setErreur(
          err.response?.data?.erreur ??
            "Vous n'êtes pas autorisé à statuer sur cette grille."
        );
      } else if (err.response?.status === 409) {
        setErreur("Cette grille n'est plus en attente de décision.");
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
            onClick={() => demanderDecision('REJETER')}
            disabled={decisionEnCours !== null}
          >
            {decisionEnCours === 'REJETER' ? 'Rejet en cours…' : 'Rejeter'}
          </Button>
          <Button type="button" onClick={() => demanderDecision('VALIDER')} disabled={decisionEnCours !== null}>
            {decisionEnCours === 'VALIDER' ? 'Validation en cours…' : 'Valider'}
          </Button>
        </CardFooter>
      </Card>

      {decisionAConfirmer && (
        <ConfirmDialog
          titre={decisionAConfirmer === 'VALIDER' ? 'Confirmer la validation' : 'Confirmer le rejet'}
          message={
            decisionAConfirmer === 'VALIDER'
              ? `Valider la grille ${grille.libelleFonction} à ${formatMontantFCFA(grille.montantFcfa)}, `
                + `applicable au ${formatDate(grille.dateDebut)} ? Elle passera à l'étape suivante du circuit.`
              : `Rejeter la grille ${grille.libelleFonction} à ${formatMontantFCFA(grille.montantFcfa)} ? `
                + `Un rejet est définitif : l'ARH devra créer une nouvelle grille et refaire tout le circuit.`
          }
          libelleConfirmer={decisionAConfirmer === 'VALIDER' ? 'Valider' : 'Rejeter'}
          onAnnuler={() => setDecisionAConfirmer(null)}
          onConfirmer={() => decider(decisionAConfirmer)}
        />
      )}
    </div>
  );
}