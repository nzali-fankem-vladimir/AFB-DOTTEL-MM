import { forwardRef } from 'react';
import { cn } from '../../utils/cn';

// Sprint D.4, bordure neutral-500 (et non plus neutral-300) -- decision prise
// avec le metier. Dans les douze formulaires, le champ blanc est pose sur une
// Card elle aussi blanche : la bordure est le SEUL repere qui dit ou l'on
// saisit. neutral-300 (#C8C8C8) donnait 1,67:1 sur blanc, pour un seuil WCAG
// 1.4.11 de 3:1 sur les contours qui identifient un controle. neutral-500
// (#8A8A8A) donne 3,45:1. Reste un gris de l'echelle deja declaree, donc
// "bordure grise" au sens de DESIGN.md, dont la section LIBRE couvre
// explicitement les contrastes d'accessibilite.
// Placeholder neutral-600 : neutral-400 tombait a 2,30:1.
// `autoComplete` / `spellCheck` (Sprint D.4) : aucun champ de DOTTEL ne decrit
// la personne qui saisit -- un ARH tape le matricule d'un TIERS, un ADMIN cree
// le compte de quelqu'un d'autre. La saisie semi-automatique du navigateur
// proposerait donc les coordonnees du saisisseur sur la fiche d'un collegue, et
// le correcteur orthographique souligne en rouge les matricules et les codes de
// fonction (AFB-2026-0148, CHARGE_INNOVATION), qui ne sont pas des mots. Les
// deux sont neutralises par defaut, surchargeables au cas par cas.
const Input = forwardRef(({ className, type, autoComplete = 'off', spellCheck = false, ...props }, ref) => (
  <input
    type={type}
    autoComplete={autoComplete}
    spellCheck={spellCheck}
    className={cn(
      'flex h-10 w-full rounded border border-neutral-500 bg-white px-3 py-2 text-sm text-neutral-900 placeholder:text-neutral-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 disabled:cursor-not-allowed disabled:opacity-50',
      className
    )}
    ref={ref}
    {...props}
  />
));
Input.displayName = 'Input';

export { Input };