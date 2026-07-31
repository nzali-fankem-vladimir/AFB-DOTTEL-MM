import { forwardRef } from 'react';
import { cva } from 'class-variance-authority';
import { cn } from '../../utils/cn';

const alertVariants = cva(
  'relative w-full rounded border px-4 py-3 text-sm [&>svg]:absolute [&>svg]:left-4 [&>svg]:top-3.5 [&>svg~*]:pl-7',
  {
    variants: {
      variant: {
        default: 'border-neutral-200 bg-neutral-50 text-neutral-900',
        destructive: 'border-primary-500 bg-primary-50 text-primary-700 [&>svg]:text-primary-500',
        warning: 'border-amber-400 bg-amber-50 text-amber-800 [&>svg]:text-amber-500',
      },
    },
    defaultVariants: { variant: 'default' },
  }
);

const Alert = forwardRef(({ className, variant, ...props }, ref) => (
  <div ref={ref} role="alert" className={cn(alertVariants({ variant }), className)} {...props} />
));
Alert.displayName = 'Alert';

const AlertDescription = forwardRef(({ className, ...props }, ref) => (
  <p ref={ref} className={cn('leading-relaxed', className)} {...props} />
));
AlertDescription.displayName = 'AlertDescription';

export { Alert, AlertDescription };