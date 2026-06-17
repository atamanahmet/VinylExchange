import { create } from "zustand";
import axios from "../api/axiosInstance";

export const MB_SEARCH_SCOPES = {
  TITLE: "title",
  ARTIST: "artist",
  BOTH: "both",
};

export const useSearchStore = create((set) => ({
  searchResult: {
    dataType: "",
    items: [],
  },
  isLoadingSearch: false,

  clearSearch: () => set({ searchResult: { dataType: "", items: [] } }),

  searchMusicBrainz: async (query, scope = MB_SEARCH_SCOPES.TITLE) => {
    set({ isLoadingSearch: true });
    try {
      const res = await axios.get("/api/mb/search", {
        params: { query, scope, limit: 75 },
      });
      if (res.status === 200) {
        set({
          searchResult: {
            dataType: "mb",
            items: res.data ?? [],
          },
        });
      }
    } catch (e) {
      console.log(e);
    } finally {
      set({ isLoadingSearch: false });
    }
  },

  searchProducts: async (query) => {
    set({ isLoadingSearch: true });
    try {
      const res = await axios.get("/api/listings/search", {
        params: { query, page: 0, size: 75 },
      });
      if (res.status === 200) {
        set({
          searchResult: {
            dataType: "listing",
            items: res.data?.content ?? [],
          },
        });
      }
    } catch (e) {
      console.log(e);
    } finally {
      set({ isLoadingSearch: false });
    }
  },
}));
