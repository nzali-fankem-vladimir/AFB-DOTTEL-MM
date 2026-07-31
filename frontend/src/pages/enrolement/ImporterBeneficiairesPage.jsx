import { useRef, useState } from 'react';
import { FileSpreadsheet, Upload, X } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { cn } from '../../utils/cn';

function formaterTaille(octets) {
  if (octets < 1024) return `${octets} o`;
  if (octets < 1024 * 1024) return `${(octets / 1024).toFixed(1)} Ko`;
  return `${(octets / (1024 * 1024)).toFixed(1)} Mo`;
}

export default function ImporterBeneficiairesPage() {
  const inputRef = useRef(null);
  const [fichier, setFichier] = useState(null);
  const [rapport, setRapport] = useState(null);
  const [erreur, setErreur] = useState(null);
  const [enCours, setEnCours] = useState(false);
  const [survole, setSurvole] = useState(false);

  const selectionnerFichier = (nouveauFichier) => {
    setRapport(null);
    setErreur(null);
    setFichier(nouveauFichier ?? null);
  };

  const choisirFichier = (event) => {
    selectionnerFichier(event.target.files?.[0]);
  };

  const surDepot = (event) => {
    event.preventDefault();
    setSurvole(false);
    selectionnerFichier(event.dataTransfer.files?.[0]);
  };

  const annulerSelection = () => {
    setFichier(null);
    if (inputRef.current) inputRef.current.value = '';
  };

  const importer = async () => {
    if (!fichier) return;
    setEnCours(true);
    setErreur(null);
    setRapport(null);
    try {
      const formData = new FormData();
      formData.append('fichier', fichier);
      // Pas de Content-Type manuel : le navigateur doit fixer lui-meme le
      // boundary multipart. Un header explicite sans boundary casse la requete.
      const { data } = await apiClient.post('/beneficiaires/import', formData);
      setRapport(data);
      setFichier(null);
      if (inputRef.current) inputRef.current.value = '';
    } catch (err) {
      const statut = err.response?.status;
      if (statut === 400) {
        setErreur('Fichier illisible ou format invalide. Vérifiez qu’il s’agit bien d’un .xlsx.');
      } else {
        setErreur('Une erreur est survenue lors de l’import. Veuillez réessayer.');
      }
    } finally {
      setEnCours(false);
    }
  };

  return (
    <>
      <PageHeader surTitre="ARH" titre="Importer des bénéficiaires" />
      <div className="mx-auto max-w-3xl p-8">
        <Card>
          <CardHeader>
            <CardTitle>Import en masse (Excel)</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-4">
            {erreur && (
              <Alert variant="destructive">
                <AlertDescription>{erreur}</AlertDescription>
              </Alert>
            )}

            {fichier ? (
              <div className="flex items-center gap-3 rounded border border-neutral-200 bg-neutral-50 px-4 py-3">
                <FileSpreadsheet className="h-8 w-8 shrink-0 text-primary-500" />
                <div className="flex min-w-0 flex-1 flex-col">
                  <span className="truncate text-sm font-medium text-neutral-900">
                    {fichier.name}
                  </span>
                  <span className="text-xs text-neutral-500">{formaterTaille(fichier.size)}</span>
                </div>
                <button
                  type="button"
                  onClick={annulerSelection}
                  aria-label="Annuler la sélection"
                  className="rounded p-1.5 text-neutral-400 hover:bg-neutral-200 hover:text-neutral-700"
                >
                  <X className="h-4 w-4" />
                </button>
              </div>
            ) : (
              <label
                htmlFor="fichier-import"
                onDragOver={(event) => {
                  event.preventDefault();
                  setSurvole(true);
                }}
                onDragLeave={() => setSurvole(false)}
                onDrop={surDepot}
                className={cn(
                  'flex cursor-pointer flex-col items-center justify-center gap-2 rounded border-2 border-dashed border-neutral-300 bg-neutral-50 px-6 py-10 text-center hover:border-primary-500',
                  survole && 'border-primary-500 bg-primary-50'
                )}
              >
                <Upload className="h-6 w-6 text-neutral-400" />
                <span className="text-sm font-medium text-neutral-700">
                  Cliquez pour choisir un fichier ou glissez-déposez-le ici
                </span>
                <span className="text-xs text-neutral-500">Format .xlsx, un seul fichier à la fois</span>
                <input
                  id="fichier-import"
                  ref={inputRef}
                  type="file"
                  accept=".xlsx"
                  className="hidden"
                  onChange={choisirFichier}
                />
              </label>
            )}

            {fichier && (
              <Button onClick={importer} disabled={enCours} className="self-start">
                {enCours ? 'Import en cours…' : 'Importer'}
              </Button>
            )}

            {rapport && (
              <div className="flex flex-col gap-4 border-t border-neutral-200 pt-4">
                <p className="text-sm">
                  <span className="font-semibold text-neutral-900">{rapport.inseres}</span>{' '}
                  <span className="text-neutral-500">insérés</span>
                  {' · '}
                  <span className="font-semibold text-neutral-900">{rapport.rejetes}</span>{' '}
                  <span className="text-neutral-500">rejetés</span>
                </p>

                {rapport.erreurs?.length > 0 && (
                  <div className="overflow-hidden rounded border border-neutral-200">
                    <table className="w-full text-left text-sm">
                      <thead className="bg-neutral-100 text-xs uppercase text-neutral-500">
                        <tr>
                          <th className="px-4 py-2">Ligne</th>
                          <th className="px-4 py-2">Matricule</th>
                          <th className="px-4 py-2">Motif</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-neutral-200">
                        {rapport.erreurs.map((erreurLigne) => (
                          <tr key={`${erreurLigne.ligne}-${erreurLigne.matricule}`}>
                            <td className="px-4 py-2 text-neutral-700">{erreurLigne.ligne}</td>
                            <td className="px-4 py-2 text-neutral-700">{erreurLigne.matricule}</td>
                            <td className="px-4 py-2 text-neutral-700">{erreurLigne.motif}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </>
  );
}