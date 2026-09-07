import { create } from "zustand";
import { createJSONStorage, persist } from "zustand/middleware";
import { DEFAULT_LISTING_SORT } from "../utils/listingFilters";

export const useUserPreferenceStore = create(
  persist(
    (set) => ({
      sort: DEFAULT_LISTING_SORT,
      setSort: (sort) => set({ sort }),
    }),
    {
      name: "user-preferences",
      storage: createJSONStorage(() => localStorage),
    },
  ),
);
