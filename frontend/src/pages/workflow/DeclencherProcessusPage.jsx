import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { AlertTriangle, CheckCircle2, RotateCcw } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../../components/ui/Card';
import { Label } from '../../components/ui/Label';
import { Select } from '../../components/ui/Select';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { LienRetour } from '../../components/ui/LienRetour';
import { ConfirmDialog } from '../../components/ui/ConfirmDialog';
import { getPeriodeLabel } from '../../utils/formatters';

const MOIS = [
  { valeur: 1, libelle: 'Janvier' }, { valeur: 2, libelle: 'Février' }, { valeur: 3, libelle: 'Mars' },
  { valeur: 4, libelle: 'Avril' }, { valeur: 5, libelle: 'Mai' }, { valeur: 6, libelle: 'Juin' },
  { valeur: 7, libelle: 'Juillet' }, { valeur: 8, libelle: 'Août' }, { valeur: 9, libelle: 'Septembre' },
  { valeur: 10, libelle: 'Octobre' }, { valeur: 11, libelle: 'Novembre' }, { valeur: 12, libelle: 'Décembre' },
];

const AUJOURDHUI = new Date();
const ANNEE_COURANTE = AUJOURDHUI.getFullYear();
const MOIS_COURANT = AUJOURDHUI.getMonth() + 1;

// Sprint MM.11 : pas de limite basse (le rattrapage autorise de remonter
// loin dans le passe), seulement une limite haute -- le mois courant.
// 5 ans en arriere suffit largement en pratique pour un incident a rattraper.
const ANNEES = Array.from({ length: 6 }, (_, i) => ANNEE_COURANTE - i);

export default function DeclencherProcessusPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const retourListe = location.state?.retour ?? '/processus';
  const [moisPaiement, setMoisPaiement] = useState(MOIS_COURANT);
  const [anneePaiement, setAnneePaiement] = useState(ANNEE_COURANTE);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState(null);
  const [resultat, setResultat] = useState(null);
  const [demandeConfirmation, setDemandeConfirmation] = useState(false);
  const [modeRattrapage, setModeRattrapage] = useState(false);
  const [verificationRattrapageEnCours, setVerificationRattrapageEnCours] = useState(false);

  // Sprint MM.11 : detection automatique du mode rattrapage. Des qu'un mois
  // deja traite par un processus NORMAL clos est selectionne, la page bascule
  // sans action supplementaire de l'ARH -- aucun nouvel endpoint necessaire,
  // GET /processus (deja utilise par la liste des processus) suffit.
  useEffect(() => {
    let ignore = false;
    setVerificationRattrapageEnCours(true);
    apiClient
      .get('/processus', { params: { statut: 'CLOTURE', anneePaiement: Number(anneePaiement) } })
      .then(({ data }) => {
        if (ignore) return;
        const processusNormalExistant = data.some(
          (p) => Number(p.moisPaiement) === Number(moisPaiement) && !p.rattrapage,
        );
        setModeRattrapage(processusNormalExistant);
      })
      .catch(() => {
        if (!ignore) setModeRattrapage(false);
      })
      .finally(() => {
        if (!ignore) setVerificationRattrapageEnCours(false);
      });
    return () => {
      ignore = true;
    };
  }, [moisPaiement, anneePaiement]);

  // Le selecteur ne doit structurellement jamais proposer un mois futur :
  // si l'annee choisie est l'annee courante, on tronque la liste des mois
  // au mois courant inclus. Pour une annee anterieure, les 12 mois restent
  // disponibles (pas de limite basse, cf. section 1.1 du guide MM.11).
  const moisDisponibles = useMemo(
    () => (anneePaiement === ANNEE_COURANTE ? MOIS.filter((m) => m.valeur <= MOIS_COURANT) : MOIS),
    [anneePaiement],
  );

  const changerAnnee = (nouvelleAnnee) => {
    const annee = Number(nouvelleAnnee);
    setAnneePaiement(annee);
    if (annee === ANNEE_COURANTE && Number(moisPaiement) > MOIS_COURANT) {
      setMoisPaiement(MOIS_COURANT);
    }
  };

  const periodeAnterieureAuMoisCourant =
    Number(anneePaiement) < ANNEE_COURANTE ||
    (Number(anneePaiement) === ANNEE_COURANTE && Number(moisPaiement) < MOIS_COURANT);

  const executerDeclenchement = async () => {
    setErreur(null);
    setResultat(null);
    setEnCours(true);
    try {
      const { data } = await apiClient.post('/processus/declencher', {
        moisPaiement: Number(moisPaiement),
        anneePaiement: Number(anneePaiement),
        rattrapage: modeRattrapage,
      });
      setResultat(data);
    } catch (err) {
      if (err.response?.status === 409) {
        setErreur(`Un processus mensuel existe déjà pour ${getPeriodeLabel(Number(moisPaiement), Number(anneePaiement))}.`);
      } else if (err.response?.status === 400) {
        // Filet de securite : l'UI empeche deja structurellement une periode
        // future, mais le backend reste la source de verite (ex. appel API
        // direct hors de cette page).
        setErreur(err.response.data?.erreur ?? 'La période sélectionnée est invalide.');
      } else {
        setErreur('Une erreur est survenue lors du déclenchement du processus. Veuillez réessayer.');
      }
    } finally {
      setEnCours(false);
    }
  };

  const declencher = (event) => {
    event.preventDefault();
    if (periodeAnterieureAuMoisCourant) {
      setDemandeConfirmation(true);
      return;
    }
    executerDeclenchement();
  };

  return (
    <>
      <LienRetour to={retourListe} label="Retour aux processus" />
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

              {modeRattrapage && (
                <Alert variant="warning">
                  <RotateCcw className="h-4 w-4" />
                  <AlertDescription>
                    Cette période a déjà été traitée. Le déclenchement se fera en{' '}
                    <strong>mode rattrapage</strong> : seuls les bénéficiaires non payés seront
                    pré-cochés, l'état d'origine reste inchangé.
                  </AlertDescription>
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
                    {moisDisponibles.map((m) => (
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
                    onChange={(e) => changerAnnee(e.target.value)}
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
              <Button type="submit" disabled={enCours || verificationRattrapageEnCours}>
                {enCours
                  ? 'Déclenchement en cours…'
                  : modeRattrapage
                    ? 'Déclencher le rattrapage'
                    : 'Déclencher'}
              </Button>
            </CardFooter>
          </form>
        </Card>

        {resultat && (
          <Card className="max-w-lg">
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-emerald-700">
                <CheckCircle2 className="h-5 w-5" />
                {resultat.rattrapage ? 'Rattrapage déclenché' : 'Processus déclenché'}
              </CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col gap-3">
              <p className="text-sm text-neutral-700">
                {getPeriodeLabel(resultat.moisPaiement, resultat.anneePaiement)}
                {resultat.rattrapage && ' (rattrapage)'} —{' '}
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
              <Button
                variant="outline"
                onClick={() =>
                  navigate(`/processus/${resultat.id}`, { state: { retour: retourListe } })
                }
              >
                Voir le détail du processus
              </Button>
            </CardFooter>
          </Card>
        )}
      </div>

      {demandeConfirmation && (
        <ConfirmDialog
          titre="Période antérieure au mois actuel"
          message={`La période sélectionnée (${getPeriodeLabel(Number(moisPaiement), Number(anneePaiement))}) est antérieure au mois actuel. Voulez-vous continuer ?`}
          libelleConfirmer="Déclencher"
          onAnnuler={() => setDemandeConfirmation(false)}
          onConfirmer={async () => {
            await executerDeclenchement();
            setDemandeConfirmation(false);
          }}
        />
      )}
    </>
  );
}
