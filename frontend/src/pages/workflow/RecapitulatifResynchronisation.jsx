import { ArrowRight, Clock, RefreshCw, UserMinus } from 'lucide-react';
import { formatMontantFCFA, formatDate } from '../../utils/formatters';
import { estBloquante } from '../../utils/resynchronisation';

// Sprint MM.12 -- variante B2-RESYNC de la decision B.
//
// Les deux categories sont volontairement presentees en BLOCS SEPARES et
// visuellement distincts, jamais dans une liste unique : leurs consequences
// n'ont rien de comparable.
//
//   resynchronisee -> la personne EST payee, a un autre montant
//   exclue         -> la personne N'EST PAS payee ce mois-ci
//
// Un ARH qui survole une liste indifferenciee peut ne pas voir qu'il vient de
// retirer quelqu'un de la paie (exigence utilisateur du 2026-08-09). D'ou le
// bloc d'exclusion en rouge, place EN PREMIER : la consequence la plus lourde
// se lit avant celle qui l'est moins.
export function RecapitulatifResynchronisation({ ecarts }) {
  const resynchronisees = ecarts?.lignesResynchronisees ?? [];
  const exclues = ecarts?.lignesExclues ?? [];
  // Trois blocs et non deux depuis MM.12 (P-1) : le cas transitoire bloque la
  // validation, il ne doit pas se confondre avec une exclusion definitive.
  const bloquantes = exclues.filter(estBloquante);
  const exclusionsDurables = exclues.filter((ligne) => !estBloquante(ligne));

  return (
    <div className="flex max-h-[60vh] flex-col gap-5 overflow-y-auto">
      {bloquantes.length > 0 && (
        <section className="rounded border border-amber-500 bg-amber-50 p-3">
          <h3 className="mb-1 flex items-center gap-2 text-sm font-semibold text-amber-800">
            <Clock className="h-4 w-4 shrink-0" aria-hidden="true" />
            {bloquantes.length} grille{bloquantes.length > 1 ? 's' : ''} en cours de validation
          </h3>
          <p className="mb-2 text-xs text-amber-800">
            Ces fonctions n&apos;ont plus de grille en vigueur, mais une nouvelle grille est déjà
            engagée dans le circuit. <strong>La validation est bloquée</strong> tant que la situation
            n&apos;est pas tranchée.
          </p>
          <ul className="mb-2 flex flex-col gap-1.5">
            {bloquantes.map((ligne) => (
              <li key={ligne.idBeneficiaire} className="text-sm text-neutral-800">
                <span className="font-medium">{ligne.nomPrenoms ?? '—'}</span>
                <span className="text-neutral-600"> ({ligne.matricule ?? '—'})</span>
                <span className="text-neutral-600"> — {ligne.fonctionRetenue}</span>
                <br />
                <span className="text-xs text-amber-800">
                  Grille de {formatMontantFCFA(ligne.montantGrilleEnAttente)} en attente de validation{' '}
                  <strong>{ligne.etapeGrilleEnAttente}</strong>
                  {ligne.dateSoumissionGrilleEnAttente
                    ? ` depuis le ${formatDate(ligne.dateSoumissionGrilleEnAttente)}`
                    : ''}
                </span>
              </li>
            ))}
          </ul>
          <p className="text-xs text-amber-900">
            <strong>Deux options :</strong> attendre la validation de la grille pour que{' '}
            {bloquantes.length > 1 ? 'ces bénéficiaires soient payés' : 'ce bénéficiaire soit payé'}, ou
            les exclure explicitement via <strong>« Ajuster les lignes »</strong> avant de valider —
            {bloquantes.length > 1 ? ' ils ne seront alors pas payés' : ' il ne sera alors pas payé'} ce
            mois-ci.
          </p>
        </section>
      )}

      {exclusionsDurables.length > 0 && (
        <section className="rounded border border-primary-500 bg-primary-50 p-3">
          <h3 className="mb-1 flex items-center gap-2 text-sm font-semibold text-primary-700">
            <UserMinus className="h-4 w-4 shrink-0" aria-hidden="true" />
            {exclusionsDurables.length} bénéficiaire{exclusionsDurables.length > 1 ? 's' : ''} ne sera
            {exclusionsDurables.length > 1 ? 'ont' : ''} PAS payé{exclusionsDurables.length > 1 ? 's' : ''} ce mois-ci
          </h3>
          <p className="mb-2 text-xs text-primary-700">
            Leur fonction n&apos;a plus de grille tarifaire active, et aucune grille n&apos;est en cours
            de validation. Ces lignes sont retirées de l&apos;état mensuel.
          </p>
          <ul className="flex flex-col gap-1.5">
            {exclusionsDurables.map((ligne) => (
              <li key={ligne.idBeneficiaire} className="text-sm text-neutral-800">
                <span className="font-medium">{ligne.nomPrenoms ?? '—'}</span>
                <span className="text-neutral-600"> ({ligne.matricule ?? '—'})</span>
                <span className="text-neutral-600"> — {ligne.fonctionRetenue}</span>
                <br />
                <span className="text-xs text-primary-700">{ligne.motifExclusion}</span>
              </li>
            ))}
          </ul>
        </section>
      )}

      {resynchronisees.length > 0 && (
        <section className="rounded border border-neutral-500 bg-neutral-50 p-3">
          <h3 className="mb-1 flex items-center gap-2 text-sm font-semibold text-neutral-800">
            <RefreshCw className="h-4 w-4 shrink-0" aria-hidden="true" />
            {resynchronisees.length} montant{resynchronisees.length > 1 ? 's' : ''} mis à jour sur la grille en vigueur
          </h3>
          <p className="mb-2 text-xs text-neutral-600">
            Ces bénéficiaires sont payés, mais leur montant a changé depuis le déclenchement du processus.
          </p>
          <ul className="flex flex-col gap-1.5">
            {resynchronisees.map((ligne) => (
              <li key={ligne.idBeneficiaire} className="text-sm text-neutral-800">
                <span className="font-medium">{ligne.nomPrenoms ?? '—'}</span>
                <span className="text-neutral-600"> ({ligne.matricule ?? '—'})</span>
                <span className="text-neutral-600"> — {ligne.fonctionRetenue}</span>
                <br />
                <span className="inline-flex items-center gap-1.5 text-xs">
                  <span className="text-neutral-600 line-through">
                    {formatMontantFCFA(ligne.ancienMontant)}
                  </span>
                  <ArrowRight className="h-3 w-3 shrink-0 text-neutral-600" aria-hidden="true" />
                  <span className="font-semibold text-neutral-900">
                    {formatMontantFCFA(ligne.nouveauMontant)}
                  </span>
                </span>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  );
}
