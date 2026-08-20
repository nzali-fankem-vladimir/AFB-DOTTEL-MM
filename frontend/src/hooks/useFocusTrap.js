import { useEffect, useRef } from 'react';

const SELECTEUR_FOCUSABLE =
  'a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])';

// Piege de focus pour une modale construite a la main (sans primitive Radix) :
// focus initial dans la modale, Tab/Shift+Tab boucles a l'interieur, Echap
// declenche `onClose`, et le focus revient au declencheur d'origine a la
// fermeture. Passer `undefined` comme `onClose` desactive la fermeture au
// clavier (ex. soumission en cours) sans desactiver le piege de focus.
export function useFocusTrap(onClose) {
  const containerRef = useRef(null);
  // Sprint D.4 -- `onClose` est recalcule a chaque rendu par tous les
  // appelants (`enCours ? undefined : onAnnuler`). En dependance directe de
  // l'effet, il le faisait donc se demonter puis se remonter a chaque
  // basculement d'etat : le focus retombait sur le PREMIER element de la
  // modale au moment ou l'utilisateur soumettait, et le declencheur d'origine
  // recevait un focus parasite en cours de route. La reference garde la
  // derniere fonction sans reveiller l'effet, qui ne depend plus que du
  // montage -- le seul moment ou un piege de focus doit s'installer.
  const onCloseRef = useRef(onClose);
  onCloseRef.current = onClose;

  useEffect(() => {
    const declencheur = document.activeElement;
    const container = containerRef.current;
    const focusables = container?.querySelectorAll(SELECTEUR_FOCUSABLE);
    (focusables?.[0] ?? container)?.focus();

    const gererClavier = (event) => {
      if (event.key === 'Escape') {
        onCloseRef.current?.();
        return;
      }
      if (event.key !== 'Tab' || !container) return;
      const elements = Array.from(container.querySelectorAll(SELECTEUR_FOCUSABLE));
      if (elements.length === 0) return;
      const premier = elements[0];
      const dernier = elements[elements.length - 1];
      if (event.shiftKey && document.activeElement === premier) {
        event.preventDefault();
        dernier.focus();
      } else if (!event.shiftKey && document.activeElement === dernier) {
        event.preventDefault();
        premier.focus();
      }
    };

    document.addEventListener('keydown', gererClavier);
    return () => {
      document.removeEventListener('keydown', gererClavier);
      declencheur?.focus?.();
    };
    // Volontairement vide : le piege s'installe au montage de la modale et se
    // defait a sa fermeture, jamais entre les deux. Voir onCloseRef ci-dessus.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return containerRef;
}
