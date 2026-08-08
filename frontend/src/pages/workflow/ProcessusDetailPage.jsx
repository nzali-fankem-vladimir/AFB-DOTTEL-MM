import { useEffect, useState } from 'react';
import { useLocation, useParams } from 'react-router-dom';
import { AlertTriangle, Check, Download, MessageSquareWarning, SlidersHorizontal, Undo2, X } from 'lucide-react';
import apiClient from '../../api/apiClient';
import { PageHeader } from '../../components/layout/PageHeader';
import { DataTable } from '../../components/ui/DataTable';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Card, CardContent } from '../../components/ui/Card';
import { Alert, AlertDescription } from '../../components/ui/Alert';
import { LienRetour } from '../../components/ui/LienRetour';
import { VoirMotifModal } from '../../components/ui/VoirMotifModal';
import { useAuth } from '../../contexts/AuthContext';
import { cn } from '../../utils/cn';
import { formatMontantFCFA, getPeriodeLabel } from '../../utils/formatters';
import { ETAPES_WORKFLOW, getStatutProcessusInfo } from '../../utils/statutProcessus';
import { AjusterLignesModal } from './AjusterLignesModal';
import { RetournerProcessusModal } from './RetournerProcessusModal';

// Statuts depuis lesquels l'ARH peut encore ajuster les lignes -- alignes sur
// ProcessusMensuelService.ajuster() (EN_COURS_ARH ou RETOURNE), pas seulement
// EN_COURS_ARH, sinon un processus retourne par le CRH/DRH resterait bloque
// cote UI sans jamais pouvoir etre corrige avant revalidation (RG-05).
const STATUTS_AJUSTABLES = ['EN_COURS_ARH', 'RETOURNE'];

// Transition (statut courant -> role autorise a valider), alignee sur
// ProcessusMensuelService.valider() : la branche ARH accepte EN_COURS_ARH ET
// RETOURNE (revalidation apres correction), memes remarques que ci-dessus.
const ROLE_PAR_STATUT_VALIDABLE = {
  EN_COURS_ARH: 'ARH',
  RETOURNE: 'ARH',
  EN_ATTENTE_CRH: 'CRH',
  EN_ATTENTE_DRH: 'DRH',
};

const STATUTS_RETOURNABLES = ['EN_ATTENTE_CRH', 'EN_ATTENTE_DRH'];

// La piece jointe n'existe qu'a partir de la validation ARH (RG-06 : un seul
// document par processus, genere puis enrichi). Un 404 avant cette etape est
// donc un cas normal, pas une erreur a afficher -- le bouton de telechargement
// reste simplement masque.
function chargerPieceJointe(idProcessus) {
  return apiClient
    .get(`/processus/${idProcessus}/piece-jointe`)
    .then(({ data }) => data)
    .catch(() => null);
}

const colonnesLignes = [
  { cle: 'matricule', entete: 'Matricule' },
  { cle: 'nomPrenoms', entete: 'Nom' },
  { cle: 'fonctionRetenue', entete: 'Fonction retenue' },
  {
    cle: 'montantApplique',
    entete: 'Montant appliqué',
    rendu: (ligne) => formatMontantFCFA(ligne.montantApplique),
  },
  {
    cle: 'inclusDansEtat',
    entete: 'Inclus',
    rendu: (ligne) =>
      ligne.inclusDansEtat ? (
        <Badge variant="success">
          <Check className="mr-1 h-3 w-3" />
          Inclus
        </Badge>
      ) : (
        <Badge variant="neutral">
          <X className="mr-1 h-3 w-3" />
          Exclu
        </Badge>
      ),
  },
];

function TimelineWorkflow({ statut }) {
  const { etapeCourante } = getStatutProcessusInfo(statut);
  const retourne = statut === 'RETOURNE';

  return (
    <div className="flex items-center">
      {ETAPES_WORKFLOW.map((etape, index) => {
        const franchie = index < etapeCourante;
        const courante = index === etapeCourante;
        const enAlerte = courante && retourne;

        return (
          <div key={etape.cle} className="flex flex-1 items-center last:flex-none">
            <div className="flex flex-col items-center gap-1.5">
              <div
                className={cn(
                  'flex h-9 w-9 items-center justify-center rounded-full border-2 text-sm font-semibold',
                  franchie && 'border-emerald-500 bg-emerald-500 text-white',
                  courante && !enAlerte && 'border-primary-500 bg-primary-500 text-white',
                  enAlerte && 'border-primary-700 bg-primary-50 text-primary-700',
                  !franchie && !courante && 'border-neutral-300 bg-white text-neutral-400'
                )}
              >
                {franchie ? <Check className="h-4 w-4" /> : index + 1}
              </div>
              <span
                className={cn(
                  'text-xs font-medium',
                  (franchie || courante) ? 'text-neutral-900' : 'text-neutral-400'
                )}
              >
                {etape.libelle}
              </span>
            </div>
            {index < ETAPES_WORKFLOW.length - 1 && (
              <div className={cn('mx-2 h-0.5 flex-1', franchie ? 'bg-emerald-500' : 'bg-neutral-200')} />
            )}
          </div>
        );
      })}
    </div>
  );
}

function EnTeteSquelette() {
  return (
    <Card>
      <CardContent className="flex flex-col gap-6 p-6">
        <div className="flex items-center justify-between">
          <div className="h-7 w-48 animate-pulse rounded bg-neutral-200" />
          <div className="h-6 w-32 animate-pulse rounded-full bg-neutral-200" />
        </div>
        <div className="flex gap-4">
          {[0, 1, 2].map((i) => (
            <div key={i} className="h-9 w-9 animate-pulse rounded-full bg-neutral-200" />
          ))}
        </div>
      </CardContent>
    </Card>
  );
}

export default function ProcessusDetailPage() {
  const { id } = useParams();
  const location = useLocation();
  const retour = location.state?.retour ?? '/processus';
  const { user } = useAuth();
  const [processus, setProcessus] = useState(null);
  const [chargement, setChargement] = useState(true);
  const [pieceJointe, setPieceJointe] = useState(null);
  const [ajustementOuvert, setAjustementOuvert] = useState(false);
  const [retourOuvert, setRetourOuvert] = useState(false);
  const [motifRetourOuvert, setMotifRetourOuvert] = useState(false);
  const [validationEnCours, setValidationEnCours] = useState(false);
  const [telechargementEnCours, setTelechargementEnCours] = useState(false);
  const [erreurValidation, setErreurValidation] = useState(null);

  useEffect(() => {
    let annule = false;
    setChargement(true);
    Promise.all([apiClient.get(`/processus/${id}`), chargerPieceJointe(id)])
      .then(([reponseProcessus, metadonneesPieceJointe]) => {
        if (annule) return;
        setProcessus(reponseProcessus.data);
        setPieceJointe(metadonneesPieceJointe);
      })
      .finally(() => {
        if (!annule) setChargement(false);
      });
    return () => {
      annule = true;
    };
  }, [id]);

  const rafraichir = async () => {
    const [reponseProcessus, metadonneesPieceJointe] = await Promise.all([
      apiClient.get(`/processus/${id}`),
      chargerPieceJointe(id),
    ]);
    setProcessus(reponseProcessus.data);
    setPieceJointe(metadonneesPieceJointe);
  };

  const valider = async () => {
    setErreurValidation(null);
    setValidationEnCours(true);
    try {
      await apiClient.post(`/processus/${id}/valider`);
      rafraichir();
    } catch (err) {
      if (err.response?.status === 403) {
        setErreurValidation(
          "Vous ne pouvez pas valider cette étape : vous avez déjà validé l'étape précédente de ce processus (séparation des tâches, RG-08)."
        );
      } else {
        setErreurValidation('Une erreur est survenue lors de la validation. Veuillez réessayer.');
      }
    } finally {
      setValidationEnCours(false);
    }
  };

  // Meme logique de blob que l'export Excel des beneficiaires (Sprint 6F.5) :
  // axios ne declenche pas le telechargement, on cree un lien temporaire.
  const telechargerPdf = async () => {
    setTelechargementEnCours(true);
    try {
      const reponse = await apiClient.get(`/pieces-jointes/${pieceJointe.id}/download`, {
        responseType: 'blob',
      });

      const entete = reponse.headers['content-disposition'];
      const correspondance = entete?.match(/filename="?([^"]+)"?/);
      const nomFichier = correspondance?.[1] ?? pieceJointe.nomFichier ?? 'etat-mensuel.pdf';

      const url = window.URL.createObjectURL(new Blob([reponse.data], { type: 'application/pdf' }));
      const lien = document.createElement('a');
      lien.href = url;
      lien.download = nomFichier;
      document.body.appendChild(lien);
      lien.click();
      lien.remove();
      window.URL.revokeObjectURL(url);
    } finally {
      setTelechargementEnCours(false);
    }
  };

  if (chargement) {
    return (
      <>
        <LienRetour to={retour} label="Retour aux processus" />
        <PageHeader surTitre="Workflow" titre="Processus mensuel" />
        <div className="flex flex-col gap-6 p-8">
          <EnTeteSquelette />
        </div>
      </>
    );
  }

  const { libelle: libelleStatut, variant: variantStatut } = getStatutProcessusInfo(processus.statut);
  const peutAjuster = user?.role === 'ARH' && STATUTS_AJUSTABLES.includes(processus.statut);
  const peutValider = ROLE_PAR_STATUT_VALIDABLE[processus.statut] === user?.role;
  // Le retour n'est ouvert qu'au CRH et a la DRH, sur leur propre etape en
  // attente (backend : retourner() refuse tout autre statut).
  const peutRetourner = peutValider && STATUTS_RETOURNABLES.includes(processus.statut);
  const peutTelecharger = pieceJointe !== null;
  const estRetourne = processus.statut === 'RETOURNE';

  return (
    <>
      <LienRetour to={retour} label="Retour aux processus" />
      <PageHeader surTitre="Workflow" titre={getPeriodeLabel(processus.moisPaiement, processus.anneePaiement)} />
      <div className="flex flex-col gap-6 p-8">
        <Card>
          <CardContent className="flex flex-col gap-6 p-6">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-neutral-900">
                {getPeriodeLabel(processus.moisPaiement, processus.anneePaiement)}
              </h2>
              <Badge variant={variantStatut}>{libelleStatut}</Badge>
            </div>

            <TimelineWorkflow statut={processus.statut} />

            {erreurValidation && (
              <Alert variant="destructive">
                <AlertTriangle className="h-4 w-4" />
                <AlertDescription>{erreurValidation}</AlertDescription>
              </Alert>
            )}

            {estRetourne && peutValider && (
              <Alert variant="warning">
                <AlertTriangle className="h-4 w-4" />
                <AlertDescription>
                  Ce processus a été retourné (étape {processus.origineRetour ?? 'CRH/DRH'}). Consultez le motif
                  avant de corriger et revalider.
                </AlertDescription>
              </Alert>
            )}

            {(peutAjuster || peutValider || peutRetourner || peutTelecharger) && (
              <div className="flex flex-wrap justify-end gap-3">
                {peutTelecharger && (
                  <Button variant="outline" onClick={telechargerPdf} disabled={telechargementEnCours}>
                    <Download className="h-4 w-4" />
                    {telechargementEnCours ? 'Téléchargement…' : "Télécharger l'état (PDF)"}
                  </Button>
                )}
                {peutAjuster && (
                  <Button variant="outline" onClick={() => setAjustementOuvert(true)}>
                    <SlidersHorizontal className="h-4 w-4" />
                    Ajuster les lignes
                  </Button>
                )}
                {estRetourne && processus.motifRetour && (
                  <Button variant="outline" onClick={() => setMotifRetourOuvert(true)}>
                    <MessageSquareWarning className="h-4 w-4" />
                    Voir le motif de retour
                  </Button>
                )}
                {peutRetourner && (
                  <Button variant="destructive" onClick={() => setRetourOuvert(true)}>
                    <Undo2 className="h-4 w-4" />
                    Retourner
                  </Button>
                )}
                {peutValider && (
                  <Button onClick={valider} disabled={validationEnCours}>
                    <Check className="h-4 w-4" />
                    {validationEnCours ? 'Validation en cours…' : 'Valider'}
                  </Button>
                )}
              </div>
            )}
          </CardContent>
        </Card>

        <DataTable
          colonnes={colonnesLignes}
          donnees={processus.lignesEtatMensuel ?? []}
          cleLigne={(ligne) => ligne.idBeneficiaire}
        />
      </div>

      {ajustementOuvert && (
        <AjusterLignesModal
          idProcessus={processus.id}
          lignes={processus.lignesEtatMensuel ?? []}
          onFerme={() => setAjustementOuvert(false)}
          onSucces={() => {
            setAjustementOuvert(false);
            rafraichir();
          }}
        />
      )}

      {retourOuvert && (
        <RetournerProcessusModal
          idProcessus={processus.id}
          onFerme={() => setRetourOuvert(false)}
          onSucces={() => {
            setRetourOuvert(false);
            rafraichir();
          }}
        />
      )}

      {motifRetourOuvert && (
        <VoirMotifModal
          titre="Motif de retour"
          origine={processus.origineRetour}
          motif={processus.motifRetour}
          onFermer={() => setMotifRetourOuvert(false)}
        />
      )}
    </>
  );
}
