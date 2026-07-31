import { useState } from 'react';
import { AlertTriangle } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../../components/ui/Card';
import { Label } from '../../components/ui/Label';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';

// PATCH /grilles-tarifaires/{id} n'accepte que le montant, et seulement
// tant que la grille est EN_ATTENTE_DRH (RG-10).
export function ModifierGrilleModal({ grille, onFerme, onSucces }) {
  const [montantFcfa, setMontantFcfa] = useState(String(grille.montantFcfa));
  const [erreur, setErreur] = useState(null);
  const [enCours, setEnCours] = useState(false);

  const modifier = async (event) => {
    event.preventDefault();
    setErreur(null);
    setEnCours(true);
    try {
      await apiClient.patch(`/grilles-tarifaires/${grille.id}`, { montantFcfa: Number(montantFcfa) });
      onSucces();
    } catch (err) {
      if (err.response?.status === 400) {
        setErreur("Cette grille n'est plus en attente de validation DRH, elle ne peut plus être modifiée.");
      } else if (err.response?.status === 404) {
        setErreur('Grille introuvable.');
      } else {
        setErreur('Une erreur est survenue lors de la modification. Veuillez réessayer.');
      }
    } finally {
      setEnCours(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Modifier le montant — {grille.libelleFonction}</CardTitle>
        </CardHeader>
        {/* Pas de noValidate : validation cote client portee par les
            attributs `required` natifs (audit Sprint 6F.9). */}
        <form onSubmit={modifier}>
          <CardContent className="flex flex-col gap-4">
            {erreur && (
              <Alert variant="destructive">
                <AlertTriangle className="h-4 w-4" />
                <AlertDescription>{erreur}</AlertDescription>
              </Alert>
            )}

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="modifier-montant">Montant (FCFA)</Label>
              <Input
                id="modifier-montant"
                type="number"
                min="1"
                value={montantFcfa}
                onChange={(e) => setMontantFcfa(e.target.value)}
                disabled={enCours}
                required
              />
            </div>
          </CardContent>
          <CardFooter className="gap-3">
            <Button type="button" variant="outline" onClick={onFerme} disabled={enCours}>
              Annuler
            </Button>
            <Button type="submit" disabled={enCours}>
              {enCours ? 'Enregistrement…' : 'Enregistrer'}
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}