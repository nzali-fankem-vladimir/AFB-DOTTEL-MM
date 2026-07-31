import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertTriangle, CheckCircle2 } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../../components/ui/Card';
import { Label } from '../../components/ui/Label';
import { Select } from '../../components/ui/Select';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { LienRetour } from '../../components/ui/LienRetour';
import { getPeriodeLabel } from '../../utils/formatters';

const MOIS = [
  { valeur: 1, libelle: 'Janvier' }, { valeur: 2, libelle: 'Février' }, { valeur: 3, libelle: 'Mars' },
  { valeur: 4, libelle: 'Avril' }, { valeur: 5, libelle: 'Mai' }, { valeur: 6, libelle: 'Juin' },
  { valeur: 7, libelle: 'Juillet' }, { valeur: 8, libelle: 'Août' }, { valeur: 9, libelle: 'Septembre' },
  { valeur: 10, libelle: 'Octobre' }, { valeur: 11, libelle: 'Novembre' }, { valeur: 12, libelle: 'Décembre' },
];

const ANNEE_COURANTE = new Date().getFullYear();
const ANNEES = [ANNEE_COURANTE - 1, ANNEE_COURANTE, ANNEE_COURANTE + 1];

export default function DeclencherProcessusPage() {
  const navigate = useNavigate();
  const [moisPaiement, setMoisPaiement] = useState(new Date().getMonth() + 1);
  const [anneePaiement, setAnneePaiement] = useState(ANNEE_COURANTE);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState(null);
  const [resultat, setResultat] = useState(null);

  const declencher = async (event) => {
    event.preventDefault();
    setErreur(null);
    setResultat(null);
    setEnCours(true);
    try {
      const { data } = await apiClient.post('/processus/declencher', {
        moisPaiement: Number(moisPaiement),
        anneePaiement: Number(anneePaiement),
      });
      setResultat(data);
    } catch (err) {
      if (err.response?.status === 409) {
        setErreur(`Un processus mensuel existe déjà pour ${getPeriodeLabel(Number(moisPaiement), Number(anneePaiement))}.`);
      } else {
        setErreur('Une erreur est survenue lors du déclenchement du processus. Veuillez réessayer.');
      }
    } finally {
      setEnCours(false);
    }
  };

  return (
    <>
      <LienRetour to="/processus" label="Retour aux processus" />
      <PageHeader surTitre="ARH" titre="Déclencher un processus mensuel" />
      <div className="flex flex-col gap-6 p-8">
        <Card className="max-w-lg">
          <CardHeader>
            <CardTitle>Période à traiter</CardTitle>
          </CardHeader>
          <form onSubmit={declencher} noValidate>
            <CardContent className="flex flex-col gap-4">
              {erreur && (
                <Alert variant="destructive">
                  <AlertTriangle className="h-4 w-4" />
                  <AlertDescription>{erreur}</AlertDescription>
                </Alert>
              )}

              <div className="flex gap-4">
                <div className="flex flex-1 flex-col gap-1.5">
                  <Label htmlFor="declencher-mois">Mois</Label>
                  <Select
                    id="declencher-mois"
                    value={moisPaiement}
                    onChange={(e) => setMoisPaiement(e.target.value)}
                    disabled={enCours}
                  >
                    {MOIS.map((m) => (
                      <option key={m.valeur} value={m.valeur}>
                        {m.libelle}
                      </option>
                    ))}
                  </Select>
                </div>

                <div className="flex flex-1 flex-col gap-1.5">
                  <Label htmlFor="declencher-annee">Année</Label>
                  <Select
                    id="declencher-annee"
                    value={anneePaiement}
                    onChange={(e) => setAnneePaiement(e.target.value)}
                    disabled={enCours}
                  >
                    {ANNEES.map((a) => (
                      <option key={a} value={a}>
                        {a}
                      </option>
                    ))}
                  </Select>
                </div>
              </div>
            </CardContent>
            <CardFooter>
              <Button type="submit" disabled={enCours}>
                {enCours ? 'Déclenchement en cours…' : 'Déclencher'}
              </Button>
            </CardFooter>
          </form>
        </Card>

        {resultat && (
          <Card className="max-w-lg">
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-emerald-700">
                <CheckCircle2 className="h-5 w-5" />
                Processus déclenché
              </CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col gap-3">
              <p className="text-sm text-neutral-700">
                {getPeriodeLabel(resultat.moisPaiement, resultat.anneePaiement)} —{' '}
                <strong>{resultat.nombreBeneficiaires}</strong> bénéficiaire
                {resultat.nombreBeneficiaires > 1 ? 's' : ''} inclus dans l'état mensuel.
              </p>

              {resultat.beneficiairesExclus?.length > 0 && (
                <Alert variant="warning">
                  <AlertTriangle className="h-4 w-4" />
                  <AlertDescription>
                    <p className="mb-2 font-medium">
                      {resultat.beneficiairesExclus.length} bénéficiaire
                      {resultat.beneficiairesExclus.length > 1 ? 's' : ''} exclu
                      {resultat.beneficiairesExclus.length > 1 ? 's' : ''} :
                    </p>
                    <ul className="flex flex-col gap-1">
                      {resultat.beneficiairesExclus.map((exclu) => (
                        <li key={exclu.matricule}>
                          {exclu.matricule} — {exclu.nomPrenoms} ({exclu.fonction}) : {exclu.motif}
                        </li>
                      ))}
                    </ul>
                  </AlertDescription>
                </Alert>
              )}
            </CardContent>
            <CardFooter>
              <Button variant="outline" onClick={() => navigate(`/processus/${resultat.id}`)}>
                Voir le détail du processus
              </Button>
            </CardFooter>
          </Card>
        )}
      </div>
    </>
  );
}
