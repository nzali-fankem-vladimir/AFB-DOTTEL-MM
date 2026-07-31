import { useEffect, useMemo, useState } from 'react';
import { AlertTriangle, Check, X } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../../components/ui/Card';
import { Checkbox } from '../../components/ui/Checkbox';
import { Select } from '../../components/ui/Select';
import { Button } from '../../components/ui/Button';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { cn } from '../../utils/cn';

export function AjusterLignesModal({ idProcessus, lignes, onFerme, onSucces }) {
  const [fonctionsEligibles, setFonctionsEligibles] = useState([]);
  const [valeurs, setValeurs] = useState(() =>
    Object.fromEntries(
      lignes.map((ligne) => [ligne.idBeneficiaire, {
        inclusDansEtat: ligne.inclusDansEtat,
        fonctionRetenue: ligne.fonctionRetenue,
      }])
    )
  );
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState(null);
  const [resultats, setResultats] = useState(null);

  useEffect(() => {
    apiClient.get('/fonctions-eligibles').then(({ data }) => setFonctionsEligibles(data));
  }, []);

  const lignesParId = useMemo(
    () => Object.fromEntries(lignes.map((ligne) => [ligne.idBeneficiaire, ligne])),
    [lignes]
  );

  const modifierInclusion = (idBeneficiaire, inclusDansEtat) => {
    setValeurs((precedent) => ({
      ...precedent,
      [idBeneficiaire]: { ...precedent[idBeneficiaire], inclusDansEtat },
    }));
  };

  const modifierFonction = (idBeneficiaire, fonctionRetenue) => {
    setValeurs((precedent) => ({
      ...precedent,
      [idBeneficiaire]: { ...precedent[idBeneficiaire], fonctionRetenue },
    }));
  };

  const soumettre = async () => {
    setErreur(null);
    setEnCours(true);
    try {
      const ajustements = lignes
        .map((ligne) => {
          const valeur = valeurs[ligne.idBeneficiaire];
          const inclusionChangee = valeur.inclusDansEtat !== ligne.inclusDansEtat;
          const fonctionChangee = valeur.fonctionRetenue !== ligne.fonctionRetenue;
          if (!inclusionChangee && !fonctionChangee) return null;
          return {
            idBeneficiaire: ligne.idBeneficiaire,
            inclusDansEtat: inclusionChangee ? valeur.inclusDansEtat : undefined,
            fonctionRetenue: fonctionChangee ? valeur.fonctionRetenue : undefined,
          };
        })
        .filter(Boolean);

      if (ajustements.length === 0) {
        setErreur('Aucune modification à soumettre.');
        return;
      }

      const { data } = await apiClient.patch(`/processus/${idProcessus}`, { ajustements });
      setResultats(data.resultats);
    } catch {
      setErreur('Une erreur est survenue lors de la soumission des ajustements. Veuillez réessayer.');
    } finally {
      setEnCours(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      {/* Hauteur du modal plafonnee a 85vh, repartie en trois : en-tete et
          pied de taille fixe (shrink-0, toujours visibles), zone de liste
          elastique au milieu. min-h-0 est indispensable sur les elements flex
          intermediaires : sans lui, min-height vaut auto et la liste refuse de
          retrecir sous la taille de son contenu -- elle deborde alors du modal
          au lieu de scroller, et pousse les boutons hors de l'ecran. */}
      <Card className="flex max-h-[85vh] w-full max-w-3xl flex-col">
        <CardHeader className="shrink-0">
          <CardTitle>Ajuster les lignes de l'état mensuel</CardTitle>
        </CardHeader>
        <CardContent className="flex min-h-0 flex-1 flex-col gap-4">
          {erreur && (
            <Alert variant="destructive" className="shrink-0">
              <AlertTriangle className="h-4 w-4" />
              <AlertDescription>{erreur}</AlertDescription>
            </Alert>
          )}

          {!resultats && (
            <div className="min-h-0 flex-1 overflow-y-auto rounded-lg border border-neutral-200">
              <table className="w-full text-left text-sm">
                <thead className="bg-neutral-100 text-xs uppercase text-neutral-500">
                  <tr>
                    <th className="px-4 py-3 font-medium">Inclus</th>
                    <th className="px-4 py-3 font-medium">Matricule</th>
                    <th className="px-4 py-3 font-medium">Nom</th>
                    <th className="px-4 py-3 font-medium">Fonction retenue</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-neutral-200">
                  {lignes.map((ligne) => {
                    const valeur = valeurs[ligne.idBeneficiaire];
                    return (
                      <tr key={ligne.idBeneficiaire}>
                        <td className="px-4 py-3">
                          <Checkbox
                            checked={valeur.inclusDansEtat}
                            onCheckedChange={(coche) => modifierInclusion(ligne.idBeneficiaire, coche === true)}
                            disabled={enCours}
                          />
                        </td>
                        <td className="px-4 py-3 text-neutral-700">{ligne.matricule}</td>
                        <td className="px-4 py-3 text-neutral-700">{ligne.nomPrenoms}</td>
                        <td className="px-4 py-3">
                          <Select
                            value={valeur.fonctionRetenue}
                            onChange={(e) => modifierFonction(ligne.idBeneficiaire, e.target.value)}
                            disabled={enCours}
                          >
                            {fonctionsEligibles.map((f) => (
                              <option key={f.code} value={f.code}>
                                {f.libelle}
                              </option>
                            ))}
                          </Select>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}

          {resultats && (
            <div className="flex min-h-0 flex-1 flex-col gap-2 overflow-y-auto">
              {resultats.map((resultat) => {
                const ligne = lignesParId[resultat.idBeneficiaire];
                return (
                  <div
                    key={resultat.idBeneficiaire}
                    className={cn(
                      'flex items-center gap-3 rounded border px-4 py-2.5 text-sm',
                      resultat.applique
                        ? 'border-emerald-100 bg-emerald-50/60 text-emerald-800'
                        : 'border-neutral-200 bg-neutral-50 text-neutral-600'
                    )}
                  >
                    {resultat.applique ? (
                      <Check className="h-4 w-4 shrink-0 text-emerald-600" />
                    ) : (
                      <X className="h-4 w-4 shrink-0 text-primary-500" />
                    )}
                    <span className="font-medium">
                      {ligne?.matricule} — {ligne?.nomPrenoms}
                    </span>
                    {!resultat.applique && resultat.motifRejet && (
                      <span className="text-neutral-500">({resultat.motifRejet})</span>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
        <CardFooter className="shrink-0 gap-3">
          {resultats ? (
            <Button onClick={() => onSucces()}>Fermer</Button>
          ) : (
            <>
              <Button type="button" variant="outline" onClick={onFerme} disabled={enCours}>
                Annuler
              </Button>
              <Button onClick={soumettre} disabled={enCours}>
                {enCours ? 'Envoi en cours…' : 'Soumettre les ajustements'}
              </Button>
            </>
          )}
        </CardFooter>
      </Card>
    </div>
  );
}
