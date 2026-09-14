import { useCallback, useMemo, useState } from "react";

import {
  countActiveFilters,
  filtersEqual,
  resetFilters,
} from "@/utils/listingFilters";

function cloneFilters(filters) {
  return {
    ...filters,
    formats: [...(filters.formats ?? [])],
    speedRpm: [...(filters.speedRpm ?? [])],
    vinylSubtype: [...(filters.vinylSubtype ?? [])],
    conditions: [...(filters.conditions ?? [])],
    countries: [...(filters.countries ?? [])],
    genreIds: [...(filters.genreIds ?? [])],
    priceRange: [...filters.priceRange],
    yearRange: [...filters.yearRange],
  };
}

/**
 * Draft/applied filter pair shared by the browse pages.
 * Draft tracks panel edits, applied is what the query actually uses.
 */
export function useListingFilterState({ onFiltersCommitted } = {}) {
  const [draftFilters, setDraftFilters] = useState(() => resetFilters());
  const [appliedFilters, setAppliedFilters] = useState(() => resetFilters());

  const commitFilters = useCallback(
    (next) => {
      const snapshot = cloneFilters(next);
      setDraftFilters(snapshot);
      setAppliedFilters(cloneFilters(snapshot));
      onFiltersCommitted?.();
    },
    [onFiltersCommitted],
  );

  const applyFilters = useCallback(() => {
    setAppliedFilters(cloneFilters(draftFilters));
    onFiltersCommitted?.();
  }, [draftFilters, onFiltersCommitted]);

  const clearFilters = useCallback(() => {
    commitFilters(resetFilters());
  }, [commitFilters]);

  const activeCount = useMemo(
    () => countActiveFilters(appliedFilters),
    [appliedFilters],
  );

  const isDirty = useMemo(
    () => !filtersEqual(draftFilters, appliedFilters),
    [draftFilters, appliedFilters],
  );

  const canReset = activeCount > 0 || isDirty;

  return {
    draftFilters,
    setDraftFilters,
    appliedFilters,
    applyFilters,
    clearFilters,
    commitFilters,
    activeCount,
    isDirty,
    canReset,
  };
}
