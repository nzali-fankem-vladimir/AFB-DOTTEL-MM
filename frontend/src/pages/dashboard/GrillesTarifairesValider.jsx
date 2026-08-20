import { useEffect, useState } from 'react';
import { AlertTriangle } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { DataTable } from '../../components/ui/DataTable';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../contexts/AuthContext';
import { formatMontantFCFA, formatDate } from '../../utils/formatters';
import { ValiderGrilleModal } from '../grilles/ValiderGrilleModal';

// Sprint MM.12 : un SEUL ecran pour les deux etages du workflow des grilles,
// parametre par le role -- plutot qu'une page CRH dupliquee de la page DRH.
// Cote backend la decision passe deja par un endpoint unique
// (POST /grilles-tarifaires/{id}/valider, hasAnyRole CRH/DRH) : dupliquer ici
// ferait diverger deux ecrans qui n'ont rien de different a montrer.
const ENDPOINT_PAR_ROLE = {
  CRH: '/grilles-tarifaires/en-attente-crh',
  DRH: '/grilles-tarifaires/en-attente-drh',
};

const colonnes = [
  { cle: 'libelleFonction', entete: 'Fonction' },
  {
    cle: 'montantFcfa',
    entete: 'Montant proposé',
    className: 'tabular-nums',
    rendu: (grille) => formatMontantFCFA(grille.montantFcfa),
  },
  {
    cle: 'dateDebut',
    entete: 'Date début',
    className: 'tabular-nums',
    rendu: (grille) => formatDate(grille.dateDebut),
  },
];

export default function GrillesTarifairesValider() {
  const { user } = useAuth();
  const [donnees, setDonnees] = useState([]);
  const [chargement, setChargement] = useState(true);
  // Sprint D.3 : l'appel n'avait AUCUN .catch(). Un echec reseau retombait sur
  // une liste vide, donc sur "Aucun resultat." -- le CRH ou la DRH en
  // concluait qu'il n'y avait rien a valider, alors que des grilles
  // attendaient peut-etre leur signature. C'est l'echec silencieux le plus
  // couteux du module.
  const [erreurChargement, setErreurChargement] = useState(false);
  const [rafraichissement, setRafraichissement] = useState(0);
  const [grilleADecider, setGrilleADecider] = useState(null);

  const endpoint = ENDPOINT_PAR_ROLE[user?.role];

  useEffect(() => {
    if (!endpoint) return undefined;
    let annule = false;
    setChargement(true);
    setErreurChargement(false);
    apiClient
      .get(endpoint)
      .then(({ data }) => {
        if (!annule) setDonnees(data.contenu);
      })
      .catch(() => {
        if (annule) return;
        setDonnees([]);
        setErreurChargement(true);
      })
      .finally(() => {
        if (!annule) setChargement(false);
      });
    return () => {
      annule = true;
    };
  }, [endpoint, rafraichissement]);

  return (
    <>
      <PageHeader surTitre={user?.role} titre="Grilles tarifaires à valider" />
      <div className="flex flex-col gap-6 p-8">
        {erreurChargement && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" aria-hidden="true" />
            <AlertDescription>
              Impossible de charger les grilles à valider. <strong>Ne concluez pas qu'il n'y en a
              aucune</strong> : vérifiez votre connexion, puis réessayez.
            </AlertDescription>
          </Alert>
        )}

        <DataTable
          colonnes={colonnes}
          donnees={donnees}
          cleLigne={(grille) => grille.id}
          chargement={chargement}
          messageVide={
            erreurChargement
              ? 'La liste n’a pas pu être chargée.'
              : 'Aucune grille tarifaire n’attend votre décision.'
          }
          actions={(grille) => (
            <Button size="sm" onClick={() => setGrilleADecider(grille)}>
              Valider / Rejeter
            </Button>
          )}
        />
      </div>

      {grilleADecider && (
        <ValiderGrilleModal
          grille={grilleADecider}
          onFerme={() => setGrilleADecider(null)}
          onSucces={() => {
            setGrilleADecider(null);
            setRafraichissement((n) => n + 1);
          }}
        />
      )}
    </>
  );
}