import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertTriangle } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../../components/ui/Card';
import { Label } from '../../components/ui/Label';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { LienRetour } from '../../components/ui/LienRetour';

// Decision actee avec le metier (section 3 du guide, Sprint 6F.7bis) : la
// grille tarifaire initiale est obligatoire a la creation -- la fonction est
// utilisable des sa creation (statut ACTIVE directement, hors workflow
// ARH/DRH de RG-10, cf. FonctionEligibleService.creer()).
export default function CreerFonctionPage() {
  const navigate = useNavigate();
  const [code, setCode] = useState('');
  const [libelle, setLibelle] = useState('');
  const [montantFcfa, setMontantFcfa] = useState('');
  const [dateDebut, setDateDebut] = useState('');
  const [erreur, setErreur] = useState(null);
  const [enCours, setEnCours] = useState(false);

  const creer = async (event) => {
    event.preventDefault();
    setErreur(null);
    setEnCours(true);
    try {
      await apiClient.post('/fonctions-eligibles', {
        code: code.trim().toUpperCase(),
        libelle,
        montantFcfa: Number(montantFcfa),
        dateDebut,
      });
      navigate('/admin/fonctions-eligibles');
    } catch (err) {
      if (err.response?.status === 409) {
        setErreur(err.response?.data?.erreur ?? 'Ce code de fonction est déjà utilisé.');
      } else {
        setErreur('Une erreur est survenue lors de la création de la fonction. Veuillez réessayer.');
      }
    } finally {
      setEnCours(false);
    }
  };

  return (
    <>
      <LienRetour to="/admin/fonctions-eligibles" label="Retour aux fonctions éligibles" />
      <PageHeader surTitre="Référentiel" titre="Créer une fonction éligible" />
      <div className="flex flex-col gap-6 p-8">
        <Card className="max-w-lg">
          <CardHeader>
            <CardTitle>Nouvelle fonction</CardTitle>
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
                <Label htmlFor="creer-code">Code</Label>
                <Input
                  id="creer-code"
                  value={code}
                  onChange={(e) => setCode(e.target.value)}
                  placeholder="Ex. CHARGE_INNOVATION"
                  disabled={enCours}
                  required
                />
              </div>

              <div className="flex flex-col gap-1.5">
                <Label htmlFor="creer-libelle">Libellé</Label>
                <Input
                  id="creer-libelle"
                  value={libelle}
                  onChange={(e) => setLibelle(e.target.value)}
                  placeholder="Ex. Chargé Innovation"
                  disabled={enCours}
                  required
                />
              </div>

              <div className="flex flex-col gap-1.5">
                <Label htmlFor="creer-montant">Montant de la dotation (FCFA)</Label>
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
                <Label htmlFor="creer-date-debut">Date de début de la grille</Label>
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
                {enCours ? 'Création en cours…' : 'Créer'}
              </Button>
            </CardFooter>
          </form>
        </Card>
      </div>
    </>
  );
}