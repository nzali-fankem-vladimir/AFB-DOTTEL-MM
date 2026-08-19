import { forwardRef } from 'react';
import { cn } from '../../utils/cn';

const Textarea = forwardRef(({ className, ...props }, ref) => (
  <textarea
    ref={ref}
    className={cn(
      'flex min-h-[80px] w-full rounded-sm border border-neutral-300 bg-white px-3 py-2 text-sm text-neutral-900 placeholder:text-neutral-400',
      'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500',
      'disabled:cursor-not-allowed disabled:opacity-50',
      className
    )}
    {...props}
  />
));
Textarea.displayName = 'Textarea';

export { Textarea };