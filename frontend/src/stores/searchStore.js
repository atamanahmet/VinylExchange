import { create } from "zustand";
import axios from "../api/axiosInstance";

export const useSearchStore = create((set, get) => ({
  searchResult: {
    dataType: "",
    items: [],
  },

  isLoadingSearch: true,

  clearSearch: () => set({ searchResult: { items: [] } }),

  searchMusicBrainz: async (title) => {
    set({
      isLoadingSearch: true,
    });

    try {
      const res = await axios.get("/api/mb/search", {
        params: {
          title: title,
          limit: 75,
        },
        withCredentials: true,
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
      set({
        isLoadingSearch: false,
      });
    }
  },
  searchProducts: async (query) => {
    set({
      isLoadingSearch: true,
    });

    try {
      const res = await axios.get("/api/listings/search", {
        params: {
          query: query,
          page: 0,
          size: 75,
        },
        withCredentials: true,
      });
      if (res.status === 200) {
        console.log("search results: ", res.data);
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
      set({
        isLoadingSearch: false,
      });
    }
  },
}));
