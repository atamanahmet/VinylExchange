import { create } from "zustand";
import axios from "../api/axiosInstance";
import { useAppStore } from "./appStore";

export const usePaymentStore = create((set, get) => ({
  payments: [],
  isFetching: false,

  fetchMyPayments: async () => {
    if (get().isFetching) return false;
    set({ isFetching: true });
    try {
      const res = await axios.get("/api/me/payments");
      if (res.status === 200) {
        set({ payments: res.data });
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
}));
