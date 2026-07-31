import { Label } from './Label';
import { Input } from './Input';
import { cn } from '../../utils/cn';

export function FormField({ label, error, className, ...props }) {
  return (
    <div className="flex flex-col gap-1.5">
      <Label htmlFor={props.id}>{label}</Label>
      <Input className={cn(error && 'border-primary-500 focus:ring-primary-500', className)} {...props} />
      {error && <p className="text-xs text-primary-500">{error.message}</p>}
    </div>
  );
}