import logoAfriland from '../../assets/logo afriland.png';
import logoAfrilandEmbleme from '../../assets/logo-afriland-embleme.png';
import { cn } from '../../utils/cn';

// ATTENTION -- l'echelle n'est PAS croissante : `sm` (h-15) est plus grand que
// `lg` (h-12). Ce n'est pas un oubli a corriger. `sm` n'a qu'un seul appelant,
// le logotype complet de la sidebar (Sidebar.jsx), et la presence du logo AFB
// en tete de navigation releve du FIGE de la charte (DESIGN.md). Ramener `sm`
// sous `md`/`lg` par souci de coherence de nommage retrecirait le logo de
// marque : signale au Sprint D.3, deliberement laisse en l'etat.
const SIZES = {
  sm: 'h-15',
  md: 'h-9',
  lg: 'h-12',
};

const SOURCES = {
  complet: logoAfriland,
  embleme: logoAfrilandEmbleme,
};

// variant "embleme" : le motif rond seul, sans le nom de la banque -- utilise
// quand l'espace horizontal ne permet plus d'afficher le logotype complet
// (ex. sidebar reduite, cf. Sidebar.jsx).

// Sprint D.4 -- dimensions intrinseques des deux fichiers PNG. Sans elles, le
// navigateur ne reserve aucune place avant d'avoir telecharge l'image : le
// haut de la sidebar et l'en-tete de la page de connexion sautaient au
// chargement (decalage cumulatif de mise en page). La hauteur reelle reste
// pilotee par les classes SIZES ci-dessus ; ces attributs ne servent qu'a
// donner le RATIO au navigateur.
const DIMENSIONS = {
  complet: { width: 832, height: 249 },
  embleme: { width: 178, height: 161 },
};

export function Logo({ size = 'md', variant = 'complet', className, ...props }) {
  return (
    <img
      src={SOURCES[variant]}
      alt="Afriland First Bank"
      width={DIMENSIONS[variant].width}
      height={DIMENSIONS[variant].height}
      className={cn(SIZES[size], 'w-auto object-contain', className)}
      {...props}
    />
  );
}
