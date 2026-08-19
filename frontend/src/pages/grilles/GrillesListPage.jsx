import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Plus, History, MessageSquareWarning, PowerOff } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { DataTable } from '../../components/ui/DataTable';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { ConfirmDialog } from '../../components/ui/ConfirmDialog';
import { Label } from '../../components/ui/Label';
import { Select } from '../../components/ui/Select';
import { VoirMotifModal } from '../../components/ui/VoirMotifModal';
import { useAuth } from '../../contexts/AuthContext';
import { formatMontantFCFA, formatDate } from '../../utils/formatters';
import { getStatutGrilleInfo } from '../../utils/statutGrille';
import { definirParametre, construireRetour } from '../../utils/searchParams';
import { ModifierGrilleModal } from './ModifierGrilleModal';

const colonnes = [
  { cle: 'libelleFonction', entete: 'Fonction' },
  {
    cle: 'montantFcfa',
    entete: 'Montant',
    className: 'tabular-nums',
    rendu: (grille) => formatMontantFCFA(grille.montantFcfa),
  },
  {
    cle: 'dateDebut',
    entete: 'Date début',
    className: 'tabular-nums',
    rendu: (grille) => formatDate(grille.dateDebut),
  },
  {
    cle: 'dateFin',
    entete: 'Date fin',
    className: 'tabular-nums',
    rendu: (grille) => (grille.dateFin ? formatDate(grille.dateFin) : '—'),
  },
  {
    cle: 'statutValidation',
    entete: 'Statut',
    rendu: (grille) => {
      const { libelle, variant } = getStatutGrilleInfo(grille);
      return <Badge variant={variant}>{libelle}</Badge>;
    },
  },
];

export default function GrillesListPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const fonction = searchParams.get('fonction') ?? '';
  const statut = searchParams.get('statut') ?? '';
  const [fonctionsEligibles, setFonctionsEligibles] = useState([]);
  const [donnees, setDonnees] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [rafraichissement, setRafraichissement] = useState(0);
  const [grilleAModifier, setGrilleAModifier] = useState(null);
  const [grilleMotifAVoir, setGrilleMotifAVoir] = useState(null);
  const [grilleADesactiver, setGrilleADesactiver] = useState(null);
  const [erreurDesactivation, setErreurDesactivation] = useState(null);

  useEffect(() => {
    apiClient.get('/fonctions-eligibles').then(({ data }) => setFonctionsEligibles(data));
  }, []);

  // Le backend ne distingue pas une grille ACTIVE courante d'une grille ACTIVE
  // close (statutValidation reste ACTIVE dans les deux cas, seul dateFin
  // change -- RG-04). Les deux valeurs de filtre "Active (courante)" et
  // "Clôturée" interrogent donc statut=ACTIVE cote backend, et le tri se
  // fait cote client sur la presence de dateFin.
  useEffect(() => {
    let annule = false;
    setChargement(true);
    const statutBackend = statut === 'ACTIVE_COURANTE' || statut === 'ACTIVE_CLOTUREE' ? 'ACTIVE' : statut;
    apiClient
      .get('/grilles-tarifaires', {
        params: {
          fonction: fonction || undefined,
          statut: statutBackend || undefined,
        },
      })
      .then(({ data }) => {
        if (annule) return;
        let contenu = data.contenu;
        if (statut === 'ACTIVE_COURANTE') contenu = contenu.filter((g) => !g.dateFin);
        if (statut === 'ACTIVE_CLOTUREE') contenu = contenu.filter((g) => g.dateFin);
        setDonnees(contenu);
      })
      .finally(() => {
        if (!annule) setChargement(false);
      });
    return () => {
      annule = true;
    };
  }, [fonction, statut, rafraichissement]);

  return (
    <>
      <PageHeader surTitre="Référentiel" titre="Grilles tarifaires" />
      <div className="flex flex-col gap-6 p-8">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div className="flex flex-wrap items-end gap-4">
            <div className="flex w-56 flex-col gap-1.5">
              <Label htmlFor="filtre-fonction">Fonction</Label>
              <Select
                id="filtre-fonction"
                value={fonction}
                onChange={(e) =>
                  setSearchParams((precedent) => definirParametre(precedent, 'fonction', e.target.value))
                }
              >
                <option value="">Toutes</option>
                {fonctionsEligibles.map((f) => (
                  <option key={f.code} value={f.code}>
                    {f.libelle}
                  </option>
                ))}
              </Select>
            </div>

            <div className="flex w-48 flex-col gap-1.5">
              <Label htmlFor="filtre-statut">Statut</Label>
              <Select
                id="filtre-statut"
                value={statut}
                onChange={(e) =>
                  setSearchParams((precedent) => definirParametre(precedent, 'statut', e.target.value))
                }
              >
                <option value="">Tous</option>
                <option value="BROUILLON">Brouillon</option>
                <option value="EN_ATTENTE_CRH">En attente CRH</option>
                <option value="EN_ATTENTE_DRH">En attente DRH</option>
                <option value="ACTIVE_COURANTE">Active (courante)</option>
                <option value="ACTIVE_CLOTUREE">Clôturée</option>
                <option value="REJETEE">Rejetée</option>
              </Select>
            </div>
          </div>

          {user?.role === 'ARH' && (
            <Button
              onClick={() =>
                navigate('/grilles-tarifaires/creer', {
                  state: { retour: construireRetour('/grilles-tarifaires', searchParams) },
                })
              }
            >
              <Plus className="h-4 w-4" />
              Créer une grille
            </Button>
          )}
        </div>

        {erreurDesactivation && (
          <Alert variant="destructive">
            <AlertDescription>{erreurDesactivation}</AlertDescription>
          </Alert>
        )}

        <DataTable
          colonnes={colonnes}
          donnees={donnees}
          cleLigne={(grille) => grille.id}
          chargement={chargement}
          actions={(grille) => (
            <div className="flex gap-2">
              {/* Sprint MM.12 : le montant est gele des que le CRH a statue --
                  le backend refuse desormais un PATCH sur EN_ATTENTE_DRH. */}
              {user?.role === 'ARH' && grille.statutValidation === 'EN_ATTENTE_CRH' && (
                <Button variant="outline" size="sm" onClick={() => setGrilleAModifier(grille)}>
                  Modifier
                </Button>
              )}
              <Button
                variant="outline"
                size="sm"
                onClick={() =>
                  navigate(`/grilles-tarifaires/historique/${grille.codeFonction}`, {
                    state: { retour: construireRetour('/grilles-tarifaires', searchParams) },
                  })
                }
              >
                <History className="h-4 w-4" />
                Historique
              </Button>
              {grille.statutValidation === 'REJETEE' && (
                <Button variant="outline" size="sm" onClick={() => setGrilleMotifAVoir(grille)}>
                  <MessageSquareWarning className="h-4 w-4" />
                  Voir le motif
                </Button>
              )}
              {user?.role === 'ARH' && grille.statutValidation === 'ACTIVE' && !grille.dateFin && (
                <Button
                  variant="destructive"
                  size="sm"
                  onClick={() => {
                    setErreurDesactivation(null);
                    setGrilleADesactiver(grille);
                  }}
                >
                  <PowerOff className="h-4 w-4" />
                  Désactiver
                </Button>
              )}
            </div>
          )}
        />
      </div>

      {grilleAModifier && (
        <ModifierGrilleModal
          grille={grilleAModifier}
          onFerme={() => setGrilleAModifier(null)}
          onSucces={() => {
            setGrilleAModifier(null);
            setRafraichissement((n) => n + 1);
          }}
        />
      )}

      {grilleMotifAVoir && (
        <VoirMotifModal
          /* Sprint MM.12 : origineRejet ("CRH" ou "DRH") permet a l'ARH de
             savoir a quel etage sa grille est tombee. Derive cote backend, il
             peut etre absent sur les grilles rejetees avant MM.12. */
          titre={
            grilleMotifAVoir.origineRejet
              ? `Motif de rejet ${grilleMotifAVoir.origineRejet} — ${grilleMotifAVoir.libelleFonction}`
              : `Motif de rejet — ${grilleMotifAVoir.libelleFonction}`
          }
          motif={grilleMotifAVoir.motifRejet}
          onFermer={() => setGrilleMotifAVoir(null)}
        />
      )}

      {grilleADesactiver && (
        <ConfirmDialog
          titre="Désactiver la grille tarifaire"
          message={`Cette action retire le montant actif pour ${grilleADesactiver.libelleFonction}. Aucun bénéficiaire de cette fonction ne sera payé tant qu'aucune nouvelle grille n'est activée. Confirmer ?`}
          libelleConfirmer="Désactiver"
          onAnnuler={() => setGrilleADesactiver(null)}
          onConfirmer={async () => {
            try {
              await apiClient.post(`/grilles-tarifaires/${grilleADesactiver.id}/desactiver`);
              setGrilleADesactiver(null);
              setRafraichissement((n) => n + 1);
            } catch (err) {
              setErreurDesactivation(
                err.response?.data?.erreur ?? 'Une erreur est survenue lors de la désactivation. Veuillez réessayer.'
              );
              setGrilleADesactiver(null);
            }
          }}
        />
      )}
    </>
  );
}