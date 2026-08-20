import { useId, useState } from 'react';
import { AlertTriangle } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../../components/ui/Card';
import { Label } from '../../components/ui/Label';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { useFocusTrap } from '../../hooks/useFocusTrap';

// PATCH /grilles-tarifaires/{id} n'accepte que le montant, et seulement
// tant que la grille est EN_ATTENTE_CRH (RG-10 ; Sprint MM.12 : le montant est
// gele des que le CRH a statue).
export function ModifierGrilleModal({ grille, onFerme, onSucces }) {
  const [montantFcfa, setMontantFcfa] = useState(String(grille.montantFcfa));
  const [erreur, setErreur] = useState(null);
  const [enCours, setEnCours] = useState(false);
  // Sprint D.3 : les corrections d'accessibilite de D.2 (Echap, piege de focus)
  // n'avaient ete appliquees qu'aux modales generiques de components/ui. Les
  // modales ecrites a la main dans pages/ etaient restees en dehors.
  const titreId = useId();
  const containerRef = useFocusTrap(enCours ? undefined : onFerme);

  const modifier = async (event) => {
    event.preventDefault();
    setErreur(null);
    setEnCours(true);
    try {
      await apiClient.patch(`/grilles-tarifaires/${grille.id}`, { montantFcfa: Number(montantFcfa) });
      onSucces();
    } catch (err) {
      if (err.response?.status === 400) {
        setErreur("Le CRH a déjà statué sur cette grille : son montant est gelé et ne peut plus être modifié.");
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
    <div
      ref={containerRef}
      role="dialog"
      aria-modal="true"
      aria-labelledby={titreId}
      tabIndex={-1}
      className="fixed inset-0 z-50 flex items-center justify-center overscroll-contain bg-black/40 p-4"
      onClick={(event) => {
        if (event.target === event.currentTarget && !enCours) onFerme?.();
      }}
    >
      <Card className="max-h-[calc(100vh-2rem)] w-full max-w-md overflow-y-auto">
        <CardHeader>
          <CardTitle id={titreId}>Modifier le montant — {grille.libelleFonction}</CardTitle>
        </CardHeader>
        {/* Pas de noValidate : validation cote client portee par les
            attributs `required` natifs (audit Sprint 6F.9). */}
        <form onSubmit={modifier}>
          <CardContent className="flex flex-col gap-4">
            {erreur && (
              <Alert variant="destructive">
                <AlertTriangle className="h-4 w-4" aria-hidden="true" />
                <AlertDescription>{erreur}</AlertDescription>
              </Alert>
            )}

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="modifier-montant" obligatoire>
                Montant (FCFA)
              </Label>
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
            <Button type="submit" isLoading={enCours}>
              {enCours ? 'Enregistrement…' : 'Enregistrer'}
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}