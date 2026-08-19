import { useEffect, useState } from 'react';
import { useLocation, useParams } from 'react-router-dom';
import { MessageSquareWarning } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { DataTable } from '../../components/ui/DataTable';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { LienRetour } from '../../components/ui/LienRetour';
import { VoirMotifModal } from '../../components/ui/VoirMotifModal';
import { formatMontantFCFA, formatDate } from '../../utils/formatters';
import { getStatutGrilleInfo } from '../../utils/statutGrille';

const colonnes = [
  {
    cle: 'montantFcfa',
    entete: 'Montant',
    className: 'tabular-nums',
    rendu: (grille) => formatMontantFCFA(grille.montantFcfa),
  },
  {
    cle: 'periode',
    entete: 'Période',
    className: 'tabular-nums',
    rendu: (grille) =>
      `${formatDate(grille.dateDebut)} — ${grille.dateFin ? formatDate(grille.dateFin) : 'en cours'}`,
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

export default function HistoriqueGrillePage() {
  const { code } = useParams();
  const location = useLocation();
  const retour = location.state?.retour ?? '/grilles-tarifaires';
  const [libelleFonction, setLibelleFonction] = useState('');
  const [donnees, setDonnees] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [grilleMotifAVoir, setGrilleMotifAVoir] = useState(null);

  useEffect(() => {
    let annule = false;
    setChargement(true);
    apiClient
      .get(`/grilles-tarifaires/fonction/${code}`)
      .then(({ data }) => {
        if (annule) return;
        setLibelleFonction(data.libelleFonction);
        setDonnees(data.grilles);
      })
      .finally(() => {
        if (!annule) setChargement(false);
      });
    return () => {
      annule = true;
    };
  }, [code]);

  return (
    <>
      <LienRetour to={retour} label="Retour aux grilles tarifaires" />
      <PageHeader surTitre="Historique" titre={libelleFonction || code} />
      <div className="flex flex-col gap-6 p-8">
        <DataTable
          colonnes={colonnes}
          donnees={donnees}
          cleLigne={(grille) => grille.id}
          chargement={chargement}
          actions={(grille) =>
            grille.statutValidation === 'REJETEE' ? (
              <Button variant="outline" size="sm" onClick={() => setGrilleMotifAVoir(grille)}>
                <MessageSquareWarning className="h-4 w-4" />
                Voir le motif
              </Button>
            ) : null
          }
        />
      </div>

      {grilleMotifAVoir && (
        <VoirMotifModal
          titre={`Motif de rejet — ${libelleFonction || code}`}
          motif={grilleMotifAVoir.motifRejet}
          onFermer={() => setGrilleMotifAVoir(null)}
        />
      )}
    </>
  );
}
