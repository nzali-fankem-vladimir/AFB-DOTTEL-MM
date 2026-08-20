import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';

export function AppLayout() {
  return (
    <div className="flex h-screen bg-neutral-50">
      {/* Sprint D.4 -- un lien d'evitement ("Aller au contenu principal") a ete
          essaye ici puis RETIRE sur decision du metier. Gain mesure : 9
          tabulations ramenees a 2 pour atteindre le premier controle du
          contenu, a chaque changement d'ecran. Juge disproportionne au regard
          de l'usage reel du module, ou la navigation se fait a la souris.
          Ecart assume au critere WCAG 2.4.1 (niveau A), consigne dans
          docs/chantier-design-frontend/RECAPITULATIF_DESIGN.md section 4.
          Ne pas le reintroduire sans en reparler. */}
      <Sidebar />
      {/* min-w-0 (Sprint D.4) : un enfant flex a `min-width: auto`, ce qui
          l'empeche de retrecir sous la largeur de son contenu. Un DataTable de
          sept colonnes a 1024 px pousserait donc <main> au-dela de la fenetre
          et ferait defiler la PAGE entiere horizontalement, au lieu du seul
          tableau dans son cadre `overflow-x-auto`. Le comportement actuel ne
          tient qu'a un effet de bord (overflow-y:auto force overflow-x:auto,
          qui annule min-width:auto) : trop fragile pour un verrou de mise en
          page. Rendu explicite. */}
      <main className="fond-filigrane min-w-0 flex-1 overflow-y-auto">
        <Outlet />
      </main>
    </div>
  );
}
