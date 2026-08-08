export function definirParametre(searchParams, cle, valeur) {
  const suivant = new URLSearchParams(searchParams);
  if (valeur === '' || valeur === null || valeur === undefined) {
    suivant.delete(cle);
  } else {
    suivant.set(cle, valeur);
  }
  return suivant;
}

export function construireRetour(chemin, searchParams) {
  const query = searchParams.toString();
  return query ? `${chemin}?${query}` : chemin;
}
