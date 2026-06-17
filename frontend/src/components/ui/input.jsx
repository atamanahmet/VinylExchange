import * as React from "react"

import { cn } from "@/lib/utils"

function Input({
  className,
  type,
  ...props
}) {
  return (
    <input
      type={type}
      data-slot="input"
      className={cn(
        "h-8 w-full min-w-0 rounded-lg border border-accent-muted bg-surface-form px-2.5 py-1 text-base text-on-surface transition-colors outline-none file:inline-flex file:h-6 file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-on-surface placeholder:text-on-surface-muted focus-visible:border-brand-active focus-visible:ring-3 focus-visible:ring-brand-active/50 disabled:pointer-events-none disabled:cursor-not-allowed disabled:bg-surface-form/50 disabled:opacity-50 aria-invalid:border-danger aria-invalid:ring-3 aria-invalid:ring-danger/20 md:text-sm",
        className
      )}
      {...props} />
  );
}

export { Input }
