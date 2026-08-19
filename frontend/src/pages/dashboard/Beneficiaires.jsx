import { useEffect, useMemo, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { AlertTriangle, Download } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { useAuth } from '../../contexts/AuthContext';
import { PageHeader } from '../../components/layout/PageHeader';
import { DataTable } from '../../components/ui/DataTable';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Label } from '../../components/ui/Label';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { formatMontantFCFA } from '../../utils/formatters';
import { ModifierBeneficiaireModal } from './ModifierBeneficiaireModal';
import { ConfirmDialog } from '../../components/ui/ConfirmDialog';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { MessageListeVide } from '../../components/ui/MessageListeVide';
import { definirParametre, effacerParametres } from '../../utils/searchParams';

const TAILLE_PAGE = 20;

function colonnes(libellesFonctions) {
  return [
    { cle: 'matricule', entete: 'Matricule' },
    { cle: 'nomPrenoms', entete: 'Nom' },
    {
      cle: 'fonction',
      entete: 'Fonction',
      rendu: (beneficiaire) => libellesFonctions[beneficiaire.fonction] ?? beneficiaire.fonction,
    },
    {
      cle: 'montantCourant',
      entete: 'Montant courant',
      className: 'tabular-nums',
      rendu: (beneficiaire) => formatMontantFCFA(beneficiaire.montantCourant),
    },
    { cle: 'uniteRattachement', entete: 'Unité' },
    {
      cle: 'actif',
      entete: 'Statut',
      rendu: (beneficiaire) => (
        <Badge variant={beneficiaire.actif ? 'success' : 'neutral'}>
          {beneficiaire.actif ? 'Actif' : 'Inactif'}
        </Badge>
      ),
    },
  ];
}

export default function Beneficiaires() {
  // Sprint MM.14 (ecart E2) : l'ecran est ouvert a la DRH en LECTURE SEULE.
  // Toutes les actions d'ecriture restent ARH -- cote backend, modifier,
  // desactiver et reactiver sont en hasRole('ARH') : les afficher pour la DRH
  // produirait des boutons qui echouent en 403.
  const { user } = useAuth();
  const peutModifier = user?.role === 'ARH';

  const [searchParams, setSearchParams] = useSearchParams();
  const fonction = searchParams.get('fonction') ?? '';
  const unite = searchParams.get('unite') ?? '';
  const recherche = searchParams.get('recherche') ?? '';
  const actif = searchParams.get('actif') ?? '';
  const page = Number(searchParams.get('page') ?? '0');

  const [uniteSaisie, setUniteSaisie] = useState(unite);
  const [rechercheSaisie, setRechercheSaisie] = useState(recherche);
  const premierRendu = useRef(true);

  const [donnees, setDonnees] = useState([]);
  const [total, setTotal] = useState(0);
  const [chargement, setChargement] = useState(true);
  const [erreurChargement, setErreurChargement] = useState(false);
  const [rafraichissement, setRafraichissement] = useState(0);
  const [beneficiaireAModifier, setBeneficiaireAModifier] = useState(null);
  const [beneficiaireADesactiver, setBeneficiaireADesactiver] = useState(null);
  const [exportEnCours, setExportEnCours] = useState(false);
  const [fonctionsEligibles, setFonctionsEligibles] = useState([]);
  const [unitesRattachement, setUnitesRattachement] = useState([]);
  const [reactivationEnCours, setReactivationEnCours] = useState(null);
  const [erreurReactivation, setErreurReactivation] = useState(null);

  useEffect(() => {
    apiClient.get('/fonctions-eligibles').then(({ data }) => setFonctionsEligibles(data));
  }, []);

  useEffect(() => {
    apiClient.get('/beneficiaires/unites-rattachement').then(({ data }) => setUnitesRattachement(data));
  }, []);

  const libellesFonctions = useMemo(
    () => Object.fromEntries(fonctionsEligibles.map((f) => [f.code, f.libelle])),
    [fonctionsEligibles]
  );

  // Debounce 300ms sur la saisie libre "unite" avant de declencher la requete.
  useEffect(() => {
    const minuteur = setTimeout(() => {
      setSearchParams((precedent) => definirParametre(precedent, 'unite', uniteSaisie));
    }, 300);
    return () => clearTimeout(minuteur);
  }, [uniteSaisie, setSearchParams]);

  // Debounce 300ms sur la saisie libre "recherche" (meme pattern que "unite").
  useEffect(() => {
    const minuteur = setTimeout(() => {
      setSearchParams((precedent) => definirParametre(precedent, 'recherche', rechercheSaisie));
    }, 300);
    return () => clearTimeout(minuteur);
  }, [rechercheSaisie, setSearchParams]);

  // PIEGE 1 : garde premierRendu pour ne pas ramener a la page 0 au premier
  // montage -- sinon un lien profond vers une page > 0 avec filtres actifs
  // serait casse des le chargement.
  useEffect(() => {
    if (premierRendu.current) {
      premierRendu.current = false;
      return;
    }
    setSearchParams((precedent) => definirParametre(precedent, 'page', ''));
  }, [fonction, unite, recherche, actif, setSearchParams]);

  useEffect(() => {
    let annule = false;
    setChargement(true);
    setErreurChargement(false);
    apiClient
      .get('/beneficiaires', {
        params: {
          fonction: fonction || undefined,
          recherche: recherche || undefined,
          uniteRattachement: unite || undefined,
          actif: actif === '' ? undefined : actif === 'true',
          page,
          taille: TAILLE_PAGE,
        },
      })
      .then(({ data }) => {
        if (annule) return;
        setDonnees(data.contenu);
        setTotal(data.total);
      })
      .catch(() => {
        // Sans ce catch, un echec reseau retombait sur une liste vide, donc sur
        // le meme message qu'une base reellement vide.
        if (annule) return;
        setDonnees([]);
        setTotal(0);
        setErreurChargement(true);
      })
      .finally(() => {
        if (!annule) setChargement(false);
      });
    return () => {
      annule = true;
    };
  }, [fonction, unite, recherche, actif, page, rafraichissement]);

  const desactiver = async () => {
    await apiClient.delete(`/beneficiaires/${beneficiaireADesactiver.id}`);
    setBeneficiaireADesactiver(null);
    setRafraichissement((n) => n + 1);
  };

  const reactiver = async (beneficiaire) => {
    setErreurReactivation(null);
    setReactivationEnCours(beneficiaire.id);
    try {
      const { data } = await apiClient.patch(`/beneficiaires/${beneficiaire.id}/reactiver`);
      setDonnees((precedent) => precedent.map((b) => (b.id === data.id ? data : b)));
    } catch (err) {
      if (err.response?.status === 403) {
        setErreurReactivation(
          err.response?.data?.erreur ??
            "Ce bénéficiaire n'est plus éligible à la dotation téléphonique, réactivation impossible."
        );
      } else {
        setErreurReactivation('Une erreur est survenue lors de la réactivation. Veuillez réessayer.');
      }
    } finally {
      setReactivationEnCours(null);
    }
  };

  const exporter = async () => {
    setExportEnCours(true);
    try {
      const reponse = await apiClient.get('/beneficiaires/export', { responseType: 'blob' });

      const entete = reponse.headers['content-disposition'];
      const correspondance = entete?.match(/filename="?([^"]+)"?/);
      const nomFichier = correspondance?.[1] ?? 'beneficiaires-actifs.xlsx';

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

  const pagination = useMemo(
    () => ({
      page,
      taille: TAILLE_PAGE,
      total,
      onChangerPage: (nouvellePage) =>
        setSearchParams((precedent) => definirParametre(precedent, 'page', String(nouvellePage))),
    }),
    [page, total, setSearchParams]
  );

  const filtresActifs = Boolean(fonction || unite || recherche || actif);
  // Les deux champs libres sont pilotes par un etat local (debounce 300 ms) :
  // vider seulement l'URL laisserait le texte affiche dans les champs.
  const effacerFiltres = () => {
    setUniteSaisie('');
    setRechercheSaisie('');
    setSearchParams((precedent) =>
      effacerParametres(precedent, 'fonction', 'recherche', 'unite', 'actif', 'page')
    );
  };

  return (
    <>
      <PageHeader surTitre="ARH" titre="Bénéficiaires" />
      <div className="flex flex-col gap-6 p-8">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div className="flex flex-wrap items-end gap-4">
            <div className="flex w-48 flex-col gap-1.5">
              <Label htmlFor="filtre-fonction">Fonction</Label>
              <Select
                id="filtre-fonction"
                value={fonction}
                onChange={(e) =>
                  setSearchParams((precedent) => definirParametre(precedent, 'fonction', e.target.value))
                }
              >
                <option value="">Toutes</option>
                {fonctionsEligibles.map((f) => (
                  <option key={f.code} value={f.code}>
                    {f.libelle}
                  </option>
                ))}
              </Select>
            </div>

            <div className="flex w-52 flex-col gap-1.5">
              <Label htmlFor="filtre-recherche">Nom ou matricule</Label>
              <Input
                id="filtre-recherche"
                placeholder="Rechercher par nom ou matricule…"
                value={rechercheSaisie}
                onChange={(e) => setRechercheSaisie(e.target.value)}
              />
            </div>

            <div className="flex w-48 flex-col gap-1.5">
              <Label htmlFor="filtre-unite">Unité de rattachement</Label>
              <Input
                id="filtre-unite"
                placeholder="Ex. Douala Bonanjo"
                value={uniteSaisie}
                onChange={(e) => setUniteSaisie(e.target.value)}
              />
            </div>

            <div className="flex w-40 flex-col gap-1.5">
              <Label htmlFor="filtre-actif">Statut</Label>
              <Select
                id="filtre-actif"
                value={actif}
                onChange={(e) =>
                  setSearchParams((precedent) => definirParametre(precedent, 'actif', e.target.value))
                }
              >
                <option value="">Tous</option>
                <option value="true">Actif</option>
                <option value="false">Inactif</option>
              </Select>
            </div>
          </div>

          <Button variant="outline" onClick={exporter} isLoading={exportEnCours}>
            {!exportEnCours && <Download className="h-4 w-4" />}
            {exportEnCours ? 'Export en cours…' : 'Exporter (Excel)'}
          </Button>
        </div>

        {erreurChargement && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" />
            <AlertDescription>
              Impossible de charger les bénéficiaires. Vérifiez votre connexion, puis réessayez.
            </AlertDescription>
          </Alert>
        )}

        {erreurReactivation && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" />
            <AlertDescription>{erreurReactivation}</AlertDescription>
          </Alert>
        )}

        <DataTable
          colonnes={colonnes(libellesFonctions)}
          donnees={donnees}
          cleLigne={(beneficiaire) => beneficiaire.id}
          chargement={chargement}
          pagination={pagination}
          messageVide={
            erreurChargement ? (
              'Les bénéficiaires n’ont pas pu être chargés.'
            ) : filtresActifs ? (
              <MessageListeVide
                message="Aucun bénéficiaire ne correspond à ces filtres."
                onEffacerFiltres={effacerFiltres}
              />
            ) : (
              'Aucun bénéficiaire n’est enrôlé pour le moment.'
            )
          }
          actions={!peutModifier ? undefined : (beneficiaire) => (
            <div className="flex gap-2">
              <Button variant="outline" size="sm" onClick={() => setBeneficiaireAModifier(beneficiaire)}>
                Modifier
              </Button>
              {beneficiaire.actif ? (
                <Button
                  variant="destructive"
                  size="sm"
                  onClick={() => setBeneficiaireADesactiver(beneficiaire)}
                >
                  Désactiver
                </Button>
              ) : (
                <Button
                  variant="outline"
                  size="sm"
                  disabled={reactivationEnCours === beneficiaire.id}
                  onClick={() => reactiver(beneficiaire)}
                >
                  {reactivationEnCours === beneficiaire.id ? 'Réactivation…' : 'Réactiver'}
                </Button>
              )}
            </div>
          )}
        />
      </div>

      {beneficiaireAModifier && (
        <ModifierBeneficiaireModal
          beneficiaire={beneficiaireAModifier}
          fonctionsEligibles={fonctionsEligibles}
          unitesRattachement={unitesRattachement}
          onFerme={() => setBeneficiaireAModifier(null)}
          onSucces={(beneficiaireModifie) => {
            setDonnees((precedent) =>
              precedent.map((b) => (b.id === beneficiaireModifie.id ? beneficiaireModifie : b))
            );
            setBeneficiaireAModifier(null);
          }}
        />
      )}

      {beneficiaireADesactiver && (
        <ConfirmDialog
          titre="Désactiver ce bénéficiaire"
          message={`Confirmez-vous la désactivation de ${beneficiaireADesactiver.nomPrenoms} (${beneficiaireADesactiver.matricule}) ? Il ne sera plus inclus dans les prochains états mensuels.`}
          libelleConfirmer="Désactiver"
          onConfirmer={desactiver}
          onAnnuler={() => setBeneficiaireADesactiver(null)}
        />
      )}
    </>
  );
}