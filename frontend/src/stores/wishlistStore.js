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
    if (!user) return;

    set({ isLoading: true, error: null });
    try {
      const res = await axios.get("/api/wishlists");
      if (res.status === 200) {
        set({ wishlist: res.data });
        return true;
      }
      return false;
    } catch (error) {
      console.log("Error fetching wishlist:", error);
      set({ error: "Failed to fetch wishlist" });
      return false;
    } finally {
      set({ isLoading: false });
    }
  },

  addToWishlist: async (release) => {
    set({ isLoading: true, error: null });
    try {
      const res = await axios.post("/api/wishlists", {
        title: release.title,
        artist:
          release.artistCredit?.[0]?.name.toLowerCase() || "Unknown Artist",
        year: release.date ? release.date.substring(0, 4) : "Unknown Date",
        format: release.media?.[0]?.format || "Unknown Format",
        externalCoverUrl: release.externalCoverUrl,
      });
      if (res.status === 201) {
        set({ wishlist: res.data });
        return true;
      }
      return false;
    } catch (error) {
      console.log(error);
      return false;
    } finally {
      set({ isLoading: false });
    }
  },

  addToWishlistBulk: async (wishlistHolder) => {
    set({ isLoading: true, error: null });
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
      const res = await axios.post("/api/wishlists/bulk", {
        bulkRequest: payload,
      });
      if (res.status === 201) {
        set({ wishlist: res.data });
        return true;
      }
      return false;
    } catch (error) {
      console.log(error);
      return false;
    } finally {
      set({ isLoading: false });
    }
  },

  removeFromWishlist: async (wishlistItemId) => {
    set({ isLoading: true, error: null });
    try {
      const res = await axios.delete(`/api/wishlists/${wishlistItemId}`);
      if (res.status === 200) {
        set({ wishlist: res.data });
        return true;
      }
      return false;
    } catch (error) {
      console.log("Error removing from wishlist:", error);
      set({ error: "Failed to remove from wishlist" });
      return false;
    } finally {
      set({ isLoading: false });
    }
  },

  /**
   * Checks if a release is already in wishlist by title + artist match
   */
  isInWishlist: (release) => {
    const { wishlist } = get();
    const releaseArtist = release.artistCredit?.[0]?.name?.toLowerCase();
    return wishlist.some(
      (item) =>
        item.title?.toLowerCase() === release.title?.toLowerCase() &&
        item.artist?.toLowerCase() === releaseArtist,
    );
  },

  findInWishlist: (release) => {
    const { wishlist } = get();
    return (
      wishlist.find(
        (item) =>
          item.title?.toLowerCase() === release.title?.toLowerCase() &&
          item.artist?.toLowerCase() ===
            release.artistCredit?.[0]?.name?.toLowerCase() &&
          item.year === (release.date ? release.date.substring(0, 4) : null) &&
          item.format === (release.media?.[0]?.format ?? null),
      ) || null
    );
  },

  toggleToWishlist: async (release) => {
    const user = useAuthStore.getState().user;
    if (!user) {
      const isLoggedIn = await useUIStore.getState().waitForLogin();
      if (!isLoggedIn) return;
    }

    const { findInWishlist, addToWishlist, removeFromWishlist } = get();
    const existing = findInWishlist(release);

    if (existing) {
      const removed = await removeFromWishlist(existing.id);
      return removed ? "removed" : null;
    } else {
      const added = await addToWishlist(release);
      return added ? "added" : null;
    }
  },

  clearError: () => set({ error: null }),
  reset: () => set({ wishlist: [], isLoading: false, error: null }),
}));

export default useWishlistStore;
