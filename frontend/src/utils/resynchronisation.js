// Sprint MM.12 (P-1/P-2) : une ligne exclue à la validation ARH l'est pour deux
// raisons de nature très différente.
//
//   DURABLE      -- aucune grille pour cette fonction, ou uniquement des
//                   grilles rejetées. La ligne est exclue, le bénéficiaire
//                   n'est pas payé ce mois-ci (arbitrage du 2026-08-09).
//   TRANSITOIRE  -- une grille attend encore une signature CRH ou DRH. La
//                   validation est alors BLOQUÉE côté backend (409) : exclure
//                   quelqu'un de la paie parce qu'une signature tarde n'a pas
//                   de sens métier.
//
// Le backend ne renseigne etapeGrilleEnAttente que dans le second cas -- c'est
// le seul discriminant, et il est centralisé ici plutôt que testé en dur dans
// les pages.
export function estBloquante(ligne) {
  return Boolean(ligne?.etapeGrilleEnAttente);
}
