import { create } from "zustand";
import axios from "../api/axiosInstance";
import { useAppStore } from "./appStore";
import { normalizeApiResponse } from "../utils/normalizeApiResponse";

export const useListingStore = create((set, get) => ({
  publicListings: { items: [], pagination: null },
  myListings: { items: [], pagination: null },
  currentListing: null,
  promotedListings: [],

  isFetchingPublic: false,
  isFetchingMine: false,
  isFetchingCurrent: false,
  isFetchingPromoted: false,

  fetchPublicListings: async (params = {}) => {
    if (get().isFetchingPublic) return false;
    set({ isFetchingPublic: true });

    try {
      const res = await axios.get("/api/listings", { params });
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
    if (get().isFetchingCurrent) return false;
    set({ isFetchingCurrent: true });

    try {
      const res = await axios.get(`/api/listings/${listingId}`);
      if (res.status === 200) {
        set({ currentListing: res.data });
        return true;
      }
      return false;
    } catch (err) {
      if (!err.response) {
        useAppStore.getState().setBackendError(true);
      }
      return false;
    } finally {
      set({ isFetchingCurrent: false });
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
