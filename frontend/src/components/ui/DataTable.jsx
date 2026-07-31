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
}) {
  const totalPages = pagination ? Math.max(1, Math.ceil(pagination.total / pagination.taille)) : 1;
  const pageActuelle = pagination?.page ?? 0;

  return (
    <div className="overflow-hidden rounded-lg border border-neutral-200 bg-white">
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead className="bg-neutral-100 text-xs uppercase text-neutral-500">
            <tr>
              {colonnes.map((colonne) => (
                <th key={colonne.cle} className={cn('px-4 py-3 font-medium', colonne.className)}>
                  {colonne.entete}
                </th>
              ))}
              {actions && <th className="px-4 py-3 font-medium">Actions</th>}
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
                  className="px-4 py-8 text-center text-neutral-500"
                >
                  Aucun résultat.
                </td>
              </tr>
            )}

            {!chargement &&
              donnees.map((ligne) => (
                <tr
                  key={cleLigne(ligne)}
                  onClick={onLigneClick ? () => onLigneClick(ligne) : undefined}
                  className={cn(
                    'transition-colors hover:bg-neutral-100',
                    onLigneClick && 'cursor-pointer'
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
        <div className="flex items-center justify-between border-t border-neutral-200 px-4 py-3 text-sm text-neutral-500">
          <span>{pagination.total} résultat{pagination.total > 1 ? 's' : ''}</span>
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => pagination.onChangerPage(pageActuelle - 1)}
              disabled={pageActuelle <= 0}
              aria-label="Page précédente"
              className="rounded p-1.5 text-neutral-500 hover:bg-neutral-100 disabled:pointer-events-none disabled:opacity-40"
            >
              <ChevronLeft className="h-4 w-4" />
            </button>
            <span>
              Page {pageActuelle + 1} sur {totalPages}
            </span>
            <button
              type="button"
              onClick={() => pagination.onChangerPage(pageActuelle + 1)}
              disabled={pageActuelle + 1 >= totalPages}
              aria-label="Page suivante"
              className="rounded p-1.5 text-neutral-500 hover:bg-neutral-100 disabled:pointer-events-none disabled:opacity-40"
            >
              <ChevronRight className="h-4 w-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}