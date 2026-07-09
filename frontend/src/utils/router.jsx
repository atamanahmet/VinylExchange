import { Navigate, Outlet, Route, Routes } from "react-router-dom";

import About from "../pages/About";
import AccountPage from "../pages/AccountPage";
import AdminDashboard from "../pages/AdminDashboard";
import CartPage from "../pages/CartPage";
import CheckoutShippingPage from "../pages/CheckoutShippingPage";
import ContactPage from "../pages/ContactPage";
import ConversationsPage from "../pages/ConversationsPage";
import ItemPage from "../pages/ItemPage";
import ListingForm from "../pages/ListingForm";
import ListingsPage from "../pages/ListingsPage";
import MainPage from "../pages/MainPage";
import OrderDetailPage from "../pages/OrderDetailsPage";
import OrdersPage from "../pages/OrdersPage";
import PaymentPage from "../pages/PaymentPage";
import PaymentResultPage from "../pages/PaymentResultPage";
import SellerProfilePage from "../pages/SellerProfilePage";
import ShipmentLabelPage from "../pages/ShipmentLabelPage";
import TermsAndConditions from "../pages/TermsAndConditions";
import WishlistPage from "../pages/WishlistPage";
import { useAuthStore } from "../stores/authStore";

let navigateFunction = null;

export const setNavigate = (navigate) => {
  navigateFunction = navigate;
};

export const navigate = (path) => {
  if (navigateFunction) {
    navigateFunction(path);
  } else {
    console.error("navigate function is not present");
  }
};

function ProtectedRoute() {
  const user = useAuthStore((state) => state.user);
  const isLoading = useAuthStore((state) => state.isLoading);

  if (isLoading) {
    return null;
  }

  if (!user) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<MainPage />} />
      <Route path="/about" element={<About />} />
      <Route path="/listings" element={<ListingsPage />} />
      <Route path="/terms" element={<TermsAndConditions />} />
      <Route path="/listing/:publicId/:slug" element={<ItemPage />} />
      <Route path="/cart" element={<CartPage />} />
      <Route path="/seller/:username" element={<SellerProfilePage />} />
      <Route path="/contact" element={<ContactPage />} />
      <Route path="/payment/result" element={<PaymentResultPage />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/newlisting" element={<ListingForm />} />
        <Route path="/edit/:listingId" element={<ListingForm />} />
        <Route path="/checkout/shipping" element={<CheckoutShippingPage />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/orders/:orderId" element={<OrderDetailPage />} />
        <Route
          path="/orders/:orderId/shipment/label"
          element={<ShipmentLabelPage />}
        />
        <Route path="/payment" element={<PaymentPage />} />
        <Route path="/messaging" element={<ConversationsPage />} />
        <Route path="/messaging/:listingId" element={<ConversationsPage />} />
        <Route path="/wishlist" element={<WishlistPage />} />
        <Route path="/account" element={<AccountPage />} />
        <Route path="/account/addresses" element={<AccountPage />} />
        <Route path="/account/payments" element={<AccountPage />} />
      </Route>
    </Routes>
  );
}
