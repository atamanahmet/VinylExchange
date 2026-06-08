import { create } from "zustand";
import axios from "../api/axiosInstance";
import { useAuthStore } from "./authStore";
import { useUIStore } from "./uiStore";
import { navigate } from "../utils/router";

export const useMessagingStore = create((set, get) => ({
  activeConvoId: null,
  unreadCount: null,
  conversations: [],
  activeConversation: {
    conversation: null,
    messages: null,
  },

  setActiveConvoId: (id) => set({ activeConvoId: id }),
  setActiveConversation: (selected) => set({ activeConversation: selected }),

  fetchUnreadCount: async () => {
    const user = useAuthStore.getState().user;
    if (!user) return;

    try {
      const res = await axios.get("/api/messages/unread");
      set({ unreadCount: res.data.unreadCount });
    } catch (error) {
      console.error("Failed to fetch unread count:", error);
    }
  },

  startConversation: async (relatedListingId) => {
    const user = useAuthStore.getState().user;

    if (!user) {
      const isLoggedIn = await useUIStore.getState().waitForLogin();
      if (!isLoggedIn) return;
    }

    try {
      const res = await axios.post("/api/messages/start", { relatedListingId });
      if (res.status === 201) {
        set({ activeConvoId: res.data.id });
        navigate(`/messaging/${relatedListingId}`);
        return true;
      }
      return false;
    } catch (error) {
      console.log("convo starting error:", error);
      return false;
    }
  },

  fetchConversations: async () => {
    const user = useAuthStore.getState().user;
    if (!user) return;

    try {
      const res = await axios.get("/api/messages/conversations");
      if (res.status === 200) {
        set({ conversations: res.data });
      }
    } catch (error) {
      console.log(error);
    }
  },

  fetchMessages: async (activeConversationId) => {
    try {
      const res = await axios.get(
        `/api/messages/conversation/${activeConversationId}`,
      );
      if (res.status === 200) {
        const currentUser = useAuthStore.getState().user;
        const convo = res.data.conversationDTO;

        /**
         * Resolve other party's username by comparing against current user
         */
        const participantUsername =
          convo.initiatorUsername === currentUser?.username
            ? convo.participantUsername
            : convo.initiatorUsername;

        set({
          activeConversation: {
            conversation: convo,
            messages: res.data.messagePage.content,
            participantUsername,
          },
        });
      }
    } catch (error) {
      console.log(error);
    }
  },

  sendMessage: async (activeConversation, message) => {
    try {
      await axios.post("/api/messages", {
        conversationId: activeConversation.conversation.id,
        relatedListingId: activeConversation.conversation.relatedListingId,
        content: message,
      });
    } catch (error) {
      console.log(error);
    }
  },

  deleteConversation: async (conversationId) => {
    try {
      const res = await axios.delete(
        `/api/messages/conversation/${conversationId}`,
      );
      if (res.status === 204) {
        set((state) => ({
          conversations: state.conversations.filter(
            (c) => c.id !== conversationId,
          ),
          activeConvoId:
            state.activeConvoId === conversationId ? null : state.activeConvoId,
          activeConversation:
            state.activeConvoId === conversationId
              ? { conversation: null, messages: null }
              : state.activeConversation,
        }));
        return true;
      }
      return false;
    } catch (error) {
      console.log(error);
      return false;
    }
  },

  deleteAllConversations: async () => {
    try {
      const res = await axios.delete("/api/messages/conversations");
      if (res.status === 204) {
        set({
          conversations: [],
          activeConvoId: null,
          activeConversation: { conversation: null, messages: null },
        });
        return true;
      }
      return false;
    } catch (error) {
      console.log(error);
      return false;
    }
  },
}));
