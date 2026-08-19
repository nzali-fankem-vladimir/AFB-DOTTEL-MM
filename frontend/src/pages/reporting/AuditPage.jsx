import { useEffect, useMemo, useState } from 'react';
import { Eye } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { DataTable } from '../../components/ui/DataTable';
import { Button } from '../../components/ui/Button';
import { Label } from '../../components/ui/Label';
import { Select } from '../../components/ui/Select';
import { Input } from '../../components/ui/Input';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../../components/ui/Card';
import { formatDateHeure } from '../../utils/formatters';

const TAILLE_PAGE = 20;

const ENTITES_CIBLES = ['beneficiaires', 'fonction_eligible', 'grille_tarifaire', 'processus_mensuel', 'utilisateurs'];

function DetailAuditModal({ action, detailJson, onFermer }) {
  let delta = null;
  try {
    const parse = JSON.parse(detailJson ?? '');
    if (parse && typeof parse === 'object' && (parse.avant || parse.apres)) {
      delta = { avant: parse.avant ?? {}, apres: parse.apres ?? {} };
    }
  } catch {
    delta = null;
  }

  const cles = delta ? [...new Set([...Object.keys(delta.avant), ...Object.keys(delta.apres)])] : [];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <Card className="w-full max-w-lg">
        <CardHeader>
          <CardTitle>Détail de l'action</CardTitle>
        </CardHeader>
        <CardContent>
          {delta ? (
            <div className="overflow-hidden rounded-lg border border-neutral-200">
              <table className="w-full text-left text-sm">
                <thead className="bg-neutral-100 text-xs uppercase text-neutral-500">
                  <tr>
                    <th className="px-4 py-2 font-medium">Champ</th>
                    <th className="px-4 py-2 font-medium">Avant</th>
                    <th className="px-4 py-2 font-medium">Après</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-neutral-200">
                  {cles.map((cle) => (
                    <tr key={cle}>
                      <td className="px-4 py-2 font-medium text-neutral-700">{cle}</td>
                      <td className="px-4 py-2 text-neutral-500">{String(delta.avant[cle] ?? '—')}</td>
                      <td className="px-4 py-2 text-neutral-900">{String(delta.apres[cle] ?? '—')}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-sm italic text-neutral-500">
              Aucun détail avant/après pour l'action {action}.
            </p>
          )}
        </CardContent>
        <CardFooter>
          <Button variant="outline" onClick={onFermer}>
            Fermer
          </Button>
        </CardFooter>
      </Card>
    </div>
  );
}

export default function AuditPage() {
  const [idUtilisateur, setIdUtilisateur] = useState('');
  const [action, setAction] = useState('');
  const [entiteCible, setEntiteCible] = useState('');
  const [dateDebut, setDateDebut] = useState('');
  const [dateFin, setDateFin] = useState('');
  const [page, setPage] = useState(0);

  const [utilisateurs, setUtilisateurs] = useState([]);
  const [actions, setActions] = useState([]);
  const [lignes, setLignes] = useState([]);
  const [total, setTotal] = useState(0);
  const [chargement, setChargement] = useState(true);
  const [ligneDetail, setLigneDetail] = useState(null);

  useEffect(() => {
    apiClient.get('/admin/utilisateurs').then(({ data }) => setUtilisateurs(data.contenu));
    // Sprint MM.11 : liste derivee des actions reellement presentes en base
    // (GET /reporting/audit/actions) plutot qu'une liste figee cote frontend,
    // qui se desynchronisait a chaque nouvelle action ajoutee par un service.
    apiClient.get('/reporting/audit/actions').then(({ data }) => setActions(data));
  }, []);

  const nomsUtilisateurs = useMemo(
    () => Object.fromEntries(utilisateurs.map((u) => [u.id, u.nomPrenoms])),
    [utilisateurs]
  );

  useEffect(() => setPage(0), [idUtilisateur, action, entiteCible, dateDebut, dateFin]);

  useEffect(() => {
    let annule = false;
    setChargement(true);
    apiClient
      .get('/reporting/audit', {
        params: {
          idUtilisateur: idUtilisateur || undefined,
          action: action || undefined,
          entiteCible: entiteCible || undefined,
          dateDebut: dateDebut || undefined,
          dateFin: dateFin || undefined,
          page,
          taille: TAILLE_PAGE,
        },
      })
      .then(({ data }) => {
        if (!annule) {
          setLignes(data.contenu);
          setTotal(data.total);
        }
      })
      .finally(() => {
        if (!annule) setChargement(false);
      });
    return () => {
      annule = true;
    };
  }, [idUtilisateur, action, entiteCible, dateDebut, dateFin, page]);

  const colonnes = [
    {
      cle: 'dateAction',
      entete: 'Date',
      className: 'tabular-nums',
      rendu: (ligne) => formatDateHeure(ligne.dateAction),
    },
    {
      cle: 'idUtilisateur',
      entete: 'Utilisateur',
      rendu: (ligne) => nomsUtilisateurs[ligne.idUtilisateur] ?? `#${ligne.idUtilisateur}`,
    },
    { cle: 'action', entete: 'Action' },
    { cle: 'entiteCible', entete: 'Entité cible' },
    {
      cle: 'detail',
      entete: 'Détail',
      rendu: (ligne) => (
        <Button variant="ghost" size="sm" onClick={() => setLigneDetail(ligne)}>
          <Eye className="h-4 w-4" />
          Voir
        </Button>
      ),
    },
  ];

  const pagination = useMemo(
    () => ({ page, taille: TAILLE_PAGE, total, onChangerPage: setPage }),
    [page, total]
  );

  return (
    <>
      <PageHeader surTitre="DRH" titre="Journal d'audit" />
      <div className="flex flex-col gap-6 p-8">
        <div className="flex flex-wrap items-end gap-4">
          <div className="flex w-56 flex-col gap-1.5">
            <Label htmlFor="filtre-utilisateur">Utilisateur</Label>
            <Select id="filtre-utilisateur" value={idUtilisateur} onChange={(e) => setIdUtilisateur(e.target.value)}>
              <option value="">Tous</option>
              {utilisateurs.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.nomPrenoms}
                </option>
              ))}
            </Select>
          </div>

          <div className="flex w-64 flex-col gap-1.5">
            <Label htmlFor="filtre-action">Action</Label>
            <Select id="filtre-action" value={action} onChange={(e) => setAction(e.target.value)}>
              <option value="">Toutes</option>
              {actions.map((a) => (
                <option key={a} value={a}>
                  {a}
                </option>
              ))}
            </Select>
          </div>

          <div className="flex w-48 flex-col gap-1.5">
            <Label htmlFor="filtre-entite">Entité cible</Label>
            <Select id="filtre-entite" value={entiteCible} onChange={(e) => setEntiteCible(e.target.value)}>
              <option value="">Toutes</option>
              {ENTITES_CIBLES.map((e) => (
                <option key={e} value={e}>
                  {e}
                </option>
              ))}
            </Select>
          </div>

          <div className="flex w-56 flex-col gap-1.5">
            <Label htmlFor="filtre-date-debut">Période — début</Label>
            <Input
              id="filtre-date-debut"
              type="datetime-local"
              value={dateDebut}
              onChange={(e) => setDateDebut(e.target.value)}
            />
          </div>

          <div className="flex w-56 flex-col gap-1.5">
            <Label htmlFor="filtre-date-fin">Période — fin</Label>
            <Input
              id="filtre-date-fin"
              type="datetime-local"
              value={dateFin}
              onChange={(e) => setDateFin(e.target.value)}
            />
          </div>
        </div>

        <DataTable
          colonnes={colonnes}
          donnees={lignes}
          cleLigne={(ligne) => ligne.id}
          chargement={chargement}
          pagination={pagination}
        />
      </div>

      {ligneDetail && (
        <DetailAuditModal
          action={ligneDetail.action}
          detailJson={ligneDetail.detailJson}
          onFermer={() => setLigneDetail(null)}
        />
      )}
    </>
  );
}