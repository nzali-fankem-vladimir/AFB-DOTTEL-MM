import { AlertCircle } from 'lucide-react';
import { Label } from './Label';
import { Input } from './Input';
import { cn } from '../../utils/cn';

export function FormField({ label, error, className, ...props }) {
  return (
    <div className="flex flex-col gap-1.5">
      <Label htmlFor={props.id}>{label}</Label>
      <Input className={cn(error && 'border-primary-500 focus-visible:ring-primary-500', className)} {...props} />
      {error && (
        <p className="flex items-center gap-1 text-xs text-neutral-800">
          <AlertCircle className="h-3.5 w-3.5 shrink-0 text-primary-500" aria-hidden="true" />
          {error.message}
        </p>
      )}
    </div>
  );
}