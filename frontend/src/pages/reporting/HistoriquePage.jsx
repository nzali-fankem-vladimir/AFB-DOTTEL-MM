import { useEffect, useState } from 'react';
import { AlertTriangle, Download } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { DataTable } from '../../components/ui/DataTable';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Label } from '../../components/ui/Label';
import { MessageListeVide } from '../../components/ui/MessageListeVide';
import { Select } from '../../components/ui/Select';
import { formatMontantFCFA, getPeriodeLabel } from '../../utils/formatters';
import { getStatutProcessusInfo } from '../../utils/statutProcessus';

const colonnes = [
  {
    cle: 'periode',
    // Tri fixe cote backend (...OrderByAnneePaiementDescMoisPaiementDesc) : on
    // le dit plutot que de laisser l'utilisateur le supposer.
    entete: 'Mois / Année (plus récent d’abord)',
    rendu: (ligne) => getPeriodeLabel(ligne.moisPaiement, ligne.anneePaiement),
  },
  {
    cle: 'statut',
    entete: 'Statut',
    rendu: (ligne) => {
      const { libelle, variant } = getStatutProcessusInfo(ligne.statut);
      return <Badge variant={variant}>{libelle}</Badge>;
    },
  },
  {
    cle: 'nombreBeneficiaires',
    entete: 'Bénéficiaires',
    className: 'tabular-nums',
    rendu: (ligne) => ligne.nombreBeneficiaires,
  },
  {
    cle: 'montantTotal',
    entete: 'Montant total',
    className: 'tabular-nums',
    rendu: (ligne) => formatMontantFCFA(ligne.montantTotal),
  },
];

export default function HistoriquePage() {
  const [annee, setAnnee] = useState('');
  const [anneesDisponibles, setAnneesDisponibles] = useState([]);
  const [lignes, setLignes] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreurChargement, setErreurChargement] = useState(false);
  const [exportEnCours, setExportEnCours] = useState(false);

  // Pas d'endpoint dedie pour les annees disponibles : derivees d'un premier
  // chargement non filtre, meme logique que ProcessusListPage.
  useEffect(() => {
    apiClient.get('/reporting/historique').then(({ data }) => {
      const annees = [...new Set(data.lignes.map((ligne) => ligne.anneePaiement))].sort((a, b) => b - a);
      setAnneesDisponibles(annees);
    });
  }, []);

  useEffect(() => {
    let annule = false;
    setChargement(true);
    setErreurChargement(false);
    apiClient
      .get('/reporting/historique', { params: { annee: annee || undefined } })
      .then(({ data }) => {
        if (!annule) setLignes(data.lignes);
      })
      .catch(() => {
        if (annule) return;
        setLignes([]);
        setErreurChargement(true);
      })
      .finally(() => {
        if (!annule) setChargement(false);
      });
    return () => {
      annule = true;
    };
  }, [annee]);

  const exporter = async () => {
    setExportEnCours(true);
    try {
      const reponse = await apiClient.get('/reporting/historique/export', {
        params: { annee: annee || undefined },
        responseType: 'blob',
      });

      const entete = reponse.headers['content-disposition'];
      const correspondance = entete?.match(/filename="?([^"]+)"?/);
      const nomFichier = correspondance?.[1] ?? 'historique-processus.xlsx';

      const url = window.URL.createObjectURL(new Blob([reponse.data]));
      const lien = document.createElement('a');
      lien.href = url;
      lien.download = nomFichier;
      document.body.appendChild(lien);
      lien.click();
      lien.remove();
      window.URL.revokeObjectURL(url);
    } finally {
      setExportEnCours(false);
    }
  };

  return (
    <>
      <PageHeader surTitre="DRH" titre="Historique annuel" />
      <div className="flex flex-col gap-6 p-8">
        {erreurChargement && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" aria-hidden="true" />
            <AlertDescription>
              Impossible de charger l'historique. Vérifiez votre connexion, puis réessayez.
            </AlertDescription>
          </Alert>
        )}

        <div className="flex flex-wrap items-end justify-between gap-4">
          <div className="flex w-36 flex-col gap-1.5">
            <Label htmlFor="filtre-annee">Année</Label>
            <Select id="filtre-annee" value={annee} onChange={(e) => setAnnee(e.target.value)}>
              <option value="">Toutes</option>
              {anneesDisponibles.map((a) => (
                <option key={a} value={a}>
                  {a}
                </option>
              ))}
            </Select>
          </div>

          <Button variant="outline" onClick={exporter} isLoading={exportEnCours}>
            {!exportEnCours && <Download className="h-4 w-4" aria-hidden="true" />}
            {exportEnCours ? 'Export en cours…' : 'Exporter (Excel)'}
          </Button>
        </div>

        <DataTable
          colonnes={colonnes}
          donnees={lignes}
          cleLigne={(ligne) => `${ligne.anneePaiement}-${ligne.moisPaiement}`}
          chargement={chargement}
          messageVide={
            erreurChargement ? (
              'L’historique n’a pas pu être chargé.'
            ) : annee ? (
              <MessageListeVide
                message={`Aucun processus clôturé pour l'année ${annee}.`}
                onEffacerFiltres={() => setAnnee('')}
              />
            ) : (
              'Aucun processus mensuel clôturé pour le moment.'
            )
          }
        />
      </div>
    </>
  );
}