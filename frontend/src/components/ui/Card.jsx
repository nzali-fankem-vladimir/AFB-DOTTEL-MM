import { cn } from '../../utils/cn';

const Card = ({ className, ...props }) => (
  <div className={cn('rounded-lg border border-neutral-200 bg-white shadow-sm', className)} {...props} />
);

const CardHeader = ({ className, ...props }) => (
  <div className={cn('flex flex-col gap-1.5 p-6', className)} {...props} />
);

// Sprint D.4 -- `h3` sautait un niveau : PageHeader pose le `h1` de chaque
// ecran, et la carte qui suit enchainait directement sur `h3`. Un lecteur
// d'ecran qui parcourt la page par titres annoncait donc un saut de niveau sur
// les 29 ecrans. `h2` est le niveau reel de ces titres -- une carte est la
// premiere subdivision sous le titre de page, et le titre d'une modale est le
// titre de son dialogue. Les trois `h3` internes (CreerUtilisateurPage,
// RecapitulatifResynchronisation) deviennent de ce fait correctement imbriques.
// AUCUN changement visuel : la taille et la graisse restent portees par les
// classes explicites ci-dessous, pas par le niveau de titre.
const CardTitle = ({ className, ...props }) => (
  <h2 className={cn('text-lg font-semibold text-neutral-900', className)} {...props} />
);

const CardContent = ({ className, ...props }) => (
  <div className={cn('p-6 pt-0', className)} {...props} />
);

const CardFooter = ({ className, ...props }) => (
  <div className={cn('flex items-center p-6 pt-0', className)} {...props} />
);

export { Card, CardHeader, CardTitle, CardContent, CardFooter };