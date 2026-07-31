import { useEffect, useState } from 'react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { DataTable } from '../../components/ui/DataTable';
import { Button } from '../../components/ui/Button';
import { formatMontantFCFA, formatDate } from '../../utils/formatters';
import { ValiderGrilleModal } from '../grilles/ValiderGrilleModal';

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
  const [donnees, setDonnees] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [rafraichissement, setRafraichissement] = useState(0);
  const [grilleADecider, setGrilleADecider] = useState(null);

  useEffect(() => {
    let annule = false;
    setChargement(true);
    apiClient
      .get('/grilles-tarifaires/en-attente-drh')
      .then(({ data }) => {
        if (!annule) setDonnees(data.contenu);
      })
      .finally(() => {
        if (!annule) setChargement(false);
      });
    return () => {
      annule = true;
    };
  }, [rafraichissement]);

  return (
    <>
      <PageHeader surTitre="DRH" titre="Grilles tarifaires à valider" />
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