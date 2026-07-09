import "./App.css";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { AppRoutes, setNavigate } from "./utils/router";
import { useAuthStore } from "./stores/authStore";
import { useAppStore } from "./stores/appStore";
import { useCartStore } from "./stores/cartStore";

import Navbar from "@/components/layout/Navbar";
import { Toaster } from "@/components/ui/sonner";
import ErrorPage from "./pages/ErrorPage";

function App() {
  const navigate = useNavigate();

  const backendError = useAppStore((state) => state.backendError);
  const user = useAuthStore((state) => state.user);
  const checkAuth = useAuthStore((state) => state.checkAuth);
  const fetchCart = useCartStore((state) => state.fetchCart);

  useEffect(() => {
    checkAuth();
  }, []);

  useEffect(() => {
    fetchCart();
  }, [user]);

  useEffect(() => {
    setNavigate(navigate);
  }, [navigate]);

  if (backendError) {
    return <ErrorPage />;
  }

  return (
    <>
      <Navbar />
      <Toaster richColors closeButton position="top-right" />
      <AppRoutes />
    </>
  );
}

export default App;
