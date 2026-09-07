import { cn } from "@/lib/utils";

/** Shared desktop list row / header grid — keep header and item cells in sync */
export const LIST_VIEW_GRID_WITH_PRICE =
  "grid grid-cols-[6.5rem_minmax(0,1.35fr)_minmax(0,1fr)_4.5rem_minmax(0,0.8fr)_4.5rem_minmax(0,0.65fr)_9rem] items-center";

export const LIST_VIEW_GRID_NO_PRICE =
  "grid grid-cols-[6.5rem_minmax(0,1.35fr)_minmax(0,1fr)_4.5rem_minmax(0,0.8fr)_4.5rem_9rem] items-center";

export const LIST_VIEW_CELL = "min-w-0 px-3 py-3";

export const LIST_VIEW_COVER_CELL =
  "flex min-w-0 items-center justify-start px-3 py-3";

export const LIST_VIEW_ACTIONS_CELL =
  "flex min-w-0 flex-col items-center justify-center px-3 py-3";

export function resolveListViewGrid({ showPrice = true, showActions = true } = {}) {
  if (!showActions) {
    return showPrice ? LIST_VIEW_GRID_WITH_PRICE : LIST_VIEW_GRID_NO_PRICE;
  }
  return showPrice ? LIST_VIEW_GRID_WITH_PRICE : LIST_VIEW_GRID_NO_PRICE;
}

export const LIST_VIEW_HEADER_CELL = "min-w-0 px-3 py-3 text-center";

export function listViewHeaderClass(options) {
  return cn(
    resolveListViewGrid(options),
    "border-b border-surface-3 bg-surface-2 text-center text-xs font-medium uppercase tracking-wide text-on-surface-muted sm:text-sm",
  );
}

export function listViewRowClass(options) {
  return cn(
    "hidden border-b border-surface-3 bg-surface-1 lg:grid",
    resolveListViewGrid(options),
  );
}
