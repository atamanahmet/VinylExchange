import { create } from "zustand";
import axios from "../api/axiosInstance";
import { useAppStore } from "./appStore";
import { getApiErrorMessage } from "../utils/apiErrorMessage";

function normalizeAddress(address) {
  return {
    ...address,
    isDefault: Boolean(address.isDefault ?? address.default),
  };
}

function sortAddresses(addresses) {
  return [...addresses].sort((a, b) => {
    if (a.isDefault !== b.isDefault) {
      return a.isDefault ? -1 : 1;
    }
    return (a.label || "").localeCompare(b.label || "");
  });
}

export const useAddressStore = create((set, get) => ({
  addresses: [],
  isFetching: false,
  isSaving: false,

  fetchAddresses: async (type) => {
    if (get().isFetching) return false;
    set({ isFetching: true });
    try {
      const url = type
        ? `/api/me/addresses?type=${type}`
        : "/api/me/addresses";
      const res = await axios.get(url);
      if (res.status === 200) {
        set({ addresses: sortAddresses(res.data.map(normalizeAddress)) });
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

  createAddress: async (payload) => {
    set({ isSaving: true });
    try {
      const res = await axios.post("/api/me/addresses", payload);
      if (res.status === 201) {
        const created = normalizeAddress(res.data);
        set((state) => ({
          addresses: sortAddresses([
            ...state.addresses.map((address) =>
              created.isDefault && address.addressType === created.addressType
                ? { ...address, isDefault: false }
                : address,
            ),
            created,
          ]),
        }));
        return { success: true, data: created };
      }
      return { success: false, message: "Could not save address." };
    } catch (err) {
      if (!err.response) useAppStore.getState().setBackendError(true);
      return {
        success: false,
        message: getApiErrorMessage(err, "Could not save address."),
      };
    } finally {
      set({ isSaving: false });
    }
  },

  updateAddress: async (addressId, payload) => {
    set({ isSaving: true });
    try {
      const res = await axios.put(`/api/me/addresses/${addressId}`, payload);
      if (res.status === 200) {
        const updated = normalizeAddress(res.data);
        set((state) => ({
          addresses: sortAddresses(
            state.addresses.map((address) => {
              if (address.id === addressId) {
                return updated;
              }
              if (
                updated.isDefault &&
                address.addressType === updated.addressType
              ) {
                return { ...address, isDefault: false };
              }
              return address;
            }),
          ),
        }));
        return { success: true, data: updated };
      }
      return { success: false, message: "Could not update address." };
    } catch (err) {
      if (!err.response) useAppStore.getState().setBackendError(true);
      return {
        success: false,
        message: getApiErrorMessage(err, "Could not update address."),
      };
    } finally {
      set({ isSaving: false });
    }
  },

  deleteAddress: async (addressId) => {
    try {
      const res = await axios.delete(`/api/me/addresses/${addressId}`);
      if (res.status === 204) {
        set((state) => ({
          addresses: state.addresses.filter((address) => address.id !== addressId),
        }));
        return { success: true };
      }
      return { success: false, message: "Could not delete address." };
    } catch (err) {
      if (!err.response) useAppStore.getState().setBackendError(true);
      return {
        success: false,
        message: getApiErrorMessage(err, "Could not delete address."),
      };
    }
  },
}));
