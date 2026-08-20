import { useEffect, useId, useMemo, useState } from 'react';
import { AlertTriangle, Eye } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { DataTable } from '../../components/ui/DataTable';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { Label } from '../../components/ui/Label';
import { MessageListeVide } from '../../components/ui/MessageListeVide';
import { Select } from '../../components/ui/Select';
import { Input } from '../../components/ui/Input';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../../components/ui/Card';
import { useFocusTrap } from '../../hooks/useFocusTrap';
import { formatDateHeure } from '../../utils/formatters';

const TAILLE_PAGE = 20;

const ENTITES_CIBLES = ['beneficiaires', 'fonction_eligible', 'grille_tarifaire', 'processus_mensuel', 'utilisateurs'];

function DetailAuditModal({ action, detailJson, onFermer }) {
  // Derniere modale du projet a ne pas avoir le piege de focus de D.2.
  const titreId = useId();
  const containerRef = useFocusTrap(onFermer);
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
    <div
      ref={containerRef}
      role="dialog"
      aria-modal="true"
      aria-labelledby={titreId}
      tabIndex={-1}
      className="fixed inset-0 z-50 flex items-center justify-center overscroll-contain bg-black/40 p-4"
      onClick={(event) => {
        if (event.target === event.currentTarget) onFermer?.();
      }}
    >
      <Card className="max-h-[calc(100vh-2rem)] w-full max-w-lg overflow-y-auto">
        <CardHeader>
          <CardTitle id={titreId}>Détail de l'action</CardTitle>
        </CardHeader>
        <CardContent>
          {delta ? (
            /* En-tete aligne sur DataTable (neutral-700) : c'etait le seul
               tableau du projet a utiliser neutral-500, sous le seuil AA. */
            <div className="overflow-hidden rounded-lg border border-neutral-200">
              <table className="w-full text-left text-sm">
                <thead className="bg-neutral-100 text-xs uppercase text-neutral-700">
                  <tr>
                    <th scope="col" className="px-4 py-2 font-medium">Champ</th>
                    <th scope="col" className="px-4 py-2 font-medium">Avant</th>
                    <th scope="col" className="px-4 py-2 font-medium">Après</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-neutral-200">
                  {cles.map((cle) => (
                    <tr key={cle}>
                      <td className="px-4 py-2 font-medium text-neutral-700">{cle}</td>
                      {/* L'ancienne valeur reste lisible (neutral-600, pas
                          neutral-500) : c'est la moitie de l'information. */}
                      <td className="px-4 py-2 text-neutral-600">{String(delta.avant[cle] ?? '—')}</td>
                      <td className="px-4 py-2 font-medium text-neutral-900">
                        {String(delta.apres[cle] ?? '—')}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-sm italic text-neutral-600">
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
  const [erreurChargement, setErreurChargement] = useState(false);
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
    setErreurChargement(false);
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
      .catch(() => {
        if (annule) return;
        setLignes([]);
        setTotal(0);
        setErreurChargement(true);
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
      // Le tri est fixe cote backend (Sort.Direction.DESC sur dateAction) et
      // n'est pas reglable ici : l'en-tete le dit plutot que de laisser
      // l'utilisateur le deduire.
      entete: 'Date (plus récent d’abord)',
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
          <Eye className="h-4 w-4" aria-hidden="true" />
          Voir
        </Button>
      ),
    },
  ];

  const pagination = useMemo(
    () => ({ page, taille: TAILLE_PAGE, total, onChangerPage: setPage }),
    [page, total]
  );

  // Filtres locaux (cet ecran n'a jamais fait partie de la persistance par
  // query params de MM.9) : on remet simplement les cinq etats a vide.
  const filtresActifs = Boolean(idUtilisateur || action || entiteCible || dateDebut || dateFin);
  const effacerFiltres = () => {
    setIdUtilisateur('');
    setAction('');
    setEntiteCible('');
    setDateDebut('');
    setDateFin('');
  };

  return (
    <>
      <PageHeader surTitre="DRH" titre="Journal d'audit" />
      <div className="flex flex-col gap-6 p-8">
        {erreurChargement && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" aria-hidden="true" />
            <AlertDescription>
              Impossible de charger le journal d'audit. Vérifiez votre connexion, puis réessayez.
            </AlertDescription>
          </Alert>
        )}

        {/* Sprint D.3 -- cinq filtres alignes d'un seul tenant se balayaient
            mal. Ils sont maintenant lus en deux temps : QUI a fait QUOI, puis
            QUAND. Aucun comportement de filtre n'est modifie. */}
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

          <div className="mx-1 hidden h-10 w-px self-end bg-neutral-200 lg:block" aria-hidden="true" />

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
          messageVide={
            erreurChargement ? (
              'Le journal d’audit n’a pas pu être chargé.'
            ) : filtresActifs ? (
              <MessageListeVide
                message="Aucune action ne correspond à ces filtres."
                onEffacerFiltres={effacerFiltres}
              />
            ) : (
              'Aucune action enregistrée dans le journal.'
            )
          }
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