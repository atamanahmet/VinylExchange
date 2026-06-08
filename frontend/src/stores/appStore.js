import { create } from "zustand";

export const useAppStore = create((set) => ({
  backendError: false,

  setBackendError: (value) => set({ backendError: value }),
}));
