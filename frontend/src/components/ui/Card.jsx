import { cn } from '../../utils/cn';

const Card = ({ className, ...props }) => (
  <div className={cn('rounded-lg border border-neutral-200 bg-white shadow-sm', className)} {...props} />
);

const CardHeader = ({ className, ...props }) => (
  <div className={cn('flex flex-col gap-1.5 p-6', className)} {...props} />
);

const CardTitle = ({ className, ...props }) => (
  <h3 className={cn('text-lg font-semibold text-neutral-900', className)} {...props} />
);

const CardContent = ({ className, ...props }) => (
  <div className={cn('p-6 pt-0', className)} {...props} />
);

const CardFooter = ({ className, ...props }) => (
  <div className={cn('flex items-center p-6 pt-0', className)} {...props} />
);

export { Card, CardHeader, CardTitle, CardContent, CardFooter };