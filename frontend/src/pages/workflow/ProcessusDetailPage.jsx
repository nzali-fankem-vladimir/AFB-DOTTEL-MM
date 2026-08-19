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
import { ConfirmDialog } from '../../components/ui/ConfirmDialog';
import { LienRetour } from '../../components/ui/LienRetour';
import { VoirMotifModal } from '../../components/ui/VoirMotifModal';
import { useAuth } from '../../contexts/AuthContext';
import { cn } from '../../utils/cn';
import { formatMontantFCFA, getPeriodeLabel } from '../../utils/formatters';
import { estBloquante } from '../../utils/resynchronisation';
import { ETAPES_WORKFLOW, getStatutProcessusInfo } from '../../utils/statutProcessus';
import { AjusterLignesModal } from './AjusterLignesModal';
import { RecapitulatifResynchronisation } from './RecapitulatifResynchronisation';
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
    className: 'tabular-nums',
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
  // Sprint MM.12 : ecarts detectes en attente de confirmation (premier temps),
  // puis recapitulatif de ce qui a reellement ete applique (apres validation).
  const [ecartsAConfirmer, setEcartsAConfirmer] = useState(null);
  const [recapitulatifApplique, setRecapitulatifApplique] = useState(null);
  // Confirmation d'une validation sans écart : garde-fou contre le clic trop
  // rapide sur une action qui fait avancer le workflow de façon irréversible.
  const [confirmationSimpleOuverte, setConfirmationSimpleOuverte] = useState(false);

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

  // Sprint MM.12, variante B2-RESYNC "en deux temps". PREMIER temps, reserve a
  // l'ARH : on interroge GET /ecarts-montants (lecture pure) avant toute
  // validation. Sinon on ouvre le recapitulatif, et l'ARH peut encore renoncer.
  //
  // Quand il n'y a AUCUN ecart, on ne valide plus directement : une validation
  // de processus mensuel fait avancer le workflow, genere le PDF et n'est
  // annulable que par un retour CRH/DRH. Elle merite donc une confirmation, ne
  // serait-ce que contre le clic trop rapide.
  const demanderValidation = async () => {
    setErreurValidation(null);
    setRecapitulatifApplique(null);

    if (ROLE_PAR_STATUT_VALIDABLE[processus?.statut] !== 'ARH') {
      setConfirmationSimpleOuverte(true);
      return;
    }

    setValidationEnCours(true);
    try {
      const { data } = await apiClient.get(`/processus/${id}/ecarts-montants`);
      const aucunEcart =
        (data.lignesResynchronisees?.length ?? 0) === 0 && (data.lignesExclues?.length ?? 0) === 0;
      if (aucunEcart) {
        setConfirmationSimpleOuverte(true);
      } else {
        setEcartsAConfirmer(data);
      }
    } catch {
      setErreurValidation(
        'Impossible de vérifier les montants avant validation. Veuillez réessayer.'
      );
    } finally {
      setValidationEnCours(false);
    }
  };

  // SECOND temps : la resynchronisation n'est appliquee qu'ici, et seulement
  // avec confirmerResynchronisation=true -- sans ce parametre le backend
  // repond 409 plutot que de recaler les montants en silence.
  const validerEffectivement = async (confirmerResynchronisation) => {
    setErreurValidation(null);
    setValidationEnCours(true);
    try {
      // Parametre porte par l'URL, et AUCUN corps de requete : passer `null`
      // comme second argument suffit a faire appliquer par axios son
      // Content-Type POST par defaut (application/x-www-form-urlencoded), que
      // Spring rejette sur ce endpoint annote @RequestBody JSON -> 500.
      const url = confirmerResynchronisation
        ? `/processus/${id}/valider?confirmerResynchronisation=true`
        : `/processus/${id}/valider`;
      const { data } = await apiClient.post(url);
      setEcartsAConfirmer(null);
      setConfirmationSimpleOuverte(false);
      // Le recapitulatif reste affiche APRES la validation : l'ARH doit pouvoir
      // relire ce qui a ete applique, pas seulement ce qu'il a confirme.
      if (data.lignesResynchronisees?.length || data.lignesExclues?.length) {
        setRecapitulatifApplique(data);
      }
      await rafraichir();
    } catch (err) {
      setEcartsAConfirmer(null);
      setConfirmationSimpleOuverte(false);
      if (err.response?.status === 403) {
        setErreurValidation(
          "Vous ne pouvez pas valider cette étape : vous avez déjà validé l'étape précédente de ce processus (séparation des tâches, RG-08)."
        );
      } else if (err.response?.status === 409) {
        setErreurValidation(
          err.response?.data?.erreur ??
            'Une erreur est survenue lors de la validation. Veuillez réessayer.'
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
  // Sprint MM.12 (P-2) : au moins une fonction attend une signature de grille.
  // La validation est refusee par le backend tant que ce n'est pas tranche.
  const comporteUnBlocage = (ecartsAConfirmer?.lignesExclues ?? []).some(estBloquante);

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

            {/* Sprint MM.12 : ce qui a REELLEMENT ete applique a la validation.
                Reutilise le meme composant que la confirmation prealable, donc
                les memes deux blocs separes -- l'ARH relit exactement ce qu'il
                a confirme. */}
            {recapitulatifApplique && (
              <Alert variant="warning">
                <AlertTriangle className="h-4 w-4" />
                <AlertDescription>
                  <p className="mb-3 font-medium">
                    Les montants ont été mis à jour sur la grille tarifaire en vigueur avant validation.
                  </p>
                  <RecapitulatifResynchronisation ecarts={recapitulatifApplique} />
                </AlertDescription>
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
                  <Button onClick={demanderValidation} disabled={validationEnCours}>
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

      {/* Validation sans ecart : confirmation simple. L'etape franchie genere
          ou signe le PDF et ne se defait que par un retour CRH/DRH. */}
      {confirmationSimpleOuverte && (
        <ConfirmDialog
          titre={`Valider l'étape ${ROLE_PAR_STATUT_VALIDABLE[processus.statut] ?? ''}`}
          message={`Vous êtes sur le point de valider l'état mensuel ${getPeriodeLabel(
            processus.moisPaiement,
            processus.anneePaiement
          )}. Cette action fait avancer le workflow et ne peut être défaite que par un retour. Confirmer ?`}
          libelleConfirmer="Valider"
          onAnnuler={() => setConfirmationSimpleOuverte(false)}
          onConfirmer={() => validerEffectivement(false)}
        />
      )}

      {/* Sprint MM.12 : point d'arret AVANT toute ecriture. Rien n'a encore ete
          modifie a ce stade -- "Annuler" laisse le processus intact. */}
      {ecartsAConfirmer && (
        <ConfirmDialog
          titre={
            comporteUnBlocage
              ? 'Validation impossible pour le moment'
              : 'Des montants ne sont plus à jour'
          }
          largeur="max-w-lg"
          libelleConfirmer="Appliquer et valider"
          contenu={<RecapitulatifResynchronisation ecarts={ecartsAConfirmer} />}
          onAnnuler={() => setEcartsAConfirmer(null)}
          /* Une grille en cours de signature n'est pas confirmable : le backend
             refuserait (409). On n'offre donc pas un bouton qui echouerait. */
          onConfirmer={comporteUnBlocage ? undefined : () => validerEffectivement(true)}
        />
      )}
    </>
  );
}
