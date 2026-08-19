import { useEffect, useState } from 'react';
import { Download } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { DataTable } from '../../components/ui/DataTable';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Label } from '../../components/ui/Label';
import { Select } from '../../components/ui/Select';
import { formatMontantFCFA, getPeriodeLabel } from '../../utils/formatters';
import { getStatutProcessusInfo } from '../../utils/statutProcessus';

const colonnes = [
  {
    cle: 'periode',
    entete: 'Mois / Année',
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
    apiClient
      .get('/reporting/historique', { params: { annee: annee || undefined } })
      .then(({ data }) => {
        if (!annule) setLignes(data.lignes);
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

          <Button variant="outline" onClick={exporter} disabled={exportEnCours}>
            <Download className="h-4 w-4" />
            {exportEnCours ? 'Export en cours…' : 'Exporter (Excel)'}
          </Button>
        </div>

        <DataTable colonnes={colonnes} donnees={lignes} cleLigne={(ligne) => `${ligne.anneePaiement}-${ligne.moisPaiement}`} chargement={chargement} />
      </div>
    </>
  );
}