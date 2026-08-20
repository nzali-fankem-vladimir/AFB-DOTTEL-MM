import { useState, useRef, useEffect } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  UserPlus,
  Users,
  FileCheck,
  Wallet,
  BarChart3,
  Shield,
  Briefcase,
  ChevronUp,
  ChevronLeft,
  ChevronRight,
  LogOut,
  Upload,
} from 'lucide-react';
import { Logo } from '../ui/Logo';
import { useAuth } from '../../contexts/AuthContext';
import { cn } from '../../utils/cn';

// L'activation ne repose plus sur le "end" booleen de NavLink (trop
// grossier : soit correspondance exacte, soit prefixe illimite -- aucun des
// deux ne gere a la fois "/beneficiaires/import a sa propre entree" ET
// "/grilles-tarifaires/creer n'en a pas mais doit activer son parent").
// A la place, trouverHrefActif() calcule le href le PLUS SPECIFIQUE (le plus
// long) parmi les entrees dont le chemin courant est une sous-route, ce qui
// gere les deux cas sans configuration par entree :
// - /beneficiaires/import active "Importer beneficiaires" (correspondance
//   exacte, plus longue que le prefixe "/beneficiaires").
// - /grilles-tarifaires/creer et /grilles-tarifaires/historique/:code
//   n'ont pas d'entree dediee : seul le prefixe "/grilles-tarifaires"
//   correspond, qui devient donc le plus specifique et s'active.
const NAV_LINKS = [
  // Dashboard : ARH, DRH uniquement -- CLAUDE.md section 8 groupe 6 ne donne
  // pas acces au CRH (decision actee le 2026-07-30).
  { href: '/dashboard', label: 'Tableau de bord', icon: LayoutDashboard, roles: ['ARH', 'DRH'] },
  { href: '/enrolement', label: 'Enrôlement', icon: UserPlus, roles: ['EMPLOYE'] },
  // Sprint MM.14 (ecart E2) : la DRH consulte les beneficiaires en lecture
  // seule. Roles STRICTEMENT identiques au ProtectedRoute de /beneficiaires
  // dans AppRouter.jsx (rappel de l'audit 6F.9). L'entree d'import juste en
  // dessous reste ARH seul : elle ecrit.
  { href: '/beneficiaires', label: 'Bénéficiaires', icon: Users, roles: ['ARH', 'DRH'] },
  { href: '/beneficiaires/import', label: 'Importer bénéficiaires', icon: Upload, roles: ['ARH'] },
  { href: '/processus', label: 'Processus mensuel', icon: FileCheck, roles: ['ARH', 'CRH', 'DRH'] },
  { href: '/grilles-tarifaires', label: 'Grilles tarifaires', icon: Wallet, roles: ['ARH', 'ADMIN'] },
  // Sprint MM.12 : le CRH entre dans le workflow des grilles et gagne donc
  // cette entree. Roles STRICTEMENT identiques au ProtectedRoute de
  // /grilles-tarifaires/valider dans AppRouter.jsx (audit 6F.9) -- l'ARH garde
  // l'entree de consultation ci-dessus et ne voit PAS celle-ci.
  { href: '/grilles-tarifaires/valider', label: 'Grilles tarifaires', icon: Wallet, roles: ['CRH', 'DRH'] },
  { href: '/reporting/historique', label: 'Historique annuel', icon: BarChart3, roles: ['DRH'] },
  { href: '/reporting/audit', label: "Journal d'audit", icon: BarChart3, roles: ['DRH'] },
  { href: '/admin/utilisateurs', label: 'Administration', icon: Shield, roles: ['ADMIN'] },
  { href: '/admin/fonctions-eligibles', label: 'Fonctions éligibles', icon: Briefcase, roles: ['ADMIN'] },
];

function trouverHrefActif(pathname, hrefs) {
  const correspondances = hrefs.filter(
    (href) => pathname === href || pathname.startsWith(`${href}/`)
  );
  if (correspondances.length === 0) return null;
  return correspondances.reduce((plusSpecifique, actuel) =>
    actuel.length > plusSpecifique.length ? actuel : plusSpecifique
  );
}

const CLE_SIDEBAR_REDUITE = 'dottel-sidebar-reduite';

function initiales(user) {
  const p = user?.prenom?.[0] ?? '';
  const n = user?.nom?.[0] ?? '';
  return (p + n).toUpperCase() || '?';
}

export function Sidebar() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [reduite, setReduite] = useState(() => localStorage.getItem(CLE_SIDEBAR_REDUITE) === 'true');
  const menuRef = useRef(null);
  const boutonCompteRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setIsMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (!isMenuOpen) return undefined;
    const handleEchap = (event) => {
      if (event.key !== 'Escape') return;
      setIsMenuOpen(false);
      // Sprint D.4 -- sans ce rappel, fermer le menu par Echap detruisait
      // l'element focalise (le bouton "Deconnexion") et le focus retombait sur
      // <body> : la tabulation repartait du tout debut de la page.
      boutonCompteRef.current?.focus();
    };
    document.addEventListener('keydown', handleEchap);
    return () => document.removeEventListener('keydown', handleEchap);
  }, [isMenuOpen]);

  useEffect(() => {
    localStorage.setItem(CLE_SIDEBAR_REDUITE, String(reduite));
  }, [reduite]);

  const visibleLinks = NAV_LINKS.filter((link) => {
    if (!link.roles) return true;
    return link.roles.includes(user?.role);
  });

  const hrefActif = trouverHrefActif(location.pathname, visibleLinks.map((link) => link.href));

  return (
    <aside
      className={cn(
        // Sprint D.4 -- `transition-[width]` est signale par le detecteur
        // (regle layout-transition) et par les Web Interface Guidelines :
        // animer une propriete de mise en page force le navigateur a
        // recalculer la position de tout le contenu a droite, une fois par
        // image. CONSERVE deliberement : la barre POUSSE le contenu, un
        // `transform` la ferait glisser par-dessus en laissant un vide. Le
        // cout reel est borne -- 200 ms, au clic sur "Reduire" seulement,
        // jamais au defilement ni a la frappe. `motion-reduce` traite le vrai
        // risque, celui des personnes sensibles au mouvement.
        'flex h-screen shrink-0 flex-col bg-neutral-950 text-white',
        'transition-[width] duration-200 ease-in-out motion-reduce:transition-none',
        reduite ? 'w-20' : 'w-64'
      )}
    >
      <div className={cn('flex items-center py-6', reduite ? 'justify-center px-2' : 'gap-2 px-5')}>
        {reduite ? (
          <Logo variant="embleme" className="h-8 w-auto brightness-0 invert" />
        ) : (
          <Logo size="sm" className="brightness-0 invert" />
        )}
      </div>

      <nav aria-label="Navigation principale" className="flex-1 space-y-1 overflow-y-auto px-3">
        {visibleLinks.map((link) => (
          <NavLink
            key={link.href}
            to={link.href}
            title={reduite ? link.label : undefined}
            className={cn(
              'flex items-center gap-3 rounded px-3 py-2.5 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2 focus-visible:ring-offset-neutral-950',
              reduite && 'justify-center px-0',
              link.href === hrefActif
                ? 'bg-primary-500 text-white'
                : 'text-neutral-300 hover:bg-neutral-900 hover:text-white'
            )}
          >
            <link.icon className="h-4 w-4 shrink-0" aria-hidden="true" />
            {/* Sprint D.4 -- en mode replie, le libelle n'etait porte que par
                `title`. Un lecteur d'ecran annoncait donc au mieux une
                infobulle, au pire rien du tout : la navigation devenait onze
                liens sans nom. `sr-only` porte le vrai nom accessible ;
                `title` reste pour l'infobulle a la souris. */}
            <span className={cn(reduite && 'sr-only')}>{link.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="border-t border-neutral-800 px-3 py-3" ref={menuRef}>
        <button
          type="button"
          onClick={() => setReduite((valeur) => !valeur)}
          title={reduite ? 'Agrandir la barre latérale' : 'Réduire la barre latérale'}
          className={cn(
            'mb-2 flex w-full items-center gap-2 rounded px-2 py-2 text-xs font-medium text-neutral-400 hover:bg-neutral-900 hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2 focus-visible:ring-offset-neutral-950',
            reduite && 'justify-center'
          )}
        >
          {reduite ? (
            <ChevronRight className="h-4 w-4 shrink-0" aria-hidden="true" />
          ) : (
            <ChevronLeft className="h-4 w-4 shrink-0" aria-hidden="true" />
          )}
          {/* Meme raison que les liens ci-dessus : replie, ce bouton n'etait
              qu'un chevron sans nom. Le libelle dit aussi ce qu'il FAIT une
              fois replie -- "Reduire" serait faux dans cet etat. */}
          <span className={cn(reduite && 'sr-only')}>
            {reduite ? 'Agrandir la barre latérale' : 'Réduire'}
          </span>
        </button>

        <div className="relative">
          <button
            type="button"
            ref={boutonCompteRef}
            onClick={() => setIsMenuOpen((open) => !open)}
            title={reduite ? (user?.nom ?? 'Utilisateur') : undefined}
            aria-expanded={isMenuOpen}
            aria-haspopup="menu"
            // Sprint D.4 -- verifie au clavier : deplie, ce bouton etait annonce
            // "MBARGAARH", le nom et le role etant deux <span> adjacents sans
            // separation lisible. Un nom accessible explicite remplace la
            // concatenation, dans les deux etats.
            aria-label={`Compte de ${user?.nom ?? user?.email ?? 'utilisateur'}${user?.role ? `, rôle ${user.role}` : ''} — ouvrir le menu`}
            className={cn(
              'flex w-full items-center gap-3 rounded px-2 py-2 text-left hover:bg-neutral-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2 focus-visible:ring-offset-neutral-950',
              reduite && 'justify-center'
            )}
          >
            {/* Tout le contenu est decoratif : le nom accessible est porte par
                l'aria-label ci-dessus, dans les deux etats. */}
            {reduite ? (
              <span
                aria-hidden="true"
                className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-neutral-800 text-xs font-semibold text-white"
              >
                {initiales(user)}
              </span>
            ) : (
              <>
                <div className="flex min-w-0 flex-1 flex-col">
                  <span className="truncate text-sm font-semibold text-white">
                    {user?.nom ?? user?.email ?? 'Utilisateur'}
                  </span>
                  <span className="text-xxs uppercase tracking-wider text-neutral-400">
                    {user?.role}
                  </span>
                </div>
                <ChevronUp
                  aria-hidden="true"
                  className={cn(
                    'h-4 w-4 shrink-0 text-neutral-400 transition-transform motion-reduce:transition-none',
                    !isMenuOpen && 'rotate-180'
                  )}
                />
              </>
            )}
          </button>

          {isMenuOpen && (
            <div
              role="menu"
              className={cn(
                'absolute bottom-full left-0 mb-2 overflow-hidden rounded-md border border-neutral-800 bg-neutral-900 py-1 shadow-xl',
                reduite ? 'w-40' : 'w-full'
              )}
            >
              <button
                type="button"
                role="menuitem"
                onClick={logout}
                className="flex w-full items-center gap-3 px-3 py-2.5 text-sm font-medium text-primary-300 hover:bg-neutral-800 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-white"
              >
                <LogOut className="h-4 w-4" aria-hidden="true" />
                Déconnexion
              </button>
            </div>
          )}
        </div>
      </div>
    </aside>
  );
}