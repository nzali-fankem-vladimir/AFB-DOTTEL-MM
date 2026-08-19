import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { AlertTriangle, Plus } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { DataTable } from '../../components/ui/DataTable';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Label } from '../../components/ui/Label';
import { MessageListeVide } from '../../components/ui/MessageListeVide';
import { Select } from '../../components/ui/Select';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { useAuth } from '../../contexts/AuthContext';
import { getRoleInfo } from '../../utils/statutUtilisateur';
import { definirParametre, construireRetour, effacerParametres } from '../../utils/searchParams';

const ROLES = ['EMPLOYE', 'ARH', 'CRH', 'DRH', 'ADMIN'];

// Les 3 garde-fous ADMIN (Sprint 4bis.4, deja appliques cote backend dans
// UtilisateurAdminService.verifierPasDernierAdminActif) : desactivation et
// retrogradation interdites sur son propre compte, et sur le dernier compte
// ADMIN actif restant, meme par un autre ADMIN. L'interface les reflete
// visuellement plutot que de laisser echouer l'appel (409).
function raisonStatutDesactive(utilisateur, connecte, nombreAdminsActifs) {
  if (!utilisateur.actif) return null;
  if (utilisateur.matricule === connecte?.matricule) {
    return 'Vous ne pouvez pas désactiver votre propre compte.';
  }
  if (utilisateur.role === 'ADMIN' && nombreAdminsActifs !== null && nombreAdminsActifs <= 1) {
    return 'Impossible de désactiver le dernier administrateur actif.';
  }
  return null;
}

function raisonRoleDesactive(utilisateur, connecte, nombreAdminsActifs) {
  if (utilisateur.matricule === connecte?.matricule) {
    return 'Vous ne pouvez pas modifier votre propre rôle.';
  }
  if (utilisateur.role === 'ADMIN' && utilisateur.actif && nombreAdminsActifs !== null && nombreAdminsActifs <= 1) {
    return 'Impossible de rétrograder le dernier administrateur actif.';
  }
  return null;
}

function colonnes() {
  return [
    { cle: 'matricule', entete: 'Matricule' },
    { cle: 'nomPrenoms', entete: 'Nom' },
    {
      cle: 'role',
      entete: 'Rôle',
      rendu: (utilisateur) => {
        const { libelle, variant } = getRoleInfo(utilisateur.role);
        return <Badge variant={variant}>{libelle}</Badge>;
      },
    },
    {
      cle: 'actif',
      entete: 'Statut',
      rendu: (utilisateur) => (
        <Badge variant={utilisateur.actif ? 'success' : 'neutral'}>
          {utilisateur.actif ? 'Actif' : 'Inactif'}
        </Badge>
      ),
    },
  ];
}

export default function UtilisateursListPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const role = searchParams.get('role') ?? '';
  const actif = searchParams.get('actif') ?? '';
  const [donnees, setDonnees] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreurChargement, setErreurChargement] = useState(false);
  const [rafraichissement, setRafraichissement] = useState(0);
  const [nombreAdminsActifs, setNombreAdminsActifs] = useState(null);
  const [enCoursId, setEnCoursId] = useState(null);
  const [erreur, setErreur] = useState(null);

  useEffect(() => {
    let annule = false;
    setChargement(true);
    setErreurChargement(false);
    apiClient
      .get('/admin/utilisateurs', {
        params: {
          role: role || undefined,
          actif: actif === '' ? undefined : actif === 'true',
        },
      })
      .then(({ data }) => {
        if (!annule) setDonnees(data.contenu);
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
  }, [role, actif, rafraichissement]);

  // Compte des administrateurs actifs independant des filtres actifs sur la
  // liste affichee, pour que le garde-fou "dernier admin actif" reste correct
  // meme si la liste est filtree sur un autre role ou statut.
  useEffect(() => {
    let annule = false;
    apiClient
      .get('/admin/utilisateurs', { params: { role: 'ADMIN', actif: true } })
      .then(({ data }) => {
        if (!annule) setNombreAdminsActifs(data.contenu.length);
      });
    return () => {
      annule = true;
    };
  }, [rafraichissement]);

  const changerStatut = async (utilisateur) => {
    setErreur(null);
    setEnCoursId(utilisateur.id);
    try {
      const { data } = await apiClient.patch(`/admin/utilisateurs/${utilisateur.id}/statut`, {
        actif: !utilisateur.actif,
      });
      setDonnees((precedent) => precedent.map((u) => (u.id === data.id ? data : u)));
      setRafraichissement((n) => n + 1);
    } catch {
      setErreur('Une erreur est survenue lors du changement de statut. Veuillez réessayer.');
    } finally {
      setEnCoursId(null);
    }
  };

  const changerRole = async (utilisateur, nouveauRole) => {
    if (nouveauRole === utilisateur.role) return;
    setErreur(null);
    setEnCoursId(utilisateur.id);
    try {
      const { data } = await apiClient.patch(`/admin/utilisateurs/${utilisateur.id}/role`, {
        role: nouveauRole,
      });
      setDonnees((precedent) => precedent.map((u) => (u.id === data.id ? data : u)));
      setRafraichissement((n) => n + 1);
    } catch {
      setErreur('Une erreur est survenue lors du changement de rôle. Veuillez réessayer.');
    } finally {
      setEnCoursId(null);
    }
  };

  const colonnesMemo = useMemo(() => colonnes(), []);

  const filtresActifs = Boolean(role || actif);
  const effacerFiltres = () =>
    setSearchParams((precedent) => effacerParametres(precedent, 'role', 'actif'));

  return (
    <>
      <PageHeader surTitre="Système" titre="Administration des utilisateurs" />
      <div className="flex flex-col gap-6 p-8">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div className="flex flex-wrap items-end gap-4">
            <div className="flex w-48 flex-col gap-1.5">
              <Label htmlFor="filtre-role">Rôle</Label>
              <Select
                id="filtre-role"
                value={role}
                onChange={(e) =>
                  setSearchParams((precedent) => definirParametre(precedent, 'role', e.target.value))
                }
              >
                <option value="">Tous</option>
                {ROLES.map((r) => (
                  <option key={r} value={r}>
                    {r}
                  </option>
                ))}
              </Select>
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

          <Button
            onClick={() =>
              navigate('/admin/utilisateurs/creer', {
                state: { retour: construireRetour('/admin/utilisateurs', searchParams) },
              })
            }
          >
            <Plus className="h-4 w-4" />
            Créer un utilisateur
          </Button>
        </div>

        {erreurChargement && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" />
            <AlertDescription>
              Impossible de charger les utilisateurs. Vérifiez votre connexion, puis réessayez.
            </AlertDescription>
          </Alert>
        )}

        {erreur && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" />
            <AlertDescription>{erreur}</AlertDescription>
          </Alert>
        )}

        <DataTable
          colonnes={colonnesMemo}
          donnees={donnees}
          cleLigne={(utilisateur) => utilisateur.id}
          chargement={chargement}
          messageVide={
            erreurChargement ? (
              'Les utilisateurs n’ont pas pu être chargés.'
            ) : filtresActifs ? (
              <MessageListeVide
                message="Aucun utilisateur ne correspond à ces filtres."
                onEffacerFiltres={effacerFiltres}
              />
            ) : (
              'Aucun utilisateur enregistré pour le moment.'
            )
          }
          actions={(utilisateur) => {
            const motifStatutDesactive = raisonStatutDesactive(utilisateur, user, nombreAdminsActifs);
            const motifRoleDesactive = raisonRoleDesactive(utilisateur, user, nombreAdminsActifs);
            const enCours = enCoursId === utilisateur.id;

            return (
              <div className="flex items-center gap-2">
                <span title={motifRoleDesactive ?? undefined}>
                  <Select
                    aria-label={`Changer le rôle de ${utilisateur.nomPrenoms}`}
                    className="h-8 w-32 py-1 text-xs"
                    value={utilisateur.role}
                    disabled={Boolean(motifRoleDesactive) || enCours}
                    onChange={(e) => changerRole(utilisateur, e.target.value)}
                  >
                    {ROLES.map((r) => (
                      <option key={r} value={r}>
                        {r}
                      </option>
                    ))}
                  </Select>
                </span>

                <span title={motifStatutDesactive ?? undefined}>
                  <Button
                    variant={utilisateur.actif ? 'destructive' : 'outline'}
                    size="sm"
                    disabled={Boolean(motifStatutDesactive) || enCours}
                    onClick={() => changerStatut(utilisateur)}
                  >
                    {enCours ? '…' : utilisateur.actif ? 'Désactiver' : 'Activer'}
                  </Button>
                </span>
              </div>
            );
          }}
        />
      </div>
    </>
  );
}