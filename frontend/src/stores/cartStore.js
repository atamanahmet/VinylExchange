import { create } from "zustand";
import axios from "../api/axiosInstance";
import { useAuthStore } from "./authStore";
import { useUIStore } from "./uiStore";

export const useCartStore = create((set, get) => ({
  cart: null,
  cartItemCount: 0,

  fetchCart: async () => {
    const user = useAuthStore.getState().user;

    if (!user) {
      set({
        cart: null,
        cartItemCount: 0,
      });
      return;
    }

    try {
      const res = await axios.get("/api/cart", {
        withCredentials: true,
      });
      if (res.status === 200) {
        set({
          cart: res.data,
          cartItemCount: res.data.items.length,
        });
      }
    } catch (e) {
      console.log(e);
    }
  },

  addToCart: async (listingId) => {
    const user = useAuthStore.getState().user;

    if (!user) {
      const isLoggedIn = await useUIStore.getState().waitForLogin();
      if (!isLoggedIn) {
        return;
      }
    }
    try {
      const res = await axios.post(
        "/api/cart/items",
        { listingId: listingId, quantity: 1 },
        { withCredentials: true },
      );
      if (res.status === 200) {
        // refresh cart after change
        await get().fetchCart();
      }
    } catch (e) {
      console.log(e);
    }
  },

  decreaseFromCart: async (cartItemId) => {
    const user = useAuthStore.getState().user;

    if (!user) {
      const isLoggedIn = await useUIStore.getState().waitForLogin();
      if (!isLoggedIn) {
        return;
      }
    }
    try {
      const res = await axios.patch(
        `/api/cart/items/${cartItemId}`,
        {},
        { withCredentials: true },
      );
      if (res.status === 200) {
        // refresh cart after change
        await get().fetchCart();
      }
    } catch (e) {
      console.log(e);
    }
  },

  removeFromCart: async (cartItemId) => {
    const user = useAuthStore.getState().user;

    if (!user) {
      const isLoggedIn = await useUIStore.getState().waitForLogin();
      if (!isLoggedIn) {
        return;
      }
    }
    try {
      const res = await axios.delete(`/api/cart/items/${cartItemId}`, {
        withCredentials: true,
      });
      if (res.status === 204) {
        // refresh shared cart after change
        await get().fetchCart();
      }
    } catch (e) {
      console.log(e);
    }
  },
}));
