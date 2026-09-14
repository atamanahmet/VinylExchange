import { cn } from "@/lib/utils";

/**
 * Shared desktop list grid. Header, skeleton and row MUST resolve the same
 * template, so they all go through resolveListViewGrid with identical flags.
 * Templates are spelled out literally because Tailwind can only compile
 * arbitrary grid values it can see in the source.
 *
 * Columns: cover | title | artist | year | format | country | [price] | [actions]
 */
const GRID_PRICE_ACTIONS =
  "grid grid-cols-[6.5rem_minmax(0,1.35fr)_minmax(0,1fr)_4.5rem_minmax(0,0.8fr)_minmax(0,0.8fr)_minmax(0,0.7fr)_9rem] items-center";

const GRID_PRICE_ONLY =
  "grid grid-cols-[6.5rem_minmax(0,1.35fr)_minmax(0,1fr)_4.5rem_minmax(0,0.8fr)_minmax(0,0.8fr)_minmax(0,0.7fr)] items-center";

const GRID_ACTIONS_ONLY =
  "grid grid-cols-[6.5rem_minmax(0,1.35fr)_minmax(0,1fr)_4.5rem_minmax(0,0.8fr)_minmax(0,0.8fr)_9rem] items-center";

const GRID_BARE =
  "grid grid-cols-[6.5rem_minmax(0,1.35fr)_minmax(0,1fr)_4.5rem_minmax(0,0.8fr)_minmax(0,0.8fr)] items-center";

export function resolveListViewGrid({ showPrice = true, showActions = true } = {}) {
  if (showPrice) {
    return showActions ? GRID_PRICE_ACTIONS : GRID_PRICE_ONLY;
  }
  return showActions ? GRID_ACTIONS_ONLY : GRID_BARE;
}

export const LIST_VIEW_CELL = "min-w-0 px-3 py-3 text-left";

export const LIST_VIEW_COVER_CELL =
  "flex min-w-0 items-center justify-start px-3 py-3";

export const LIST_VIEW_ACTIONS_CELL =
  "flex min-w-0 flex-col items-center justify-center px-3 py-3";

/** Header labels sit left over left-aligned data; only the actions column centers. */
export const LIST_VIEW_HEADER_CELL = "min-w-0 px-3 py-3 text-left";

export const LIST_VIEW_HEADER_ACTIONS_CELL = "min-w-0 px-3 py-3 text-center";

export function listViewHeaderClass(options) {
  return cn(
    resolveListViewGrid(options),
    "border-b border-surface-3 bg-surface-2 text-left text-xs font-medium uppercase tracking-wide text-on-surface-muted sm:text-sm",
  );
}

export function listViewRowClass(options) {
  return cn(
    "hidden border-b border-surface-3 bg-surface-1 lg:grid",
    resolveListViewGrid(options),
  );
}
