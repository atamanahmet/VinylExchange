import { create } from "zustand";
import axios from "../api/axiosInstance";

export const useSearchStore = create((set) => ({
  searchResult: {
    dataType: "",
    items: [],
  },
  isLoadingSearch: false,

  clearSearch: () => set({ searchResult: { dataType: "", items: [] } }),

  searchMusicBrainz: async (title) => {
    set({ isLoadingSearch: true });
    try {
      const res = await axios.get("/api/mb/search", {
        params: { title, limit: 75 },
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
