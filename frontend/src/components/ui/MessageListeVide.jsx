import { Button } from './Button';

// Sprint D.3 -- "il n'y a rien" et "il n'y a rien QUI CORRESPONDE" sont deux
// situations differentes. Les confondre derriere un meme "Aucun resultat."
// laisse croire que la base est vide alors qu'un filtre est actif, et n'offre
// aucune sortie. Ce composant porte le second cas, avec le moyen d'en sortir ;
// le premier cas reste une simple chaine, propre a chaque ecran.
//
// Partage entre les listes pour que la formulation et l'action de sortie
// soient identiques partout (coherence entre ecrans, cf. DESIGN.md).
export function MessageListeVide({ message, onEffacerFiltres }) {
  return (
    <span className="flex flex-col items-center gap-3">
      <span>{message}</span>
      {onEffacerFiltres && (
        <Button variant="outline" size="sm" onClick={onEffacerFiltres}>
          Effacer les filtres
        </Button>
      )}
    </span>
  );
}
