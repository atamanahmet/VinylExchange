import { create } from "zustand";
import axios from "../api/axiosInstance";
import { mapAuthError } from "../utils/authSession";
import { navigate } from "../utils/router";

export const useAuthStore = create((set, get) => ({
  user: null,
  isLoading: false,

  checkAuth: async () => {
    set({ isLoading: true });
    try {
      const res = await axios.get("/api/me", {
        withCredentials: true,
      });
      if (res.status === 200) {
        set({ user: res.data });
        return true;
      } else {
        console.warn("Unexpected auth check status:", res.status);
        return false;
      }
    } catch (error) {
      set({ user: null });
      return false;
    } finally {
      set({ isLoading: false });
    }
  },

  loginUser: async (formData) => {
    try {
      const res = await axios.post(
        "/login",
        {
          username: formData.username,
          password: formData.password,
        },
        { withCredentials: true },
      );

      if (res.status === 200) {
        const { username, email } = res.data;
        set({ user: { username, email } });
        return { success: true };
      }

      return {
        success: false,
        message: "Server error. Try again later.",
        errorType: "server",
      };
    } catch (error) {
      const mapped = mapAuthError(error, {
        credentialsMessage: "Wrong credentials. Try again or register.",
        fallbackMessage: "Could not log in. Try again.",
      });
      return { success: false, ...mapped };
    }
  },

  registerUser: async (formData) => {
    try {
      const res = await axios.post(
        "/register",
        {
          username: formData.username,
          password: formData.password,
          email: formData.email,
        },
        { withCredentials: true },
      );

      if (res.status === 201) {
        set({
          user: { username: formData.username, email: formData.email },
        });
        return { success: true };
      }

      return {
        success: false,
        message: "Server error. Try again later.",
        errorType: "server",
      };
    } catch (error) {
      const mapped = mapAuthError(error, {
        credentialsMessage: "Registration failed. Check your details and try again.",
        fallbackMessage: "Could not create account. Try again.",
      });
      return { success: false, ...mapped };
    }
  },

  updateEmail: async (email) => {
    try {
      const res = await axios.patch(
        "/api/me/email",
        { email },
        { withCredentials: true },
      );

      if (res.status === 200) {
        set({ user: res.data });
        return { success: true };
      }

      return {
        success: false,
        message: "Could not update email. Try again.",
        errorType: "server",
      };
    } catch (error) {
      const mapped = mapAuthError(error, {
        credentialsMessage: "Sign in again to update your email.",
        fallbackMessage: "Could not update email. Try again.",
      });
      return { success: false, ...mapped };
    }
  },

  logOut: async () => {
    try {
      const res = await axios.post("/logout", null, {
        withCredentials: true,
      });
      if (res.status === 204) {
        sessionStorage.clear();
        set({ user: null });
        navigate("/");
        return { success: true };
      }

      return {
        success: false,
        message: "Could not log out. Try again.",
        errorType: "error",
      };
    } catch (error) {
      // still clear local session and send home
      sessionStorage.clear();
      set({ user: null });
      navigate("/");
      const mapped = mapAuthError(error, {
        credentialsMessage: "Could not log out. Try again.",
        fallbackMessage: "Could not log out. Try again.",
      });
      return { success: false, ...mapped };
    }
  },
}));
