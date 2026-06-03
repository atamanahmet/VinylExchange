import { create } from "zustand";
import axios from "../api/axiosInstance";

export const useAuthStore = create((set, get) => ({
  user: null,
  isLoading: true,
  authResponse: null,

  setAuthResponse: (message) => set({ authResponse: message }),

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
      set({ user: null }); // Not logged in, continue
      return;
    } finally {
      set({ isLoading: false });
    }
  },

  loginUser: async (formData) => {
    const url = "/login";

    try {
      const res = await axios.post(
        url,
        {
          username: formData.username,
          password: formData.password,
        },
        { withCredentials: true },
      );

      if (res.status === 200) {
        const { username, email } = res.data;

        set({
          user: { username, email },
          authResponse: "Logged in succesfully. Redirecting...",
        });

        return true;
      }

      console.warn("Unexpected login status:", res.status);
      return false;
    } catch (error) {
      console.log(error);
      set({ authResponse: "Wrong credentials. Try again or register" });

      return false;
    }
  },

  registerUser: async (formData) => {
    const url = "/register";

    try {
      const res = await axios.post(
        url,
        {
          username: formData.username,
          password: formData.password,
          email: formData.email,
        },

        { withCredentials: true },
      );
      if (res.status === 201) {
        const { username, email } = res.data;

        set({
          user: { username, email },
          authResponse: "Account created succesfully. Redirecting...",
        });

        return true;
      }
      console.warn("Unexpected register status:", res.status);
      return false;
    } catch (error) {
      console.log(error);
      set({
        authResponse: "User register issues, try again or change credentials",
      });

      return false;
    }
  },

  logOut: async () => {
    const url = "/logout";
    try {
      const res = await axios.post(url, null, {
        withCredentials: true,
      });
      if (res.status === 204) {
        console.log("User logged out.");
        sessionStorage.clear();
        set({
          user: null,
          authResponse: "Log out succesfull",
        });
        return true;
      }
      console.warn("Unexpected log out status:", res.status);
      return false;
    } catch (err) {
      console.log("Error during logout: ", err);

      return false;
    }
  },
}));
