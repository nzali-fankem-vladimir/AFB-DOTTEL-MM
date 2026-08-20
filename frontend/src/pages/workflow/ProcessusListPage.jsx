import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { AlertTriangle, Plus } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { DataTable } from '../../components/ui/DataTable';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Label } from '../../components/ui/Label';
import { MessageListeVide } from '../../components/ui/MessageListeVide';
import { Select } from '../../components/ui/Select';
import { useAuth } from '../../contexts/AuthContext';
import { getPeriodeLabel, formatDateHeure } from '../../utils/formatters';
import { getStatutProcessusInfo, STATUTS_PROCESSUS } from '../../utils/statutProcessus';
import { definirParametre, construireRetour, effacerParametres } from '../../utils/searchParams';

const colonnes = [
  {
    cle: 'periode',
    entete: 'Période',
    rendu: (processus) => getPeriodeLabel(processus.moisPaiement, processus.anneePaiement),
  },
  {
    cle: 'statut',
    entete: 'Statut',
    rendu: (processus) => {
      const { libelle, variant } = getStatutProcessusInfo(processus.statut);
      return <Badge variant={variant}>{libelle}</Badge>;
    },
  },
  {
    cle: 'dateCreation',
    entete: 'Date de création',
    className: 'tabular-nums',
    rendu: (processus) => formatDateHeure(processus.dateCreation),
  },
];

export default function ProcessusListPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const statut = searchParams.get('statut') ?? '';
  const annee = searchParams.get('annee') ?? '';
  const [anneesDisponibles, setAnneesDisponibles] = useState([]);
  const [donnees, setDonnees] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreurChargement, setErreurChargement] = useState(false);

  // Pas d'endpoint dedie pour les annees disponibles : derivees d'un premier
  // chargement non filtre, une seule fois (meme logique que fonctionsEligibles
  // sur GrillesListPage, charge separement des donnees filtrees elles-memes).
  useEffect(() => {
    apiClient.get('/processus').then(({ data }) => {
      const annees = [...new Set(data.map((processus) => processus.anneePaiement))].sort((a, b) => b - a);
      setAnneesDisponibles(annees);
    });
  }, []);

  useEffect(() => {
    let annule = false;
    setChargement(true);
    setErreurChargement(false);
    apiClient
      .get('/processus', {
        params: {
          statut: statut || undefined,
          annee: annee || undefined,
        },
      })
      .then(({ data }) => {
        if (!annule) setDonnees(data);
      })
      .catch(() => {
        // Sans ce catch, un echec reseau retombait sur une liste vide, donc sur
        // le meme "Aucun resultat." qu'une base reellement vide.
        if (!annule) {
          setDonnees([]);
          setErreurChargement(true);
        }
      })
      .finally(() => {
        if (!annule) setChargement(false);
      });
    return () => {
      annule = true;
    };
  }, [statut, annee]);

  // Sprint D.3 -- "aucune donnee" et "aucun resultat pour ce filtre" sont deux
  // situations differentes : la premiere decrit l'etat du systeme, la seconde
  // celui de la recherche en cours. Les confondre laisse croire que rien n'a
  // jamais ete declenche.
  const filtresActifs = Boolean(statut || annee);
  const effacerFiltres = () =>
    setSearchParams((precedent) => effacerParametres(precedent, 'statut', 'annee'));

  return (
    <>
      <PageHeader surTitre="Workflow" titre="Processus mensuel" />
      <div className="flex flex-col gap-6 p-8">
        {erreurChargement && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" aria-hidden="true" />
            <AlertDescription>
              Impossible de charger les processus mensuels. Vérifiez votre connexion, puis réessayez.
            </AlertDescription>
          </Alert>
        )}
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div className="flex flex-wrap items-end gap-4">
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
                {Object.entries(STATUTS_PROCESSUS).map(([valeur, { libelle }]) => (
                  <option key={valeur} value={valeur}>
                    {libelle}
                  </option>
                ))}
              </Select>
            </div>

            <div className="flex w-36 flex-col gap-1.5">
              <Label htmlFor="filtre-annee">Année</Label>
              <Select
                id="filtre-annee"
                value={annee}
                onChange={(e) =>
                  setSearchParams((precedent) => definirParametre(precedent, 'annee', e.target.value))
                }
              >
                <option value="">Toutes</option>
                {anneesDisponibles.map((a) => (
                  <option key={a} value={a}>
                    {a}
                  </option>
                ))}
              </Select>
            </div>
          </div>

          {user?.role === 'ARH' && (
            <Button
              onClick={() =>
                navigate('/processus/declencher', {
                  state: { retour: construireRetour('/processus', searchParams) },
                })
              }
            >
              <Plus className="h-4 w-4" aria-hidden="true" />
              Déclencher un processus
            </Button>
          )}
        </div>

        <DataTable
          colonnes={colonnes}
          donnees={donnees}
          cleLigne={(processus) => processus.id}
          chargement={chargement}
          messageVide={
            erreurChargement ? (
              'Les processus mensuels n’ont pas pu être chargés.'
            ) : filtresActifs ? (
              <MessageListeVide
                message="Aucun processus ne correspond à ces filtres."
                onEffacerFiltres={effacerFiltres}
              />
            ) : (
              'Aucun processus mensuel n’a encore été déclenché.'
            )
          }
          onLigneClick={(processus) =>
            navigate(`/processus/${processus.id}`, {
              state: { retour: construireRetour('/processus', searchParams) },
            })
          }
        />
      </div>
    </>
  );
}
