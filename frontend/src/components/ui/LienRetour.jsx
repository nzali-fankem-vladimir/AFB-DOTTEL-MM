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
        className="-mx-1 -my-1 inline-flex items-center gap-1.5 rounded px-1 py-1 text-sm font-medium text-neutral-600 transition-colors hover:text-neutral-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 focus-visible:ring-offset-2"
      >
        <ArrowLeft className="h-4 w-4" aria-hidden="true" />
        {label}
      </Link>
    </div>
  );
}