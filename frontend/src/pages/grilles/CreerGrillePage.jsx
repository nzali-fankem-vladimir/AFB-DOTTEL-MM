import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { AlertTriangle } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../../components/ui/Card';
import { Label } from '../../components/ui/Label';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { ConfirmDialog } from '../../components/ui/ConfirmDialog';
import { LienRetour } from '../../components/ui/LienRetour';
import { formatMontantFCFA, formatDate } from '../../utils/formatters';

export default function CreerGrillePage() {
  const navigate = useNavigate();
  const location = useLocation();
  const retourListe = location.state?.retour ?? '/grilles-tarifaires';
  const [fonctionsEligibles, setFonctionsEligibles] = useState([]);
  const [codeFonction, setCodeFonction] = useState('');
  const [montantFcfa, setMontantFcfa] = useState('');
  const [dateDebut, setDateDebut] = useState('');
  const [erreur, setErreur] = useState(null);
  const [enCours, setEnCours] = useState(false);
  const [confirmationOuverte, setConfirmationOuverte] = useState(false);

  // Le formulaire ne connait que le CODE de la fonction ; le recapitulatif de
  // confirmation doit montrer le libelle que l'ARH vient de choisir.
  const libelleFonctionChoisie =
    fonctionsEligibles.find((f) => f.code === codeFonction)?.libelle ?? codeFonction;

  useEffect(() => {
    apiClient.get('/fonctions-eligibles').then(({ data }) => {
      setFonctionsEligibles(data);
      if (data.length > 0) setCodeFonction(data[0].code);
    });
  }, []);

  // Sprint MM.12 : la soumission n'est plus declenchee par le seul submit du
  // formulaire. Une grille soumise part au CRH puis a la DRH et n'est plus
  // annulable -- seul un rejet peut la faire retomber. Le recapitulatif
  // ci-dessous laisse une derniere chance de relire le montant saisi.
  const demanderCreation = (event) => {
    event.preventDefault();
    setErreur(null);
    setConfirmationOuverte(true);
  };

  const creer = async () => {
    setErreur(null);
    setConfirmationOuverte(false);
    setEnCours(true);
    try {
      await apiClient.post('/grilles-tarifaires', {
        codeFonction,
        montantFcfa: Number(montantFcfa),
        dateDebut,
      });
      navigate(retourListe);
    } catch (err) {
      if (err.response?.status === 409) {
        // Sprint MM.12 : le 409 couvre les DEUX etages d'attente (CRH et DRH),
        // le message ne peut donc plus nommer la seule DRH.
        setErreur('Une grille est déjà en attente de validation pour cette fonction.');
      } else if (err.response?.status === 404) {
        setErreur('Fonction éligible introuvable.');
      } else {
        setErreur('Une erreur est survenue lors de la création de la grille. Veuillez réessayer.');
      }
    } finally {
      setEnCours(false);
    }
  };

  return (
    <>
      <LienRetour to={retourListe} label="Retour aux grilles tarifaires" />
      <PageHeader surTitre="ARH" titre="Créer une grille tarifaire" />
      <div className="flex flex-col gap-6 p-8">
        <Card className="max-w-lg">
          <CardHeader>
            <CardTitle>Nouvelle grille</CardTitle>
          </CardHeader>
          {/* Pas de noValidate : validation cote client portee par les
              attributs `required` natifs (audit Sprint 6F.9). */}
          <form onSubmit={demanderCreation}>
            <CardContent className="flex flex-col gap-4">
              {erreur && (
                <Alert variant="destructive">
                  <AlertTriangle className="h-4 w-4" aria-hidden="true" />
                  <AlertDescription>{erreur}</AlertDescription>
                </Alert>
              )}

              <div className="flex flex-col gap-1.5">
                <Label htmlFor="creer-fonction" obligatoire>
                  Fonction
                </Label>
                <Select
                  id="creer-fonction"
                  value={codeFonction}
                  onChange={(e) => setCodeFonction(e.target.value)}
                  disabled={enCours}
                  required
                >
                  {fonctionsEligibles.map((f) => (
                    <option key={f.code} value={f.code}>
                      {f.libelle}
                    </option>
                  ))}
                </Select>
              </div>

              <div className="flex flex-col gap-1.5">
                <Label htmlFor="creer-montant" obligatoire>
                  Montant (FCFA)
                </Label>
                <Input
                  id="creer-montant"
                  type="number"
                  min="1"
                  value={montantFcfa}
                  onChange={(e) => setMontantFcfa(e.target.value)}
                  disabled={enCours}
                  required
                />
              </div>

              <div className="flex flex-col gap-1.5">
                <Label htmlFor="creer-date-debut" obligatoire>
                  Date de début
                </Label>
                <Input
                  id="creer-date-debut"
                  type="date"
                  value={dateDebut}
                  onChange={(e) => setDateDebut(e.target.value)}
                  disabled={enCours}
                  required
                />
              </div>
            </CardContent>
            {/* Sprint D.3 -- "Annuler" au meme endroit et dans le meme ordre
                que dans les quatre modales du groupe : un utilisateur qui
                apprend un formulaire doit connaitre les six autres. Le
                LienRetour du haut reste, il ne remplace pas une action de
                sortie posee a cote de l'action principale. */}
            <CardFooter className="gap-3">
              <Button type="button" variant="outline" onClick={() => navigate(retourListe)} disabled={enCours}>
                Annuler
              </Button>
              <Button type="submit" isLoading={enCours}>
                {/* Sprint MM.12 : le premier etage du workflow est desormais le CRH. */}
                {enCours ? 'Soumission en cours…' : 'Soumettre au CRH'}
              </Button>
            </CardFooter>
          </form>
        </Card>
      </div>

      {confirmationOuverte && (
        <ConfirmDialog
          titre="Soumettre la grille au CRH"
          message={
            `${libelleFonctionChoisie} — ${formatMontantFCFA(Number(montantFcfa))} `
            + `applicable au ${formatDate(dateDebut)}. `
            + `Une fois soumise, la grille suit le circuit CRH puis DRH et n'est plus modifiable `
            + `dès que le CRH a statué. Confirmer ?`
          }
          libelleConfirmer="Soumettre"
          /* Soumettre fait entrer la grille dans le circuit, ca ne defait
             rien : bouton primaire. */
          variantConfirmer="default"
          onAnnuler={() => setConfirmationOuverte(false)}
          onConfirmer={creer}
        />
      )}
    </>
  );
}