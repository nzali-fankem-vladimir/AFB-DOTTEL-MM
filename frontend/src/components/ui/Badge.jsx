import { cva } from 'class-variance-authority';
import { cn } from '../../utils/cn';

const badgeVariants = cva(
  'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium',
  {
    variants: {
      variant: {
        success: 'bg-emerald-50 text-emerald-700',
        neutral: 'bg-neutral-100 text-neutral-500',
        warning: 'bg-amber-50 text-amber-700',
        info: 'bg-sky-50 text-sky-700',
        destructive: 'bg-primary-50 text-primary-700',
      },
    },
    defaultVariants: { variant: 'neutral' },
  }
);

export function Badge({ className, variant, ...props }) {
  return <span className={cn(badgeVariants({ variant }), className)} {...props} />;
}