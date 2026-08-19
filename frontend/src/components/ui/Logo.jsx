import logoAfriland from '../../assets/logo afriland.png';
import logoAfrilandEmbleme from '../../assets/logo-afriland-embleme.png';
import { cn } from '../../utils/cn';

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
export function Logo({ size = 'md', variant = 'complet', className, ...props }) {
  return (
    <img
      src={SOURCES[variant]}
      alt="Afriland First Bank"
      className={cn(SIZES[size], 'w-auto object-contain', className)}
      {...props}
    />
  );
}
