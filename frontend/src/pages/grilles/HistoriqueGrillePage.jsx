import { useEffect, useState } from 'react';
import { useLocation, useParams } from 'react-router-dom';
import { AlertTriangle, MessageSquareWarning } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { DataTable } from '../../components/ui/DataTable';
import { Alert, AlertDescription } from '../../components/ui/Alert';
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
    // Tri fixe cote backend (findByIdFonctionEligibleOrderByDateDebutDesc).
    entete: 'Période (plus récent d’abord)',
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
  const [erreurChargement, setErreurChargement] = useState(false);
  const [grilleMotifAVoir, setGrilleMotifAVoir] = useState(null);

  useEffect(() => {
    let annule = false;
    setChargement(true);
    setErreurChargement(false);
    apiClient
      .get(`/grilles-tarifaires/fonction/${code}`)
      .then(({ data }) => {
        if (annule) return;
        setLibelleFonction(data.libelleFonction);
        setDonnees(data.grilles);
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
  }, [code]);

  return (
    <>
      <LienRetour to={retour} label="Retour aux grilles tarifaires" />
      <PageHeader surTitre="Historique" titre={libelleFonction || code} />
      <div className="flex flex-col gap-6 p-8">
        {erreurChargement && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" />
            <AlertDescription>
              Impossible de charger l'historique de cette grille. Vérifiez votre connexion, puis réessayez.
            </AlertDescription>
          </Alert>
        )}

        <DataTable
          colonnes={colonnes}
          donnees={donnees}
          cleLigne={(grille) => grille.id}
          chargement={chargement}
          /* Pas de filtre sur cet ecran : un seul message vide possible. */
          messageVide={
            erreurChargement
              ? 'L’historique n’a pas pu être chargé.'
              : 'Aucune grille tarifaire pour cette fonction.'
          }
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
