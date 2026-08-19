import { forwardRef } from 'react';
import { cn } from '../../utils/cn';

const Select = forwardRef(({ className, children, ...props }, ref) => (
  <select
    className={cn(
      'flex h-10 w-full rounded border border-neutral-300 bg-white px-3 py-2 text-sm text-neutral-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 disabled:cursor-not-allowed disabled:opacity-50',
      className
    )}
    ref={ref}
    {...props}
  >
    {children}
  </select>
));
Select.displayName = 'Select';

export { Select };