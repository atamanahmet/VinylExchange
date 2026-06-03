import "./App.css";
import { useState, useEffect } from "react";
import { Routes, Route, useNavigate } from "react-router-dom";
import { setNavigate } from "./utils/router";
import { useAuthStore } from "./stores/authStore";
import { useDataStore } from "./stores/dataStore";

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
import OrderItemsPage from "./pages/OrderItemsPage";
import ConversationsPage from "./pages/ConversationsPage";
import { useCartStore } from "./stores/cartStore";
import WishlistPage from "./pages/WishlistPage";
import MyListingsPage from "./pages/ListingsPage";
import ListingForm from "./pages/ListingForm";

function App() {
  const navigate = useNavigate();

  const hasError = useDataStore((state) => state.hasError);
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

  if (hasError) {
    return <ErrorPage />;
  }

  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/about" element={<About />} />
        <Route path="/newlisting" element={<ListingForm />} />
        <Route path="/listings" element={<MyListingsPage />} />
        <Route path="/terms" element={<TermsAndConditions />} />
        <Route path="/edit/:listingId" element={<ListingForm />} />
        <Route path="/listing/:listingId" element={<ItemPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/orderItems" element={<OrderItemsPage />} />
        <Route path="/messaging" element={<ConversationsPage />} />
        <Route path="/messaging/:listingId" element={<ConversationsPage />} />
        <Route path="/wishlist" element={<WishlistPage />} />
      </Routes>
    </>
  );
}

export default App;
