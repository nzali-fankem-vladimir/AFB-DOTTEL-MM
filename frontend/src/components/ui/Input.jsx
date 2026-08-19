import { forwardRef } from 'react';
import { cn } from '../../utils/cn';

const Input = forwardRef(({ className, type, ...props }, ref) => (
  <input
    type={type}
    className={cn(
      'flex h-10 w-full rounded border border-neutral-300 bg-white px-3 py-2 text-sm text-neutral-900 placeholder:text-neutral-400 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 disabled:cursor-not-allowed disabled:opacity-50',
      className
    )}
    ref={ref}
    {...props}
  />
));
Input.displayName = 'Input';

export { Input };