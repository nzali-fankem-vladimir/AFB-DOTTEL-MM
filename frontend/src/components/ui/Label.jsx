import { forwardRef } from 'react';
import * as LabelPrimitive from '@radix-ui/react-label';
import { cn } from '../../utils/cn';

// `obligatoire` (Sprint D.3) : l'asterisque rouge n'existait qu'a un seul
// endroit (RetournerProcessusModal), recopie a la main. Le porter par le Label
// garantit qu'il est identique partout plutot que reinvente formulaire par
// formulaire. `aria-hidden` : le champ porte deja `required`, que les lecteurs
// d'ecran annoncent -- l'asterisque est un repere visuel, pas une seconde
// annonce. Defaut a false : tous les appels existants sont inchanges.
const Label = forwardRef(({ className, obligatoire = false, children, ...props }, ref) => (
  <LabelPrimitive.Root
    ref={ref}
    className={cn('text-sm font-medium text-neutral-900 leading-none peer-disabled:opacity-70', className)}
    {...props}
  >
    {children}
    {obligatoire && (
      <span className="ml-0.5 text-primary-500" aria-hidden="true">
        *
      </span>
    )}
  </LabelPrimitive.Root>
));
Label.displayName = LabelPrimitive.Root.displayName;

export { Label };