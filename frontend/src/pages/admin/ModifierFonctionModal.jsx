import { useId, useState } from 'react';
import { AlertTriangle } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../../components/ui/Card';
import { Label } from '../../components/ui/Label';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { useFocusTrap } from '../../hooks/useFocusTrap';

// Decision actee avec le metier (Sprint 6F.7bis, suite) : libelle est
// toujours modifiable (donnee d'affichage pure). code ne l'est que si au plus
// 1 beneficiaire actif est rattache -- le backend bloque avec 409 sinon, mais
// on desactive deja le champ cote UI pour eviter l'aller-retour inutile.
export function ModifierFonctionModal({ fonction, onFerme, onSucces }) {
  const codeModifiable = fonction.nombreBeneficiairesActifs <= 1;
  const [nouveauCode, setNouveauCode] = useState(fonction.code);
  const [libelle, setLibelle] = useState(fonction.libelle);
  const [erreur, setErreur] = useState(null);
  const [enCours, setEnCours] = useState(false);
  const titreId = useId();
  const containerRef = useFocusTrap(enCours ? undefined : onFerme);

  const modifier = async (event) => {
    event.preventDefault();
    setErreur(null);
    setEnCours(true);
    try {
      const corps = { libelle };
      if (codeModifiable && nouveauCode.trim().toUpperCase() !== fonction.code) {
        corps.nouveauCode = nouveauCode.trim().toUpperCase();
      }
      await apiClient.patch(`/fonctions-eligibles/${fonction.code}`, corps);
      onSucces();
    } catch (err) {
      if (err.response?.status === 409) {
        setErreur(
          err.response?.data?.erreur ?? 'Ce code est déjà utilisé, ou des bénéficiaires actifs y sont rattachés.'
        );
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
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      onClick={(event) => {
        if (event.target === event.currentTarget && !enCours) onFerme?.();
      }}
    >
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle id={titreId}>Modifier — {fonction.libelle}</CardTitle>
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
              <Label htmlFor="modifier-code" obligatoire>
                Code
              </Label>
              <Input
                id="modifier-code"
                value={nouveauCode}
                onChange={(e) => setNouveauCode(e.target.value)}
                disabled={enCours || !codeModifiable}
                required
              />
              {!codeModifiable && (
                <p className="text-xs text-neutral-500">
                  Code verrouillé : {fonction.nombreBeneficiairesActifs} bénéficiaire(s) actif(s) rattaché(s).
                </p>
              )}
            </div>

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="modifier-libelle" obligatoire>
                Libellé
              </Label>
              <Input
                id="modifier-libelle"
                value={libelle}
                onChange={(e) => setLibelle(e.target.value)}
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
