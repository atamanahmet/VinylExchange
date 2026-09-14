import { useEffect } from "react";
import { Navigate } from "react-router-dom";

import { useUIStore } from "@/stores/uiStore";

/**
 * App has no dedicated login page — auth is a modal.
 * /login opens that modal and lands on home.
 */
export default function LoginPage() {
  const setOpenLogin = useUIStore((state) => state.setOpenLogin);

  useEffect(() => {
    setOpenLogin(true);
  }, [setOpenLogin]);

  return <Navigate to="/" replace />;
}
