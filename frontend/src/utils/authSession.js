import { toast } from "sonner";

import { useAuthStore } from "../stores/authStore";
import { useUIStore } from "../stores/uiStore";
import { navigate } from "./router";
import { getApiErrorMessage } from "./apiErrorMessage";

export function isAuthRequiredError(error) {
  const status = error?.response?.status;

  if (status === 401) {
    return true;
  }

  if (status === 403) {
    const message = getApiErrorMessage(error, "").toLowerCase();
    return (
      message.includes("authentication") ||
      message.includes("log in") ||
      message.includes("session expired")
    );
  }

  return false;
}

export async function ensureUserLoggedIn({
  promptMessage = "Sign in to continue. Your draft stays on this page.",
} = {}) {
  if (useAuthStore.getState().user) {
    return true;
  }

  await useAuthStore.getState().checkAuth();
  if (useAuthStore.getState().user) {
    return true;
  }

  toast.info(promptMessage);
  const loggedIn = await useUIStore.getState().waitForLogin();
  return loggedIn && !!useAuthStore.getState().user;
}

/**
 * Clears stale auth, sends user to main page, opens login modal.
 */
export async function promptReauthentication(error) {
  useAuthStore.setState({ user: null });

  toast.warning(
    getApiErrorMessage(
      error,
      "Your session expired. Sign in again to continue.",
    ),
  );

  navigate("/");
  return useUIStore.getState().waitForLogin();
}

export function mapAuthError(
  error,
  { credentialsMessage, fallbackMessage = "Something went wrong. Try again." },
) {
  const status = error?.response?.status;
  const backendMessage = getApiErrorMessage(error, "");

  if (!error?.response) {
    return {
      message: "Server error. Try again later.",
      errorType: "server",
    };
  }

  if (status === 401) {
    return {
      message: credentialsMessage,
      errorType: "credentials",
    };
  }

  if (status === 409) {
    return {
      message: backendMessage || fallbackMessage,
      errorType: "conflict",
    };
  }

  if (status === 400) {
    return {
      message: backendMessage || fallbackMessage,
      errorType: "validation",
    };
  }

  if (status >= 500) {
    return {
      message: "Server error. Try again later.",
      errorType: "server",
    };
  }

  return {
    message: backendMessage || fallbackMessage,
    errorType: "error",
  };
}
