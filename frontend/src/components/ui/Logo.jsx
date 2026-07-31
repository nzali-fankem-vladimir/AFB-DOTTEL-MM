import logoAfriland from '../../assets/logo afriland.png';
import { cn } from '../../utils/cn';

const SIZES = {
  sm: 'h-15',
  md: 'h-9',
  lg: 'h-12',
};

export function Logo({ size = 'md', className, ...props }) {
  return (
    <img
      src={logoAfriland}
      alt="Afriland First Bank"
      className={cn(SIZES[size], 'w-auto object-contain', className)}
      {...props}
    />
  );
}
