import { useEffect, useState } from 'react';
import { AlertTriangle, Users, Wallet, FileCheck, CheckCircle2 } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { Card, CardContent } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { formatMontantFCFA } from '../../utils/formatters';
import { getStatutProcessusInfo } from '../../utils/statutProcessus';

function CarteSquelette() {
  return (
    <Card>
      <CardContent className="flex items-start justify-between gap-4 p-6">
        <div className="flex flex-col gap-3">
          <div className="h-3 w-24 animate-pulse rounded bg-neutral-200" />
          <div className="h-7 w-20 animate-pulse rounded bg-neutral-200" />
        </div>
        <div className="h-9 w-9 shrink-0 animate-pulse rounded-full bg-neutral-200" />
      </CardContent>
    </Card>
  );
}

// `children` (Sprint D.3) : la carte "Processus en cours" recopiait tout le
// balisage de CarteSynthese pour pouvoir afficher un Badge au lieu d'un
// nombre. Les deux cartes divergeaient donc a chaque retouche. Un seul
// composant desormais, avec une echappatoire pour le contenu.
function CarteSynthese({ icone: Icone, libelle, valeur, children }) {
  return (
    <Card>
      <CardContent className="flex items-start justify-between gap-4 p-6">
        <div className="flex min-w-0 flex-col gap-1">
          {/* neutral-600 et non neutral-500 : sous 4,5:1 sur fond blanc. */}
          <p className="text-sm font-medium text-neutral-600">{libelle}</p>
          {children ?? (
            // tabular-nums : les colonnes de montants l'avaient deja, les
            // cartes non -- un chiffre qui change de largeur en changeant de
            // valeur fait sautiller la carte.
            <p className="text-2xl font-bold tabular-nums text-neutral-900">{valeur}</p>
          )}
        </div>
        <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary-50">
          <Icone className="h-5 w-5 text-primary-500" aria-hidden="true" />
        </div>
      </CardContent>
    </Card>
  );
}

export default function DashboardPage() {
  const [donnees, setDonnees] = useState(null);
  const [chargement, setChargement] = useState(true);
  // Sprint D.3 : il n'y avait aucun .catch(). En cas d'echec reseau,
  // `chargement` repassait a false, `donnees` restait null, et le rendu lisait
  // donnees.nombreBeneficiairesActifs -> ecran blanc sur la page d'atterrissage
  // quotidienne de l'ARH et de la DRH.
  const [erreurChargement, setErreurChargement] = useState(false);

  useEffect(() => {
    let annule = false;
    apiClient
      .get('/reporting/dashboard')
      .then(({ data }) => {
        if (!annule) setDonnees(data);
      })
      .catch(() => {
        if (!annule) setErreurChargement(true);
      })
      .finally(() => {
        if (!annule) setChargement(false);
      });
    return () => {
      annule = true;
    };
  }, []);

  const processusEnCours = donnees?.processusEnCours;

  return (
    <>
      <PageHeader surTitre="Vue d'ensemble" titre="Tableau de bord" />

      {erreurChargement && (
        <div className="px-8 pt-8">
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" />
            <AlertDescription>
              Impossible de charger le tableau de bord. Vérifiez votre connexion, puis réessayez.
            </AlertDescription>
          </Alert>
        </div>
      )}

      {/* Sprint D.3 -- ordre de lecture. Les quatre cartes avaient le meme
          poids et se suivaient sans logique (un compte, un montant, un statut,
          un compte). Elles racontent maintenant la meme chose de gauche a
          droite : ce qui se passe maintenant, ce que ca coute, qui c'est
          concerne, ce qui a deja ete fait. */}
      <div className="grid grid-cols-1 gap-4 p-8 sm:grid-cols-2 lg:grid-cols-4">
        {chargement ? (
          <>
            <CarteSquelette />
            <CarteSquelette />
            <CarteSquelette />
            <CarteSquelette />
          </>
        ) : erreurChargement || !donnees ? null : (
          <>
            <CarteSynthese icone={FileCheck} libelle="Processus en cours">
              {processusEnCours ? (
                <div className="pt-0.5">
                  <Badge variant={getStatutProcessusInfo(processusEnCours.statut).variant}>
                    {getStatutProcessusInfo(processusEnCours.statut).libelle}
                  </Badge>
                </div>
              ) : (
                <p className="text-2xl font-bold text-neutral-400">Aucun</p>
              )}
            </CarteSynthese>
            <CarteSynthese
              icone={Wallet}
              libelle="Montant mensuel courant"
              valeur={formatMontantFCFA(donnees.montantTotalMensuel)}
            />
            <CarteSynthese
              icone={Users}
              libelle="Bénéficiaires actifs"
              valeur={donnees.nombreBeneficiairesActifs}
            />
            <CarteSynthese
              icone={CheckCircle2}
              libelle="Clôturés cette année"
              valeur={donnees.processusClotureesCetteAnnee}
            />
          </>
        )}
      </div>
    </>
  );
}