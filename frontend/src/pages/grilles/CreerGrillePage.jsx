import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertTriangle } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../../components/ui/Card';
import { Label } from '../../components/ui/Label';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { LienRetour } from '../../components/ui/LienRetour';

export default function CreerGrillePage() {
  const navigate = useNavigate();
  const [fonctionsEligibles, setFonctionsEligibles] = useState([]);
  const [codeFonction, setCodeFonction] = useState('');
  const [montantFcfa, setMontantFcfa] = useState('');
  const [dateDebut, setDateDebut] = useState('');
  const [erreur, setErreur] = useState(null);
  const [enCours, setEnCours] = useState(false);

  useEffect(() => {
    apiClient.get('/fonctions-eligibles').then(({ data }) => {
      setFonctionsEligibles(data);
      if (data.length > 0) setCodeFonction(data[0].code);
    });
  }, []);

  const creer = async (event) => {
    event.preventDefault();
    setErreur(null);
    setEnCours(true);
    try {
      await apiClient.post('/grilles-tarifaires', {
        codeFonction,
        montantFcfa: Number(montantFcfa),
        dateDebut,
      });
      navigate('/grilles-tarifaires');
    } catch (err) {
      if (err.response?.status === 409) {
        setErreur('Une grille est déjà en attente de validation DRH pour cette fonction.');
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
      <LienRetour to="/grilles-tarifaires" label="Retour aux grilles tarifaires" />
      <PageHeader surTitre="ARH" titre="Créer une grille tarifaire" />
      <div className="flex flex-col gap-6 p-8">
        <Card className="max-w-lg">
          <CardHeader>
            <CardTitle>Nouvelle grille</CardTitle>
          </CardHeader>
          {/* Pas de noValidate : validation cote client portee par les
              attributs `required` natifs (audit Sprint 6F.9). */}
          <form onSubmit={creer}>
            <CardContent className="flex flex-col gap-4">
              {erreur && (
                <Alert variant="destructive">
                  <AlertTriangle className="h-4 w-4" />
                  <AlertDescription>{erreur}</AlertDescription>
                </Alert>
              )}

              <div className="flex flex-col gap-1.5">
                <Label htmlFor="creer-fonction">Fonction</Label>
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
                <Label htmlFor="creer-montant">Montant (FCFA)</Label>
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
                <Label htmlFor="creer-date-debut">Date de début</Label>
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
            <CardFooter>
              <Button type="submit" disabled={enCours}>
                {enCours ? 'Création en cours…' : 'Soumettre à la DRH'}
              </Button>
            </CardFooter>
          </form>
        </Card>
      </div>
    </>
  );
}