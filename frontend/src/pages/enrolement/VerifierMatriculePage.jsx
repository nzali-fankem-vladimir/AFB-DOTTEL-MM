import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/Card';
import { FormField } from '../../components/ui/FormField';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';

const schema = z.object({
  matricule: z.string().min(1, 'Le matricule est requis'),
});

export default function VerifierMatriculePage() {
  const navigate = useNavigate();
  const [resultat, setResultat] = useState(null);
  const [erreur, setErreur] = useState(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) });

  const onSubmit = async ({ matricule }) => {
    setErreur(null);
    setResultat(null);
    try {
      const { data } = await apiClient.get('/enrolement/verifier', { params: { matricule } });
      setResultat(data);
    } catch (err) {
      const statut = err.response?.status;
      if (statut === 409) {
        setErreur('Ce matricule est déjà enrôlé.');
      } else if (statut === 404) {
        setErreur("Matricule inconnu de l'EHR. Vérifiez la saisie.");
      } else {
        setErreur('Une erreur est survenue. Veuillez réessayer.');
      }
    }
  };

  const continuer = () => {
    navigate('/enrolement/confirmer', { state: { enrolement: resultat } });
  };

  return (
    <>
      <PageHeader surTitre="Employé" titre="Enrôlement" />
      <div className="mx-auto max-w-lg p-8">
        <Card>
          <CardHeader>
            <CardTitle>Vérifier mon matricule</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-4">
            <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4" noValidate>
              {erreur && (
                <Alert variant="destructive">
                  <AlertDescription>{erreur}</AlertDescription>
                </Alert>
              )}
              <FormField
                id="matricule"
                label="Matricule"
                error={errors.matricule}
                {...register('matricule')}
              />
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Vérification…' : 'Vérifier'}
              </Button>
            </form>

            {resultat && (
              <div className="flex flex-col gap-4 border-t border-neutral-200 pt-4">
                <dl className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
                  <dt className="text-neutral-500">Nom</dt>
                  <dd className="font-medium text-neutral-900">
                    {resultat.nom} {resultat.prenom}
                  </dd>
                  <dt className="text-neutral-500">Fonction</dt>
                  <dd className="font-medium text-neutral-900">
                    {resultat.libelleFonction || resultat.fonction}
                  </dd>
                  <dt className="text-neutral-500">Unité</dt>
                  <dd className="font-medium text-neutral-900">{resultat.uniteRattachement}</dd>
                </dl>

                {resultat.eligible ? (
                  <Button onClick={continuer}>Continuer vers la confirmation</Button>
                ) : (
                  <Alert>
                    <AlertDescription>
                      Votre fonction n'est actuellement pas éligible à la dotation téléphonique
                      mensuelle. Aucune action supplémentaire n'est requise.
                    </AlertDescription>
                  </Alert>
                )}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </>
  );
}