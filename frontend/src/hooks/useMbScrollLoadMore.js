import { useEffect } from "react";

import { useSearchStore } from "../stores/searchStore";

export function useMbScrollLoadMore(scrollRef, threshold = 0.7) {
  const loadMoreMusicBrainz = useSearchStore((state) => state.loadMoreMusicBrainz);
  const hasMoreMbResults = useSearchStore((state) => state.hasMoreMbResults);
  const isLoadingMoreMb = useSearchStore((state) => state.isLoadingMoreMb);
  const isLoadingMbSearch = useSearchStore((state) => state.isLoadingMbSearch);
  const itemCount = useSearchStore((state) => state.mbSearchResult.items.length);

  useEffect(() => {
    const element = scrollRef.current;
    if (!element) {
      return;
    }

    const maybeLoadMore = () => {
      if (!hasMoreMbResults || isLoadingMoreMb || isLoadingMbSearch) {
        return;
      }

      const { scrollTop, scrollHeight, clientHeight } = element;
      const progress =
        scrollHeight <= clientHeight
          ? 1
          : (scrollTop + clientHeight) / scrollHeight;

      if (progress >= threshold) {
        void loadMoreMusicBrainz();
      }
    };

    element.addEventListener("scroll", maybeLoadMore, { passive: true });
    maybeLoadMore();

    return () => element.removeEventListener("scroll", maybeLoadMore);
  }, [
    scrollRef,
    threshold,
    itemCount,
    hasMoreMbResults,
    isLoadingMoreMb,
    isLoadingMbSearch,
    loadMoreMusicBrainz,
  ]);
}
