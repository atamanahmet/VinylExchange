import "./App.css";
import { useEffect } from "react";
import { Routes, Route, useNavigate } from "react-router-dom";
import { setNavigate } from "./utils/router";
import { useAuthStore } from "./stores/authStore";
import { useAppStore } from "./stores/appStore";
import { useCartStore } from "./stores/cartStore";

import Navbar from "./comps/Navbar";
import MainPage from "./pages/MainPage";
import About from "./pages/About";
import TermsAndConditions from "./pages/TermsAndConditions";
import ListingsPage from "./pages/ListingsPage";
import ItemPage from "./pages/ItemPage";
import CartPage from "./pages/CartPage";
import OrdersPage from "./pages/OrdersPage";
import AdminDashboard from "./pages/AdminDashboard";
import ErrorPage from "./pages/ErrorPage";
import ConversationsPage from "./pages/ConversationsPage";
import OrderDetailPage from "./pages/OrderDetailsPage";
import WishlistPage from "./pages/WishlistPage";
import ListingForm from "./pages/ListingForm";
import PaymentPage from "./pages/PaymentPage";
import PaymentResultPage from "./pages/PaymentResultPage";

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
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/about" element={<About />} />
        <Route path="/newlisting" element={<ListingForm />} />
        <Route path="/listings" element={<ListingsPage />} />
        <Route path="/terms" element={<TermsAndConditions />} />
        <Route path="/edit/:listingId" element={<ListingForm />} />
        <Route path="/listing/:listingId" element={<ItemPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/orders/:orderId" element={<OrderDetailPage />} />
        <Route path="/payment" element={<PaymentPage />} />
        <Route path="/payment/result" element={<PaymentResultPage />} />
        <Route path="/messaging" element={<ConversationsPage />} />
        <Route path="/messaging/:listingId" element={<ConversationsPage />} />
        <Route path="/wishlist" element={<WishlistPage />} />
      </Routes>
    </>
  );
}

export default App;
