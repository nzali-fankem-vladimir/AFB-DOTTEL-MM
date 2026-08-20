import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertTriangle, Plus } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { DataTable } from '../../components/ui/DataTable';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { ConfirmDialog } from '../../components/ui/ConfirmDialog';
import { ModifierFonctionModal } from './ModifierFonctionModal';

const colonnes = [
  { cle: 'code', entete: 'Code' },
  { cle: 'libelle', entete: 'Libellé' },
  { cle: 'nombreBeneficiairesActifs', entete: 'Bénéficiaires actifs rattachés' },
  {
    cle: 'actif',
    entete: 'Statut',
    rendu: (fonction) => (
      <Badge variant={fonction.actif ? 'success' : 'neutral'}>
        {fonction.actif ? 'Actif' : 'Inactif'}
      </Badge>
    ),
  },
];

// Ecart cahier des charges II.1.7 comble au Sprint 6F.7bis : fonction_eligible
// passe d'un referentiel fige par migration Flyway a un referentiel gere par
// l'application (creation, desactivation, reactivation), reserve ADMIN.
export default function FonctionsEligiblesListPage() {
  const navigate = useNavigate();
  const [donnees, setDonnees] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreurChargement, setErreurChargement] = useState(false);
  const [rafraichissement, setRafraichissement] = useState(0);
  const [fonctionADesactiver, setFonctionADesactiver] = useState(null);
  const [fonctionAModifier, setFonctionAModifier] = useState(null);
  const [reactivationEnCours, setReactivationEnCours] = useState(null);
  const [erreur, setErreur] = useState(null);

  useEffect(() => {
    let annule = false;
    setChargement(true);
    setErreurChargement(false);
    apiClient
      .get('/fonctions-eligibles/toutes')
      .then(({ data }) => {
        if (!annule) setDonnees(data);
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
  }, [rafraichissement]);

  const desactiver = async () => {
    // RG-01 : aucune verification bloquante ici -- l'avertissement sur le
    // nombre de beneficiaires actifs concernes est affiche avant confirmation
    // (decision metier actee), le backend trace ce nombre dans l'audit.
    await apiClient.patch(`/fonctions-eligibles/${fonctionADesactiver.code}/desactiver`);
    setFonctionADesactiver(null);
    setRafraichissement((n) => n + 1);
  };

  const reactiver = async (fonction) => {
    setErreur(null);
    setReactivationEnCours(fonction.code);
    try {
      await apiClient.patch(`/fonctions-eligibles/${fonction.code}/reactiver`);
      setRafraichissement((n) => n + 1);
    } catch {
      setErreur('Une erreur est survenue lors de la réactivation. Veuillez réessayer.');
    } finally {
      setReactivationEnCours(null);
    }
  };

  return (
    <>
      <PageHeader surTitre="Référentiel" titre="Fonctions éligibles" />
      <div className="flex flex-col gap-6 p-8">
        <div className="flex justify-end">
          <Button onClick={() => navigate('/admin/fonctions-eligibles/creer')}>
            <Plus className="h-4 w-4" aria-hidden="true" />
            Créer une fonction
          </Button>
        </div>

        {erreurChargement && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" aria-hidden="true" />
            <AlertDescription>
              Impossible de charger les fonctions éligibles. Vérifiez votre connexion, puis réessayez.
            </AlertDescription>
          </Alert>
        )}

        {erreur && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" aria-hidden="true" />
            <AlertDescription>{erreur}</AlertDescription>
          </Alert>
        )}

        <DataTable
          colonnes={colonnes}
          donnees={donnees}
          cleLigne={(fonction) => fonction.code}
          chargement={chargement}
          /* Cet ecran n'a pas de filtre : "vide" ne peut signifier qu'une
             chose, il n'y a donc pas de second message a distinguer. */
          messageVide={
            erreurChargement
              ? 'Les fonctions éligibles n’ont pas pu être chargées.'
              : 'Aucune fonction éligible enregistrée pour le moment.'
          }
          actions={(fonction) => (
            <div className="flex gap-2">
              <Button variant="outline" size="sm" onClick={() => setFonctionAModifier(fonction)}>
                Modifier
              </Button>
              {fonction.actif ? (
                <Button variant="destructive" size="sm" onClick={() => setFonctionADesactiver(fonction)}>
                  Désactiver
                </Button>
              ) : (
                <Button
                  variant="outline"
                  size="sm"
                  disabled={reactivationEnCours === fonction.code}
                  onClick={() => reactiver(fonction)}
                >
                  {reactivationEnCours === fonction.code ? 'Réactivation…' : 'Réactiver'}
                </Button>
              )}
            </div>
          )}
        />
      </div>

      {fonctionAModifier && (
        <ModifierFonctionModal
          fonction={fonctionAModifier}
          onFerme={() => setFonctionAModifier(null)}
          onSucces={() => {
            setFonctionAModifier(null);
            setRafraichissement((n) => n + 1);
          }}
        />
      )}

      {fonctionADesactiver && (
        <ConfirmDialog
          titre="Désactiver la fonction éligible"
          message={
            fonctionADesactiver.nombreBeneficiairesActifs > 0
              ? `${fonctionADesactiver.nombreBeneficiairesActifs} bénéficiaire(s) actif(s) sont rattachés à ${fonctionADesactiver.libelle}. Ils seront exclus du prochain processus mensuel tant que la fonction reste inactive. Confirmer la désactivation ?`
              : `Confirmez-vous la désactivation de ${fonctionADesactiver.libelle} ? Aucun bénéficiaire actif n'y est rattaché.`
          }
          libelleConfirmer="Désactiver"
          onConfirmer={desactiver}
          onAnnuler={() => setFonctionADesactiver(null)}
        />
      )}
    </>
  );
}