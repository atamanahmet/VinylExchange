import { create } from "zustand";
import axios from "../api/axiosInstance";
import { useAuthStore } from "./authStore";
import { useUIStore } from "./uiStore";
import { useOrderStore } from "./orderStore";
import { navigate } from "../utils/router";

export const useCartStore = create((set, get) => ({
  cart: null,
  cartItemCount: 0,
  checkoutResult: null,

  fetchCart: async () => {
    const user = useAuthStore.getState().user;

    if (!user) {
      set({ cart: null, cartItemCount: 0 });
      return;
    }

    try {
      const res = await axios.get("/api/cart");
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
      if (!isLoggedIn) return;
    }

    try {
      const res = await axios.post("/api/cart/items", {
        listingId,
        quantity: 1,
      });
      if (res.status === 200) {
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
      if (!isLoggedIn) return;
    }

    try {
      const res = await axios.patch(`/api/cart/items/${cartItemId}`, {});
      if (res.status === 200) {
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
      if (!isLoggedIn) return;
    }

    try {
      const res = await axios.delete(`/api/cart/items/${cartItemId}`);
      if (res.status === 204) {
        await get().fetchCart();
      }
    } catch (e) {
      console.log(e);
    }
  },

  checkout: async () => {
    try {
      const res = await axios.post("/api/cart/checkout");
      if (res.status === 201) {
        const orderIds = res.data.orders.map((o) => o.orderId);
        set({ cart: null, cartItemCount: 0, checkoutResult: res.data });
        useOrderStore.getState().setPendingOrderIds(orderIds);
        navigate("/payment");
        return true;
      }
      return false;
    } catch (e) {
      console.log(e);
      return false;
    }
  },
}));
