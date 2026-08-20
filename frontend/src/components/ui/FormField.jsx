import { AlertCircle } from 'lucide-react';
import { Label } from './Label';
import { Input } from './Input';
import { cn } from '../../utils/cn';

// Sprint D.4 -- le message d'erreur etait visuellement sous le champ, mais rien
// ne les reliait dans le DOM : un lecteur d'ecran annoncait "Matricule, zone
// d'edition" et s'arretait la. L'utilisateur entendait le libelle du champ,
// jamais la raison du refus, et devait deviner en parcourant la page.
//   aria-describedby : rattache le message au champ, annonce a la prise de focus
//   aria-invalid     : dit que la valeur est refusee, avant meme le message
//   role="alert"     : fait annoncer le message des son apparition
export function FormField({ label, error, className, ...props }) {
  const idErreur = props.id ? `${props.id}-erreur` : undefined;

  return (
    <div className="flex flex-col gap-1.5">
      <Label htmlFor={props.id}>{label}</Label>
      <Input
        className={cn(error && 'border-primary-500 focus-visible:ring-primary-500', className)}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? idErreur : undefined}
        {...props}
      />
      {error && (
        <p id={idErreur} role="alert" className="flex items-center gap-1 text-xs text-neutral-800">
          <AlertCircle className="h-3.5 w-3.5 shrink-0 text-primary-500" aria-hidden="true" />
          {error.message}
        </p>
      )}
    </div>
  );
}