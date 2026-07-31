import { Link } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';

// Navigation secondaire (pas une action metier) : pas de rouge primaire E30613,
// texte neutre discret qui se fonce legerement au survol. A placer avant
// PageHeader sur toute page de detail ou de sous-formulaire qui a besoin d'un
// retour explicite vers sa page parente.
export function LienRetour({ to, label }) {
  return (
    <div className="px-8 pt-4">
      <Link
        to={to}
        className="inline-flex items-center gap-1.5 text-sm font-medium text-neutral-500 transition-colors hover:text-neutral-900"
      >
        <ArrowLeft className="h-4 w-4" />
        {label}
      </Link>
    </div>
  );
}