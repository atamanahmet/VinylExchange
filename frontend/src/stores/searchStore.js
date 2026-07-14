import { create } from "zustand";
import axios from "../api/axiosInstance";

export const MB_SEARCH_SCOPES = {
  TITLE: "title",
  ARTIST: "artist",
  BOTH: "both",
};

export const MB_PAGE_SIZE = 75;

function mergeMbReleases(existing = [], incoming = []) {
  if (!incoming.length) {
    return existing;
  }

  const seen = new Set(existing.map((release) => release.id));
  const appended = incoming.filter((release) => release?.id && !seen.has(release.id));
  return appended.length ? [...existing, ...appended] : existing;
}

export const useSearchStore = create((set, get) => ({
  listingSearchResult: {
    items: [],
  },
  lastListingQuery: null,
  isLoadingListingSearch: false,

  mbSearchResult: {
    items: [],
  },
  mbSearchContext: null,
  hasMoreMbResults: false,
  isLoadingMbSearch: false,
  isLoadingMoreMb: false,

  clearSearch: () =>
    set({
      listingSearchResult: { items: [] },
      lastListingQuery: null,
      isLoadingListingSearch: false,
    }),

  clearMbSearch: () =>
    set({
      mbSearchResult: { items: [] },
      mbSearchContext: null,
      hasMoreMbResults: false,
      isLoadingMbSearch: false,
      isLoadingMoreMb: false,
    }),

  searchMusicBrainz: async (query, scope = MB_SEARCH_SCOPES.TITLE) => {
    const trimmedQuery = query?.trim();
    if (!trimmedQuery) {
      return;
    }

    set({
      isLoadingMbSearch: true,
      isLoadingMoreMb: false,
      mbSearchContext: { query: trimmedQuery, scope },
      hasMoreMbResults: false,
      mbSearchResult: { items: [] },
    });

    try {
      const res = await axios.get("/api/mb/search", {
        params: {
          query: trimmedQuery,
          scope,
          limit: MB_PAGE_SIZE,
          offset: 0,
        },
      });

      if (res.status === 200) {
        const items = res.data ?? [];
        set({
          mbSearchResult: { items },
          hasMoreMbResults: items.length === MB_PAGE_SIZE,
        });
      }
    } catch (e) {
      console.log(e);
    } finally {
      set({ isLoadingMbSearch: false });
    }
  },

  loadMoreMusicBrainz: async () => {
    const {
      mbSearchContext,
      mbSearchResult,
      hasMoreMbResults,
      isLoadingMoreMb,
      isLoadingMbSearch,
    } = get();

    if (
      !mbSearchContext ||
      !hasMoreMbResults ||
      isLoadingMoreMb ||
      isLoadingMbSearch
    ) {
      return;
    }

    set({ isLoadingMoreMb: true });

    try {
      const offset = mbSearchResult.items.length;
      const res = await axios.get("/api/mb/search", {
        params: {
          query: mbSearchContext.query,
          scope: mbSearchContext.scope,
          limit: MB_PAGE_SIZE,
          offset,
        },
      });

      if (res.status === 200) {
        const pageItems = res.data ?? [];
        const mergedItems = mergeMbReleases(mbSearchResult.items, pageItems);

        set({
          mbSearchResult: { items: mergedItems },
          hasMoreMbResults: pageItems.length === MB_PAGE_SIZE,
        });
      }
    } catch (e) {
      console.log(e);
    } finally {
      set({ isLoadingMoreMb: false });
    }
  },

  /** Sets active listing search query; MainPage fetches with current sort. */
  startListingSearch: (query) => {
    const trimmed = query?.trim() ?? "";
    if (!trimmed) {
      return;
    }
    set({ lastListingQuery: trimmed });
  },

  searchProducts: async (query, { sort } = {}) => {
    const trimmed = (query ?? get().lastListingQuery)?.trim() ?? "";
    if (!trimmed) {
      return;
    }

    set({
      isLoadingListingSearch: true,
      lastListingQuery: trimmed,
    });
    try {
      const params = { query: trimmed, page: 0, size: 60 };
      if (sort) {
        params.sort = sort;
      }

      const res = await axios.get("/api/listings/search", { params });
      if (res.status === 200) {
        set({
          listingSearchResult: {
            items: res.data?.content ?? [],
          },
        });
      }
    } catch (e) {
      console.log(e);
    } finally {
      set({ isLoadingListingSearch: false });
    }
  },
}));
