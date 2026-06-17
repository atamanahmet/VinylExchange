import * as React from "react";

import { cn } from "@/lib/utils";

function Textarea({ className, ...props }) {
  return (
    <textarea
      data-slot="textarea"
      className={cn(
        "flex min-h-[120px] w-full min-w-0 rounded-lg border border-accent-muted bg-surface-form px-2.5 py-2 text-base text-on-surface transition-colors outline-none placeholder:text-on-surface-muted focus-visible:border-brand-active focus-visible:ring-3 focus-visible:ring-brand-active/50 disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 aria-invalid:border-danger aria-invalid:ring-3 aria-invalid:ring-danger/20 md:text-sm",
        className,
      )}
      {...props}
    />
  );
}

export { Textarea };
