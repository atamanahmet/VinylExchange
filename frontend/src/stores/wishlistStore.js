import { create } from "zustand";
import axios from "../api/axiosInstance";
import { useAuthStore } from "./authStore";
import { useUIStore } from "./uiStore";

const useWishlistStore = create((set, get) => ({
  wishlist: [],
  isLoading: false,
  error: null,

  fetchWishlist: async () => {
    const user = useAuthStore.getState().user;

    if (user == null) return;

    set({ isLoading: true, error: null });

    try {
      const res = await axios.get("/api/wishlists", {
        withCredentials: true,
      });

      if (res.status === 200) {
        set({ wishlist: res.data, isLoading: false });
        return true;
      }
      // exist
      console.warn("Unexpected fetch wishlist status:", res.status);
      set({ isLoading: false });
      return false;
    } catch (error) {
      console.log("Error fetching wishlist:", error);
      set({ error: "Failed to fetch wishlist", isLoading: false });
      return false;
    }
  },

  addToWishlist: async (release) => {
    set({ isLoading: true, error: null });

    const url = "/api/wishlists";

    try {
      const res = await axios.post(
        url,
        {
          title: release.title,
          artist:
            release.artistCredit?.[0]?.name.toLowerCase() || "Unknown Artist",
          year: release.date ? release.date.substring(0, 4) : "Unknown Date",
          format: release.media?.[0]?.format || "Unknown Format",
          externalCoverUrl: release.externalCoverUrl,
        },

        { withCredentials: true },
      );
      if (res.status === 201) {
        set({ wishlist: res.data, isLoading: false });

        return true;
      }

      console.warn("Unexpected wishlist post status:", res.status);
      return false;
    } catch (error) {
      console.log(error);

      return false;
    }
  },
  addToWishlistBulk: async (wishlistHolder) => {
    set({ isLoading: true, error: null });

    const url = "/api/wishlists/bulk";

    const payload = wishlistHolder.map((release) => ({
      title: release.title,
      artist: release.artistCredit?.[0]?.name.toLowerCase(),
      year: release.date ? release.date.substring(0, 4) : null,
      format: release.media?.[0]?.format,
      barcode: release.barcode,
      country: release.country,
      label: release.labelInfo?.[0].label?.name,
      externalCoverUrl: release.externalCoverUrl,
    }));

    try {
      const res = await axios.post(
        url,
        {
          bulkRequest: payload,
        },

        { withCredentials: true },
      );
      if (res.status === 201) {
        set({ wishlist: res.data, isLoading: false });

        return true;
      }

      console.warn("Unexpected wishlist post status:", res.status);
      return false;
    } catch (error) {
      console.log(error);

      return false;
    }
  },

  removeFromWishlist: async (wishlistItemId) => {
    set({ isLoading: true, error: null });

    const url = `/api/wishlists/${wishlistItemId}`;

    try {
      const res = await axios.delete(url, {
        withCredentials: true,
      });

      if (res.status === 200) {
        set({ wishlist: res.data, isLoading: false });
        return true;
      }

      console.warn("Unexpected remove wishlist status: ", res.status);
      set({ isLoading: false });

      return false;
    } catch (error) {
      console.log("Error removing from wishlist: ", error);
      set({
        error: "Failed to remove from wishlist",
        isLoading: false,
      });
      return false;
    }
  },

  isInWishlist: (release) => {
    const { wishlist } = get();

    //set? calcualte overhead for smaller than 500item
    return wishlist.some(
      (item) =>
        (item.title === release.title &&
          item.artist === release.artistCredit?.[0]?.name) ||
        (release.artist &&
          item.year ===
            (release.date ? release.date.substring(0, 4) : release.year) &&
          item.format === release.media?.[0]?.format) ||
        release.format,
    );
  },

  findInWishlist: (release) => {
    const { wishlist } = get();
    return (
      wishlist.find(
        (item) =>
          item.title.toLowerCase() === release.title.toLowerCase() &&
          item.artist.toLowerCase() ===
            release.artistCredit?.[0]?.name.toLowerCase() &&
          item.year === (release.date ? release.date.substring(0, 4) : null) &&
          item.format ===
            (release.media?.[0]?.format ? release.media?.[0]?.format : null),
      ) || null
    );
  },

  toggleToWishlist: async (release) => {
    const user = useAuthStore.getState().user;

    if (!user) {
      const isLoggedIn = await useUIStore.getState().waitForLogin();
      if (!isLoggedIn) {
        return;
      }
    }
    const { findInWishlist, addToWishlist, removeFromWishlist } = get();
    const existing = findInWishlist(release);

    if (existing) {
      const remowed = await removeFromWishlist(existing.id);
      return remowed ? "removed" : null;
    } else {
      const added = await addToWishlist(release);
      return added ? "added" : null;
    }
  },

  clearError: () => set({ error: null }),

  reset: () => set({ wishlist: [], isLoading: false, error: null }),
}));

export default useWishlistStore;
