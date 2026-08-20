import { ChevronLeft, ChevronRight } from 'lucide-react';
import { cn } from '../../utils/cn';

const TAILLE_SQUELETTE = 5;

export function DataTable({
  colonnes,
  donnees,
  cleLigne,
  chargement = false,
  pagination,
  actions,
  onLigneClick,
  messageVide = 'Aucun résultat.',
}) {
  const totalPages = pagination ? Math.max(1, Math.ceil(pagination.total / pagination.taille)) : 1;
  const pageActuelle = pagination?.page ?? 0;

  return (
    <div className="overflow-hidden rounded-lg border border-neutral-200 bg-white">
      {/* Sprint D.4 -- aria-busy pendant le chargement : les cinq lignes de
          squelette sont un signal purement visuel. Sans lui, un lecteur
          d'ecran annoncait un tableau de cinq lignes vides comme s'il etait
          charge. La zone est polie (pas d'interruption) et annonce le
          resultat une fois les donnees arrivees. */}
      <div className="overflow-x-auto" aria-busy={chargement} aria-live="polite">
        <table className="w-full text-left text-sm">
          <thead className="bg-neutral-100 text-xs uppercase text-neutral-700">
            <tr>
              {colonnes.map((colonne) => (
                <th key={colonne.cle} scope="col" className={cn('px-4 py-3 font-medium', colonne.className)}>
                  {colonne.entete}
                </th>
              ))}
              {actions && (
                <th scope="col" className="px-4 py-3 font-medium">
                  Actions
                </th>
              )}
            </tr>
          </thead>
          <tbody className="divide-y divide-neutral-200">
            {chargement &&
              Array.from({ length: TAILLE_SQUELETTE }).map((_, index) => (
                <tr key={`squelette-${index}`}>
                  {colonnes.map((colonne) => (
                    <td key={colonne.cle} className="px-4 py-3">
                      <div className="h-4 w-3/4 animate-pulse rounded bg-neutral-200" />
                    </td>
                  ))}
                  {actions && (
                    <td className="px-4 py-3">
                      <div className="h-4 w-16 animate-pulse rounded bg-neutral-200" />
                    </td>
                  )}
                </tr>
              ))}

            {!chargement && donnees.length === 0 && (
              <tr>
                <td
                  colSpan={colonnes.length + (actions ? 1 : 0)}
                  className="px-4 py-8 text-center text-neutral-600"
                >
                  {messageVide}
                </td>
              </tr>
            )}

            {!chargement &&
              donnees.map((ligne) => (
                <tr
                  key={cleLigne(ligne)}
                  onClick={onLigneClick ? () => onLigneClick(ligne) : undefined}
                  onKeyDown={
                    onLigneClick
                      ? (event) => {
                          if (event.key === 'Enter' || event.key === ' ') {
                            event.preventDefault();
                            onLigneClick(ligne);
                          }
                        }
                      : undefined
                  }
                  tabIndex={onLigneClick ? 0 : undefined}
                  // Sprint D.4 -- une <tr> focalisable qui reagit a Entree est
                  // un bouton pour l'utilisateur au clavier, mais reste une
                  // simple ligne pour le lecteur d'ecran, qui n'annonce donc
                  // aucune action possible. `role="button"` le dit. La ligne
                  // conserve sa semantique de rangee : le tableau reste lisible
                  // en mode navigation par tableau.
                  role={onLigneClick ? 'button' : undefined}
                  className={cn(
                    'transition-colors motion-reduce:transition-none hover:bg-neutral-100',
                    onLigneClick &&
                      'cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-primary-500'
                  )}
                >
                  {colonnes.map((colonne) => (
                    <td key={colonne.cle} className={cn('px-4 py-3 text-neutral-700', colonne.className)}>
                      {colonne.rendu ? colonne.rendu(ligne) : ligne[colonne.cle]}
                    </td>
                  ))}
                  {actions && (
                    <td className="px-4 py-3" onClick={(event) => event.stopPropagation()}>
                      {actions(ligne)}
                    </td>
                  )}
                </tr>
              ))}
          </tbody>
        </table>
      </div>

      {pagination && (
        <div className="flex items-center justify-between border-t border-neutral-200 px-4 py-3 text-sm text-neutral-600">
          <span>{pagination.total} résultat{pagination.total > 1 ? 's' : ''}</span>
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => pagination.onChangerPage(pageActuelle - 1)}
              disabled={pageActuelle <= 0}
              aria-label="Page précédente"
              className="rounded p-1.5 text-neutral-600 hover:bg-neutral-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 disabled:pointer-events-none disabled:opacity-40"
            >
              <ChevronLeft className="h-4 w-4" aria-hidden="true" />
            </button>
            <span>
              Page {pageActuelle + 1} sur {totalPages}
            </span>
            <button
              type="button"
              onClick={() => pagination.onChangerPage(pageActuelle + 1)}
              disabled={pageActuelle + 1 >= totalPages}
              aria-label="Page suivante"
              className="rounded p-1.5 text-neutral-600 hover:bg-neutral-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 disabled:pointer-events-none disabled:opacity-40"
            >
              <ChevronRight className="h-4 w-4" aria-hidden="true" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}