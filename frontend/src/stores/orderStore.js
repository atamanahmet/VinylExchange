import { create } from "zustand";
import axios from "../api/axiosInstance";
import { useAppStore } from "./appStore";

export const useOrderStore = create((set, get) => ({
  purchases: [],
  sales: [],
  pendingOrderIds: [],
  setPendingOrderIds: (ids) => set({ pendingOrderIds: ids }),
  currentOrder: null,
  isFetching: false,
  isFetchingCurrent: false,

  fetchPurchases: async () => {
    if (get().isFetching) return false;
    set({ isFetching: true });
    try {
      const res = await axios.get("/api/orders/my/purchases");
      if (res.status === 200) {
        set({ purchases: res.data });
        return true;
      }
      return false;
    } catch (err) {
      if (!err.response) useAppStore.getState().setBackendError(true);
      return false;
    } finally {
      set({ isFetching: false });
    }
  },

  fetchSales: async () => {
    try {
      const res = await axios.get("/api/orders/my/sales");
      if (res.status === 200) {
        set({ sales: res.data });
        return true;
      }
      return false;
    } catch (err) {
      if (!err.response) useAppStore.getState().setBackendError(true);
      return false;
    }
  },

  fetchOrder: async (orderId) => {
    if (get().isFetchingCurrent) return false;
    set({ isFetchingCurrent: true, currentOrder: null });
    try {
      const res = await axios.get(`/api/orders/${orderId}`);
      if (res.status === 200) {
        set({ currentOrder: res.data });
        return true;
      }
      return false;
    } catch (err) {
      if (!err.response) useAppStore.getState().setBackendError(true);
      return false;
    } finally {
      set({ isFetchingCurrent: false });
    }
  },

  initiatePayment: async (orderId) => {
    try {
      const res = await axios.post("/api/payment/initiate", { orderId });
      if (res.status === 200) return res.data;
      return null;
    } catch (err) {
      if (!err.response) useAppStore.getState().setBackendError(true);
      return null;
    }
  },

  shipOrder: async (orderId) => {
    try {
      const res = await axios.post(`/api/orders/${orderId}/ship`);
      if (res.status === 200) {
        set({ currentOrder: res.data });
        return true;
      }
      return false;
    } catch (err) {
      if (!err.response) useAppStore.getState().setBackendError(true);
      return false;
    }
  },

  confirmDelivery: async (orderId) => {
    try {
      const res = await axios.post(`/api/orders/${orderId}/confirm-delivery`);
      if (res.status === 200) {
        set({ currentOrder: res.data });
        return true;
      }
      return false;
    } catch (err) {
      if (!err.response) useAppStore.getState().setBackendError(true);
      return false;
    }
  },

  cancelOrder: async (orderId, reason) => {
    try {
      const res = await axios.post(`/api/orders/${orderId}/cancel`, { reason });
      return res.status === 204;
    } catch (err) {
      if (!err.response) useAppStore.getState().setBackendError(true);
      return false;
    }
  },

  openDispute: async (orderId, reason, note) => {
    try {
      const res = await axios.post(`/api/orders/${orderId}/dispute`, {
        reason,
        note,
      });
      return res.status === 204;
    } catch (err) {
      if (!err.response) useAppStore.getState().setBackendError(true);
      return false;
    }
  },
}));
