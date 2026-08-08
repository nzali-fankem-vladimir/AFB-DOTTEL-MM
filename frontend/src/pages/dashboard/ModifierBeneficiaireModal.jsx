import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useState } from 'react';
import apiClient from '../../api/apiClient';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../../components/ui/Card';
import { Label } from '../../components/ui/Label';
import { Select } from '../../components/ui/Select';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';

const schema = z.object({
  fonction: z.string().optional(),
  uniteRattachement: z.string().optional(),
});

// Champs tous optionnels (RG cf. Sprint 6F.5) : une chaine vide equivaut a
// "ne pas modifier" cote backend (BeneficiaireService.modifier ignore les
// champs null), donc on ne transmet jamais de chaine vide.
function versValeurOuVide(valeur) {
  const nettoyee = valeur?.trim();
  return nettoyee ? nettoyee : undefined;
}

export function ModifierBeneficiaireModal({
  beneficiaire,
  fonctionsEligibles,
  unitesRattachement,
  onFerme,
  onSucces,
}) {
  const [erreur, setErreur] = useState(null);

  const {
    register,
    handleSubmit,
    formState: { isSubmitting },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      fonction: beneficiaire.fonction ?? '',
      uniteRattachement: beneficiaire.uniteRattachement ?? '',
    },
  });

  const onSubmit = async (valeurs) => {
    setErreur(null);
    try {
      const { data } = await apiClient.patch(`/beneficiaires/${beneficiaire.id}`, {
        fonction: versValeurOuVide(valeurs.fonction),
        uniteRattachement: versValeurOuVide(valeurs.uniteRattachement),
      });
      onSucces(data);
    } catch (err) {
      const statut = err.response?.status;
      if (statut === 403) {
        setErreur(
          err.response?.data?.erreur ??
            "Cette fonction ou ce grade n'est plus éligible à la dotation téléphonique."
        );
      } else {
        setErreur('Une erreur est survenue lors de la modification. Veuillez réessayer.');
      }
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Modifier {beneficiaire.nomPrenoms}</CardTitle>
        </CardHeader>
        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <CardContent className="flex flex-col gap-4">
            {erreur && (
              <Alert variant="destructive">
                <AlertDescription>{erreur}</AlertDescription>
              </Alert>
            )}

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="modif-fonction">Fonction</Label>
              <Select id="modif-fonction" {...register('fonction')}>
                {fonctionsEligibles.map((f) => (
                  <option key={f.code} value={f.code}>
                    {f.libelle}
                  </option>
                ))}
              </Select>
            </div>

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="modif-unite">Unité de rattachement</Label>
              <Select id="modif-unite" {...register('uniteRattachement')}>
                {/* Le code_unite n'est jamais saisi a la main : il est resolu
                    automatiquement cote backend a partir du libelle choisi ici
                    (BeneficiaireService.modifier, source EHR). */}
                {!unitesRattachement.some((u) => u.uniteRattachement === beneficiaire.uniteRattachement) &&
                  beneficiaire.uniteRattachement && (
                    <option value={beneficiaire.uniteRattachement}>{beneficiaire.uniteRattachement}</option>
                  )}
                {unitesRattachement.map((u) => (
                  <option key={u.codeUnite} value={u.uniteRattachement}>
                    {u.uniteRattachement}
                  </option>
                ))}
              </Select>
            </div>
          </CardContent>
          <CardFooter className="gap-3">
            <Button type="button" variant="outline" onClick={onFerme} disabled={isSubmitting}>
              Annuler
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Enregistrement…' : 'Enregistrer'}
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}