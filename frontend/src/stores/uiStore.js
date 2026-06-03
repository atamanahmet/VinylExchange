import { create } from "zustand";
import axios from "../api/axiosInstance";

export const useUIStore = create((set, get) => ({
  openLogin: false,
  loginResolver: null,
  layout: localStorage.getItem("layout") || "grid",
  navbarActive: true,

  setNavbarActive: (action) => set({ navbarActive: action }),

  setOpenLogin: (isOpen) => set({ openLogin: isOpen }),

  setLayout: (selection) => {
    set({ layout: selection });
    localStorage.setItem("layout", selection);
  },

  resolveLogin: (success) => {
    const { loginResolver } = get();

    if (loginResolver) {
      loginResolver(success);
      set({ loginResolver: null });
    }
  },

  waitForLogin: () => {
    return new Promise((resolve) => {
      set({
        loginResolver: () => resolve,
        openLogin: true,
      });
    });
  },
}));
