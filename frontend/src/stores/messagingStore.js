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
    participantUsername: null,
  },
  messages: null,
  participantUsername: null,

  setActiveConvoId: (id) => set({ activeConvoId: id }),
  setActiveConversation: (selected) => set({ activeConversation: selected }),

  fetchUnreadCount: async () => {
    const user = useAuthStore.getState().user;

    if (!user) {
      return;
    }

    try {
      const response = await axios.get("/api/messages/unread", {
        withCredentials: true,
      });

      set({ unreadCount: response.data.unreadCount });
    } catch (error) {
      console.error("Failed to fetch unread count:", error);
    }
  },

  startConversation: async (relatedListingId) => {
    const user = useAuthStore.getState().user;

    if (!user) {
      const isLoggedIn = await useUIStore.getState().waitForLogin();
      if (!isLoggedIn) {
        return;
      }
    }

    try {
      const res = await axios.post(
        `/api/messages/start`,
        { relatedListingId: relatedListingId },
        { withCredentials: true },
      );

      if (res.status == 201) {
        console.log("convo started");
        set({ activeConvoId: res.data.id });
        navigate(`/messaging/${relatedListingId}`);
        return true;
      }

      console.warn("Unexpected convo starting status:", res.status);
      return false;
    } catch (error) {
      console.log("convo starting error " + error);
      return false;
    }
  },

  fetchConversations: async () => {
    const user = useAuthStore.getState().user;

    if (!user) {
      return;
    }

    try {
      const res = await axios.get(`/api/messages/conversations`, {
        withCredentials: true,
      });
      if (res.status == 200) {
        set({ conversations: res.data });
        console.log(res.data);
      }
    } catch (error) {
      console.log(error);
    }
  },

  sendMessage: async (activeConversation, message) => {
    try {
      const res = await axios.post(
        "/api/messages",
        {
          conversationId: activeConversation.conversation.id,
          relatedListingId: activeConversation.conversation.relatedListingId,
          content: message,
        },
        { withCredentials: true },
      );
    } catch (error) {
      console.log(error);
    }
  },

  fetchMessages: async (activeConversationId) => {
    console.log(activeConversationId);
    try {
      const res = await axios.get(
        `/api/messages/conversation/${activeConversationId}`,
        {
          withCredentials: true,
        },
      );
      if (res.status == 200) {
        set({
          activeConversation: {
            conversation: res.data.conversationDTO,
            messages: res.data.messagePage.content,
            participantUsername: res.data.conversationDTO.initiatorUsername
              ? res.data.conversationDTO.participantUsername
              : res.data.conversationDTO.initiatorUsername,
          },
        });
        console.log("active convo", get().activeConversation);
      }
    } catch (error) {
      console.log(error);
    }
  },

  deleteAllConversations: async () => {
    try {
      const res = await axios.delete(`/api/messages/conversations`, {
        withCredentials: true,
      });
    } catch (error) {
      console.log(error);
    } finally {
      get().fetchConversations();
      set({ activeConversation: null });
    }
  },
}));
