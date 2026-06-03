import axios from "../api/axiosInstance";
import { create } from "zustand";
import { normalizeApiResponse } from "../utils/normalizeApiResponse";

export const useDataStore = create((set, get) => ({
  isFetching: false,
  hasError: false,

  data: {
    items: [],
    pagination: null,
    dataType: "",
  },

  avatar: sessionStorage.getItem("avatar") || null,

  fetchAllListings: async (params = {}) => {
    if (get().isFetching) return false;

    set({ isFetching: true });

    const url = "/api/listings";

    try {
      const res = await axios.get(url, {
        params,
        withCredentials: true,
      });

      if (res.status === 200) {
        const normalized = normalizeApiResponse(res.data);

        set({
          data: {
            items: normalized.data,
            pagination: normalized.pagination,
            dataType: "listing",
          },
          hasError: false,
        });

        return true;
      }

      console.warn("Unexpected data fetch status:", res.status);
      return false;
    } catch (err) {
      console.error("Backend error:", err);

      set({
        data: {
          items: [],
          pagination: null,
          dataType: "",
        },
        hasError: true,
      });

      return false;
    } finally {
      set({ isFetching: false });
    }
  },
  fetchMyActiveListings: async (params = {}) => {
    if (get().isFetching) return false;

    set({ isFetching: true });

    const url = "/api/me/listings/active";

    try {
      const res = await axios.get(url, {
        params,
        withCredentials: true,
      });

      if (res.status === 200) {
        const normalized = normalizeApiResponse(res.data);

        set({
          data: {
            items: normalized.data,
            pagination: normalized.pagination,
            dataType: "listing",
          },
          hasError: false,
        });

        return true;
      }

      console.warn("Unexpected data fetch status:", res.status);
      return false;
    } catch (err) {
      console.error("Backend error:", err);

      set({
        data: {
          items: [],
          pagination: null,
          dataType: "",
        },
        hasError: true,
      });

      return false;
    } finally {
      set({ isFetching: false });
    }
  },

  fetchMyArchivedListings: async (params = {}) => {
    if (get().isFetching) return false;

    set({ isFetching: true });

    const url = "/api/me/listings/archived";

    try {
      const res = await axios.get(url, {
        params,
        withCredentials: true,
      });

      if (res.status === 200) {
        const normalized = normalizeApiResponse(res.data);

        set({
          data: {
            items: normalized.data,
            pagination: normalized.pagination,
            dataType: "listing",
          },
          hasError: false,
        });

        return true;
      }

      console.warn("Unexpected data fetch status:", res.status);
      return false;
    } catch (err) {
      console.error("Backend error:", err);

      set({
        data: {
          items: [],
          pagination: null,
          dataType: "",
        },
        hasError: true,
      });

      return false;
    } finally {
      set({ isFetching: false });
    }
  },

  deleteListing: async (listingId) => {
    console.log("Deleting:", listingId);
    try {
      const res = await axios.delete(`/api/listings/${listingId}`, {
        withCredentials: true,
      });
      console.log("listing deleted");
      get().fetchMyActiveListings();
    } catch (error) {
      console.log(error);
    }
  },

  fetchListing: async (listingId) => {
    if (get().isFetching) return false;

    set({ isFetching: true });

    try {
      const res = await axios.get(`/api/listings/${listingId}`, {
        withCredentials: true,
      });
      if (res.status === 200) {
        const normalized = normalizeApiResponse(res.data);

        console.log(res.data);

        set({
          data: {
            items: normalized.data,
            pagination: normalized.pagination,
            dataType: "listing",
          },
          hasError: false,
        });

        return true;
      }
    } catch (error) {
      console.log(error);
    } finally {
      set({ isFetching: false });
    }
  },

  fetchListingsByUser: async (username) => {
    if (get().isFetching) return false;

    set({ isFetching: true });

    try {
      const res = await axios.get(`/api/listings/${username}`, {
        withCredentials: true,
      });
      if (res.status === 200) {
        const normalized = normalizeApiResponse(res.data);

        set({
          data: {
            items: normalized.data,
            pagination: normalized.pagination,
            dataType: "listing",
          },
          hasError: false,
        });

        return true;
      }
    } catch (error) {
      console.log(error);
    } finally {
      set({ isFetching: false });
    }
  },
}));
