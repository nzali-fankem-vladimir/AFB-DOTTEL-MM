export function definirParametre(searchParams, cle, valeur) {
  const suivant = new URLSearchParams(searchParams);
  if (valeur === '' || valeur === null || valeur === undefined) {
    suivant.delete(cle);
  } else {
    suivant.set(cle, valeur);
  }
  return suivant;
}

// Sprint D.3 : remise a zero d'un jeu de filtres en une fois, pour l'action
// "Effacer les filtres" des etats vides. Passe par definirParametre plutot que
// de reconstruire une URLSearchParams : la mecanique installee en MM.9 reste
// le seul point de verite, et les parametres hors filtres sont preserves.
export function effacerParametres(searchParams, ...cles) {
  return cles.reduce((accumulateur, cle) => definirParametre(accumulateur, cle, ''), searchParams);
}

export function construireRetour(chemin, searchParams) {
  const query = searchParams.toString();
  return query ? `${chemin}?${query}` : chemin;
}
