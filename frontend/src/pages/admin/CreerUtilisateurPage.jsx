import { useState } from 'react';
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
import { LienRetour } from '../../components/ui/LienRetour';

const ROLES = ['EMPLOYE', 'ARH', 'CRH', 'DRH', 'ADMIN'];

export default function CreerUtilisateurPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const retourListe = location.state?.retour ?? '/admin/utilisateurs';
  const [matricule, setMatricule] = useState('');
  const [nom, setNom] = useState('');
  const [prenom, setPrenom] = useState('');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('EMPLOYE');
  const [motDePasse, setMotDePasse] = useState('');
  const [erreur, setErreur] = useState(null);
  const [enCours, setEnCours] = useState(false);

  const creer = async (event) => {
    event.preventDefault();
    setErreur(null);
    setEnCours(true);
    try {
      await apiClient.post('/admin/utilisateurs', { matricule, nom, prenom, email, role, motDePasse });
      navigate(retourListe);
    } catch (err) {
      if (err.response?.status === 409) {
        setErreur(
          err.response?.data?.erreur ?? 'Ce matricule ou cet email est déjà utilisé par un autre utilisateur.'
        );
      } else {
        setErreur("Une erreur est survenue lors de la création de l'utilisateur. Veuillez réessayer.");
      }
    } finally {
      setEnCours(false);
    }
  };

  return (
    <>
      <LienRetour to={retourListe} label="Retour aux utilisateurs" />
      <PageHeader surTitre="Système" titre="Créer un utilisateur" />
      <div className="flex flex-col gap-6 p-8">
        <Card className="max-w-lg">
          <CardHeader>
            <CardTitle>Nouvel utilisateur</CardTitle>
          </CardHeader>
          {/* Pas de noValidate : ce formulaire s'appuie sur les attributs
              `required` natifs (aucun schema zod ici). Le mettre neutralisait
              silencieusement toute validation cote client -- constat de
              l'audit Sprint 6F.9. La garantie reste le Bean Validation
              backend (@NotBlank/@NotNull sur CreerUtilisateurRequestDto). */}
          <form onSubmit={creer}>
            {/* Sprint D.3 -- six champs d'affilee se lisaient comme une liste
                indifferenciee. Deux groupes : qui est la personne, puis
                comment elle se connecte. Le rythme vertical distingue
                l'interieur d'un groupe (gap-4) de l'espace entre groupes. */}
            <CardContent className="flex flex-col gap-6">
              {erreur && (
                <Alert variant="destructive">
                  <AlertTriangle className="h-4 w-4" aria-hidden="true" />
                  <AlertDescription>{erreur}</AlertDescription>
                </Alert>
              )}

              <div className="flex flex-col gap-4">
                <h3 className="text-xs font-semibold uppercase tracking-wide text-neutral-600">
                  Identité
                </h3>

                <div className="flex flex-col gap-1.5">
                  <Label htmlFor="creer-matricule" obligatoire>
                    Matricule
                  </Label>
                  <Input
                    id="creer-matricule"
                    value={matricule}
                    onChange={(e) => setMatricule(e.target.value)}
                    placeholder="Ex. AFB-2026-0148"
                    disabled={enCours}
                    required
                  />
                </div>

                <div className="flex gap-4">
                  <div className="flex flex-1 flex-col gap-1.5">
                    <Label htmlFor="creer-nom" obligatoire>
                      Nom
                    </Label>
                    <Input
                      id="creer-nom"
                      value={nom}
                      onChange={(e) => setNom(e.target.value)}
                      placeholder="Ex. NKOLO"
                      disabled={enCours}
                      required
                    />
                  </div>
                  <div className="flex flex-1 flex-col gap-1.5">
                    <Label htmlFor="creer-prenom" obligatoire>
                      Prénom
                    </Label>
                    <Input
                      id="creer-prenom"
                      value={prenom}
                      onChange={(e) => setPrenom(e.target.value)}
                      placeholder="Ex. Solange"
                      disabled={enCours}
                      required
                    />
                  </div>
                </div>

                <div className="flex flex-col gap-1.5">
                  <Label htmlFor="creer-email" obligatoire>
                    Email
                  </Label>
                  <Input
                    id="creer-email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="Ex. solange.nkolo@afrilandfirstbank.com"
                    disabled={enCours}
                    required
                  />
                </div>
              </div>

              <div className="flex flex-col gap-4 border-t border-neutral-200 pt-6">
                <h3 className="text-xs font-semibold uppercase tracking-wide text-neutral-600">
                  Accès
                </h3>

                <div className="flex flex-col gap-1.5">
                  {/* Pas d'asterisque : le selecteur est pre-rempli et ne peut
                      pas etre laisse vide. */}
                  <Label htmlFor="creer-role">Rôle</Label>
                  <Select id="creer-role" value={role} onChange={(e) => setRole(e.target.value)} disabled={enCours}>
                    {ROLES.map((r) => (
                      <option key={r} value={r}>
                        {r}
                      </option>
                    ))}
                  </Select>
                </div>

                <div className="flex flex-col gap-1.5">
                  <Label htmlFor="creer-mot-de-passe" obligatoire>
                    Mot de passe provisoire
                  </Label>
                  <Input
                    id="creer-mot-de-passe"
                    type="password"
                    value={motDePasse}
                    onChange={(e) => setMotDePasse(e.target.value)}
                    disabled={enCours}
                    required
                  />
                </div>
              </div>
            </CardContent>
            <CardFooter className="gap-3">
              <Button type="button" variant="outline" onClick={() => navigate(retourListe)} disabled={enCours}>
                Annuler
              </Button>
              <Button type="submit" isLoading={enCours}>
                {enCours ? 'Création en cours…' : 'Créer'}
              </Button>
            </CardFooter>
          </form>
        </Card>
      </div>
    </>
  );
}