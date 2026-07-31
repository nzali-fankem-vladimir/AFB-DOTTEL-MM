import { useEffect, useState } from 'react';
import { Users, Wallet, FileCheck, CheckCircle2 } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
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

function CarteSynthese({ icone: Icone, libelle, valeur }) {
  return (
    <Card>
      <CardContent className="flex items-start justify-between gap-4 p-6">
        <div className="flex flex-col gap-1">
          <p className="text-sm font-medium text-neutral-500">{libelle}</p>
          <p className="text-2xl font-bold text-neutral-900">{valeur}</p>
        </div>
        <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary-50">
          <Icone className="h-5 w-5 text-primary-500" />
        </div>
      </CardContent>
    </Card>
  );
}

export default function DashboardPage() {
  const [donnees, setDonnees] = useState(null);
  const [chargement, setChargement] = useState(true);

  useEffect(() => {
    let annule = false;
    apiClient
      .get('/reporting/dashboard')
      .then(({ data }) => {
        if (!annule) setDonnees(data);
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
      <div className="grid grid-cols-1 gap-4 p-8 sm:grid-cols-2 lg:grid-cols-4">
        {chargement ? (
          <>
            <CarteSquelette />
            <CarteSquelette />
            <CarteSquelette />
            <CarteSquelette />
          </>
        ) : (
          <>
            <CarteSynthese
              icone={Users}
              libelle="Bénéficiaires actifs"
              valeur={donnees.nombreBeneficiairesActifs}
            />
            <CarteSynthese
              icone={Wallet}
              libelle="Montant mensuel courant"
              valeur={formatMontantFCFA(donnees.montantTotalMensuel)}
            />
            <Card>
              <CardContent className="flex items-start justify-between gap-4 p-6">
                <div className="flex flex-col gap-1">
                  <p className="text-sm font-medium text-neutral-500">Processus en cours</p>
                  {processusEnCours ? (
                    <div className="pt-0.5">
                      <Badge variant={getStatutProcessusInfo(processusEnCours.statut).variant}>
                        {getStatutProcessusInfo(processusEnCours.statut).libelle}
                      </Badge>
                    </div>
                  ) : (
                    <p className="text-2xl font-bold text-neutral-300">—</p>
                  )}
                </div>
                <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary-50">
                  <FileCheck className="h-5 w-5 text-primary-500" />
                </div>
              </CardContent>
            </Card>
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