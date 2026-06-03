import { create } from "zustand";
import axios from "../api/axiosInstance";
import { useAuthStore } from "./authStore";

export const useNotificationStore = create((set, get) => ({
  notifications: [],
  unreadCount: 0,
  isLoading: false,

  fetchDropdownNotifications: async () => {
    const user = useAuthStore.getState().user;

    if (!user) {
      set({
        notifications: [],
        cartItemCount: 0,
        isLoading: false,
      });
      return;
    }

    const url = "/api";

    set({ isLoading: true });
    try {
      const res = await axios.get(url + "/notifications/dropdown", {
        withCredentials: true,
      });
      set({
        notifications: res.data.notifications,
        unreadCount: res.data.unreadCount,
        isLoading: false,
      });
    } catch (err) {
      console.error("Failed to fetch notifications:", err);
      set({ isLoading: false });
    }
  },

  fetchAllNotifications: async () => {
    const url = "/api";

    set({ isLoading: true });
    try {
      const res = await axios.get(url + "/notifications", {
        withCredentials: true,
      });
      set({
        notifications: res.data.notifications,
        unreadCount: res.data.unreadCount,
        isLoading: false,
      });
    } catch (err) {
      console.error("Failed to fetch all notifications:", err);
      set({ isLoading: false });
    }
  },

  markAsRead: async (notificationId) => {
    const url = "/api";

    try {
      await axios.post(url + `/notifications/${notificationId}/read`, null, {
        withCredentials: true,
      });

      const updated = get().notifications.map((n) =>
        n.id === notificationId ? { ...n, read: true } : n,
      );
      const unread = updated.filter((n) => !n.read).length;
      set({ notifications: updated, unreadCount: unread });
    } catch (err) {
      console.error("Failed to mark notification as read:", err);
    }
  },

  markAllAsRead: async () => {
    const url = "/api";

    const allIds = get().notifications.map((n) => n.id);
    await Promise.all(
      allIds.map((id) =>
        axios.post(url + `/notifications/${id}/read`, null, {
          withCredentials: true,
        }),
      ),
    );

    set((state) => ({
      notifications: state.notifications.map((n) => ({ ...n, read: true })),
      unreadCount: 0,
    }));
  },

  addNotification: (notification) => {
    set((state) => ({
      notifications: [notification, ...state.notifications].slice(0, 50),
      unreadCount: state.unreadCount + 1,
    }));
  },
}));
