import { create } from "zustand";
import axios from "../api/axiosInstance";
import { useAuthStore } from "./authStore";

export const useNotificationStore = create((set, get) => ({
  notifications: [],
  unreadCount: 0,
  isLoading: false,

  fetchDropdownNotifications: async () => {
    const user = useAuthStore.getState().user;
    if (!user) return;

    set({ isLoading: true });
    try {
      const res = await axios.get("/api/notifications/dropdown");
      if (res.status === 200) {
        set({
          notifications: res.data.notifications,
          unreadCount: res.data.unreadCount,
        });
      }
    } catch (err) {
      console.error("Failed to fetch notifications:", err);
    } finally {
      set({ isLoading: false });
    }
  },

  fetchAllNotifications: async () => {
    set({ isLoading: true });
    try {
      const res = await axios.get("/api/notifications");
      if (res.status === 200) {
        set({
          notifications: res.data.notifications,
          unreadCount: res.data.unreadCount,
        });
      }
    } catch (err) {
      console.error("Failed to fetch all notifications:", err);
    } finally {
      set({ isLoading: false });
    }
  },

  markAsRead: async (notificationId) => {
    try {
      await axios.post(`/api/notifications/${notificationId}/read`, null);
      set((state) => {
        const updated = state.notifications.map((n) =>
          n.id === notificationId ? { ...n, read: true } : n,
        );
        return {
          notifications: updated,
          unreadCount: updated.filter((n) => !n.read).length,
        };
      });
    } catch (err) {
      console.error("Failed to mark notification as read:", err);
    }
  },

  /**
   * TODO: replace with single bulk endpoint when available
   * Sequential to avoid hammering the server
   */
  markAllAsRead: async () => {
    const ids = get()
      .notifications.filter((n) => !n.read)
      .map((n) => n.id);
    for (const id of ids) {
      try {
        await axios.post(`/api/notifications/${id}/read`, null);
      } catch (err) {
        console.error(`Failed to mark ${id} as read:`, err);
      }
    }
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
