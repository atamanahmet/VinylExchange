import PageContainer from "@/components/layout/PageContainer";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

import CheckoutShippingPanel from "@/components/checkout/CheckoutShippingPanel";
import { useAuthStore } from "../stores/authStore";
import { useCartStore } from "../stores/cartStore";

export default function CheckoutShippingPage() {
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const cart = useCartStore((state) => state.cart);
  const fetchCart = useCartStore((state) => state.fetchCart);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  useEffect(() => {
    if (!user) {
      navigate("/");
    }
  }, [user, navigate]);

  useEffect(() => {
    if (user && cart && cart.items?.length === 0) {
      navigate("/cart");
    }
  }, [user, cart, navigate]);

  return (
    <PageContainer width="wide" className="antialiased">
      <div>
        <h2 className="text-xl font-semibold text-on-surface sm:text-2xl">
          Shipping address
        </h2>
        <p className="mt-2 text-sm text-on-surface-muted">
          Choose where your order should be delivered.
        </p>
        <div className="mt-6 sm:mt-8">
          <CheckoutShippingPanel />
        </div>
      </div>
    </PageContainer>
  );
}
