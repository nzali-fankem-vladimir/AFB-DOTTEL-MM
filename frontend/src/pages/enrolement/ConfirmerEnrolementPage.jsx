import { useState } from 'react';
import { useLocation, useNavigate, Navigate } from 'react-router-dom';
import { AlertTriangle, CheckCircle2 } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { LienRetour } from '../../components/ui/LienRetour';

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
      {/* Sans ce lien, l'etape 2 n'offrait aucun moyen de revenir corriger un
          matricule mal saisi -- il fallait passer par la navigation laterale. */}
      {!confirmation && <LienRetour to="/enrolement" label="Revenir à la vérification" />}
      <PageHeader surTitre="Employé" titre="Confirmation d'enrôlement" />
      <div className="mx-auto max-w-lg p-8">
        <Card>
          <CardHeader>
            <p className="text-xs font-semibold uppercase tracking-wide text-neutral-600">
              Étape 2 sur 2
            </p>
            <CardTitle>Confirmer mon enrôlement</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-4">
            {erreur && (
              <Alert variant="destructive">
                <AlertTriangle className="h-4 w-4" />
                <AlertDescription>{erreur}</AlertDescription>
              </Alert>
            )}

            {confirmation ? (
              <>
                {/* Le succes et le refus d'eligibilite utilisaient la MEME
                    alerte grise. La charte n'a pas de couleur "succes"
                    declaree (DESIGN.md) et en introduire une exigerait la
                    procedure de token : la distinction passe donc par l'icone,
                    pas par une couleur nouvelle. */}
                <Alert>
                  <CheckCircle2 className="h-4 w-4 text-emerald-600" />
                  <AlertDescription>
                    <strong>Enrôlement confirmé</strong> pour {confirmation.nomPrenoms} (matricule{' '}
                    {confirmation.matricule}). Votre dotation sera prise en compte au prochain
                    processus mensuel.
                  </AlertDescription>
                </Alert>
                <Button variant="outline" onClick={() => navigate('/enrolement')}>
                  Retour
                </Button>
              </>
            ) : (
              <>
                <dl className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
                  <dt className="text-neutral-600">Nom</dt>
                  <dd className="font-medium text-neutral-900">
                    {enrolement.nom} {enrolement.prenom}
                  </dd>
                  <dt className="text-neutral-600">Fonction</dt>
                  <dd className="font-medium text-neutral-900">
                    {enrolement.libelleFonction || enrolement.fonction}
                  </dd>
                  <dt className="text-neutral-600">Unité</dt>
                  <dd className="font-medium text-neutral-900">{enrolement.uniteRattachement}</dd>
                </dl>
                <Button onClick={confirmer} isLoading={enCours}>
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