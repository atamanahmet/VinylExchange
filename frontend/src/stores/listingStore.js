import { create } from "zustand";
import axios from "../api/axiosInstance";
import { useAppStore } from "./appStore";
import { normalizeApiResponse } from "../utils/normalizeApiResponse";

export const useListingStore = create((set, get) => ({
  publicListings: { items: [], pagination: null },
  sellerListings: { items: [], pagination: null },
  myListings: { items: [], pagination: null },
  currentListing: null,
  currentListingFetchSeq: 0,
  promotedListings: [],

  isFetchingPublic: false,
  isFetchingSeller: false,
  isFetchingMine: false,
  isFetchingCurrent: false,
  isFetchingPromoted: false,

  fetchPublicListings: async (params = {}) => {
    if (get().isFetchingPublic) return false;
    set({ isFetchingPublic: true });

    try {
      const res = await axios.get("/api/listings", {
        params,
        paramsSerializer: {
          indexes: null,
        },
      });
      if (res.status === 200) {
        const normalized = normalizeApiResponse(res.data);
        set({
          publicListings: {
            items: normalized.data,
            pagination: normalized.pagination,
          },
        });
        return true;
      }
      return false;
    } catch (err) {
      if (!err.response) {
        // network error or 5xx, backend is down
        useAppStore.getState().setBackendError(true);
      }
      return false;
    } finally {
      set({ isFetchingPublic: false });
    }
  },

  fetchSellerListings: async (username, params = {}) => {
    if (!username || get().isFetchingSeller) return false;
    set({ isFetchingSeller: true });

    try {
      const res = await axios.get(
        `/api/listings/by-username/${encodeURIComponent(username)}`,
        {
          params,
          paramsSerializer: {
            indexes: null,
          },
        },
      );
      if (res.status === 200) {
        const normalized = normalizeApiResponse(res.data);
        set({
          sellerListings: {
            items: normalized.data,
            pagination: normalized.pagination,
          },
        });
        return true;
      }
      return false;
    } catch (err) {
      if (!err.response) {
        useAppStore.getState().setBackendError(true);
      }
      return false;
    } finally {
      set({ isFetchingSeller: false });
    }
  },

  fetchMyActiveListings: async (params = {}) => {
    if (get().isFetchingMine) return false;
    set({ isFetchingMine: true });

    try {
      const res = await axios.get("/api/me/listings/active", { params });
      if (res.status === 200) {
        const normalized = normalizeApiResponse(res.data);
        set({
          myListings: {
            items: normalized.data,
            pagination: normalized.pagination,
          },
        });
        return true;
      }
      return false;
    } catch (err) {
      if (!err.response) {
        useAppStore.getState().setBackendError(true);
      }
      return false;
    } finally {
      set({ isFetchingMine: false });
    }
  },

  fetchListing: async (listingId) => {
    const requestSeq = get().currentListingFetchSeq + 1;
    set({
      isFetchingCurrent: true,
      currentListing: null,
      currentListingFetchSeq: requestSeq,
    });

    try {
      const res = await axios.get(`/api/listings/${listingId}`);
      if (get().currentListingFetchSeq !== requestSeq) {
        return false;
      }
      if (res.status === 200) {
        set({ currentListing: res.data });
        return true;
      }
      return false;
    } catch (err) {
      if (get().currentListingFetchSeq === requestSeq) {
        set({ currentListing: null });
      }
      if (!err.response) {
        useAppStore.getState().setBackendError(true);
      }
      return false;
    } finally {
      if (get().currentListingFetchSeq === requestSeq) {
        set({ isFetchingCurrent: false });
      }
    }
  },

  fetchPromotedListings: async () => {
    if (get().isFetchingPromoted) return false;
    set({ isFetchingPromoted: true });

    try {
      const res = await axios.get("/api/listings/promote");
      if (res.status === 200) {
        set({ promotedListings: res.data });
        return true;
      }
      return false;
    } catch (err) {
      if (!err.response) {
        useAppStore.getState().setBackendError(true);
      }
      return false;
    } finally {
      set({ isFetchingPromoted: false });
    }
  },

  deleteListing: async (listingId) => {
    try {
      const res = await axios.delete(`/api/listings/${listingId}`);
      if (res.status === 204) {
        await useListingStore.getState().fetchMyActiveListings();
        return true;
      }
      return false;
    } catch (err) {
      if (!err.response) {
        useAppStore.getState().setBackendError(true);
      }
      return false;
    }
  },
}));
