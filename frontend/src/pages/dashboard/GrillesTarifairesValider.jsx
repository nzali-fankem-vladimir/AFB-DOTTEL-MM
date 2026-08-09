import { useEffect, useState } from 'react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { DataTable } from '../../components/ui/DataTable';
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
    rendu: (grille) => formatMontantFCFA(grille.montantFcfa),
  },
  {
    cle: 'dateDebut',
    entete: 'Date début',
    rendu: (grille) => formatDate(grille.dateDebut),
  },
];

export default function GrillesTarifairesValider() {
  const { user } = useAuth();
  const [donnees, setDonnees] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [rafraichissement, setRafraichissement] = useState(0);
  const [grilleADecider, setGrilleADecider] = useState(null);

  const endpoint = ENDPOINT_PAR_ROLE[user?.role];

  useEffect(() => {
    if (!endpoint) return undefined;
    let annule = false;
    setChargement(true);
    apiClient
      .get(endpoint)
      .then(({ data }) => {
        if (!annule) setDonnees(data.contenu);
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
        <DataTable
          colonnes={colonnes}
          donnees={donnees}
          cleLigne={(grille) => grille.id}
          chargement={chargement}
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