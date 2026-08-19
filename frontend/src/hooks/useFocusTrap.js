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

  useEffect(() => {
    const declencheur = document.activeElement;
    const container = containerRef.current;
    const focusables = container?.querySelectorAll(SELECTEUR_FOCUSABLE);
    (focusables?.[0] ?? container)?.focus();

    const gererClavier = (event) => {
      if (event.key === 'Escape') {
        onClose?.();
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
  }, [onClose]);

  return containerRef;
}
