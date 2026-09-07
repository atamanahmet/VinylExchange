import { useEffect, useMemo, useRef, useState } from "react";

import {
  applyMbReleaseFilters,
  createInitialMbFilters,
  getMbFilterBounds,
  mergeMbFiltersWithBounds,
} from "../utils/mbReleaseFilters";

/**
 * Manages MusicBrainz release filter state for search modals.
 * Resets on new search; merges bounds when infinite scroll loads more.
 */
export function useMbReleaseFilters(releases, { isLoadingSearch, searchContext }) {
  const [filters, setFilters] = useState(() => createInitialMbFilters());
  const prevBoundsRef = useRef(null);

  const bounds = useMemo(
    () => getMbFilterBounds(releases ?? []),
    [releases],
  );

  useEffect(() => {
    if (isLoadingSearch && searchContext) {
      prevBoundsRef.current = null;
    }
  }, [isLoadingSearch, searchContext]);

  useEffect(() => {
    if (!searchContext || isLoadingSearch || !(releases?.length ?? 0)) {
      return;
    }

    if (prevBoundsRef.current === null) {
      setFilters(createInitialMbFilters(releases));
    } else {
      setFilters((prev) =>
        mergeMbFiltersWithBounds(prev, bounds, prevBoundsRef.current),
      );
    }

    prevBoundsRef.current = bounds;
  }, [searchContext, isLoadingSearch, bounds, releases?.length]);

  const filteredReleases = useMemo(
    () => applyMbReleaseFilters(releases ?? [], filters, bounds),
    [releases, filters, bounds],
  );

  const releaseCount = releases?.length ?? 0;

  return {
    filters,
    setFilters,
    bounds,
    filteredReleases,
    hasReleases: releaseCount > 0,
    releaseCount,
  };
}
