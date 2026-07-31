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
  { href: '/beneficiaires', label: 'Bénéficiaires', icon: Users, roles: ['ARH'] },
  { href: '/beneficiaires/import', label: 'Importer bénéficiaires', icon: Upload, roles: ['ARH'] },
  { href: '/processus', label: 'Processus mensuel', icon: FileCheck, roles: ['ARH', 'CRH', 'DRH'] },
  { href: '/grilles-tarifaires', label: 'Grilles tarifaires', icon: Wallet, roles: ['ARH', 'ADMIN'] },
  { href: '/grilles-tarifaires/valider', label: 'Grilles tarifaires', icon: Wallet, roles: ['DRH'] },
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
        'flex h-screen shrink-0 flex-col bg-neutral-950 text-white transition-[width] duration-200 ease-in-out',
        reduite ? 'w-20' : 'w-64'
      )}
    >
      <div className={cn('flex items-center py-6', reduite ? 'justify-center px-2' : 'gap-2 px-5')}>
        <Logo size="sm" className="brightness-0 invert" />
      </div>

      <nav className="flex-1 space-y-1 overflow-y-auto px-3">
        {visibleLinks.map((link) => (
          <NavLink
            key={link.href}
            to={link.href}
            title={reduite ? link.label : undefined}
            className={cn(
              'flex items-center gap-3 rounded px-3 py-2.5 text-sm font-medium transition-colors',
              reduite && 'justify-center px-0',
              link.href === hrefActif
                ? 'bg-primary-500 text-white'
                : 'text-neutral-300 hover:bg-neutral-900 hover:text-white'
            )}
          >
            <link.icon className="h-4 w-4 shrink-0" />
            {!reduite && link.label}
          </NavLink>
        ))}
      </nav>

      <div className="border-t border-neutral-800 px-3 py-3" ref={menuRef}>
        <button
          type="button"
          onClick={() => setReduite((valeur) => !valeur)}
          title={reduite ? 'Agrandir la barre latérale' : 'Réduire la barre latérale'}
          className={cn(
            'mb-2 flex w-full items-center gap-2 rounded px-2 py-2 text-xs font-medium text-neutral-400 hover:bg-neutral-900 hover:text-white',
            reduite && 'justify-center'
          )}
        >
          {reduite ? <ChevronRight className="h-4 w-4 shrink-0" /> : <ChevronLeft className="h-4 w-4 shrink-0" />}
          {!reduite && 'Réduire'}
        </button>

        <div className="relative">
          <button
            type="button"
            onClick={() => setIsMenuOpen((open) => !open)}
            title={reduite ? (user?.nom ?? 'Utilisateur') : undefined}
            className={cn(
              'flex w-full items-center gap-3 rounded px-2 py-2 text-left hover:bg-neutral-900',
              reduite && 'justify-center'
            )}
          >
            {reduite ? (
              <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-neutral-800 text-xs font-semibold text-white">
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
                  className={cn(
                    'h-4 w-4 shrink-0 text-neutral-400 transition-transform',
                    !isMenuOpen && 'rotate-180'
                  )}
                />
              </>
            )}
          </button>

          {isMenuOpen && (
            <div
              className={cn(
                'absolute bottom-full left-0 mb-2 overflow-hidden rounded-md border border-neutral-800 bg-neutral-900 py-1 shadow-xl',
                reduite ? 'w-40' : 'w-full'
              )}
            >
              <button
                type="button"
                onClick={logout}
                className="flex w-full items-center gap-3 px-3 py-2.5 text-sm font-medium text-primary-300 hover:bg-neutral-800"
              >
                <LogOut className="h-4 w-4" />
                Déconnexion
              </button>
            </div>
          )}
        </div>
      </div>
    </aside>
  );
}