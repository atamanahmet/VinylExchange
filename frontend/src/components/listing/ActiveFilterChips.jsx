import { X } from "lucide-react";

import { cn } from "@/lib/utils";

/**
 * Removable pills for every applied filter. Removing a chip applies
 * immediately, the panel's Apply button is only for batched edits.
 */
export default function ActiveFilterChips({ chips, onRemove, onClear, className }) {
  if (!chips.length) {
    return null;
  }

  return (
    <div className={cn("flex flex-wrap items-center gap-2", className)}>
      {chips.map((chip) => (
        <button
          key={chip.key}
          type="button"
          onClick={() => onRemove(chip)}
          className="group inline-flex max-w-full items-center gap-1.5 rounded-full border border-surface-4 bg-surface-2 py-1 pl-3 pr-2 text-xs font-medium text-on-surface-dim transition-colors hover:border-brand hover:text-on-surface"
        >
          <span className="truncate">{chip.label}</span>
          <X className="size-3.5 shrink-0 text-on-surface-muted transition-colors group-hover:text-brand-fg" />
          <span className="sr-only">Remove filter</span>
        </button>
      ))}

      {chips.length > 1 && (
        <button
          type="button"
          onClick={onClear}
          className="text-xs font-medium text-on-surface-muted underline underline-offset-4 transition-colors hover:text-brand-fg"
        >
          Clear all
        </button>
      )}
    </div>
  );
}
