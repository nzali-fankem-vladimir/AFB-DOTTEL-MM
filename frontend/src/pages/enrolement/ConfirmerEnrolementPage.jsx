import { useState } from 'react';
import { useLocation, useNavigate, Navigate } from 'react-router-dom';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';

const MESSAGES_ERREUR = {
  400: 'Requête invalide : le matricule est manquant ou mal formé.',
  403: "Fonction ou grade non éligible à la dotation téléphonique.",
  404: "Matricule inconnu de l'EHR.",
  409: 'Ce matricule est déjà enrôlé.',
};

export default function ConfirmerEnrolementPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const enrolement = location.state?.enrolement;
  const [confirmation, setConfirmation] = useState(null);
  const [erreur, setErreur] = useState(null);
  const [enCours, setEnCours] = useState(false);

  if (!enrolement || !enrolement.eligible) {
    return <Navigate to="/enrolement" replace />;
  }

  const confirmer = async () => {
    setEnCours(true);
    setErreur(null);
    try {
      const { data } = await apiClient.post('/enrolement/confirmer', {
        matricule: enrolement.matricule,
      });
      setConfirmation(data);
    } catch (err) {
      const statut = err.response?.status;
      setErreur(MESSAGES_ERREUR[statut] || 'Une erreur est survenue. Veuillez réessayer.');
    } finally {
      setEnCours(false);
    }
  };

  return (
    <>
      <PageHeader surTitre="Employé" titre="Confirmation d'enrôlement" />
      <div className="mx-auto max-w-lg p-8">
        <Card>
          <CardHeader>
            <CardTitle>Confirmer mon enrôlement</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-4">
            {erreur && (
              <Alert variant="destructive">
                <AlertDescription>{erreur}</AlertDescription>
              </Alert>
            )}

            {confirmation ? (
              <>
                <Alert>
                  <AlertDescription>
                    Enrôlement confirmé pour {confirmation.nomPrenoms} (matricule{' '}
                    {confirmation.matricule}).
                  </AlertDescription>
                </Alert>
                <Button variant="outline" onClick={() => navigate('/enrolement')}>
                  Retour
                </Button>
              </>
            ) : (
              <>
                <dl className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
                  <dt className="text-neutral-500">Nom</dt>
                  <dd className="font-medium text-neutral-900">
                    {enrolement.nom} {enrolement.prenom}
                  </dd>
                  <dt className="text-neutral-500">Fonction</dt>
                  <dd className="font-medium text-neutral-900">
                    {enrolement.libelleFonction || enrolement.fonction}
                  </dd>
                  <dt className="text-neutral-500">Unité</dt>
                  <dd className="font-medium text-neutral-900">{enrolement.uniteRattachement}</dd>
                </dl>
                <Button onClick={confirmer} disabled={enCours}>
                  {enCours ? 'Confirmation…' : "Confirmer l'enrôlement"}
                </Button>
              </>
            )}
          </CardContent>
        </Card>
      </div>
    </>
  );
}